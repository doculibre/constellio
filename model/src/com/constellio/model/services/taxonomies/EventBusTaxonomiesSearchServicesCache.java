package com.constellio.model.services.taxonomies;

import com.constellio.data.events.Event;
import com.constellio.data.events.EventBus;
import com.constellio.data.events.EventBusListener;
import com.constellio.data.events.EventBusManager;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.entities.records.Record;

import static com.constellio.data.events.EventBusEventsExecutionStrategy.EXECUTED_LOCALLY_THEN_SENT_REMOTELY;

public class EventBusTaxonomiesSearchServicesCache implements TaxonomiesSearchServicesCache, EventBusListener {

	private static final String INVALIDATE_ALL_EVENT_TYPE = "all";
	private static final String INVALIDATE_WITH_CHILDREN_EVENT_TYPE = "w";
	private static final String INVALIDATE_WITHOUT_CHILDREN_EVENT_TYPE = "wo";
	private static final String INVALIDATE_RECORD_EVENT_TYPE = "r";
	private static final String INVALIDATE_USER_EVENT_TYPE = "u";

	TaxonomiesSearchServicesCache nestedCache;

	EventBus eventBus;

	public EventBusTaxonomiesSearchServicesCache(TaxonomiesSearchServicesCache nestedCache,
												 EventBusManager eventBusManager) {
		this.nestedCache = nestedCache;
		this.eventBus = eventBusManager.createEventBus("taxonomiesHasChildren", EXECUTED_LOCALLY_THEN_SENT_REMOTELY);
		this.eventBus.register(this);
	}

	@Override
	public void initialize(String collection) {

	}

	@Override
	public void insert(String username, String recordId, String mode, Boolean value) {
		//inserts are not sent remotelly, since a user will usually use the same instance
		nestedCache.insert(username, recordId, mode, value);
	}

	@Override
	public Boolean getCachedValue(String username, Record record, String mode) {
		return nestedCache.getCachedValue(username, record, mode);
	}

	@Override
	public void invalidateAll() {
		eventBus.send(INVALIDATE_ALL_EVENT_TYPE);
	}

	@Override
	public void invalidateWithChildren(String recordId) {
		eventBus.send(INVALIDATE_WITH_CHILDREN_EVENT_TYPE, recordId);
	}

	@Override
	public void invalidateWithoutChildren(String recordId) {
		eventBus.send(INVALIDATE_WITHOUT_CHILDREN_EVENT_TYPE, recordId);
	}

	@Override
	public void invalidateRecord(String recordId) {
		eventBus.send(INVALIDATE_RECORD_EVENT_TYPE, recordId);
	}

	@Override
	public void invalidateUser(String username) {
		eventBus.send(INVALIDATE_USER_EVENT_TYPE, username);
	}

	@Override
	public void onEventReceived(Event event) {
		switch (event.getType()) {
			case INVALIDATE_ALL_EVENT_TYPE:
				nestedCache.invalidateAll();
				break;

			case INVALIDATE_RECORD_EVENT_TYPE:
				nestedCache.invalidateRecord(event.<String>getData());
				break;

			case INVALIDATE_WITH_CHILDREN_EVENT_TYPE:
				nestedCache.invalidateWithChildren(event.<String>getData());
				break;

			case INVALIDATE_WITHOUT_CHILDREN_EVENT_TYPE:
				nestedCache.invalidateWithoutChildren(event.<String>getData());
				break;

			case INVALIDATE_USER_EVENT_TYPE:
				nestedCache.invalidateUser(event.<String>getData());
				break;

			default:
				throw new ImpossibleRuntimeException("Unsupported event type " + event.getType());

		}
	}


}
