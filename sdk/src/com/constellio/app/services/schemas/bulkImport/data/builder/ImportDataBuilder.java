package com.constellio.app.services.schemas.bulkImport.data.builder;

import java.util.HashMap;
import java.util.Locale;
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

	public ImportDataBuilder addField(String key, Object value, Locale locale) {
		if (locale == null) {
			this.fields.put(key, value);
		} else {
			this.fields.put(key + "_" + locale.getLanguage(), value);
		}
		return this;
	}

	public ImportDataBuilder addOption(String key, Object value) {
		this.fields.put("option_" + key, value);
		return this;
	}

	public String getSchema() {
		return schema;
	}

	public String getId() {
		return id;
	}

	public Map<String, Object> getFields() {
		return fields;
	}

	public ImportData build(int index) {
		return new ImportData(index, schema, id, fields);
	}
}
