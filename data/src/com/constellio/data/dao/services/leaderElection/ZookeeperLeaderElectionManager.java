package com.constellio.data.dao.services.leaderElection;

import com.constellio.data.dao.services.factories.DataLayerFactory;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.utils.CloseableUtils;

import java.util.UUID;

public class ZookeeperLeaderElectionManager implements LeaderElectionManager {

	private static final String PATH = "/constellio/leader";

	DataLayerFactory dataLayerFactory;
	LeaderLatch leaderLatch;
	String ID = UUID.randomUUID().toString();

	public ZookeeperLeaderElectionManager(DataLayerFactory dataLayerFactory) {
		this.dataLayerFactory = dataLayerFactory;
	}

	public boolean isCurrentNodeLeader() {
		return leaderLatch.hasLeadership();
	}

	@Override
	public void initialize() {
		initialize(PATH);
	}

	void initialize(String path) {
		CuratorFramework curatorFramework = dataLayerFactory.getCuratorFramework();

		this.leaderLatch = new LeaderLatch(curatorFramework, path, ID);
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
