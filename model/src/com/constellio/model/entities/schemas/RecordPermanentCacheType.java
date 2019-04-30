package com.constellio.model.entities.schemas;

public enum RecordPermanentCacheType {

	NOT_CACHED, SUMMARY_CACHED, FULLY_CACHED;

	public boolean hasPermanentCache() {
		return this == SUMMARY_CACHED || this == FULLY_CACHED;
	}
}
