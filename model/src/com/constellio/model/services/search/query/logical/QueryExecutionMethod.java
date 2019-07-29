package com.constellio.model.services.search.query.logical;

public enum QueryExecutionMethod {
	ENSURE_INDEXED_METADATA_USED, USE_CACHE, USE_CACHE_IF_POSSIBLE, USE_CACHED_RESULTS, USE_SOLR;

	public boolean tryCache() {
		return this == ENSURE_INDEXED_METADATA_USED || this == USE_CACHE || this == USE_CACHE_IF_POSSIBLE
			   || this == USE_CACHED_RESULTS;
	}

	public boolean requiringCacheExecution() {
		return this == ENSURE_INDEXED_METADATA_USED || this == USE_CACHE;
	}
}
