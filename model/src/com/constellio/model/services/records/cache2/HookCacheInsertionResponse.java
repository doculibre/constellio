package com.constellio.model.services.records.cache2;

import com.constellio.model.services.records.cache.CacheInsertionStatus;

public class HookCacheInsertionResponse {

	CacheInsertionStatus status;

	RemoteCacheAction hookRemoteCacheAction;

	public HookCacheInsertionResponse(CacheInsertionStatus status,
									  RemoteCacheAction hookRemoteCacheAction) {
		this.status = status;
		this.hookRemoteCacheAction = hookRemoteCacheAction;
	}
}
