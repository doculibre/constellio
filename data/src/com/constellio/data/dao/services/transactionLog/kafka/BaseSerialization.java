package com.constellio.data.dao.services.transactionLog.kafka;

import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class BaseSerialization {
	private String encoding = "UTF-8";
	
	protected final Gson gson;
	
	public BaseSerialization() {
		gson = new GsonBuilder().serializeNulls().enableComplexMapKeySerialization().create();
	}

	public void configure(Map<String, ?> configs, boolean isKey) {
		String propertyName = isKey ? "key.serializer.encoding" : "value.serializer.encoding";
		Object encodingValue = configs.get(propertyName);
		if (encodingValue == null)
			encodingValue = configs.get("serializer.encoding");
		if (encodingValue != null && encodingValue instanceof String)
			encoding = (String) encodingValue;
	}

	public void close() {
	}
	
	public String getEncoding() {
		return encoding;
	}
}
