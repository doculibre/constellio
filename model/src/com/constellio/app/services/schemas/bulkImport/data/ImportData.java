package com.constellio.app.services.schemas.bulkImport.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImportData {

	private int index;

	private String legacyId;

	private String schema;

	private Map<String, Object> fields;

	public ImportData(int index, String schema, String legacyId, Map<String, Object> fields) {
		this.legacyId = legacyId;
		this.index = index;
		this.fields = fields;
		this.schema = schema;
	}

	public String getLegacyId() {
		return legacyId;
	}

	public int getIndex() {
		return index;
	}

	public String getSchema() {
		return schema;
	}

	public Map<String, Object> getFields() {
		return Collections.unmodifiableMap(fields);
	}

	public <T> T getValue(String key) {
		return (T) fields.get(key);
	}

	public <K, V> Map<K, V> getMap(String key) {
		Map<K, V> values = getValue(key);
		return values == null ? new HashMap<K, V>() : values;
	}

	public <V> List<V> getList(String key) {
		List<V> values = getValue(key);
		return values == null ? new ArrayList<V>() : values;
	}

}
