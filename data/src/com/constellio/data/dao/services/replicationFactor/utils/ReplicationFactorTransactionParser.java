package com.constellio.data.dao.services.replicationFactor.utils;

import com.constellio.data.dao.services.replicationFactor.dto.ReplicationFactorTransaction;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ReplicationFactorTransactionParser {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	public static ReplicationFactorTransaction toTransaction(String json) {
		try {
			return OBJECT_MAPPER.readValue(json, ReplicationFactorTransaction.class);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static String toJson(ReplicationFactorTransaction transaction) {
		try {
			return OBJECT_MAPPER.writeValueAsString(transaction);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
