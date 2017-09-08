package com.constellio.data.dao.services.transactionLog.kafka;

import java.util.Map;

public class Transaction {
	private Map<String, Long> versions;
	private String transaction;
	
	public Map<String, Long> getVersions() {
		return versions;
	}

	public void setVersions(Map<String, Long> versions) {
		this.versions = versions;
	}

	public String getTransaction() {
		return transaction;
	}

	public void setTransaction(String transaction) {
		this.transaction = transaction;
	}
}
