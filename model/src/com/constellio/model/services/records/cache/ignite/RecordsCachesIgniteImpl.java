package com.constellio.model.services.records.cache.ignite;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.constellio.model.entities.records.Record;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.cache.RecordsCache;
import com.constellio.model.services.records.cache.RecordsCaches;

public class RecordsCachesIgniteImpl implements RecordsCaches {

	ModelLayerFactory modelLayerFactory;

	Map<String, RecordsCache> collectionsCache = new HashMap<>();

	public RecordsCachesIgniteImpl(ModelLayerFactory modelLayerFactory) {
		this.modelLayerFactory = modelLayerFactory;
	}

	public RecordsCache getCache(String collection) {

		//This method is called whenever a service is created
		//Since a synchronize block is slow, we try to use it only when necessary

		RecordsCache cache = collectionsCache.get(collection);

		if (cache == null) {
			return getORCreateCache(collection, modelLayerFactory);
		} else {
			return cache;
		}
	}

	private synchronized RecordsCache getORCreateCache(String collection, ModelLayerFactory modelLayerFactory) {
		RecordsCache cache = collectionsCache.get(collection);

		if (cache == null) {
			cache = newRecordsCache(collection, modelLayerFactory);
			collectionsCache.put(collection, cache);
		}
		return cache;
	}

	protected RecordsCache newRecordsCache(String collection, ModelLayerFactory modelLayerFactory) {
		return new RecordsCacheIgniteImpl(collection, modelLayerFactory);
	}

	public boolean isCached(String id) {
		for (RecordsCache cache : collectionsCache.values()) {
			if (cache.isCached(id)) {
				return true;
			}
		}
		return false;
	}

	public void insert(String collection, List<Record> records) {
		RecordsCache cache = getCache(collection);
		cache.insert(records);
	}

	public void insert(Record record) {
		RecordsCache cache = getCache(record.getCollection());
		cache.insert(record);
	}

	public Record getRecord(String id) {
		long start = new Date().getTime();
		for (RecordsCache cache : collectionsCache.values()) {

			Record record = cache.get(id);
			if (record != null) {
				long end = new Date().getTime();
				onCacheHit(record, end - start);
				return record;
			}
		}

		long end = new Date().getTime();
		onCacheMiss(id, end - start);

		return null;
	}

	protected void onCacheMiss(String id, long duration) {
		modelLayerFactory.getExtensions().getSystemWideExtensions().onGetByIdCacheMiss(id, duration);
	}

	protected void onCacheHit(Record record, long duration) {
		modelLayerFactory.getExtensions().getSystemWideExtensions().onGetByIdCacheHit(record, duration);
	}

	public void invalidateAll() {
		for (RecordsCache cache : collectionsCache.values()) {
			cache.invalidateAll();
		}
	}

	public void invalidate(String collection) {
		collectionsCache.remove(collection);
	}

	public int getCacheObjectsCount() {
		int cacheTotalSize = 0;

		for (RecordsCache cache : collectionsCache.values()) {
			cacheTotalSize += cache.getCacheObjectsCount();
		}

		return cacheTotalSize;
	}
}
