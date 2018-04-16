package com.constellio.model.services.records.cache.eventBus;

import static com.constellio.data.events.EventBusEventsExecutionStrategy.ONLY_SENT_REMOTELY;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.data.events.EventBus;
import com.constellio.data.events.EventBusManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.cache.RecordsCache;
import com.constellio.model.services.records.cache.RecordsCachesMemoryImpl;

/**
 * Records caches implementations using event bus to update and invalidate records.
 *
 * Each collection has it's own event bus
 *
 */
public class EventsBusRecordsCachesImpl extends RecordsCachesMemoryImpl {

	Logger LOGGER = LoggerFactory.getLogger(EventsBusRecordsCachesImpl.class);

	EventBusManager eventBusManager;

	public EventsBusRecordsCachesImpl(ModelLayerFactory modelLayerFactory) {
		super(modelLayerFactory);
		this.eventBusManager = modelLayerFactory.getDataLayerFactory().getEventBusManager();
	}

	@Override
	protected RecordsCache newRecordsCache(String collection, ModelLayerFactory modelLayerFactory) {

		String recordsCacheEventBusName = "recordsCache-" + collection;

		if (eventBusManager.hasEventBus(recordsCacheEventBusName)) {
			LOGGER.warn("Event bus with name already exist, it is recreated");
			eventBusManager.removeEventBus(recordsCacheEventBusName);
		}
		EventBus eventBus = eventBusManager.createEventBus(recordsCacheEventBusName, ONLY_SENT_REMOTELY);

		RecordsCache recordsCache = super.newRecordsCache(collection, modelLayerFactory);
		return new EventBusRecordsCacheImpl(eventBus, recordsCache);
	}
}
