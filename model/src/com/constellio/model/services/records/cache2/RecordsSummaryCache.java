package com.constellio.model.services.records.cache2;

import com.constellio.data.dao.services.cache.InsertionReason;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordStream;
import com.constellio.model.services.records.cache.CacheInsertionStatus;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

public class RecordsSummaryCache {

	MemoryNotEnoughEfficientRecordsCachesDataStore dataStore = new MemoryNotEnoughEfficientRecordsCachesDataStore();

	RecordServices recordServices;

	MetadataSchemasManager schemasManager;

	public RecordsSummaryCache(ModelLayerFactory modelLayerFactory) {
		this.recordServices = modelLayerFactory.newCachelessRecordServices();
		this.schemasManager = modelLayerFactory.getMetadataSchemasManager();
	}

	void initialize() {

	}

	CacheInsertionStatus insert(Record record, InsertionReason insertionReason) {
//		MetadataSchemaType schemaType = schemasManager.getSchemaTypeOf(record);
		//		//RecordCacheConfig cacheConfig = schemaType.getCacheConfig();
		//		RecordPermanentCacheType cacheType = cacheConfig.getRecordPermanentCacheType();
		//
		//		//TODO Handle getRecordPermanentCacheTypeProvider
		//
		//		if (cacheType.hasPermanentCache()) {
		//			dataStore.put(record.getId(), ((RecordImpl) record).getRecordDTO());
		//		}
		return null;
	}

	Record get(String id) {
		return null;
	}

	RecordStream streamQueryResults(LogicalSearchQuery query) {
		return null;
	}

	RecordStream streamQueryResults(LogicalSearchCondition condition) {
		return null;
	}

	RecordStream streamReferencingId(String id) {
		return null;
	}

	RecordStream streamReferencingIdInMetadata(String id, Metadata metadata) {
		return null;
	}

	void removeFromCache(LogicalSearchQuery query) {

	}

	void removeFromCache(LogicalSearchCondition condition) {

	}

}
