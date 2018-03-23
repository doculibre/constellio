package com.constellio.data.dao.services.ignite;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCluster;
import org.apache.ignite.cluster.ClusterNode;
import org.apache.ignite.internal.util.typedef.internal.CU;
import org.apache.ignite.internal.util.typedef.internal.U;

import com.constellio.data.dao.services.factories.DataLayerFactory;

public class DefaultLeaderElectionServiceImpl implements LeaderElectionService {

	DataLayerFactory dataLayerFactory;

	boolean leader = true;

	public DefaultLeaderElectionServiceImpl(DataLayerFactory dataLayerFactory) {
		this.dataLayerFactory = dataLayerFactory;
	}

	@Override
	public boolean isCurrentNodeLeader() {
		Ignite ignite = dataLayerFactory.getIgniteClient();
		if (ignite != null) {
			return isCurrentNodeLeader(ignite);
		} else {

			return leader;
		}

	}

	public boolean isCurrentNodeLeader(Ignite ignite) {
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

	public DefaultLeaderElectionServiceImpl setLeader(boolean leader) {
		this.leader = leader;
		return this;
	}
}
