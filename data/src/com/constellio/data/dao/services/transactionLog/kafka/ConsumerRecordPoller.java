package com.constellio.data.dao.services.transactionLog.kafka;

import java.util.Map;
import java.util.Set;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.record.Records;

public class ConsumerRecordPoller<K, V> {
	private final Consumer<K, V> consumer;

	public ConsumerRecordPoller(Consumer<K, V> consumer) {
		this.consumer = consumer;
	}

	public void poll(ConsumerRecordCallback<K, V> callback) {
		long elements = 0;
		long minOffset = Long.MAX_VALUE;

		ConsumerRecords<K, V> records;
		do {
			records = consumer.poll(30000);

			for (ConsumerRecord<K, V> record : records) {
				if (callback != null) {
					callback.onConsumerRecord(record.offset(), record.topic(), record.key(), record.value());
					elements++;
					minOffset = Math.min(minOffset, record.offset());
				}
			}
		} while (!records.isEmpty());
		 System.out.printf("Min offset %d, element max %d\n", minOffset, elements);
	}
}
