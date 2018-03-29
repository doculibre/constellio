package com.constellio.data.dao.services.leaderElection;

import static org.apache.curator.framework.recipes.leader.LeaderLatch.CloseMode.SILENT;

import java.io.IOException;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.ignite.Ignite;
import org.apache.ignite.spi.discovery.DiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.zk.TcpDiscoveryZookeeperIpFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.data.dao.services.factories.DataLayerFactory;

public class ZookeeperLeaderElectionManager implements LeaderElectionManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperLeaderElectionManager.class);

	DataLayerFactory dataLayerFactory;
	private LeaderLatch leaderLatch;

	public static final int DEFAULT_SLEEP_MS = 10000;
	public static final int DEFAULT_RETRY = 3;

	public ZookeeperLeaderElectionManager(DataLayerFactory dataLayerFactory) {
		this.dataLayerFactory = dataLayerFactory;
	}

	public boolean isCurrentNodeLeader() {
		return leaderLatch.hasLeadership();

	}

	@Override
	public void initialize() {
		CuratorFramework curatorFramework = dataLayerFactory.getCuratorFramework();
		Ignite ignite = dataLayerFactory.getIgniteClient();
		DiscoverySpi discoverySpi = ignite.configuration().getDiscoverySpi();
		TcpDiscoveryZookeeperIpFinder ipFinder = (TcpDiscoveryZookeeperIpFinder) ((TcpDiscoverySpi) discoverySpi).getIpFinder();

		this.leaderLatch = new LeaderLatch(curatorFramework, ipFinder.getBasePath(), ipFinder.getServiceName());
		try {
			leaderLatch.start();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void close() {
		try {
			leaderLatch.close(SILENT);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
