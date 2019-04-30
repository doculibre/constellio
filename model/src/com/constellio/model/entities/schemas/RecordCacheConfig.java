package com.constellio.model.entities.schemas;

import com.constellio.model.entities.records.Record;

public class RecordCacheConfig {

	private final RecordPermanentCacheType recordPermanentCacheType;

	private final RecordPermanentCacheTypeProvider recordPermanentCacheTypeProvider;

	private final boolean hasVolatileCache;

	private final int volatileCacheSize;

	public RecordCacheConfig(
			RecordPermanentCacheTypeProvider recordPermanentCacheTypeProvider, boolean hasVolatileCache,
			int volatileCacheSize) {
		this.recordPermanentCacheType = null;
		this.recordPermanentCacheTypeProvider = recordPermanentCacheTypeProvider;
		this.hasVolatileCache = hasVolatileCache;
		this.volatileCacheSize = volatileCacheSize;
	}

	public RecordCacheConfig(
			RecordPermanentCacheType recordPermanentCacheType, boolean hasVolatileCache,
			int volatileCacheSize) {
		this.recordPermanentCacheType = recordPermanentCacheType;
		this.recordPermanentCacheTypeProvider = null;
		this.hasVolatileCache = hasVolatileCache;
		this.volatileCacheSize = volatileCacheSize;
	}

	public RecordPermanentCacheTypeProvider getRecordPermanentCacheTypeProvider() {
		return recordPermanentCacheTypeProvider;
	}

	public boolean isHasVolatileCache() {
		return hasVolatileCache;
	}

	public int getVolatileCacheSize() {
		return volatileCacheSize;
	}

	public RecordPermanentCacheType getRecordPermanentCacheType() {
		return recordPermanentCacheType;
	}

	public interface RecordPermanentCacheTypeProvider {
		RecordPermanentCacheType getCacheType(Record record);
	}

	public static final RecordCacheConfig ALWAYS_FULLY_CACHED = new RecordCacheConfig(
			(Record) -> RecordPermanentCacheType.FULLY_CACHED, false, 0);

	public static final RecordCacheConfig ALWAYS_SUMMARY_CACHED_WITHOUT_VOLATILE = new RecordCacheConfig(
			(Record) -> RecordPermanentCacheType.SUMMARY_CACHED, false, 0);

	public static final RecordCacheConfig ALWAYS_SUMMARY_CACHED_WITH_FULL_VOLATILE(int size) {
		return new RecordCacheConfig((Record) -> RecordPermanentCacheType.FULLY_CACHED, true, size);
	}

	public static final RecordCacheConfig NEVER_CACHED = new RecordCacheConfig(
			(Record) -> RecordPermanentCacheType.NOT_CACHED, false, 0);

}
