package com.constellio.data.dao.services.cache.ignite;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cache.CacheWriteSynchronizationMode;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.events.CacheEvent;
import org.apache.ignite.events.EventType;
import org.apache.ignite.lang.IgniteBiPredicate;
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
			IgniteConfiguration igniteConfiguration = getConfiguration(cacheUrl);
			igniteClient = Ignition.getOrStart(igniteConfiguration);
			addListener();
			initialized = true;
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
		partitionedCacheCfg.setName("PARTITIONED");
		partitionedCacheCfg.setCacheMode(CacheMode.PARTITIONED);
		partitionedCacheCfg.setBackups(0);
		partitionedCacheCfg.setReadFromBackup(true);
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
	public synchronized ConstellioCache getCache(String name) {
		initializeIfNecessary();
		name = versionedCacheName(name);
		ConstellioIgniteCache cache = caches.get(name);
		if (cache == null) {
			IgniteCache<String, Object> igniteCache = igniteClient.getOrCreateCache(name);
			cache = new ConstellioIgniteCache(name, igniteCache, igniteClient);
			caches.put(name, cache);
		}
		return cache;
	}

	public synchronized ConstellioCache getCache(CacheConfiguration<String, Object> cacheConfiguration) {
		initializeIfNecessary();

		String name = cacheConfiguration.getName();
		name = versionedCacheName(name);
		cacheConfiguration.setName(name);
		ConstellioIgniteCache cache = caches.get(name);
		if (cache == null) {
			IgniteCache<String, Object> igniteCache = igniteClient.getOrCreateCache(cacheConfiguration);
			cache = new ConstellioIgniteCache(name, igniteCache, igniteClient);
			caches.put(name, cache);
		}
		return cache;
	}

	private void addListener() {
		IgniteBiPredicate<UUID, CacheEvent> localListener = new IgniteBiPredicate<UUID, CacheEvent>() {
			@Override
			public boolean apply(UUID uuid, CacheEvent evt) {
				String cacheName = evt.cacheName();
				if (caches.containsKey(cacheName)) {
					ConstellioIgniteCache cache = (ConstellioIgniteCache) getCache(cacheName);
					cache.removeLocal((String) evt.key());
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
				.remoteListen(ConstellioIgniteCache.CLEAR_MESSAGE_TOPIC, new IgniteBiPredicate<UUID, String>() {
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

}
