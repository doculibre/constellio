package com.constellio.data.dao.services.cache;

import static com.constellio.data.dao.services.cache.InsertionReason.WAS_OBTAINED;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public abstract class AutoReloadingConstellioCache implements ConstellioCache {

	ConstellioCache nestedConstellioCache;

	public AutoReloadingConstellioCache(ConstellioCache nestedConstellioCache) {
		this.nestedConstellioCache = nestedConstellioCache;
	}

	protected abstract <T extends Serializable> T reload(String key);

	@Override
	public String getName() {
		return nestedConstellioCache.getName();
	}

	@Override
	public <T extends Serializable> T get(String key) {
		T value = nestedConstellioCache.get(key);
		if (value == null) {
			value = reload(key);
			put(key, value, WAS_OBTAINED);
		}
		return value;
	}

	@Override
	public <T extends Serializable> void put(String key, T value, InsertionReason insertionReason) {
		nestedConstellioCache.put(key, value, insertionReason);
	}

	@Override
	public void remove(String key) {
		nestedConstellioCache.remove(key);
	}

	@Override
	public void removeAll(Set<String> keys) {
		nestedConstellioCache.removeAll(keys);
	}

	@Override
	public void clear() {
		nestedConstellioCache.clear();
	}

	@Override
	public Iterator<String> keySet() {
		return nestedConstellioCache.keySet();
	}

	@Override
	public int size() {
		return nestedConstellioCache.size();
	}

	@Override
	public List<Object> getAllValues() {
		return nestedConstellioCache.getAllValues();
	}
}
