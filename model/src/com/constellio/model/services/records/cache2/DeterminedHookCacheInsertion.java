package com.constellio.model.services.records.cache2;

public enum DeterminedHookCacheInsertion {

	INSERT_WITH_HOOK_REPLACING_DEFAULT_INSERT,

	INSERT_WITH_HOOK_ALONG_DEFAULT_INSERT,

	INSERT_WITH_HOOK_ALONG_DEFAULT_INSERT_WITHOUT_VOLATILE,


	DEFAULT_INSERT;


	public boolean isInsertingUsingHook() {
		return this != DEFAULT_INSERT;
	}

	public boolean isContinuingVolatileCacheInsertion() {
		return this == DEFAULT_INSERT || this == INSERT_WITH_HOOK_ALONG_DEFAULT_INSERT;
	}

	public boolean isContinuingPermanentCacheInsertion() {
		return this != INSERT_WITH_HOOK_REPLACING_DEFAULT_INSERT;
	}

}
