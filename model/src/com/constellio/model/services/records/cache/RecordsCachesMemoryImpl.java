package com.constellio.model.services.records.cache;

import com.constellio.data.dao.services.cache.InsertionReason;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.factories.ModelLayerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class RecordsCachesMemoryImpl implements RecordsCaches {

	ModelLayerFactory modelLayerFactory;

	protected Map<String, RecordsCache> collectionsCache = new HashMap<>();

	RecordsCaches nested;

	AtomicBoolean enabled;

	public RecordsCachesMemoryImpl(ModelLayerFactory modelLayerFactory) {
		this.modelLayerFactory = modelLayerFactory;
		this.enabled = new AtomicBoolean(true);
	}

	public RecordsCachesMemoryImpl(ModelLayerFactory modelLayerFactory, RecordsCaches nested) {
		this.modelLayerFactory = modelLayerFactory;
		this.nested = nested;
		this.enabled = new AtomicBoolean(true);
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
		return new RecordsCacheImpl(collection, modelLayerFactory, enabled);
	}

	public boolean isCached(String id) {
		for (RecordsCache cache : collectionsCache.values()) {
			if (cache.isCached(id)) {
				return true;
			}
		}
		return false;
	}

	public List<CacheInsertionStatus> insert(String collection, List<Record> records, InsertionReason reason) {
		RecordsCache cache = getCache(collection);
		return cache.insert(records, reason);
	}

	public CacheInsertionStatus insert(Record record, InsertionReason reason) {
		RecordsCache cache = getCache(record.getCollection());
		return cache.insert(record, reason);
	}

	@Override
	public Record getRecordSummary(String id) {
		return null;
	}

	public Record getRecord(String id) {

		if (!enabled.get()) {
			return null;
		}

		for (RecordsCache cache : collectionsCache.values()) {
			Record record = cache.get(id);
			if (record != null) {
				onCacheHit(record);
				return record;
			}
		}

		if (nested != null) {
			Record record = nested.getRecord(id);
			if (record != null) {
				onCacheHit(record);
				return record;
			}
		}

		onCacheMiss(id);

		return null;
	}

	protected void onCacheMiss(String id) {
		modelLayerFactory.getExtensions().getSystemWideExtensions().onGetByIdCacheMiss(id, 0);
	}

	protected void onCacheHit(Record record) {
		modelLayerFactory.getExtensions().getSystemWideExtensions().onGetByIdCacheHit(record, 0);
	}

	public void invalidateAll() {
		for (RecordsCache cache : collectionsCache.values()) {
			cache.invalidateAll();
		}
	}

	public void invalidate(String collection) {
		collectionsCache.remove(collection);
	}


}
