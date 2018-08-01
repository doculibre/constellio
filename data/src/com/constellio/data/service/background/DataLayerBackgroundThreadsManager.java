package com.constellio.data.service.background;

import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.threads.BackgroundThreadsManager;
import org.joda.time.Duration;

import static com.constellio.data.threads.BackgroundThreadConfiguration.repeatingAction;
import static com.constellio.data.threads.BackgroundThreadExceptionHandling.CONTINUE;

public class DataLayerBackgroundThreadsManager implements StatefulService {
	public static Duration FLUSH_EVENTS_EVERY_DURATION = Duration.standardSeconds(15);

	DataLayerFactory dataLayerFactory;
	BackgroundThreadsManager backgroundThreadsManager;


	public DataLayerBackgroundThreadsManager(DataLayerFactory dataLayerFactory) {
		this.dataLayerFactory = dataLayerFactory;
		this.backgroundThreadsManager = dataLayerFactory.getBackgroundThreadsManager();
	}

	@Override
	public void initialize() {
		backgroundThreadsManager.configure(repeatingAction("vaultAndReplicationRecoverySystem",
				new ReadFileSystemContentDaoRecoveryLogsAndRepairs(dataLayerFactory))
				.executedEvery(Duration.standardMinutes(5)).handlingExceptionWith(CONTINUE));
	}

	@Override
	public void close() {

	}
}
