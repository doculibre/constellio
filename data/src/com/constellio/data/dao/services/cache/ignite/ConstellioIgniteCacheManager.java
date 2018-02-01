package com.constellio.data.dao.services.cache.ignite;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.binary.BinaryObject;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cache.CacheWriteSynchronizationMode;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.events.CacheEvent;
import org.apache.ignite.events.EventType;
import org.apache.ignite.lang.IgniteBiPredicate;
import org.apache.ignite.lang.IgniteFuture;
import org.apache.ignite.lang.IgnitePredicate;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;

import com.constellio.data.dao.services.cache.ConstellioCache;
import com.constellio.data.dao.services.cache.ConstellioCacheManager;

public class ConstellioIgniteCacheManager implements ConstellioCacheManager {

	private String cacheUrl;
	private String constellioVersion;
	private Map<String, ConstellioIgniteCache> caches = new ConcurrentHashMap<>();

	private Ignite igniteClient;
	private boolean initialized = false;
	
	private static ThreadLocal<Map<ConstellioIgniteCache, Map<String, Object>>> putTransaction = new ThreadLocal<>();

	public ConstellioIgniteCacheManager(String cacheUrl, String constellioVersion) {
		this.cacheUrl = cacheUrl;
		this.constellioVersion = constellioVersion;
	}

	@Override
	public void initialize() {
		initializeIfNecessary();
	}

	private void initializeIfNecessary() {
        if (!initialized) {
            synchronized (this) {
                if (!initialized) {
                    IgniteConfiguration igniteConfiguration = getConfiguration(cacheUrl);
                    igniteClient = Ignition.getOrStart(igniteConfiguration);
                    addListener();
                    initialized = true;
                }
            }
        }
    }

	@Override
	public void close() {
		if (igniteClient != null) {
			igniteClient.close();
		}
	}

	public Ignite getClient() {
		return igniteClient;
	}

	private IgniteConfiguration getConfiguration(String cacheUrl) {
		TcpDiscoverySpi spi = new TcpDiscoverySpi();
		TcpDiscoveryVmIpFinder ipFinder = new TcpDiscoveryVmIpFinder();
		ipFinder.setAddresses(splitAddress(cacheUrl));
		spi.setIpFinder(ipFinder);

		IgniteConfiguration cfg = new IgniteConfiguration();
		cfg.setPeerClassLoadingEnabled(true);
		cfg.setDiscoverySpi(spi);
		cfg.setClientMode(true);
		cfg.setIncludeEventTypes(EventType.EVT_CACHE_OBJECT_PUT, EventType.EVT_CACHE_OBJECT_REMOVED);

		CacheConfiguration<String, Object> partitionedCacheCfg = new CacheConfiguration<>();
		partitionedCacheCfg.setName("REPLICATED");
		partitionedCacheCfg.setCacheMode(CacheMode.REPLICATED);
		partitionedCacheCfg.setWriteSynchronizationMode(CacheWriteSynchronizationMode.FULL_SYNC);

		cfg.setCacheConfiguration(partitionedCacheCfg);
		return cfg;
	}

	private List<String> splitAddress(String addressList) {
		String[] addresses = addressList.split(",");
		return new ArrayList<>(Arrays.asList(addresses));
	}

	@Override
	public List<String> getCacheNames() {
		return Collections.unmodifiableList(new ArrayList<>(caches.keySet()));
	}

	@Override
	public void clearAll() {
		for (ConstellioCache cache : caches.values()) {
			cache.clear();
		}
	}


	private String versionedCacheName(String name) {
		String versionedCacheName;
		String prefix = constellioVersion + "_";
		if (name.startsWith(prefix)) {
			versionedCacheName = name;
		} else {
			versionedCacheName = prefix + name;
		}
		return versionedCacheName;
	}

	@Override
	public ConstellioCache getCache(String name) {
		initializeIfNecessary();
		name = versionedCacheName(name);
		ConstellioIgniteCache cache = caches.get(name);
		if (cache == null) {
			synchronized (this) {
				if (cache == null) {
					IgniteCache<String, Object> igniteCache = igniteClient.getOrCreateCache(name);
					cache = new ConstellioIgniteCache(name, igniteCache, igniteClient);
					caches.put(name, cache);
				}
			}
		}
		return cache;
	}

