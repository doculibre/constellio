package com.constellio.data.dao.services.cache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cache.CacheWriteSynchronizationMode;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.events.EventType;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;

import com.constellio.model.services.schemas.MetadataSchemasManager;

public class IgniteCacheManagerAcceptanceTest {
	
	private static List<String> splitAddress(String addressList) {
		String[] addresses = addressList.split(",");
		return new ArrayList<>(Arrays.asList(addresses));
	}

	private static IgniteConfiguration getConfiguration() {
		TcpDiscoverySpi spi = new TcpDiscoverySpi();
		TcpDiscoveryVmIpFinder ipFinder = new TcpDiscoveryVmIpFinder();
		ipFinder.setAddresses(splitAddress("localhost:47500"));
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
	
	public static void main(String[] args) throws Exception {
		IgniteConfiguration igniteConfiguration = getConfiguration();
		Ignite client = Ignition.start(igniteConfiguration);

		IgniteCache<String, Object> igniteCache = client.getOrCreateCache(MetadataSchemasManager.class.getName());
		
		igniteCache.remove("zeCollection");
		igniteCache.put("test", "OneTwo! " + new Date());
		
		client.close();
	}

}
