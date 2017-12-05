package com.constellio.data.dao.services.cache.map;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.constellio.data.dao.services.cache.ConstellioCache;

public class ConstellioMapCache implements ConstellioCache {
	
	private String name;
	
	private Map<String, Object> map = new LinkedHashMap<>();

	public ConstellioMapCache(String name) {
		this.name = name;
	}

	@Override
	public final String getName() {
		return name;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Serializable> T get(String key) {
		return (T) map.get(key);
	}

	@Override
	public <T extends Serializable> void put(String key, T value) {
		map.put(key, value);
	}

	@Override
	public void remove(String key) {
		map.remove(key);
	}

	@Override
	public void removeAll(Set<String> keys) {
		for (String key : keys) {
			remove(key);
		}
	}

	@Override
	public void clear() {
		map.clear();
	}

	@Override
	public Iterator<String> keySet() {
		return map.keySet().iterator();
	}

	@Override
	public int size() {
		return map.size();
	}

	@Override
	public List<Object> getAllValues() {
		return new ArrayList<>(map.values());
	}

}
