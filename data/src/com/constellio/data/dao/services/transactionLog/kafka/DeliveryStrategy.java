package com.constellio.data.dao.services.transactionLog.kafka;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

public interface DeliveryStrategy {
	public <K, V> boolean send(Producer<K, V> producer, ProducerRecord<K, V> producerRecord, FailedDeliveryCallback callback);
}
