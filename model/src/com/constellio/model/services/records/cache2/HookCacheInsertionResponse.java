package com.constellio.model.services.records.cache2;

import com.constellio.model.services.records.cache.CacheInsertionStatus;

public class HookCacheInsertionResponse {

	CacheInsertionStatus status;

	RemoteCacheAction remoteCacheAction;

	public HookCacheInsertionResponse(CacheInsertionStatus status,
									  RemoteCacheAction remoteCacheAction) {
		this.status = status;
		this.remoteCacheAction = remoteCacheAction;
	}
}
