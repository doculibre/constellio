package com.constellio.model.services.background;

import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.cache.PersistedSortValuesServices;
import com.constellio.model.services.records.cache.RecordsCaches;
import org.joda.time.LocalDateTime;

public class RefreshSortValuesBackgroundAction implements Runnable {

	private PersistedSortValuesServices persistedSortValuesServices;
	private RecordsCaches recordsCaches;

	private LocalDateTime lastRefresh;

	public RefreshSortValuesBackgroundAction(ModelLayerFactory modelLayerFactory) {
		this.persistedSortValuesServices = new PersistedSortValuesServices(modelLayerFactory);
		this.recordsCaches = modelLayerFactory.getRecordsCaches();
	}


	@Override
	public synchronized void run() {

		if (lastRefresh == null) {
			lastRefresh = persistedSortValuesServices.getLastVersionTimeStamp();
			//This is called juste after the loading of the cache, no need to refresh
			return;
		}

		if (!lastRefresh.equals(persistedSortValuesServices.getLastVersionTimeStamp())) {
			recordsCaches.updateRecordsMainSortValue();
		}

	}


}
