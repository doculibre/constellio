package com.constellio.data.dao.services.transactionLog.kafka;

import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

import java.io.UnsupportedEncodingException;

public class TransactionSerializer extends BaseSerialization implements Serializer<Transaction> {

	public TransactionSerializer() {
		super();
	}

	@Override
	public byte[] serialize(String topic, Transaction data) {
		if (data == null) {
			return null;
		}

		try {
			return gson.toJson(data).getBytes(getEncoding());
		} catch (UnsupportedEncodingException e) {
			throw new SerializationException(e);
		}
	}
}
