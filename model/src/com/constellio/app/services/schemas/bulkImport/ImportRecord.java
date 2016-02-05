package com.constellio.app.services.schemas.bulkImport;

import java.util.Map;

public class ImportRecord {
	private final String collection;
	private final String schemaType;
	private final String previousSystemId;
	private final Map<String, Object> fields;

	public ImportRecord(String collection, String schemaType, String previousSystemId, Map<String, Object> fields) {
		this.collection = collection;
		this.schemaType = schemaType;
		this.previousSystemId = previousSystemId;
		this.fields = fields;
	}

	public String getCollection() {
		return collection;
	}

	public String getSchemaType() {
		return schemaType;
	}

	public String getPreviousSystemId() {
		return previousSystemId;
	}

	public Map<String, Object> getFields() {
		return fields;
	}
}
