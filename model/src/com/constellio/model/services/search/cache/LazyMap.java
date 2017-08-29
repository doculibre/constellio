package com.constellio.model.services.search.cache;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.NotNull;

public abstract class LazyMap<K, V> implements Map<K, V> {

	Map<K, V> map;

	abstract Map<K, V> getMap();

	private Map<K, V> map() {
		if (map == null) {
			map = getMap();
		}
		return map;
	}

	@Override
	public int size() {
		return map().size();
	}

	@Override
	public boolean isEmpty() {
		return map().isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return map().containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return map().containsValue(value);
	}

	@Override
	public V get(Object key) {
		return map().get(key);
	}

	@Override
	public V put(K key, V value) {
		return map().put(key, value);
	}

	@Override
	public V remove(Object key) {
		return map().remove(key);
	}

	@Override
	public void putAll(@NotNull Map<? extends K, ? extends V> m) {
		map().putAll(m);
	}

	@Override
	public void clear() {
		map().clear();
	}

	@NotNull
	@Override
	public Set<K> keySet() {
		return map().keySet();
	}

	@NotNull
	@Override
	public Collection<V> values() {
		return map().values();
	}

	@NotNull
	@Override
	public Set<Entry<K, V>> entrySet() {
		return map().entrySet();
	}
}
