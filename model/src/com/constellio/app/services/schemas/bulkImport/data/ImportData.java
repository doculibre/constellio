package com.constellio.app.services.schemas.bulkImport.data;

import com.constellio.data.utils.XmlUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static org.apache.commons.lang3.StringUtils.substringAfter;

public class ImportData {

	private int index;

	private String legacyId;

	private String schema;

	private Map<String, Object> fields;

	private Map<String, Object> options;

	public ImportData(int index, String schema, String legacyId, Map<String, Object> fieldsAndOptions) {
		this.legacyId = legacyId;
		this.index = index;
		this.fields = new HashMap<>();
		this.options = new HashMap<>();
		this.schema = schema;
		for (Map.Entry<String, Object> fieldOrOption : fieldsAndOptions.entrySet()) {
			if (fieldOrOption.getKey().startsWith("option_")) {
				options.put(substringAfter(fieldOrOption.getKey(), "option_"), fieldOrOption.getValue());

			} else {
				fields.put(fieldOrOption.getKey(), fieldOrOption.getValue());
			}
		}
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

	public Map<String, Object> getOptions() {
		return options;
	}

	public Map<String, Object> getFields() {
		return Collections.unmodifiableMap(fields);
	}

	public <T> T getValue(String key) {
		return (T) fields.get(key);
	}

	public <T> T getOption(String key) {
		return (T) options.get(key);
	}

	public <K, V> Map<K, V> getMap(String key) {
		Map<K, V> values = getValue(key);
		return values == null ? new HashMap<K, V>() : values;
	}

	public <V> List<V> getList(String key) {
		List<V> values = getValue(key);
		return values == null ? new ArrayList<V>() : values;
	}

	public void unescapeFieldNames() {
		Iterator<Entry<String, Object>> entryIterator = fields.entrySet().iterator();

		Map<String, Object> newEntries = new HashMap<>();
		while (entryIterator.hasNext()) {
			Entry<String, Object> entry = entryIterator.next();
			String key = entry.getKey();
			String decodedKey = XmlUtils.unescapeAttributeName(key);
			if (!key.equals(decodedKey)) {
				newEntries.put(decodedKey, entry.getValue());
				entryIterator.remove();
			}
		}
		fields.putAll(newEntries);
	}
}
