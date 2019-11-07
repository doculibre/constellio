package com.constellio.app.services.background;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.threads.BackgroundThreadsManager;

import static com.constellio.data.threads.BackgroundThreadConfiguration.repeatingAction;
import static com.constellio.data.threads.BackgroundThreadExceptionHandling.CONTINUE;
import static org.joda.time.Duration.standardHours;
import static org.joda.time.Duration.standardMinutes;

public class AppLayerBackgroundThreadsManager implements StatefulService {


	AppLayerFactory appLayerFactory;
	BackgroundThreadsManager backgroundThreadsManager;
	UpdateSystemInfoBackgroundAction updateSystemInfoBackgroundAction;
	DownloadLastAlertBackgroundAction downloadLastAlertBackgroundAction;

	public AppLayerBackgroundThreadsManager(AppLayerFactory appLayerFactory) {
		this.appLayerFactory = appLayerFactory;
		this.backgroundThreadsManager = appLayerFactory.getModelLayerFactory().getDataLayerFactory().getBackgroundThreadsManager();
	}

	@Override
	public void initialize() {
		updateSystemInfoBackgroundAction = new UpdateSystemInfoBackgroundAction();
		backgroundThreadsManager.configure(repeatingAction("updateSystemInfo", updateSystemInfoBackgroundAction)
				.executedEvery(standardMinutes(5)).handlingExceptionWith(CONTINUE).runningOnAllInstances());

		downloadLastAlertBackgroundAction = new DownloadLastAlertBackgroundAction(appLayerFactory);
		backgroundThreadsManager.configure(repeatingAction("downloadLastAlertBackgroundAction", downloadLastAlertBackgroundAction)
				.executedEvery(standardHours(1)).handlingExceptionWith(CONTINUE));
	}

	@Override
	public void close() {

	}
}
