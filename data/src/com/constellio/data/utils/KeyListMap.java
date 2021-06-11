package com.constellio.data.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class KeyListMap<K, V> implements Serializable {
	Map<K, List<V>> map = new HashMap<>();

	public void addAll(K key, List<V> values) {
		for (V value : values) {
			add(key, value);
		}
	}

	public void add(K key, V value) {
		List<V> values = map.get(key);
		if (values == null) {
			values = new ArrayList<>();
			map.put(key, values);
		}
		values.add(value);
	}

	public void addAtStart(K key, V value) {
		List<V> values = map.get(key);
		if (values == null) {
			values = new ArrayList<>();
			map.put(key, values);
		}
		values.add(0, value);
	}

	public void remove(K key) {
		map.remove(key);
	}

	public List<V> get(K key) {
		List<V> values = map.get(key);
		if (values == null) {
			values = new ArrayList<>();
		}
		return values;
	}

	public Set<Entry<K, List<V>>> getMapEntries() {
		return map.entrySet();
	}

	public Map<K, List<V>> getNestedMap() {
		return map;
	}

	public void set(K key, List<V> values) {
		map.put(key, values);
	}

	public boolean contains(K key) {
		return map.containsKey(key);
	}

	public void clear() {
		map.clear();
	}

	public boolean contains(K key, V value) {
		return contains(key) && get(key).contains(value);
	}
}
