package com.constellio.model.extensions.behaviors;

import com.constellio.model.extensions.events.recordsCache.CacheHitParams;
import com.constellio.model.extensions.events.recordsCache.CacheMissParams;
import com.constellio.model.extensions.events.recordsCache.CachePutParams;
import com.constellio.model.extensions.events.recordsCache.CacheQueryParams;

public abstract class RecordCacheExtension {

	public void onCacheHit(CacheHitParams params) {
	}

	public void onCacheMiss(CacheMissParams params) {
	}

	public void onCachePut(CachePutParams params) {
	}

	public void onCacheQueryHit(CacheQueryParams params) {
	}

	public void onCacheQuery(CacheQueryParams params) {
	}

}
