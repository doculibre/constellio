package com.constellio.model.services.records.cache.eventBus;

import static com.constellio.model.services.records.cache.CacheInsertionStatus.ACCEPTED;
import static com.constellio.model.services.records.cache.RecordsCachesUtils.evaluateCacheInsert;
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.data.events.Event;
import com.constellio.data.events.EventBus;
import com.constellio.data.events.EventBusListener;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.records.cache.CacheConfig;
import com.constellio.model.services.records.cache.CacheInsertionStatus;
import com.constellio.model.services.records.cache.DefaultEventBusListenerAdapter;
import com.constellio.model.services.records.cache.RecordsCache;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

public class EventBusRecordsCacheImpl extends DefaultEventBusListenerAdapter implements EventBusListener, RecordsCache {

	private static final Logger LOGGER = LoggerFactory.getLogger(EventBusRecordsCacheImpl.class);

	public static final String INSERT_RECORDS_EVENT_TYPE = "insertRecords";
	public static final String INVALIDATE_SCHEMA_TYPE_EVENT_TYPE = "invalidateSchemaTypeRecords";
	public static final String INVALIDATE_RECORDS_EVENT_TYPE = "invalidateRecords";
	public static final String INVALIDATE_ALL_EVENT_TYPE = "invalidateAll";

	public static final String INSERT_QUERY_RESULTS = "insertQueryResults";
	public static final String INSERT_QUERY_RESULTS_IDS = "insertQueryResultsIds";

	EventBus recordsCacheEventBus;

	public EventBusRecordsCacheImpl(EventBus recordsEventBus, RecordsCache nestedRecordsCache) {
		super(nestedRecordsCache);
		this.recordsCacheEventBus = recordsEventBus;
		this.recordsCacheEventBus.register(this);
	}

	@Override
	public CacheInsertionStatus insert(Record insertedRecord) {
		if (insertedRecord == null) {
			return CacheInsertionStatus.REFUSED_NULL;
		}

		CacheConfig cacheConfig = getCacheConfigOf(insertedRecord.getTypeCode());
		CacheInsertionStatus status = evaluateCacheInsert(insertedRecord, cacheConfig);
		if (cacheConfig != null) {
			synchronized (cacheConfig) {

				if (status == CacheInsertionStatus.REFUSED_NOT_FULLY_LOADED) {
					recordsCacheEventBus
							.sendAndAwaitLocalExecution(INVALIDATE_RECORDS_EVENT_TYPE, asList(insertedRecord.getId()));
				}

				if (status == ACCEPTED) {
					recordsCacheEventBus.sendAndAwaitLocalExecution(INSERT_RECORDS_EVENT_TYPE, asList(insertedRecord));
					return ACCEPTED;

				} else {
					return status;
				}
			}
		} else {
			return status;
		}
	}

	//	@Override
	//	public void insert(List<Record> record) {
	//		super.insert(record);
	//	}

	@Override
	public void insert(List<Record> records) {

		//		for (Record record : records) {
		//			insert(record);
		//		}

		List<Record> insertedRecords = new ArrayList<>();
		List<String> invalidatedRecords = new ArrayList<>();

		for (Record insertedRecord : records) {
			if (insertedRecord != null) {

				CacheConfig cacheConfig = getCacheConfigOf(insertedRecord.getTypeCode());
				CacheInsertionStatus status = evaluateCacheInsert(insertedRecord, cacheConfig);
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
		}
		if (!invalidatedRecords.isEmpty()) {
			recordsCacheEventBus.sendAndAwaitLocalExecution(INVALIDATE_RECORDS_EVENT_TYPE, invalidatedRecords);
		}
		if (!insertedRecords.isEmpty()) {
			recordsCacheEventBus.sendAndAwaitLocalExecution(INSERT_RECORDS_EVENT_TYPE, insertedRecords);
		}

	}

	@Override
	public CacheInsertionStatus forceInsert(Record insertedRecord) {
		recordsCacheEventBus.sendAndAwaitLocalExecution(INSERT_RECORDS_EVENT_TYPE, asList(insertedRecord));
		return CacheInsertionStatus.ACCEPTED;
	}

	@Override
	public void insertQueryResults(LogicalSearchQuery query, List<Record> records) {
		Map<String, Object> data = new HashMap<>();
		data.put("query", query);
		data.put("records", records);
		recordsCacheEventBus.sendAndAwaitLocalExecution(INSERT_QUERY_RESULTS, data);
	}

	@Override
	public void insertQueryResultIds(LogicalSearchQuery query, List<String> recordIds) {
		Map<String, Object> data = new HashMap<>();
		data.put("query", query);
		data.put("recordIds", recordIds);
		recordsCacheEventBus.sendAndAwaitLocalExecution(INSERT_QUERY_RESULTS_IDS, data);
	}

	@Override
	public void invalidateRecordsOfType(String recordType) {
		recordsCacheEventBus.sendAndAwaitLocalExecution(INVALIDATE_SCHEMA_TYPE_EVENT_TYPE, recordType);
	}

	@Override
	public void invalidate(List<String> recordIds) {
		recordsCacheEventBus.sendAndAwaitLocalExecution(INVALIDATE_RECORDS_EVENT_TYPE, recordIds);
	}

	@Override
	public void invalidate(String recordId) {
		this.invalidate(asList(recordId));
	}

	@Override
	public void invalidateAll() {
		recordsCacheEventBus.sendAndAwaitLocalExecution(INVALIDATE_ALL_EVENT_TYPE);
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

		case INSERT_QUERY_RESULTS:
			nestedRecordsCache.insertQueryResults(
					event.<LogicalSearchQuery>getData("query"), event.<List<Record>>getData("records"));
			break;

		case INSERT_QUERY_RESULTS_IDS:
			nestedRecordsCache.insertQueryResultIds(
					event.<LogicalSearchQuery>getData("query"), event.<List<String>>getData("recordIds"));
			break;

		default:
			throw new ImpossibleRuntimeException("Unsupported event type '" + event.getType()
					+ "' on record's cache event bus '" + recordsCacheEventBus.getName());

		}
	}

}
