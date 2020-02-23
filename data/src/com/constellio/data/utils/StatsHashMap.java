package com.constellio.data.utils;

import org.checkerframework.checker.units.qual.K;

import java.util.HashMap;
import java.util.Map;

public class StatsHashMap<H, V> extends HashMap<K, V> {

	public static int mapCount;

	public StatsHashMap(int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);
		mapCount++;
	}

	public StatsHashMap(int initialCapacity) {
		super(initialCapacity);
		mapCount++;
	}

	public StatsHashMap() {
		mapCount++;
	}

	public StatsHashMap(Map<? extends K, ? extends V> m) {
		super(m);
		mapCount++;
	}

	@Override
	public V put(K key, V value) {
		return super.put(key, value);
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		super.putAll(m);
	}

	@Override
	public V remove(Object key) {
		return super.remove(key);
	}

	@Override
	public void clear() {
		super.clear();
	}

	@Override
	public boolean remove(Object key, Object value) {
		return super.remove(key, value);
	}

	protected void finalize() throws Throwable {
		mapCount--;
	}
}
