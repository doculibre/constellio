package com.constellio.data.dao.services.transactionLog.kafka;

import com.google.gson.JsonSyntaxException;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.UnsupportedEncodingException;

public class TransactionDeserializer extends BaseSerialization implements Deserializer<Transaction> {

	public TransactionDeserializer() {
	}

	@Override
	public Transaction deserialize(String topic, byte[] data) {
		if (data == null) {
			return null;
		}

		try {
			return gson.fromJson(new String(data, getEncoding()), Transaction.class);
		} catch (JsonSyntaxException | UnsupportedEncodingException e) {
			throw new SerializationException(e);
		}
	}
}
