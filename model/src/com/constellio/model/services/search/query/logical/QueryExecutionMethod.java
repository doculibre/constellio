package com.constellio.model.services.search.query.logical;

import com.constellio.model.entities.schemas.RecordCacheType;

public enum QueryExecutionMethod {
	ENSURE_INDEXED_METADATA_USED, IF_USING_INDEXED_METADATA, USE_CACHE, DEFAULT, USE_CACHE_IF_POSSIBLE, USE_SOLR;

	public boolean tryCache() {
		return this == ENSURE_INDEXED_METADATA_USED || this == USE_CACHE || this == USE_CACHE_IF_POSSIBLE
			   || this == DEFAULT;
	}

	public boolean requiringCacheExecution() {
		return this == ENSURE_INDEXED_METADATA_USED || this == USE_CACHE;
	}

	public boolean requiringCacheIndexBaseStream(RecordCacheType cacheType) {
		return this == ENSURE_INDEXED_METADATA_USED || this == IF_USING_INDEXED_METADATA || (this == DEFAULT && cacheType.isSummaryCache());
	}
}
