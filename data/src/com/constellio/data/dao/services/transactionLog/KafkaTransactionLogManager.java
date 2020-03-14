package com.constellio.data.dao.services.transactionLog;

import com.constellio.data.conf.DataLayerConfiguration;
import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.data.dao.dto.records.TransactionDTO;
import com.constellio.data.dao.dto.records.TransactionResponseDTO;
import com.constellio.data.dao.services.DataLayerLogger;
import com.constellio.data.dao.services.bigVault.RecordDaoException.OptimisticLocking;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServerTransaction;
import com.constellio.data.dao.services.idGenerator.UUIDV1Generator;
import com.constellio.data.dao.services.records.RecordDao;
import com.constellio.data.dao.services.transactionLog.SecondTransactionLogRuntimeException.SecondTransactionLogRuntimeException_LogIsInInvalidStateCausedByPreviousException;
import com.constellio.data.dao.services.transactionLog.SecondTransactionLogRuntimeException.SecondTransactionLogRuntimeException_NotAllLogsWereDeletedCorrectlyException;
import com.constellio.data.dao.services.transactionLog.kafka.BlockingDeliveryStrategy;
import com.constellio.data.dao.services.transactionLog.kafka.ConsumerRecordPoller;
import com.constellio.data.dao.services.transactionLog.kafka.DeliveryStrategy;
import com.constellio.data.dao.services.transactionLog.kafka.FailedDeliveryCallback;
import com.constellio.data.dao.services.transactionLog.kafka.Transaction;
import com.constellio.data.dao.services.transactionLog.kafka.TransactionReplayer;
import com.constellio.data.extensions.DataLayerSystemExtensions;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.solr.common.params.ModifiableSolrParams;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;

public class KafkaTransactionLogManager implements SecondTransactionLogManager {

	private final Map<String, String> transactions;

	private DataLayerConfiguration configuration;
	private DataLayerSystemExtensions extensions;
	private RecordDao recordDao;
	private DataLayerLogger dataLayerLogger;

	private static volatile KafkaProducer<String, Transaction> producer;
	private DeliveryStrategy deliveryStrategy;

	private boolean lastFlushFailed;

	private boolean automaticRegroupAndMoveInVaultEnabled;

	public KafkaTransactionLogManager(DataLayerConfiguration configuration, DataLayerSystemExtensions extensions,
									  RecordDao recordDao, DataLayerLogger dataLayerLogger) {
		transactions = Collections.<String, String>synchronizedMap(new HashMap<String, String>());

		this.configuration = configuration;
		this.extensions = extensions;
		this.recordDao = recordDao;
		this.dataLayerLogger = dataLayerLogger;
	}

	@Override
	public void initialize() {
	}

	@Override
	public void close() {
		transactions.clear();

		try {
			producer.close();
		} finally {
			producer = null;
		}
	}

	@Override
	public void prepare(String transactionId, BigVaultServerTransaction transaction) {
		String data = getTransactionLogReadWriteServices().toLogEntry(transaction);
		addTransactionData(transactionId, data);
	}

	private void addTransactionData(String transactionId, String data) {
		transactions.put(transactionId, data);
	}

	private TransactionLogReadWriteServices getTransactionLogReadWriteServices() {
		return new TransactionLogReadWriteServices(null, configuration, extensions);
	}

	@Override
	public void flush(final String transactionId, TransactionResponseDTO transactionInfo) {
		if (isLastFlushFailed()) {
			throw new SecondTransactionLogRuntimeException_LogIsInInvalidStateCausedByPreviousException();
		}

		final String data = transactions.remove(transactionId);

		if (StringUtils.isBlank(data)) {
			return;
		}

		sendTransaction(transactionInfo, data);
	}

	private void sendTransaction(TransactionResponseDTO transactionInfo, final String data) {
		FailedDeliveryCallback callback = new FailedDeliveryCallback() {
			@Override
			public void onFailedDelivery(Throwable e) {
				// FIXME : do we need to stop constellio?
				e.printStackTrace();
			}
		};

		Transaction transaction = new Transaction();
		transaction.setVersions(transactionInfo.getNewDocumentVersions());
		transaction.setTransaction(data);

		setLastFlushFailed(!getDeliveryStrategy().<String, Transaction>send(getProducer(), getRecord(transaction), callback));
	}

	private ProducerRecord<String, Transaction> getRecord(Transaction data) {
		return new ProducerRecord<String, Transaction>(configuration.getKafkaTopic(), getHostname(), data);
	}

	private synchronized KafkaProducer<String, Transaction> getProducer() {
		if (producer == null) {
			HashMap<String, Object> configs = new HashMap<>();
			configs.put("bootstrap.servers", configuration.getKafkaServers());
			configs.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
			configs.put("value.serializer", "com.constellio.data.dao.services.transactionLog.kafka.TransactionSerializer");

			producer = new KafkaProducer<>(configs);
		}

		return producer;
	}

