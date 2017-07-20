package com.constellio.model.services.records.cache;

import com.constellio.model.entities.records.Record;
import com.constellio.model.services.factories.ModelLayerFactory;

public class RecordsCachesRequestMemoryImpl extends RecordsCachesMemoryImpl {

	String id;
	boolean disconnected;

	public RecordsCachesRequestMemoryImpl(ModelLayerFactory modelLayerFactory, String id) {
		super(modelLayerFactory);
		this.id = id;
	}

	@Override
	protected RecordsCache newRecordsCache(String collection, ModelLayerFactory modelLayerFactory) {
		return new RecordsCacheRequestImpl(id, modelLayerFactory.getBottomRecordsCaches().getCache(collection));

	}

	@Override
	protected void onCacheMiss(String id) {
	}

	@Override
	protected void onCacheHit(Record record) {
	}

	public String getId() {
		return id;
	}

	public void disconnect() {
		disconnected = true;
		for (RecordsCache recordsCache : collectionsCache.values()) {
			if (recordsCache instanceof RecordsCacheRequestImpl) {
				((RecordsCacheRequestImpl)recordsCache).disconnect();
			}
		}
	}
}
