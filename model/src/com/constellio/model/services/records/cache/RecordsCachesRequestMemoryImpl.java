package com.constellio.model.services.records.cache;

import com.constellio.model.entities.records.Record;
import com.constellio.model.services.factories.ModelLayerFactory;

public class RecordsCachesRequestMemoryImpl extends RecordsCachesMemoryImpl {

	public RecordsCachesRequestMemoryImpl(ModelLayerFactory modelLayerFactory) {
		super(modelLayerFactory);
	}

	@Override
	protected RecordsCache newRecordsCache(String collection, ModelLayerFactory modelLayerFactory) {
		RecordsCache recordsCache = super.newRecordsCache(collection, modelLayerFactory);
		//return recordsCache;
		return new RecordsCacheRequestImpl(recordsCache);

	}

	@Override
	protected void onCacheMiss(String id) {
	}

	@Override
	protected void onCacheHit(Record record) {
	}
}
