package com.constellio.model.services.background;

import static com.constellio.data.threads.BackgroundThreadConfiguration.repeatingAction;
import static com.constellio.data.threads.BackgroundThreadExceptionHandling.CONTINUE;
import static org.joda.time.Duration.*;

import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.threads.BackgroundThreadConfiguration;
import com.constellio.data.threads.BackgroundThreadExceptionHandling;
import com.constellio.data.threads.BackgroundThreadsManager;
import com.constellio.model.conf.ModelLayerConfiguration;
import com.constellio.model.services.event.EventService;
import com.constellio.model.services.factories.ModelLayerFactory;
import org.joda.time.Duration;

public class ModelLayerBackgroundThreadsManager implements StatefulService {

	ModelLayerFactory modelLayerFactory;
	BackgroundThreadsManager backgroundThreadsManager;
	RecordsReindexingBackgroundAction recordsReindexingBackgroundAction;
	TemporaryRecordsDeletionBackgroundAction temporaryRecordsDeletionBackgroundAction;
	EventService eventService;

	public ModelLayerBackgroundThreadsManager(ModelLayerFactory modelLayerFactory) {
		this.modelLayerFactory = modelLayerFactory;
		this.backgroundThreadsManager = modelLayerFactory.getDataLayerFactory().getBackgroundThreadsManager();
	}

	@Override
	public void initialize() {
		recordsReindexingBackgroundAction = new RecordsReindexingBackgroundAction(modelLayerFactory);
		backgroundThreadsManager.configure(repeatingAction("recordsReindexingBackgroundAction",
				recordsReindexingBackgroundAction)
				.executedEvery(standardSeconds(120)).handlingExceptionWith(CONTINUE));

		ModelLayerConfiguration configuration = modelLayerFactory.getConfiguration();
		backgroundThreadsManager.configure(BackgroundThreadConfiguration.repeatingAction("removeTimedOutTokens", new Runnable() {
			@Override
			public void run() {
				modelLayerFactory.getUserCredentialsManager().removeTimedOutTokens();
			}
		}).handlingExceptionWith(BackgroundThreadExceptionHandling.CONTINUE)
				.executedEvery(configuration.getTokenRemovalThreadDelayBetweenChecks()));

		temporaryRecordsDeletionBackgroundAction = new TemporaryRecordsDeletionBackgroundAction(modelLayerFactory);
		backgroundThreadsManager.configure(repeatingAction("temporaryRecordsDeletionBackgroundAction",
				temporaryRecordsDeletionBackgroundAction)
				.executedEvery(standardHours(12)).handlingExceptionWith(CONTINUE));

		//Will be enabled in version 7.6
//		eventService = new EventService(modelLayerFactory);
//		backgroundThreadsManager.configure(repeatingAction("eventServiceArchiveEventsAndDeleteFromSolr", eventService
//		).executedEvery(Duration.standardHours(3)).handlingExceptionWith(CONTINUE));
	}

	@Override
	public void close() {

	}

	public RecordsReindexingBackgroundAction getRecordsReindexingBackgroundAction() {
		return recordsReindexingBackgroundAction;
	}

	public TemporaryRecordsDeletionBackgroundAction getTemporaryRecordsDeletionBackgroundAction() {
		return temporaryRecordsDeletionBackgroundAction;
	}

	public void configureBackgroundThreadConfiguration(BackgroundThreadConfiguration configuration) {
		backgroundThreadsManager.configure(configuration);
	}
}
