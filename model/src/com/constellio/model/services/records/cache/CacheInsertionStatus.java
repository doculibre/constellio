package com.constellio.model.services.records.cache;

public enum CacheInsertionStatus {
	ACCEPTED, REFUSED_OLD_VERSION, REFUSED_NULL, REFUSED_UNSAVED, REFUSED_DIRTY, REFUSED_NOT_FULLY_LOADED, REFUSED_NOT_CACHED;

}