	public ConstellioCache getCache(CacheConfiguration<String, Object> cacheConfiguration) {
		initializeIfNecessary();

		String name = cacheConfiguration.getName();
		name = versionedCacheName(name);
		cacheConfiguration.setName(name);
		ConstellioIgniteCache cache = caches.get(name);
		if (cache == null) {
			synchronized (this) {
				if (cache == null) {
					final IgniteCache<String, Object> igniteCache = igniteClient.getOrCreateCache(cacheConfiguration);
					cache = new ConstellioIgniteCache(name, igniteCache, igniteClient) {
						@Override
						public <T extends Serializable> void put(String key, T value) {
							Map<ConstellioIgniteCache, Map<String, Object>> transactionMap = putTransaction.get();
							if (transactionMap != null) {
								super.put(key, value, true);
								Map<String, Object> transactionObjects = transactionMap.get(this);
								if (transactionObjects == null) {
									transactionObjects = new TreeMap<>();
									transactionMap.put(this, transactionObjects);
								}
								transactionObjects.put(key, value);
							} else {
								super.put(key, value);
		//						Map<String, Object> keyValue = new TreeMap<>();
		//						keyValue.put(key, value);
		//						igniteCache.putAll(keyValue);
							}
						}
					};
					caches.put(name, cache);
				}
			}
		}	
		return cache;
	}

	private void addListener() {
		IgniteBiPredicate<UUID, CacheEvent> localListener = new IgniteBiPredicate<UUID, CacheEvent>() {
			@SuppressWarnings("unchecked")
			@Override
			public boolean apply(UUID uuid, CacheEvent evt) {
				String cacheName = evt.cacheName();
				if (caches.containsKey(cacheName)) {
					ConstellioIgniteCache cache = (ConstellioIgniteCache) getCache(cacheName);
					String key = evt.key();
					Serializable value = (Serializable) evt.newValue();
					if (evt.type() == EventType.EVT_CACHE_OBJECT_PUT) {
						if (value instanceof BinaryObject) {
							BinaryObject bo = (BinaryObject) value;
							value = bo.deserialize();
						} else if (value instanceof List) {
							List<Serializable> valueAsList = (List<Serializable>) value;
							List<Serializable> newList = new ArrayList<>();
							for (Serializable serializable : valueAsList) {
								if (serializable instanceof BinaryObject) {
									BinaryObject bo = (BinaryObject) serializable;
									newList.add((Serializable) bo.deserialize());
								} else {
									newList.add(serializable);
								}
							}
							value = (Serializable) newList;
						}
						cache.put(key, value, true);
					} else {
						cache.removeLocal(key);
					}
				}
				return true;
			}
		};

		IgnitePredicate<CacheEvent> remoteListener = new IgnitePredicate<CacheEvent>() {
			@Override
			public boolean apply(CacheEvent evt) {
				return true;
			}
		};

		igniteClient.events(igniteClient.cluster()).remoteListen(localListener, remoteListener,
				EventType.EVT_CACHE_OBJECT_PUT,
				EventType.EVT_CACHE_OBJECT_REMOVED);

		igniteClient.message(igniteClient.cluster().forRemotes())
				.localListen(ConstellioIgniteCache.CLEAR_MESSAGE_TOPIC, new IgniteBiPredicate<UUID, String>() {
					@Override
					public boolean apply(UUID nodeId, String cacheName) {
						if (caches.containsKey(cacheName)) {
							ConstellioIgniteCache cache = (ConstellioIgniteCache) getCache(cacheName);
							cache.clearLocal();
						}
						return true;
					}
				});
	}
	
	public void beginPutTransaction() {
		putTransaction.set(new HashMap<ConstellioIgniteCache, Map<String,Object>>());
	}
	
	public void commitPutTransaction() {
		Map<ConstellioIgniteCache, Map<String, Object>> transactionMap = putTransaction.get();
		if (transactionMap != null) {
			for (Iterator<ConstellioIgniteCache> it = transactionMap.keySet().iterator(); it.hasNext();) {
				ConstellioIgniteCache cache = it.next();
				Map<String, Object> transactionObjects = transactionMap.get(cache);
				if (transactionObjects != null) {
//					cache.getIgniteCache().putAll(transactionObjects);
					IgniteFuture<?> result = cache.getIgniteStreamer().addData(transactionObjects);
					cache.getIgniteStreamer().flush();
					result.get();
				}	
				it.remove();
			}
		}
		putTransaction.set(null);
	}

}
