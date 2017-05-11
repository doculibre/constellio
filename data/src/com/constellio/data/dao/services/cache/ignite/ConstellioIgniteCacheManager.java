package com.constellio.data.dao.services.cache.ignite;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cache.CacheWriteSynchronizationMode;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;

import com.constellio.data.conf.DataLayerConfiguration;
import com.constellio.data.dao.services.cache.ConstellioCacheManager;
import com.constellio.data.dao.services.cache.ConstellioCache;

public class ConstellioIgniteCacheManager implements ConstellioCacheManager {
	
	private Map<String, ConstellioCache> caches = new HashMap<>();
	
	private Ignite client;

	public ConstellioIgniteCacheManager(DataLayerConfiguration dataLayerConfiguration) {
		IgniteConfiguration igniteConfiguration = getConfiguration(dataLayerConfiguration);
		client = Ignition.getOrStart(igniteConfiguration);
	}

	private IgniteConfiguration getConfiguration(DataLayerConfiguration dataLayerConfiguration) {
		TcpDiscoverySpi spi = new TcpDiscoverySpi();
		TcpDiscoveryVmIpFinder ipFinder = new TcpDiscoveryVmIpFinder();
		ipFinder.setAddresses(splitAddress(dataLayerConfiguration.getSettingsCacheUrl()));
		spi.setIpFinder(ipFinder);

		IgniteConfiguration cfg = new IgniteConfiguration();
		cfg.setDiscoverySpi(spi);
		cfg.setClientMode(true);

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
		ConstellioCache cache = caches.get(name);
		if (cache == null) {
			IgniteCache<String, Object> igniteCache = client.getOrCreateCache(name);
			cache = new ConstellioIgniteCache(name, igniteCache);
			caches.put(name, cache);
		}
		return cache;
	}

}
