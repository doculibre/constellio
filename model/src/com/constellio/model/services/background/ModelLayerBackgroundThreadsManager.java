package com.constellio.model.services.background;

import static com.constellio.data.threads.BackgroundThreadConfiguration.repeatingAction;
import static com.constellio.data.threads.BackgroundThreadExceptionHandling.CONTINUE;
import static org.joda.time.Duration.standardSeconds;

import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.threads.BackgroundThreadsManager;
import com.constellio.model.services.factories.ModelLayerFactory;

public class ModelLayerBackgroundThreadsManager implements StatefulService {

	ModelLayerFactory modelLayerFactory;
	BackgroundThreadsManager backgroundThreadsManager;
	RecordsReindexingBackgroundAction recordsReindexingBackgroundAction;

	public ModelLayerBackgroundThreadsManager(ModelLayerFactory modelLayerFactory) {
		this.modelLayerFactory = modelLayerFactory;
		this.backgroundThreadsManager = modelLayerFactory.getDataLayerFactory().getBackgroundThreadsManager();
	}

	@Override
	public void initialize() {
		recordsReindexingBackgroundAction = new RecordsReindexingBackgroundAction(modelLayerFactory);
		backgroundThreadsManager.configure(repeatingAction("recordsReindexingBackgroundAction",
				recordsReindexingBackgroundAction)
				.executedEvery(standardSeconds(60)).handlingExceptionWith(CONTINUE));
	}

	@Override
	public void close() {

	}

	public RecordsReindexingBackgroundAction getRecordsReindexingBackgroundAction() {
		return recordsReindexingBackgroundAction;
	}
}
