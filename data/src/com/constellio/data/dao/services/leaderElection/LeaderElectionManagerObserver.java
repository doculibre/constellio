package com.constellio.data.dao.services.leaderElection;

public interface LeaderElectionManagerObserver {

	void onLeaderStatusChanged(boolean newStatus);

}
