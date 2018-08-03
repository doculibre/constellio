package com.constellio.data.utils;

import java.util.*;

public class KeyLongMap<K> extends HashMap<K, Long> {

	public void increment(K key, long value) {

		Long currentValue = get(key);
		put(key, currentValue == null ? value : currentValue + value);

	}

	public void increment(K key) {
		increment(key, 1);
	}

	public void decrement(K key, long value) {
		increment(key, -1 * value);
	}

	public void decrement(K key) {
		increment(key, -1);
	}

	public List<Entry<K, Long>> entriesSortedByDescValue() {
		List<Entry<K, Long>> entries = new ArrayList<>(entrySet());

		Collections.sort(entries, new Comparator<Entry<K, Long>>() {
			@Override
			public int compare(Entry<K, Long> o1, Entry<K, Long> o2) {
				return -1 * o1.getValue().compareTo(o2.getValue());
			}
		});

		return entries;
	}

	public List<Entry<K, Long>> entriesSortedByAscValue() {
		List<Entry<K, Long>> entries = new ArrayList<>(entrySet());

		Collections.sort(entries, new Comparator<Entry<K, Long>>() {
			@Override
			public int compare(Entry<K, Long> o1, Entry<K, Long> o2) {
				return o1.getValue().compareTo(o2.getValue());
			}
		});

		return entries;
	}
}
