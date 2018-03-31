package com.constellio.data.dao.services.leaderElection;

import static org.apache.curator.framework.recipes.leader.LeaderLatch.CloseMode.SILENT;

import java.io.IOException;
import java.util.UUID;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.utils.CloseableUtils;
import org.apache.ignite.Ignite;
import org.apache.ignite.spi.discovery.DiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.zk.TcpDiscoveryZookeeperIpFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.data.dao.services.factories.DataLayerFactory;

public class ZookeeperLeaderElectionManager implements LeaderElectionManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperLeaderElectionManager.class);
	private static final String PATH = "/constellio/leader";
	private static final String ID = UUID.randomUUID().toString();

	DataLayerFactory dataLayerFactory;
	private LeaderLatch leaderLatch;

	public ZookeeperLeaderElectionManager(DataLayerFactory dataLayerFactory) {
		this.dataLayerFactory = dataLayerFactory;
	}

	public boolean isCurrentNodeLeader() {
		return leaderLatch.hasLeadership();

	}

	@Override
	public void initialize() {
		CuratorFramework curatorFramework = dataLayerFactory.getCuratorFramework();

		this.leaderLatch = new LeaderLatch(curatorFramework, PATH, ID);
		try {
			leaderLatch.start();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void close() {
		CloseableUtils.closeQuietly(leaderLatch);
	}
}
