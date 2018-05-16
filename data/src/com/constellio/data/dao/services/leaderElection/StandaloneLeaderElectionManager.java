package com.constellio.data.dao.services.leaderElection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StandaloneLeaderElectionManager implements LeaderElectionManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(StandaloneLeaderElectionManager.class);

	boolean leader = true;

	public boolean isCurrentNodeLeader() {
		return leader;
	}

	/**
	 * For test purposes only
	 * @param leader
	 */
	public StandaloneLeaderElectionManager setLeader(boolean leader) {
		this.leader = leader;
		return this;
	}

	@Override
	public void initialize() {
		
	}

	@Override
	public void close() {

	}
}
