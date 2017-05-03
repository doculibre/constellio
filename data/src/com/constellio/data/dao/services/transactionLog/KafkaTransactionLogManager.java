package com.constellio.data.dao.services.transactionLog;

import static com.constellio.data.dao.services.bigVault.solr.BigVaultServerTransactionCombinator.DEFAULT_MAX_TRANSACTION_SIZE;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.solr.common.params.ModifiableSolrParams;

import com.constellio.data.conf.DataLayerConfiguration;
import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.data.dao.dto.records.TransactionDTO;
import com.constellio.data.dao.services.DataLayerLogger;
import com.constellio.data.dao.services.bigVault.RecordDaoException.OptimisticLocking;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServerTransaction;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServerTransactionCombinator;
import com.constellio.data.dao.services.idGenerator.UUIDV1Generator;
import com.constellio.data.dao.services.records.RecordDao;
import com.constellio.data.dao.services.transactionLog.SecondTransactionLogRuntimeException.SecondTransactionLogRuntimeException_LogIsInInvalidStateCausedByPreviousException;
import com.constellio.data.dao.services.transactionLog.SecondTransactionLogRuntimeException.SecondTransactionLogRuntimeException_NotAllLogsWereDeletedCorrectlyException;
import com.constellio.data.dao.services.transactionLog.kafka.BlockingDeliveryStrategy;
import com.constellio.data.dao.services.transactionLog.kafka.ConsumerRecordCallback;
import com.constellio.data.dao.services.transactionLog.kafka.ConsumerRecordPoller;
import com.constellio.data.dao.services.transactionLog.kafka.DeliveryStrategy;
import com.constellio.data.dao.services.transactionLog.kafka.FailedDeliveryCallback;
import com.constellio.data.dao.services.transactionLog.reader1.ReaderTransactionLinesIteratorV1;
import com.constellio.data.dao.services.transactionLog.reader1.ReaderTransactionsIteratorV1;
import com.constellio.data.dao.services.transactionLog.replay.TransactionsLogImportHandler;
import com.constellio.data.extensions.DataLayerSystemExtensions;

public class KafkaTransactionLogManager implements SecondTransactionLogManager {

	private final Map<String, String> transactions;

	private DataLayerConfiguration configuration;
	private DataLayerSystemExtensions extensions;
	private RecordDao recordDao;
	private DataLayerLogger dataLayerLogger;

	private Producer<String, String> producer;
	private DeliveryStrategy deliveryStrategy;

	private boolean lastFlushFailed;

	private boolean automaticRegroupAndMoveInVaultEnabled;

