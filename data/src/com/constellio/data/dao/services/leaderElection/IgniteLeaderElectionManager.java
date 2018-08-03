package com.constellio.data.dao.services.leaderElection;

import com.constellio.data.dao.services.factories.DataLayerFactory;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCluster;
import org.apache.ignite.cluster.ClusterNode;
import org.apache.ignite.internal.util.typedef.internal.CU;
import org.apache.ignite.internal.util.typedef.internal.U;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;

public class IgniteLeaderElectionManager implements LeaderElectionManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(IgniteLeaderElectionManager.class);

	DataLayerFactory dataLayerFactory;

	public IgniteLeaderElectionManager(DataLayerFactory dataLayerFactory) {
		this.dataLayerFactory = dataLayerFactory;
	}

	public boolean isCurrentNodeLeader() {
		return isCurrentNodeLeader(dataLayerFactory.getIgniteClient());

	}

	boolean isCurrentNodeLeader(Ignite ignite) {
		IgniteCluster cluster = ignite.cluster();

		Collection<ClusterNode> clients = new ArrayList<>();

		Collection<ClusterNode> all = cluster.nodes();
		for (ClusterNode clusterNode : all) {
			if (CU.clientNode(clusterNode)) {
				clients.add(clusterNode);
			}
		}

		ClusterNode oldest = U.oldest(clients, null);

		ClusterNode localNode = cluster.localNode();
		return localNode.id().equals(oldest.id());

	}

	@Override
	public void initialize() {

	}

	@Override
	public void close() {

	}
}
