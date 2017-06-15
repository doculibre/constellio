package com.constellio.data.dao.services.cache.ignite;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cache.CacheWriteSynchronizationMode;
import org.apache.ignite.cache.eviction.lru.LruEvictionPolicy;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.configuration.NearCacheConfiguration;
import org.apache.ignite.events.CacheEvent;
import org.apache.ignite.events.EventType;
import org.apache.ignite.lang.IgniteBiPredicate;
import org.apache.ignite.lang.IgnitePredicate;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;

import com.constellio.data.conf.DataLayerConfiguration;
import com.constellio.data.dao.services.cache.ConstellioCache;
import com.constellio.data.dao.services.cache.ConstellioCacheManager;

public class ConstellioIgniteCacheManager implements ConstellioCacheManager {
	
	/**
	 * Serialized
	 */
	private static final class ConstellioIgniteCacheRemoteListener implements IgnitePredicate<CacheEvent> {

		private ConstellioIgniteCacheRemoteListener(ConstellioIgniteCacheManager cacheManager) {
		}

		@Override 
		public boolean apply(CacheEvent evt) {
			return true;
		}
	}

	private static final class ConstellioIgniteCacheLocalListener implements IgniteBiPredicate<UUID, CacheEvent> {
		
		private ConstellioIgniteCacheManager cacheManager;
		
		private ConstellioIgniteCacheLocalListener(ConstellioIgniteCacheManager cacheManager) {
			this.cacheManager = cacheManager;
		}

		@Override 
		public boolean apply(UUID nodeId, CacheEvent evt) {
			String cacheName = evt.cacheName();
			ConstellioIgniteCache cache = (ConstellioIgniteCache) cacheManager.getCache(cacheName);
			System.out.println(evt.node().id());
			cache.removeLocal((String) evt.key());
			return true;
		}
	}

	private Map<String, ConstellioIgniteCache> caches = new ConcurrentHashMap<>();
	
	private Ignite client;

	public ConstellioIgniteCacheManager(DataLayerConfiguration dataLayerConfiguration) {
		IgniteConfiguration igniteConfiguration = getConfiguration(dataLayerConfiguration);
		client = Ignition.getOrStart(igniteConfiguration);
		addLocalListener();
	}

	private IgniteConfiguration getConfiguration(DataLayerConfiguration dataLayerConfiguration) {
		TcpDiscoverySpi spi = new TcpDiscoverySpi();
		TcpDiscoveryVmIpFinder ipFinder = new TcpDiscoveryVmIpFinder();
		ipFinder.setAddresses(splitAddress(dataLayerConfiguration.getSettingsCacheUrl()));
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
		ConstellioIgniteCache cache = caches.get(name);
		if (cache == null) {
			// Create near-cache configuration for "myCache".
			NearCacheConfiguration<String, Object> nearCfg = 
			    new NearCacheConfiguration<>();

			// Use LRU eviction policy to automatically evict entries
			// from near-cache, whenever it reaches 100_000 in size.
			nearCfg.setNearEvictionPolicy(new LruEvictionPolicy<String, Object>(100_000));
			
			IgniteCache<String, Object> igniteCache = client.getOrCreateCache(
				    new CacheConfiguration<String, Object>(name), nearCfg);
			cache = new ConstellioIgniteCache(name, igniteCache);
			caches.put(name, cache);
		}
		return cache;
	}

	private void addLocalListener() {
		new Thread() {
			@Override
			public void run() {
		        IgniteBiPredicate<UUID, CacheEvent> locLsnr = new ConstellioIgniteCacheLocalListener(ConstellioIgniteCacheManager.this);
		        IgnitePredicate<CacheEvent> rmtLsnr = new ConstellioIgniteCacheRemoteListener(ConstellioIgniteCacheManager.this);
		        client.events().remoteListen(locLsnr, rmtLsnr, EventType.EVT_CACHE_OBJECT_PUT, EventType.EVT_CACHE_OBJECT_REMOVED);
			}
		}.start();
	}

}
