package com.constellio.app.modules.es.connectors.smb.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;

public class SmbRecordServiceCache {
	private Map<String, String> cachedIdsPerUrl;

	public SmbRecordServiceCache() {
		this.cachedIdsPerUrl = new ConcurrentHashMap<String, String>();
	}

	public String getRecordId(String url) {
		return cachedIdsPerUrl.get(url);
	}

	public void add(String url, String id) {
		cachedIdsPerUrl.put(url, id);
	}

	public void remove(String url) {
		if (StringUtils.isBlank(url)) {
			// Do nothing
		} else {
			cachedIdsPerUrl.remove(url);
		}
	}
}