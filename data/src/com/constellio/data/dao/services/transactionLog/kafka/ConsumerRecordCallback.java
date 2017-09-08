package com.constellio.data.dao.services.transactionLog.kafka;

public interface ConsumerRecordCallback<K, V> {
	/**
	 * Fire the listener to handle the record
	 * 
	 * @param offset
	 *            The current offset
	 * @param topic
	 *            The record's topic
	 * @param key
	 *            The key content
	 * @param value
	 *            The stream content
	 */
	public void onConsumerRecord(long offset, String topic, K key, V value);
}
