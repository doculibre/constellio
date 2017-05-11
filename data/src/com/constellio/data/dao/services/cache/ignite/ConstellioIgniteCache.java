package com.constellio.data.dao.services.cache.ignite;

import java.io.Serializable;
import java.util.Iterator;

import javax.cache.Cache;

import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.query.SqlQuery;

import com.constellio.data.dao.services.cache.ConstellioCache;

public class ConstellioIgniteCache implements ConstellioCache {
	
	private String name;
	
	private IgniteCache<String, Object> igniteCache;

	public ConstellioIgniteCache(String name, IgniteCache<String, Object> igniteCache) {
		this.name = name;
		this.igniteCache = igniteCache;
	}

	@Override
	public final String getName() {
		return name;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Serializable> T get(String key) {
		return (T) igniteCache.get(key);
	}

	@Override
	public <T extends Serializable> void put(String key, T value) {
		igniteCache.put(key, value);
	}

	@Override
	public void remove(String key) {
		igniteCache.clear(key);
	}

	@Override
	public void clear() {
		igniteCache.clear();
	}

	@Override
	public Iterator<String> keySet() {
		final Iterator<Cache.Entry<String, Object>> adaptee = igniteCache.iterator();
		return new Iterator<String>() {
			@Override
			public boolean hasNext() {
				return adaptee.hasNext();
			}

			@Override
			public String next() {
				return adaptee.next().getKey();
			}
		};
	}

}
