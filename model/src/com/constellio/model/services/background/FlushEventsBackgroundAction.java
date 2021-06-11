package com.constellio.model.services.background;

import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.services.factories.ModelLayerFactory;

public class FlushEventsBackgroundAction implements Runnable {

	ModelLayerFactory modelLayerFactory;

	public FlushEventsBackgroundAction(ModelLayerFactory modelLayerFactory) {
		this.modelLayerFactory = modelLayerFactory;
	}

	@Override
	public void run() {
		if (!modelLayerFactory.isReindexing()
			&& modelLayerFactory.getRecordsCaches().areSummaryCachesInitialized()
			&& Toggle.ADVANCED_SEARCH_CONFIGS.isEnabled()) {
			modelLayerFactory.getDataLayerFactory().newEventsDao().flush();
		}
	}
}
