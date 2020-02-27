package com.constellio.data.utils;

import org.joda.time.LocalDateTime;
import org.joda.time.ReadableDuration;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static com.constellio.data.utils.TimeProvider.getLocalDateTime;

public class TimedCache<K, V> implements Serializable {

	Map<K, TimedCacheEntry<V>> cache = new HashMap<>();

	ReadableDuration durationInCache;

	public TimedCache(ReadableDuration durationInCache) {
		this.durationInCache = durationInCache;
	}

	public synchronized void insert(K key, V value) {

		if (key != null && value != null) {
			TimedCacheEntry entry = new TimedCacheEntry<V>(value, getLocalDateTime().plus(durationInCache));
			cache.put(key, entry);
		}
	}

	public V get(K key) {
		TimedCacheEntry<V> entry = cache.get(key);
		if (entry != null && entry.endingTime.isAfter(getLocalDateTime())) {
			return entry.value;
		} else {
			return null;
		}
	}

	public static class TimedCacheEntry<V> implements Serializable {
		V value;
		LocalDateTime endingTime;

		public TimedCacheEntry(V value, LocalDateTime endingTime) {
			this.value = value;
			this.endingTime = endingTime;
		}
	}
}
