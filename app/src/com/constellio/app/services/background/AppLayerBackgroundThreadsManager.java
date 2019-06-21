package com.constellio.app.services.background;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.threads.BackgroundThreadsManager;

import static com.constellio.data.threads.BackgroundThreadConfiguration.repeatingAction;
import static com.constellio.data.threads.BackgroundThreadExceptionHandling.CONTINUE;
import static org.joda.time.Duration.standardMinutes;
import static org.joda.time.Duration.standardSeconds;

public class AppLayerBackgroundThreadsManager implements StatefulService {


	AppLayerFactory appLayerFactory;
	BackgroundThreadsManager backgroundThreadsManager;
	UpdateSystemInfoBackgroundAction updateSystemInfoBackgroundAction;
	DownloadLastSystemAlertBackgroundAction downloadLastSystemAlertBackgroundAction;

	public AppLayerBackgroundThreadsManager(AppLayerFactory appLayerFactory) {
		this.appLayerFactory = appLayerFactory;
		this.backgroundThreadsManager = appLayerFactory.getModelLayerFactory().getDataLayerFactory().getBackgroundThreadsManager();
	}

	@Override
	public void initialize() {
		updateSystemInfoBackgroundAction = new UpdateSystemInfoBackgroundAction();
		backgroundThreadsManager.configure(repeatingAction("updateSystemInfo", updateSystemInfoBackgroundAction)
				.executedEvery(standardMinutes(1)).handlingExceptionWith(CONTINUE).runningOnAllInstances());

		downloadLastSystemAlertBackgroundAction = new DownloadLastSystemAlertBackgroundAction(appLayerFactory);
		backgroundThreadsManager.configure(repeatingAction("downloadLastSystemAlertBackgroundAction", downloadLastSystemAlertBackgroundAction)
				.executedEvery(standardSeconds(30)).handlingExceptionWith(CONTINUE)); // TODO set hours
	}

	@Override
	public void close() {

	}
}
