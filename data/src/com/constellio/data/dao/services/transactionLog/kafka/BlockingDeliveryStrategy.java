package com.constellio.data.dao.services.transactionLog.kafka;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

public class BlockingDeliveryStrategy implements DeliveryStrategy {

	private TimeUnit timeUnit;
	private long timeout = 0L;

	@Override
	public <K, V> boolean send(Producer<K, V> producer, ProducerRecord<K, V> record, FailedDeliveryCallback callback) {
		try {
			Future<RecordMetadata> future = producer.send(record);

			if (getTimeout() > 0L) {
				future.get(getTimeout(), getTimeUnit());
			} else {
				future.get();
			}

			return true;
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			if (callback != null) {
				callback.onFailedDelivery(e);
			} else {
				throw new RuntimeException(e);
			}
		} 
		
		return false;
	}

	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	public TimeUnit getTimeUnit() {
		if (timeUnit == null) {
			timeUnit = TimeUnit.MILLISECONDS;
		}

		return timeUnit;
	}

	public void setTimeUnit(TimeUnit timeUnit) {
		this.timeUnit = timeUnit;
	}
}
