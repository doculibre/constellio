package com.constellio.data.dao.services.transactionLog.kafka;

public interface ConsumerRecordCallback<K, V> {
	/**
	 * 
	 * @param offset
	 * @param topic
	 * @param key
	 * @param value
	 */
	public void onConsumerRecord(long offset, String topic, K key, V value);
}
