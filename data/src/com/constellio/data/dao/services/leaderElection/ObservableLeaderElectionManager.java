package com.constellio.data.dao.services.leaderElection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ObservableLeaderElectionManager implements LeaderElectionManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(ObservableLeaderElectionManager.class);

	private List<LeaderElectionManagerObserver> observers = new ArrayList<>();

	private LeaderElectionManager nestedLeaderElectionManager;

	private Boolean previousStatus;

	public ObservableLeaderElectionManager(
			LeaderElectionManager nestedLeaderElectionManager) {
		this.nestedLeaderElectionManager = nestedLeaderElectionManager;
	}

	public void register(LeaderElectionManagerObserver observer) {
		this.observers.add(observer);
	}

	public void unregister(LeaderElectionManagerObserver observer) {
		this.observers.remove(observer);
	}

	public LeaderElectionManager getNestedLeaderElectionManager() {
		return nestedLeaderElectionManager;
	}

	@Override
	public boolean isCurrentNodeLeader() {
		Boolean currentStatus = nestedLeaderElectionManager.isCurrentNodeLeader();

		if (previousStatus == null) {
			previousStatus = currentStatus;

		} else if (previousStatus != currentStatus) {
			previousStatus = currentStatus;
			for (LeaderElectionManagerObserver observer : observers) {
				try {
					observer.onLeaderStatusChanged(currentStatus);

				} catch (Throwable t) {
					LOGGER.error("Leader election observer failed ", t);
				}
			}

		}

		return previousStatus;
	}

	@Override
	public void initialize() {
		nestedLeaderElectionManager.initialize();
	}

	@Override
	public void close() {
		nestedLeaderElectionManager.close();
	}
}
