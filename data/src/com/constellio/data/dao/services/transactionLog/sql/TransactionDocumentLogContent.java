package com.constellio.data.dao.services.transactionLog.sql;

import java.util.HashMap;
import java.util.Map;

public class TransactionDocumentLogContent {

	private String id;

	private Long version;

	private Map<String, String> fields;

	public TransactionDocumentLogContent() {
		this.fields = new HashMap<>();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	public Map<String, String> getFields() {
		return fields;
	}

	public void setFields(Map<String, String> fields) {
		this.fields = fields;
	}

	public void addField(String key, String fieldValue) {
		this.fields.put(key,fieldValue);
	}
}
