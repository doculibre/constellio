package com.constellio.model.services.background;

import com.constellio.data.conf.FoldersLocator;
import com.constellio.data.conf.FoldersLocatorMode;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.cache.PersistedSortValuesServices;
import com.constellio.model.services.records.cache.RecordsCaches;
import org.joda.time.LocalDateTime;

public class RefreshSortValuesBackgroundAction implements Runnable {

	private PersistedSortValuesServices persistedSortValuesServices;
	private RecordsCaches recordsCaches;
	private ModelLayerFactory modelLayerFactory;
	private LocalDateTime lastRefresh;

	public RefreshSortValuesBackgroundAction(ModelLayerFactory modelLayerFactory) {
		this.persistedSortValuesServices = new PersistedSortValuesServices(modelLayerFactory);
		this.recordsCaches = modelLayerFactory.getRecordsCaches();
		this.modelLayerFactory = modelLayerFactory;
	}


	@Override
	public synchronized void run() {

		if (lastRefresh == null) {
			lastRefresh = persistedSortValuesServices.getLastVersionTimeStamp();
			//This is called juste after the loading of the cache, no need to refresh
			return;
		}

		boolean officeHours = new FoldersLocator().getFoldersLocatorMode() == FoldersLocatorMode.WRAPPER
							  && TimeProvider.getLocalDateTime().getHourOfDay() >= 7
							  && TimeProvider.getLocalDateTime().getHourOfDay() <= 18;

		if (!officeHours
			&& !lastRefresh.equals(persistedSortValuesServices.getLastVersionTimeStamp())
			&& !modelLayerFactory.isReindexing()) {
			recordsCaches.updateRecordsMainSortValue();
		}

	}


}
