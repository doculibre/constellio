package com.constellio.data.utils;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class KeySetMap<K, V> implements Serializable {
	Map<K, Set<V>> map = new HashMap<>();

	public KeySetMap() {
	}

	public KeySetMap(KeySetMap<K, V> filters) {
		for (Entry<K, Set<V>> entry : filters.getMapEntries()) {
			Set<V> values = new HashSet<>(entry.getValue());
			map.put(entry.getKey(), values);
		}
	}

	public void addAll(K key, Collection<V> values) {
		for (V value : values) {
			add(key, value);
		}
	}

	public void addAll(KeySetMap<K, V> values) {
		for (Map.Entry<K, Set<V>> entry : values.getMapEntries()) {
			addAll(entry.getKey(), entry.getValue());
		}
	}

	public void add(K key, V value) {
		Set<V> values = map.get(key);
		if (values == null) {
			values = new HashSet<>();
			map.put(key, values);
		}
		values.add(value);
	}

	public void remove(V key) {
		map.remove(key);
	}

	public Set<V> get(K key) {
		Set<V> values = map.get(key);
		if (values == null) {
			values = new HashSet<>();
			map.put(key, values);
		}
		return values;
	}

	public Set<Entry<K, Set<V>>> getMapEntries() {
		return map.entrySet();
	}

	public Map<K, Set<V>> getNestedMap() {
		return map;
	}

	public void set(K key, Set<V> values) {
		map.put(key, values);
	}

	public boolean contains(K key) {
		return map.containsKey(key);
	}

	public void clear() {
		map.clear();
	}

	public void remove(V key, V value) {
		if (map.containsKey(key)) {
			map.get(key).remove(value);
		}
	}

	public boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	public void putAll(Map<K, Set<V>> values) {
		getNestedMap().putAll(values);
	}
}
