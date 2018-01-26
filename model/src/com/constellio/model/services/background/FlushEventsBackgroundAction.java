package com.constellio.model.services.background;

import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.reindexing.ReindexingServices;

public class FlushEventsBackgroundAction implements Runnable {

	ModelLayerFactory modelLayerFactory;

	public FlushEventsBackgroundAction(ModelLayerFactory modelLayerFactory) {
		this.modelLayerFactory = modelLayerFactory;
	}

	@Override
	public void run() {
		if (ReindexingServices.getReindexingInfos() == null) {
			modelLayerFactory.getDataLayerFactory().newEventsDao().flush();
		}
	}
}
