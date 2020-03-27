package com.constellio.data.dao.services.transactionLog.sql;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransactionDocumentLogContent {

	private String id;

	private String version;

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

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public Map<String, String> getFields() {
		if (fields == null) {
			return new HashMap<>();
		}
		return fields;
	}

	public void setFields(Map<String, String> fields) {
		this.fields = fields;
	}

	public void addField(String key, String fieldValue) throws IOException {
		if (this.fields.containsKey(key)) {
			fieldValue = buildMultiValue(key, fieldValue);
		}
		this.fields.put(key, fieldValue);
	}

	private String buildMultiValue(String key, String fieldValue) throws IOException {
		String oldValue = this.fields.get(key);
		List<String> multiValue = new ArrayList<>();
		ObjectMapper mapper = new ObjectMapper();
		if (!oldValue.startsWith("[")) {

			multiValue.add(oldValue);
			multiValue.add(fieldValue);
		} else {
			multiValue = new ArrayList<String>(Arrays.asList(mapper.readValue(oldValue, String[].class)));
			multiValue.add(fieldValue);
		}
		fieldValue = mapper.writeValueAsString(multiValue);
		return fieldValue;
	}
}
