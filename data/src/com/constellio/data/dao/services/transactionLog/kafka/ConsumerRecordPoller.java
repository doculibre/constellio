package com.constellio.data.dao.services.transactionLog.kafka;

import java.util.Map;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;

public class ConsumerRecordPoller<K, V> {
	private final Consumer<K, V> consumer;

	public ConsumerRecordPoller(Consumer<K, V> consumer) {
		this.consumer = consumer;
	}

	public void poll(ConsumerRecordCallback<K, V> callback) {
		Map<TopicPartition, Long> endOffsets = null;

		ConsumerRecords<K, V> records;
		do {
			records = consumer.poll(30000);

			if (endOffsets == null) {
				endOffsets = consumer.endOffsets(consumer.assignment());
			}

			for (ConsumerRecord<K, V> record : records) {
				if (callback != null) {
					callback.onConsumerRecord(record.offset(), record.topic(), record.key(), record.value());
				}

				checkEndOfTopicOffset(endOffsets, record);
			}
		} while (!endOffsets.isEmpty() && !records.isEmpty());
	}

	private void checkEndOfTopicOffset(Map<TopicPartition, Long> endOffsets, ConsumerRecord<K, V> record) {
		TopicPartition tp = new TopicPartition(record.topic(), record.partition());
		Long offset = endOffsets.get(tp);

		if (offset != null && record.offset() == (offset - 1)) {
			endOffsets.remove(tp);
		}
	}
}
