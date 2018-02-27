package com.constellio.data.dao.services.cache.ignite;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.cache.Cache.Entry;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteDataStreamer;

import com.constellio.data.dao.services.cache.ConstellioCache;

public class ConstellioIgniteCache implements ConstellioCache {
	
	public static final String CLEAR_MESSAGE_TOPIC = "clear";

	private static final Object NULL = "__NULL__";

	private String name;

	private IgniteCache<String, Object> igniteCache;
	
	private IgniteDataStreamer<String, Object> igniteStreamer;
	
	private Ignite igniteClient;

	private Map<String, Object> localCache = new ConcurrentHashMap<>();
	
	private volatile Date lastSynchronizationDate = new Date();   
	
	private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock r = rwl.readLock();
    private final Lock w = rwl.writeLock();


	public ConstellioIgniteCache(String name, IgniteCache<String, Object> igniteCache, Ignite igniteClient) {
		this.name = name;
		this.igniteCache = igniteCache;
		this.igniteClient = igniteClient;
		this.igniteStreamer = igniteClient.dataStreamer(igniteCache.getName()); 
		this.igniteStreamer.allowOverwrite(true);
	}

	@Override
	public final String getName() {
		return name;
	}
	
	public IgniteCache<String, Object> getIgniteCache() {
		return igniteCache;
	}

	public IgniteDataStreamer<String, Object> getIgniteStreamer() {
		return igniteStreamer;
	}
	
	@SuppressWarnings("unchecked")
	private <T extends Serializable> void synchronizeIfNecessary() {
		r.lock();
		Date expiryDate = DateUtils.addMinutes(lastSynchronizationDate, 5);
		Date now = new Date();
		if (now.after(expiryDate)) {
	        // Must release read lock before acquiring write lock
	        r.unlock();
	        w.lock();
	        try {
				localCache.clear();
				Iterator<Entry<String, Object>> remoteIterator = igniteCache.iterator();
				while (remoteIterator.hasNext()) {
					Entry<String, Object> remoteEntry = remoteIterator.next();
					put(remoteEntry.getKey(), (T) remoteEntry.getValue(), true);
				}
				lastSynchronizationDate = new Date();
	            // Downgrade by acquiring read lock before releasing write lock
	            r.lock();
	        } finally {
	            w.unlock(); // Unlock write, still hold read
	        }
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Serializable> T get(String key) {
		r.lock();
		try {
			T result = (T) localCache.get(key);
			if (result == null) {
				result = (T) igniteCache.get(key);
				if (result != null) {
					localCache.put(key, result);
				} else {
					localCache.put(key, NULL);
				}
			}
			result = NULL.equals(result) ? null : result;
			return result;
		} finally {
			r.unlock();
		}
	}

	@Override
	public <T extends Serializable> void put(String key, T value) {
		w.lock();
		try {
			synchronizeIfNecessary();
			put(key, value, false);
		} finally {
			w.unlock();
		}
	}
		
	@SuppressWarnings("unchecked")
	<T extends Serializable> void put(String key, T value, boolean locallyOnly) {	
		if (locallyOnly) {
			localCache.put(key, value);
		} else {
			w.lock();
			try {
				synchronizeIfNecessary();
				value = value == null ? (T) NULL : value;
				localCache.put(key, value);
				if (!locallyOnly) {
					igniteCache.put(key, value);
				}
			} finally {
				w.unlock();
			}
		}
	}

	@Override
	public void remove(String key) {
		w.lock();
		try {
			synchronizeIfNecessary();
			localCache.remove(key);
			igniteCache.remove(key);
		} finally {
			w.unlock();
		}
	}

	@Override
	public void removeAll(Set<String> keys) {
		w.lock();
		try {
			synchronizeIfNecessary();
			for (String key : keys) {
				removeLocal(key);
			}
			igniteCache.removeAll(keys);
		} finally {
			w.unlock();
		}
	}

	@Override
	public void clear() {
		w.lock();
		try {
			synchronizeIfNecessary();
			clearLocal();
			igniteClient.message(igniteClient.cluster().forRemotes()).send(CLEAR_MESSAGE_TOPIC, igniteCache.getName());
		} finally {
			w.unlock();
		}
	}

	public void removeLocal(String key) {
		w.lock();
		try {
			synchronizeIfNecessary();
			localCache.remove(key);
		} finally {
			w.unlock();
		}
	}

	public void clearLocal() {
		w.lock();
		try {
			synchronizeIfNecessary();
			localCache.clear();
			igniteCache.clear();
		} finally {
			w.unlock();
		}
	}

	@Override
	public Iterator<String> keySet() {
		r.lock();
		try {
			synchronizeIfNecessary();
			final Iterator<String> adaptee = localCache.keySet().iterator();

			return new Iterator<String>() {
				@Override
				public boolean hasNext() {
					return adaptee.hasNext();
				}

				@Override
				public String next() {
					return adaptee.next();
				}

				@Override
				public void remove() {
					throw new UnsupportedOperationException();
				}
			};
		} finally {
			r.unlock();
		}
	}

	@Override
	public int size() {
		r.lock();
		try {
			synchronizeIfNecessary();
			return localCache.size();
		} finally {
			r.unlock();
		}
	}

	@Override
	public List<Object> getAllValues() {
		r.lock();
		try {
			synchronizeIfNecessary();
			return new ArrayList<>(localCache.values());
		} finally {
			r.unlock();
		}
	}

}
