package com.constellio.data.dao.services.ignite;

import org.apache.ignite.Ignite;

public interface IgniteLeaderElectionService {
	public boolean isCurrentNodeLeader(Ignite ignite);
}
