package com.constellio.data.dao.services.cache.ignite;

import com.constellio.data.dao.services.cache.ConstellioCache;
import com.constellio.data.dao.services.cache.ConstellioCacheOptions;
import com.constellio.data.dao.services.cache.InsertionReason;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteDataStreamer;

import javax.cache.Cache.Entry;
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

import static com.constellio.data.dao.services.cache.InsertionReason.WAS_OBTAINED;

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

	private ConstellioCacheOptions options;

	public ConstellioIgniteCache(String name, IgniteCache<String, Object> igniteCache, Ignite igniteClient,
								 ConstellioCacheOptions options) {
		this.name = name;
		this.options = options;
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

	private void readLock() {
		r.lock();
	}

	private void readUnlock() {
		r.unlock();
	}

	private void writeLock() {
		w.lock();
	}

	private void writeUnlock() {
		w.unlock();
	}

	@SuppressWarnings("unchecked")
	private <T extends Serializable> void synchronizeIfNecessary() {
		Date expiryDate = DateUtils.addMinutes(lastSynchronizationDate, 5);
		Date now = new Date();
		if (now.after(expiryDate)) {
			writeLock();
			try {
				localCache.clear();
				Iterator<Entry<String, Object>> remoteIterator = igniteCache.iterator();
				while (remoteIterator.hasNext()) {
					Entry<String, Object> remoteEntry = remoteIterator.next();
					put(remoteEntry.getKey(), (T) remoteEntry.getValue(), true);
				}
				lastSynchronizationDate = new Date();
			} finally {
				writeUnlock();
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Serializable> T get(String key) {
		synchronizeIfNecessary();
		readLock();
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
			readUnlock();
		}
	}

	@Override
	public <T extends Serializable> void put(String key, T value, InsertionReason insertionReason) {
		synchronizeIfNecessary();
		writeLock();
		try {
			put(key, value, insertionReason == WAS_OBTAINED);
		} finally {
			writeUnlock();
		}
	}

	@SuppressWarnings("unchecked")
	<T extends Serializable> void put(String key, T value, boolean locallyOnly) {
		if (locallyOnly) {
			localCache.put(key, value);
		} else {
			synchronizeIfNecessary();
			writeLock();
			try {
				value = value == null ? (T) NULL : value;
				localCache.put(key, value);
				igniteCache.put(key, value);
			} finally {
				writeUnlock();
			}
		}
	}

	@Override
	public void remove(String key) {
		synchronizeIfNecessary();
		writeLock();
		try {
			localCache.remove(key);
			igniteCache.remove(key);
		} finally {
			writeUnlock();
		}
	}

	@Override
	public void removeAll(Set<String> keys) {
		synchronizeIfNecessary();
		writeLock();
		try {
			for (String key : keys) {
				localCache.remove(key);
			}
			igniteCache.removeAll(keys);
		} finally {
			writeUnlock();
		}
	}

	@Override
	public void clear() {
		synchronizeIfNecessary();
		writeLock();
		try {
			clearLocal();
			igniteClient.message(igniteClient.cluster().forRemotes()).send(CLEAR_MESSAGE_TOPIC, igniteCache.getName());
		} finally {
			writeUnlock();
		}
	}

	public void removeLocal(String key) {
		synchronizeIfNecessary();
		writeLock();
		try {
			localCache.remove(key);
		} finally {
			writeUnlock();
		}
	}

	public void clearLocal() {
		synchronizeIfNecessary();
		writeLock();
		try {
			localCache.clear();
			igniteCache.clear();
		} finally {
			writeUnlock();
		}
	}

	@Override
	public Iterator<String> keySet() {
		synchronizeIfNecessary();
		readLock();
		try {
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
			readUnlock();
		}
	}

	@Override
	public int size() {
		synchronizeIfNecessary();
		readLock();
		try {
			return localCache.size();
		} finally {
			readUnlock();
		}
	}

	@Override
	public List<Object> getAllValues() {
		synchronizeIfNecessary();
		readLock();
		try {
			return new ArrayList<>(localCache.values());
		} finally {
			readUnlock();
		}
	}

	@Override
	public void setOptions(ConstellioCacheOptions options) {
		this.options = options;
	}

}
