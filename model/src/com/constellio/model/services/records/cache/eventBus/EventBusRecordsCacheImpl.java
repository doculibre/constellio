package com.constellio.model.services.records.cache.eventBus;

import static com.constellio.model.services.records.cache.CacheInsertionStatus.ACCEPTED;
import static com.constellio.model.services.records.cache.RecordsCachesUtils.evaluateCacheInsert;
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.data.events.Event;
import com.constellio.data.events.EventBus;
import com.constellio.data.events.EventBusListener;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.records.cache.CacheConfig;
import com.constellio.model.services.records.cache.CacheInsertionStatus;
import com.constellio.model.services.records.cache.DefaultRecordsCacheAdapter;
import com.constellio.model.services.records.cache.RecordsCache;

/**
 * insertQueryRecords and insertQueryIds are not distributed, the request may repeated once per node
 */
public class EventBusRecordsCacheImpl extends DefaultRecordsCacheAdapter implements EventBusListener, RecordsCache {

	private static final Logger LOGGER = LoggerFactory.getLogger(EventBusRecordsCacheImpl.class);

	public static final String INSERT_RECORDS_EVENT_TYPE = "insertRecords";
	public static final String INVALIDATE_SCHEMA_TYPE_EVENT_TYPE = "invalidateSchemaTypeRecords";
	public static final String INVALIDATE_RECORDS_EVENT_TYPE = "invalidateRecords";
	public static final String INVALIDATE_ALL_EVENT_TYPE = "invalidateAll";

	EventBus eventBus;

	public EventBusRecordsCacheImpl(EventBus recordsEventBus, RecordsCache nestedRecordsCache) {
		super(nestedRecordsCache);
		this.eventBus = recordsEventBus;
		this.eventBus.register(this);
	}

	@Override
	public CacheInsertionStatus insert(Record insertedRecord) {
		return insertAllWithResponses(asList(insertedRecord)).get(0);
	}

	@Override
	public void insert(List<Record> records) {
		insertAllWithResponses(records);
	}

	private List<CacheInsertionStatus> insertAllWithResponses(List<Record> records) {

		List<Record> insertedRecords = new ArrayList<>();
		List<String> invalidatedRecords = new ArrayList<>();
		List<CacheInsertionStatus> statuses = new ArrayList<>();
		for (Record insertedRecord : records) {

			CacheInsertionStatus status = null;
			if (insertedRecord != null) {

				CacheConfig cacheConfig = getCacheConfigOf(insertedRecord.getTypeCode());
				status = evaluateCacheInsert(insertedRecord, cacheConfig);
				if (cacheConfig != null) {
					synchronized (cacheConfig) {

						if (status == CacheInsertionStatus.REFUSED_NOT_FULLY_LOADED) {
							invalidatedRecords.add(insertedRecord.getId());
						}

						if (status == ACCEPTED) {
							insertedRecords.add(insertedRecord);
						}
					}
				}
			}

			statuses.add(status);
		}
		if (!invalidatedRecords.isEmpty()) {
			eventBus.send(INVALIDATE_RECORDS_EVENT_TYPE, invalidatedRecords);
		}
		if (!insertedRecords.isEmpty()) {
			eventBus.send(INSERT_RECORDS_EVENT_TYPE, insertedRecords);
		}

		return statuses;
	}

	@Override
	public CacheInsertionStatus forceInsert(Record insertedRecord) {
		eventBus.send(INSERT_RECORDS_EVENT_TYPE, asList(insertedRecord));
		return CacheInsertionStatus.ACCEPTED;
	}

	@Override
	public void invalidateRecordsOfType(String recordType) {
		eventBus.send(INVALIDATE_SCHEMA_TYPE_EVENT_TYPE, recordType);
	}

	@Override
	public void invalidate(List<String> recordIds) {
		eventBus.send(INVALIDATE_RECORDS_EVENT_TYPE, recordIds);
	}

	@Override
	public void invalidate(String recordId) {
		this.invalidate(asList(recordId));
	}

	@Override
	public void invalidateAll() {
		eventBus.send(INVALIDATE_ALL_EVENT_TYPE);
	}

	@Override
	public void onEventReceived(Event event) {
		switch (event.getType()) {
		case INSERT_RECORDS_EVENT_TYPE:
			for (Record record : event.<List<Record>>getData()) {
				nestedRecordsCache.forceInsert(record);
			}
			break;

		case INVALIDATE_SCHEMA_TYPE_EVENT_TYPE:
			nestedRecordsCache.invalidateRecordsOfType(event.<String>getData());
			break;

		case INVALIDATE_RECORDS_EVENT_TYPE:
			nestedRecordsCache.invalidate(event.<List<String>>getData());
			break;

		case INVALIDATE_ALL_EVENT_TYPE:
			nestedRecordsCache.invalidateAll();
			break;

		default:
			throw new ImpossibleRuntimeException("Unsupported event type '" + event.getType()
					+ "' on record's cache event bus '" + eventBus.getName());

		}
	}

}
