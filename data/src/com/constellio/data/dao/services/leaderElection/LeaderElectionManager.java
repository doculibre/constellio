package com.constellio.data.dao.services.leaderElection;

import com.constellio.data.dao.managers.StatefulService;

public interface LeaderElectionManager extends StatefulService {

	boolean isCurrentNodeLeader();
}
