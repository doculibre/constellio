package com.constellio.model.services.records.cache.cacheIndexHook;

import com.constellio.model.services.records.RecordId;

public class MetadataIndexCacheDataStoreHookUtils {


	public static long combine(RecordId id1, RecordId id2) {
		return toIntKey(id1) * (long) Math.pow(2, 32) + toIntKey(id2);
	}

	public static int toIntKey(RecordId id) {
		return id.isInteger() ? id.intValue() : id.stringValue().hashCode();
	}

	public static long toLongKey(RecordId id) {
		return id.isInteger() ? id.intValue() : id.stringValue().hashCode();
	}

	/**
	 * based on String.hashCode()
	 */
	public static long hash(String string) {
		long h = 1125899906842597L; // prime
		int len = string.length();

		for (int i = 0; i < len; i++) {
			h = 31 * h + string.charAt(i);
		}
		return h;
	}
}
