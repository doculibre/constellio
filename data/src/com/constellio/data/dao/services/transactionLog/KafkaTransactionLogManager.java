package com.constellio.data.dao.services.transactionLog;

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
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;

import com.constellio.data.conf.DataLayerConfiguration;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServerTransaction;
import com.constellio.data.dao.services.idGenerator.UUIDV1Generator;
import com.constellio.data.dao.services.transactionLog.SecondTransactionLogRuntimeException.SecondTransactionLogRuntimeException_LogIsInInvalidStateCausedByPreviousException;
import com.constellio.data.dao.services.transactionLog.SecondTransactionLogRuntimeException.SecondTransactionLogRuntimeException_NotAllLogsWereDeletedCorrectlyException;
import com.constellio.data.dao.services.transactionLog.kafka.BlockingDeliveryStrategy;
import com.constellio.data.dao.services.transactionLog.kafka.ConsumerRecordCallback;
import com.constellio.data.dao.services.transactionLog.kafka.ConsumerRecordPoller;
import com.constellio.data.dao.services.transactionLog.kafka.DeliveryStrategy;
import com.constellio.data.dao.services.transactionLog.kafka.FailedDeliveryCallback;
import com.constellio.data.extensions.DataLayerSystemExtensions;

public class KafkaTransactionLogManager implements SecondTransactionLogManager {

	private final Map<String, String> transactions;

	private DataLayerConfiguration configuration;
	private DataLayerSystemExtensions extensions;

	private Producer<String, String> producer;
	private DeliveryStrategy deliveryStrategy;

	private boolean lastFlushFailed;

	private boolean automaticRegroupAndMoveInVaultEnabled;

	public KafkaTransactionLogManager(DataLayerConfiguration configuration, DataLayerSystemExtensions extensions) {
		transactions = Collections.<String, String> synchronizedMap(new HashMap<String, String>());

		this.configuration = configuration;
		this.extensions = extensions;
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
		// FIXME : how to destroy and restore
		
		if (transactions.isEmpty()) {
			Set<String> keySet = transactions.keySet();
			for (Iterator<String> iterator = keySet.iterator(); iterator.hasNext();) {
				flush(iterator.next());
			}
		}

		KafkaConsumer<String, String> consumer = getConsumer();
		ConsumerRecordPoller<String, String> poller = new ConsumerRecordPoller<String, String>(consumer);
		poller.poll(new ConsumerRecordCallback<String, String>() {
			@Override
			public void onConsumerRecord(long offset, String topic, String key, String value) {
				System.out.printf("offset = %d, key = %s, value = %s\n", offset, key, value);
			}
		});
		consumer.close();
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