	public KafkaTransactionLogManager(DataLayerConfiguration configuration, DataLayerSystemExtensions extensions,
			RecordDao recordDao, DataLayerLogger dataLayerLogger) {
		transactions = Collections.<String, String> synchronizedMap(new HashMap<String, String>());

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
	public void flush(final String transactionId) {
		if (isLastFlushFailed()) {
			throw new SecondTransactionLogRuntimeException_LogIsInInvalidStateCausedByPreviousException();
		}

		final String data = transactions.remove(transactionId);

		if (StringUtils.isBlank(data)) {
			return;
		}

		FailedDeliveryCallback callback = new FailedDeliveryCallback() {
			@Override
			public void onFailedDelivery(Throwable e) {
				// FIXME : do we need to stop constellio?
				addTransactionData(transactionId, data);

				e.printStackTrace();
			}
		};

		setLastFlushFailed(!getDeliveryStrategy().<String, String> send(getProducer(), getRecord(data), callback));
	}

	private ProducerRecord<String, String> getRecord(String data) {
		return new ProducerRecord<String, String>(configuration.getKafkaTopic(), getHostname(), data);
	}

	private Producer<String, String> getProducer() {
		if (producer == null) {
			HashMap<String, Object> configs = new HashMap<>();
			configs.put("bootstrap.servers", configuration.getKafkaServers());
			configs.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
			configs.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

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

		return host;
	}

	@Override
	public void cancel(String transactionId) {
		transactions.remove(transactionId);
	}

	@Override
	public void setSequence(String sequenceId, long value) {
		String transactionId = UUIDV1Generator.newRandomId();
		String data = getTransactionLogReadWriteServices().toSetSequenceLogEntry(sequenceId, value);
		addTransactionData(transactionId, data);
	}

	@Override
	public void nextSequence(String sequenceId) {
		String transactionId = UUIDV1Generator.newRandomId();
		String data = getTransactionLogReadWriteServices().toNextSequenceLogEntry(sequenceId);
		addTransactionData(transactionId, data);
	}

	@Override
	public String regroupAndMoveInVault() {
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

		if (transactions.isEmpty()) {
			Set<String> keySet = transactions.keySet();
			for (Iterator<String> iterator = keySet.iterator(); iterator.hasNext();) {
				// FIXME : should be tested as a valid solr transaction before flush
				flush(iterator.next());
			}
		}

		TransactionsLogImportHandler transactionsLogImportHandler = new TransactionsLogImportHandler(
				recordDao.getBigVaultServer(), dataLayerLogger, 2);
		transactionsLogImportHandler.start();

		final BigVaultLogAddUpdater addUpdater = new BigVaultLogAddUpdater(transactionsLogImportHandler);

		KafkaConsumer<String, String> consumer = getConsumer();
		ConsumerRecordPoller<String, String> poller = getConsumerRecordPoller(consumer);
		poller.poll(new ConsumerRecordCallback<String, String>() {
			@Override
			public void onConsumerRecord(long offset, String topic, String key, String value) {
				System.out.printf("offset = %d, key = %s, value = %s\n", offset, key, value);
				replayTransactionLog(value, addUpdater);
			}
		});

		addUpdater.close();
		transactionsLogImportHandler.join();
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

	protected void replayTransactionLog(String value, BigVaultLogAddUpdater addUpdater) {
		Iterator<BigVaultServerTransaction> iteratorTransaction = toIteratorTransaction(value);
		while (iteratorTransaction.hasNext()) {
			addUpdater.add(iteratorTransaction.next());
		}
	}

	private static class BigVaultLogAddUpdater {

		TransactionsLogImportHandler replayHandler;
		BigVaultServerTransactionCombinator combinator;

		private BigVaultLogAddUpdater(TransactionsLogImportHandler replayHandler) {
			this.replayHandler = replayHandler;
			this.combinator = new BigVaultServerTransactionCombinator(DEFAULT_MAX_TRANSACTION_SIZE);
		}

		private void add(BigVaultServerTransaction newTransaction) {
			if (combinator.canCombineWith(newTransaction)) {
				combinator.combineWith(newTransaction);

			} else {
				replayHandler.pushTransaction(combinator.combineAndClean());
				combinator.combineWith(newTransaction);
			}
		}

		public void close() {

			if (combinator.hasData()) {
				replayHandler.pushTransaction(combinator.combineAndClean());
			}
		}
	}

	protected Iterator<BigVaultServerTransaction> toIteratorTransaction(String transaction) {
		List<String> lines = Arrays.asList(StringUtils.split(transaction, "\n"));
		Iterator<List<String>> transactionLinesIterator = new ReaderTransactionLinesIteratorV1(lines.iterator());
		return new ReaderTransactionsIteratorV1("Kafka", transactionLinesIterator, configuration);
	}

	protected ConsumerRecordPoller<String, String> getConsumerRecordPoller(KafkaConsumer<String, String> consumer) {
		return new ConsumerRecordPoller<String, String>(consumer);
	}

	protected KafkaConsumer<String, String> getConsumer() {
		Properties props = new Properties();
		props.put("bootstrap.servers", configuration.getKafkaServers());
		props.put("group.id", UUID.randomUUID().toString());
		// props.put("enable.auto.commit", "true");
		// props.put("auto.commit.interval.ms", "1000");
		props.put("session.timeout.ms", "30000");
		props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		props.put("auto.offset.reset", "earliest");

		KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);

		consumer.subscribe(Arrays.asList(configuration.getKafkaTopic()));

		return consumer;
	}

	@Override
	public void moveTLOGToBackup() {
		// TODO : Kafka backup?
	}

	@Override
	public void deleteLastTLOGBackup() {
		// TODO : Delete Kafka backup?
	}

	@Override
	public void setAutomaticRegroupAndMoveInVaultEnabled(boolean enabled) {
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
