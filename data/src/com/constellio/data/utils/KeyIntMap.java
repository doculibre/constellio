package com.constellio.data.utils;

import java.util.*;

public class KeyIntMap<K> extends HashMap<K, Integer> {

	public void increment(K key, int value) {

		Integer currentValue = get(key);
		put(key, currentValue == null ? value : currentValue + value);

	}

	public void increment(K key) {
		increment(key, 1);
	}

	public void decrement(K key, int value) {
		increment(key, -1 * value);
	}

	public void decrement(K key) {
		increment(key, -1);
	}

	public List<Entry<K, Integer>> entriesSortedByDescValue() {
		List<Entry<K, Integer>> entries = new ArrayList<>(entrySet());

		Collections.sort(entries, new Comparator<Entry<K, Integer>>() {
			@Override
			public int compare(Entry<K, Integer> o1, Entry<K, Integer> o2) {
				return -1 * o1.getValue().compareTo(o2.getValue());
			}
		});

		return entries;
	}

	public List<Entry<K, Integer>> entriesSortedByAscValue() {
		List<Entry<K, Integer>> entries = new ArrayList<>(entrySet());

		Collections.sort(entries, new Comparator<Entry<K, Integer>>() {
			@Override
			public int compare(Entry<K, Integer> o1, Entry<K, Integer> o2) {
				return o1.getValue().compareTo(o2.getValue());
			}
		});

		return entries;
	}

	@Override
	public Integer get(Object key) {
		Integer mapValue = super.get(key);
		return mapValue == null ? 0 : mapValue;
	}
}
