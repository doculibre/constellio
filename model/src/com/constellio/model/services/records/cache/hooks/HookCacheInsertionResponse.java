package com.constellio.model.services.records.cache.hooks;

import com.constellio.model.services.records.cache.CacheInsertionStatus;

public class HookCacheInsertionResponse {

	CacheInsertionStatus status;

	RemoteCacheAction hookRemoteCacheAction;

	public HookCacheInsertionResponse(CacheInsertionStatus status,
									  RemoteCacheAction hookRemoteCacheAction) {
		this.status = status;
		this.hookRemoteCacheAction = hookRemoteCacheAction;
	}

	public CacheInsertionStatus getStatus() {
		return status;
	}

	public RemoteCacheAction getHookRemoteCacheAction() {
		return hookRemoteCacheAction;
	}
}
