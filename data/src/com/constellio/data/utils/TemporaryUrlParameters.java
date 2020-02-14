package com.constellio.data.utils;

import java.util.HashMap;
import java.util.Map;

public class TemporaryUrlParameters {
	private Map<String, Object> parameters = new HashMap();
	private String key;
	//TimedCache<String, Map<String, Object>> cache = new TimedCache<>(Duration.standardHours(1));

	public TemporaryUrlParameters(String key) {
		this.key = key;
	}

	public String getKey() {
		return key;
	}

	public TemporaryUrlParameters addParameters(String key, Object value) {
		parameters.put(key, value);
		return this;
	}

	public Map<String, Object> getParameters() {
		return parameters;
	}

	/*
	public void save() {
		cache.insert(key, parameters);
	}

	public TimedCache<String, Map<String, Object>> getCache() {
		return cache;
	}
	*/

}