package com.constellio.data.utils;

import java.util.HashMap;
import java.util.Map;

public class HashMapBuilder<K, V> {

	Map<K, V> map = new HashMap<>();

	public HashMapBuilder<K, V> entry(K key, V value) {
		map.put(key, value);
		return this;
	}

	public Map<K, V> build() {
		return map;
	}

	public static HashMapBuilder<String, Object> stringObjectMap() {
		return new HashMapBuilder<String, Object>();
	}

}
