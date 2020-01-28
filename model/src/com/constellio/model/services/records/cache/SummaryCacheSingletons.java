package com.constellio.model.services.records.cache;

import com.constellio.model.services.records.cache.dataStore.FileSystemRecordsValuesCacheDataStore;

import java.util.HashMap;
import java.util.Map;

public class SummaryCacheSingletons {

	static Map<Short, FileSystemRecordsValuesCacheDataStore> dataStore = new HashMap<>();

	/**
	 * Does not support mutli-tenancy
	 */
	static FileSystemRecordsValuesCacheDataStore getDataStore() {
		return dataStore.values().stream().findFirst().get();
	}

}
