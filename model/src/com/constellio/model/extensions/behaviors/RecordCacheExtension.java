package com.constellio.model.extensions.behaviors;

import com.constellio.model.extensions.events.recordsCache.*;

public abstract class RecordCacheExtension {

	public void onCacheHit(CacheHitParams params) {
	}

	public void onCacheMiss(CacheMissParams params) {
	}

	public void onCachePut(CachePutParams params) {
	}

	public void onCacheQueryHit(CacheQueryHitParams params) {
	}

	public void onCacheQueryMiss(CacheQueryMissParams params) {
	}

	public void onCacheQueryPut(CacheQueryPutParams params) {
	}

}
