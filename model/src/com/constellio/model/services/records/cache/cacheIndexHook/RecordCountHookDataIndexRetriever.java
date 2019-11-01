package com.constellio.model.services.records.cache.cacheIndexHook;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class RecordCountHookDataIndexRetriever<K> {

	Map<K, AtomicInteger> map;

	public RecordCountHookDataIndexRetriever(
			Map<K, AtomicInteger> map) {
		this.map = map;
	}

	public int getRecordsCountWith(K key) {
		AtomicInteger atomicInteger = map.get(key);
		return atomicInteger == null ? 0 : atomicInteger.intValue();
	}

	public boolean hasRecordsWith(K key) {
		AtomicInteger atomicInteger = map.get(key);
		return atomicInteger == null ? false : atomicInteger.intValue() > 0;
	}

	public boolean hasRecordsWithAnyKey(List<K> keys) {
		for (K key : keys) {
			if (hasRecordsWith(key)) {
				return true;
			}

		}
		return false;
	}
}