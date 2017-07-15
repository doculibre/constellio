package com.constellio.model.extensions;

import java.util.List;

import com.constellio.data.frameworks.extensions.VaultBehaviorsList;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.extensions.behaviors.RecordCacheExtension;
import com.constellio.model.extensions.events.recordsCache.CacheHitParams;
import com.constellio.model.extensions.events.recordsCache.CacheMissParams;
import com.constellio.model.extensions.events.recordsCache.CachePutParams;
import com.constellio.model.extensions.events.recordsCache.CacheQueryHitParams;
import com.constellio.model.extensions.events.recordsCache.CacheQueryMissParams;
import com.constellio.model.extensions.events.recordsCache.CacheQueryPutParams;
import com.constellio.model.services.search.query.logical.LogicalSearchQuerySignature;

public class ModelLayerSystemExtensions {

	//------------ Extension points -----------

	public VaultBehaviorsList<RecordCacheExtension> recordCacheExtensions = new VaultBehaviorsList<>();

	//----------------- Callers ---------------

	public void onGetByUniqueMetadataCacheHit(Record record, Metadata metadata, String value, long duration) {
		for (RecordCacheExtension extension : recordCacheExtensions) {
			extension.onCacheHit(new CacheHitParams(metadata, value, record, duration));
		}
	}

	public void onGetByIdCacheHit(Record record, long duration) {
		for (RecordCacheExtension extension : recordCacheExtensions) {
			extension.onCacheHit(new CacheHitParams(Schemas.IDENTIFIER, record.getId(), record, duration));
		}
	}

	public void onGetByUniqueMetadataCacheMiss(Metadata metadata, String value, long duration) {
		for (RecordCacheExtension extension : recordCacheExtensions) {
			extension.onCacheMiss(new CacheMissParams(metadata, value, duration));
		}
	}

	public void onGetByIdCacheMiss(String id, long duration) {
		for (RecordCacheExtension extension : recordCacheExtensions) {
			extension.onCacheMiss(new CacheMissParams(Schemas.IDENTIFIER, id, duration));
		}
	}

	public void onPutInCache(Record record, long duration) {
		for (RecordCacheExtension extension : recordCacheExtensions) {
			extension.onCachePut(new CachePutParams(record, duration));
		}
	}

	public void onQueryCacheHit(LogicalSearchQuerySignature signature, long duration) {
		for (RecordCacheExtension extension : recordCacheExtensions) {
			extension.onCacheQueryHit(new CacheQueryHitParams(signature, duration));
		}
	}

	public void onQueryCacheMiss(LogicalSearchQuerySignature signature, long duration) {
		for (RecordCacheExtension extension : recordCacheExtensions) {
			extension.onCacheQueryMiss(new CacheQueryMissParams(signature, duration));
		}
	}

	public void onPutQueryResultsInCache(LogicalSearchQuerySignature signature, List<String> ids, long duration) {
		for (RecordCacheExtension extension : recordCacheExtensions) {
			extension.onCacheQueryPut(new CacheQueryPutParams(signature, ids, duration));
		}
	}
}
