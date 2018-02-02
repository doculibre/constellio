package com.constellio.model.services.records.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.model.entities.records.Record;
import com.constellio.model.services.factories.ModelLayerFactory;

public class RecordsCachesRequestMemoryImpl extends RecordsCachesMemoryImpl {

	private static final Logger LOGGER = LoggerFactory.getLogger(RecordsCachesRequestMemoryImpl.class);

	String id;
	boolean disconnected;

	public RecordsCachesRequestMemoryImpl(ModelLayerFactory modelLayerFactory, String id) {
		super(modelLayerFactory, modelLayerFactory.getBottomRecordsCaches());
		this.id = id;
	}

	@Override
	protected RecordsCache newRecordsCache(String collection, ModelLayerFactory modelLayerFactory) {
		RecordsCache recordsCache = new RecordsCacheRequestImpl(id,
				modelLayerFactory.getBottomRecordsCaches().getCache(collection));

		if (disconnected) {
			((RecordsCacheRequestImpl) recordsCache).disconnect();
			LOGGER.warn("A cache is created for disconnected cache '" + id + "'", new Throwable());
		}

		return recordsCache;

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
				((RecordsCacheRequestImpl) recordsCache).disconnect();
			}
		}
	}

	public boolean isDisconnected() {
		return disconnected;
	}
}
