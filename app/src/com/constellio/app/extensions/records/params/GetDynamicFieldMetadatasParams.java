package com.constellio.app.extensions.records.params;

public class GetDynamicFieldMetadatasParams {

	private String schemaTypeCode;

	private String collection;

	public GetDynamicFieldMetadatasParams(String schemaTypeCode, String collection) {
		this.schemaTypeCode = schemaTypeCode;
		this.collection = collection;
	}

	public String getSchemaTypeCode() {
		return schemaTypeCode;
	}

	public String getCollection() {
		return collection;
	}

}
