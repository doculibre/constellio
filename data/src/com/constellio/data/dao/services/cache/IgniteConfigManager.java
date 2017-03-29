package com.constellio.data.dao.services.cache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

public class IgniteConfigManager {
	private Ignite client;

	public IgniteConfigManager(DataLayerConfiguration dataLayerConfiguration) {
		client = Ignition.getOrStart(getConfiguration(dataLayerConfiguration));
	}

	private IgniteConfiguration getConfiguration(DataLayerConfiguration dataLayerConfiguration) {
		TcpDiscoverySpi spi = new TcpDiscoverySpi();
		TcpDiscoveryVmIpFinder ipFinder = new TcpDiscoveryVmIpFinder();
		ipFinder.setAddresses(splitAddress(dataLayerConfiguration.getSettingsCacheUrl()));
		spi.setIpFinder(ipFinder);

		IgniteConfiguration cfg = new IgniteConfiguration();
		cfg.setDiscoverySpi(spi);
		cfg.setClientMode(true);

		CacheConfiguration partitionedCacheCfg = new CacheConfiguration();
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

	public IgniteCache getRecordCache() {
		return client.getOrCreateCache("records");
	}

	public IgniteCache getManagerCache() {
		return client.getOrCreateCache("manager");
	}
}
