package com.constellio.data.dao.services.cache.ignite;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
import org.apache.ignite.lang.IgnitePredicate;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;

import com.constellio.data.dao.services.cache.ConstellioCache;
import com.constellio.data.dao.services.cache.ConstellioCacheManager;

public class ConstellioIgniteCacheManager implements ConstellioCacheManager {
	
	private String cacheUrl;
	
	private Map<String, ConstellioIgniteCache> caches = new ConcurrentHashMap<>();
	
	private Ignite client;
	
	private boolean initialized = false;

	public ConstellioIgniteCacheManager(String cacheUrl) {
		this.cacheUrl = cacheUrl;
	}

	@Override
	public void initialize() {
		initializeIfNecessary();
	}
	
	private void initializeIfNecessary() {
		if (!initialized) {
			IgniteConfiguration igniteConfiguration = getConfiguration(cacheUrl);
			client = Ignition.getOrStart(igniteConfiguration);
			addLocalListener();
			initialized = true;
		}	
	}

	@Override
	public void close() {
		if (client != null) {
			client.close();
		}
	}

	private IgniteConfiguration getConfiguration(String cacheUrl) {
		TcpDiscoverySpi spi = new TcpDiscoverySpi();
		TcpDiscoveryVmIpFinder ipFinder = new TcpDiscoveryVmIpFinder();
		ipFinder.setAddresses(splitAddress(cacheUrl));
		spi.setIpFinder(ipFinder);

		IgniteConfiguration cfg = new IgniteConfiguration();
		cfg.setDiscoverySpi(spi);
		cfg.setClientMode(true);
		cfg.setIncludeEventTypes(EventType.EVT_CACHE_OBJECT_PUT,  EventType.EVT_CACHE_OBJECT_REMOVED);

		CacheConfiguration<String, Object> partitionedCacheCfg = new CacheConfiguration<>();
		partitionedCacheCfg.setName("PARTITIONED");
		partitionedCacheCfg.setCacheMode(CacheMode.PARTITIONED);
		partitionedCacheCfg.setBackups(1);
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
	
	@Override
	public synchronized ConstellioCache getCache(String name) {
		initializeIfNecessary();
		
		ConstellioIgniteCache cache = caches.get(name);
		if (cache == null) {
			IgniteCache<String, Object> igniteCache = client.getOrCreateCache(name);
			cache = new ConstellioIgniteCache(name, igniteCache);
			caches.put(name, cache);
		}
		return cache;
	}
	
	public synchronized ConstellioCache getCache(CacheConfiguration<String, Object> cacheConfiguration) {
		initializeIfNecessary();
		
		String name = cacheConfiguration.getName();
		ConstellioIgniteCache cache = caches.get(name);
		if (cache == null) {
			IgniteCache<String, Object> igniteCache = client.getOrCreateCache(cacheConfiguration);
			cache = new ConstellioIgniteCache(name, igniteCache);
			caches.put(name, cache);
		}
		return cache;
	}

	private void addLocalListener() {
		IgnitePredicate<CacheEvent> localListener = new IgnitePredicate<CacheEvent>() {
			@Override 
			public boolean apply(CacheEvent evt) {
				String cacheName = evt.cacheName();
				if (caches.containsKey(cacheName)) {
					ConstellioIgniteCache cache = (ConstellioIgniteCache) getCache(cacheName);
					cache.removeLocal((String) evt.key());
				}
				return true;
			}
		};

		client.events(client.cluster()).localListen(localListener,
				EventType.EVT_CACHE_OBJECT_PUT,
				EventType.EVT_CACHE_OBJECT_REMOVED);
	}

}
