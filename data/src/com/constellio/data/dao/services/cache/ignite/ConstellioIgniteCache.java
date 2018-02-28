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
	
	private void lockRead() {
		r.lock();
	}
	
	private void unlockRead() {
		r.unlock();
	}
	
	private void lockWrite() {
		w.lock();
	}
	
	private void unlockWrite() {
		w.unlock();
	}
	
	@SuppressWarnings("unchecked")
	private <T extends Serializable> void synchronizeIfNecessary() {
		lockRead();
		Date expiryDate = DateUtils.addMinutes(lastSynchronizationDate, 5);
		Date now = new Date();
		if (now.after(expiryDate)) {
	        // Must release read lock before acquiring write lock
	        unlockRead();
	        lockWrite();
	        try {
				localCache.clear();
				Iterator<Entry<String, Object>> remoteIterator = igniteCache.iterator();
				while (remoteIterator.hasNext()) {
					Entry<String, Object> remoteEntry = remoteIterator.next();
					put(remoteEntry.getKey(), (T) remoteEntry.getValue(), true);
				}
				lastSynchronizationDate = new Date();
	            // Downgrade by acquiring read lock before releasing write lock
	            lockRead();
	        } finally {
	            unlockWrite(); // Unlock write, still hold read
	        }
		} else {
			unlockRead();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Serializable> T get(String key) {
		lockRead();
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
			unlockRead();
		}
	}

	@Override
	public <T extends Serializable> void put(String key, T value) {
		lockWrite();
		try {
			synchronizeIfNecessary();
			put(key, value, false);
		} finally {
			unlockWrite();
		}
	}
		
	@SuppressWarnings("unchecked")
	<T extends Serializable> void put(String key, T value, boolean locallyOnly) {	
		if (locallyOnly) {
			localCache.put(key, value);
		} else {
			lockWrite();
			try {
				synchronizeIfNecessary();
				value = value == null ? (T) NULL : value;
				localCache.put(key, value);
				if (!locallyOnly) {
					igniteCache.put(key, value);
				}
			} finally {
				unlockWrite();
			}
		}
	}

	@Override
	public void remove(String key) {
		lockWrite();
		try {
			synchronizeIfNecessary();
			localCache.remove(key);
			igniteCache.remove(key);
		} finally {
			unlockWrite();
		}
	}

	@Override
	public void removeAll(Set<String> keys) {
		lockWrite();
		try {
			synchronizeIfNecessary();
			for (String key : keys) {
				removeLocal(key);
			}
			igniteCache.removeAll(keys);
		} finally {
			unlockWrite();
		}
	}

	@Override
	public void clear() {
		lockWrite();
		try {
			synchronizeIfNecessary();
			clearLocal();
			igniteClient.message(igniteClient.cluster().forRemotes()).send(CLEAR_MESSAGE_TOPIC, igniteCache.getName());
		} finally {
			unlockWrite();
		}
	}

	public void removeLocal(String key) {
		lockWrite();
		try {
			synchronizeIfNecessary();
			localCache.remove(key);
		} finally {
			unlockWrite();
		}
	}

	public void clearLocal() {
		lockWrite();
		try {
			synchronizeIfNecessary();
			localCache.clear();
			igniteCache.clear();
		} finally {
			unlockWrite();
		}
	}

	@Override
	public Iterator<String> keySet() {
		lockRead();
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
			unlockRead();
		}
	}

	@Override
	public int size() {
		lockRead();
		try {
			synchronizeIfNecessary();
			return localCache.size();
		} finally {
			unlockRead();
		}
	}

	@Override
	public List<Object> getAllValues() {
		lockRead();
		try {
			synchronizeIfNecessary();
			return new ArrayList<>(localCache.values());
		} finally {
			unlockRead();
		}
	}

}
