package com.constellio.data.dao.services.cache;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static com.constellio.data.dao.services.cache.InsertionReason.WAS_OBTAINED;

public abstract class AutoReloadingConstellioCache implements ConstellioCache {

	private static final String NULL_VALUE = "--NULL--";

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
			if (value == null) {
				put(key, NULL_VALUE, WAS_OBTAINED);
			} else {
				put(key, value, WAS_OBTAINED);
			}
		}

		if (NULL_VALUE.equals(value)) {
			return null;
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
		List<Object> values = new ArrayList<>(nestedConstellioCache.getAllValues());
		Iterator<Object> it = values.iterator();
		while (it.hasNext()) {
			if (NULL_VALUE.equals(it.next())) {
				it.remove();
			}
		}
		return values;
	}

	@Override
	public void setOptions(ConstellioCacheOptions options) {
		nestedConstellioCache.setOptions(options);
	}
}
