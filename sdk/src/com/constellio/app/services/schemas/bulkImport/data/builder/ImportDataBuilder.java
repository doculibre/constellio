package com.constellio.app.services.schemas.bulkImport.data.builder;

import java.util.HashMap;
import java.util.Map;

import com.constellio.app.services.schemas.bulkImport.data.ImportData;

public class ImportDataBuilder {

	private String schema;

	private String id;

	private Map<String, Object> fields = new HashMap<>();

	public ImportDataBuilder setSchema(String schema) {
		this.schema = schema;
		return this;
	}

	public ImportDataBuilder setId(String id) {
		this.id = id;
		return this;
	}

	public ImportDataBuilder addField(String key, Object value) {
		this.fields.put(key, value);
		return this;
	}

	public ImportData build(int index) {
		return new ImportData(index, schema, id, fields);
	}
}
