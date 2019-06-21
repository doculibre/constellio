package com.constellio.app.services.background;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.AppLayerFactoryImpl;
import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.threads.BackgroundThreadConfiguration;
import com.constellio.data.threads.BackgroundThreadExceptionHandling;
import com.constellio.data.threads.BackgroundThreadsManager;
import com.constellio.model.conf.FoldersLocatorMode;
import com.constellio.model.conf.ModelLayerConfiguration;
import com.constellio.model.services.background.AuthorizationWithTimeRangeTokenUpdateBackgroundAction;
import com.constellio.model.services.background.FlushEventsBackgroundAction;
import com.constellio.model.services.background.FlushRecordsBackgroundAction;
import com.constellio.model.services.background.RecordsReindexingBackgroundAction;
import com.constellio.model.services.background.TemporaryFolderCleanerBackgroundAction;
import com.constellio.model.services.background.TemporaryRecordsDeletionBackgroundAction;
import com.constellio.model.services.factories.ModelLayerFactory;
import org.apache.commons.lang3.SystemUtils;
import org.joda.time.Duration;

import static com.constellio.data.threads.BackgroundThreadConfiguration.repeatingAction;
import static com.constellio.data.threads.BackgroundThreadExceptionHandling.CONTINUE;
import static org.joda.time.Duration.standardHours;
import static org.joda.time.Duration.standardMinutes;
import static org.joda.time.Duration.standardSeconds;

public class AppLayerBackgroundThreadsManager implements StatefulService {


	AppLayerFactory appLayerFactory;
	BackgroundThreadsManager backgroundThreadsManager;
	UpdateSystemInfoBackgroundAction updateSystemInfoBackgroundAction;

	public AppLayerBackgroundThreadsManager(AppLayerFactory appLayerFactory) {
		this.appLayerFactory = appLayerFactory;
		this.backgroundThreadsManager = appLayerFactory.getModelLayerFactory().getDataLayerFactory().getBackgroundThreadsManager();
	}

	@Override
	public void initialize() {
		updateSystemInfoBackgroundAction = new UpdateSystemInfoBackgroundAction();
		backgroundThreadsManager.configure(repeatingAction("updateSystemInfo", updateSystemInfoBackgroundAction)
				.executedEvery(standardMinutes(1)).handlingExceptionWith(CONTINUE).runningOnAllInstances());
	}

	@Override
	public void close() {

	}
}