	private String getHostname() {
		String host = System.getenv("COMPUTERNAME");

		if (StringUtils.isBlank(host)) {
			host = System.getenv("HOSTNAME");

			if (StringUtils.isBlank(host)) {
				host = "localhost";
			}
		}

		return host + new Random().nextInt(10);
	}

	@Override
	public void cancel(String transactionId) {
		transactions.remove(transactionId);
	}

	@Override
	public void setSequence(String sequenceId, long value, TransactionResponseDTO transactionInfo) {
		String data = getTransactionLogReadWriteServices().toSetSequenceLogEntry(sequenceId, value);
		sendTransaction(transactionInfo, data);
	}

	@Override
	public void nextSequence(String sequenceId, TransactionResponseDTO transactionInfo) {
		String data = getTransactionLogReadWriteServices().toNextSequenceLogEntry(sequenceId);
		sendTransaction(transactionInfo, data);
	}

	@Override
	public String regroupAndMove() {
		if (transactions.isEmpty()) {
			return null;
		}

		String transactionId = UUIDV1Generator.newRandomId();

		StringBuilder sb = new StringBuilder();
		for (String transaction : transactions.values()) {
			sb.append(transaction);
		}

		transactions.clear();
		transactions.put(transactionId, sb.toString());

		return transactionId;
	}

	@Override
	public void destroyAndRebuildSolrCollection() {
		clearSolrCollection();

		TransactionReplayer replayer = new TransactionReplayer(configuration, recordDao.getBigVaultServer(), dataLayerLogger);

		KafkaConsumer<String, Transaction> consumer = getConsumer();
		ConsumerRecordPoller<String, Transaction> poller = getConsumerRecordPoller(consumer);
		poller.poll(replayer);

		replayer.replayRemainsAndClose();
		consumer.close();
	}

	private void clearSolrCollection() {
		ModifiableSolrParams deleteAllSolrDocumentsOfEveryConstellioCollectionsQuery = new ModifiableSolrParams();
		deleteAllSolrDocumentsOfEveryConstellioCollectionsQuery.set("q", "*:*");
		try {
			recordDao.execute(new TransactionDTO(RecordsFlushing.NOW())
					.withDeletedByQueries(deleteAllSolrDocumentsOfEveryConstellioCollectionsQuery));
		} catch (OptimisticLocking optimisticLocking) {
			throw new RuntimeException(optimisticLocking);
		}
	}

	protected ConsumerRecordPoller<String, Transaction> getConsumerRecordPoller(
			KafkaConsumer<String, Transaction> consumer) {
		return new ConsumerRecordPoller<String, Transaction>(consumer);
	}

	protected KafkaConsumer<String, Transaction> getConsumer() {
		Properties props = new Properties();
		props.put("bootstrap.servers", configuration.getKafkaServers());
		props.put("group.id", UUID.randomUUID().toString());
		// props.put("enable.auto.commit", "true");
		// props.put("auto.commit.interval.ms", "1000");
		props.put("session.timeout.ms", "30000");
		props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		props.put("value.deserializer", "com.constellio.data.dao.services.transactionLog.kafka.TransactionDeserializer");
		props.put("auto.offset.reset", "earliest");

		KafkaConsumer<String, Transaction> consumer = new KafkaConsumer<>(props);

		consumer.subscribe(Arrays.asList(configuration.getKafkaTopic()));

		return consumer;
	}

	@Override
	public void transactionLOGReindexationStartStrategy() {
		// TODO : Kafka backup?
	}

	@Override
	public void transactionLOGReindexationCleanupStrategy() {
		// TODO : Delete Kafka backup?
	}

	@Override
	public void moveLastBackupAsCurrentLog() {
		// TODO : Kafka backup?
	}

	@Override
	public boolean isAlive() {
		return true;
	}

	@Override
	public long getLoggedTransactionCount() {
		return -1;
	}

	@Override
	public void setAutomaticRegroupAndMoveEnabled(boolean enabled) {
		automaticRegroupAndMoveInVaultEnabled = enabled;
	}

	@Override
	public void deleteUnregroupedLog()
			throws SecondTransactionLogRuntimeException_NotAllLogsWereDeletedCorrectlyException {
		// TODO : what to delete?
	}

	public DeliveryStrategy getDeliveryStrategy() {
		if (deliveryStrategy == null) {
			deliveryStrategy = new BlockingDeliveryStrategy();
		}

		return deliveryStrategy;
	}

	public void setDeliveryStrategy(DeliveryStrategy deliveryStrategy) {
		this.deliveryStrategy = deliveryStrategy;
	}

	public boolean isLastFlushFailed() {
		return lastFlushFailed;
	}

	private void setLastFlushFailed(boolean lastFlushFailed) {
		this.lastFlushFailed = lastFlushFailed;
	}

	public Map<String, String> getTransactions() {
		return Collections.unmodifiableMap(transactions);
	}
}
