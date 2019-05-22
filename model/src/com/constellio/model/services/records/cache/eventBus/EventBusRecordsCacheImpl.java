package com.constellio.model.services.records.cache.eventBus;

import com.constellio.data.dao.services.cache.InsertionReason;
import com.constellio.data.events.Event;
import com.constellio.data.events.EventBus;
import com.constellio.data.events.EventBusListener;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.records.cache.CacheConfig;
import com.constellio.model.services.records.cache.CacheInsertionStatus;
import com.constellio.model.services.records.cache.DefaultRecordsCacheAdapter;
import com.constellio.model.services.records.cache.RecordsCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.model.services.records.cache.CacheInsertionStatus.ACCEPTED;
import static com.constellio.model.services.records.cache.RecordsCachesUtils.evaluateCacheInsert;
import static java.util.Arrays.asList;

/**
 * insertQueryRecords and insertQueryIds are not distributed, the request may repeated once per node
 */
public class EventBusRecordsCacheImpl extends DefaultRecordsCacheAdapter implements EventBusListener, RecordsCache {

	private static final Logger LOGGER = LoggerFactory.getLogger(EventBusRecordsCacheImpl.class);

	public static final String INSERT_RECORDS_EVENT_TYPE = "insertRecords";
	public static final String INVALIDATE_SCHEMA_TYPE_EVENT_TYPE = "invalidateSchemaTypeRecords";
	public static final String INVALIDATE_RECORDS_EVENT_TYPE = "invalidateRecords";
	public static final String INVALIDATE_RECORDS_WITH_OLDER_VERSION_EVENT_TYPE = "invalidateRecordsWithOlderVersion";

	EventBus eventBus;

	public EventBusRecordsCacheImpl(EventBus recordsEventBus, RecordsCache nestedRecordsCache) {
		super(nestedRecordsCache);
		this.eventBus = recordsEventBus;
		this.eventBus.register(this);
	}

	@Override
	public CacheInsertionStatus insert(Record insertedRecord, InsertionReason insertionReason) {
		CacheInsertionStatus status = nestedRecordsCache.insert(insertedRecord, insertionReason);
		handleRemotely(asList(insertedRecord), insertionReason);
		return status;
	}

	@Override
	public List<CacheInsertionStatus> insert(List<Record> records, InsertionReason insertionReason) {
		List<CacheInsertionStatus> statuses = nestedRecordsCache.insert(records, insertionReason);
		handleRemotely(records, insertionReason);
		return statuses;
	}

	private void handleRemotely(List<Record> records, InsertionReason insertionReason) {

		if (insertionReason == InsertionReason.WAS_MODIFIED) {
			List<Record> insertedRecords = new ArrayList<>();
			List<String> invalidatedRecords = new ArrayList<>();
			Map<String, Long> invalidatedRecordsWithOlderVersion = new HashMap<>();
			for (Record insertedRecord : records) {
				if (insertedRecord != null) {
					CacheConfig cacheConfig = getCacheConfigOf(insertedRecord.getTypeCode());

					if (cacheConfig != null) {
						CacheInsertionStatus status = evaluateCacheInsert(insertedRecord, cacheConfig);
						if (status == CacheInsertionStatus.REFUSED_NOT_FULLY_LOADED) {
							invalidatedRecords.add(insertedRecord.getId());
						}

						if (status == ACCEPTED) {
							if (cacheConfig.isPermanent()) {
								insertedRecords.add(insertedRecord);
							} else {
								invalidatedRecordsWithOlderVersion.put(insertedRecord.getId(), insertedRecord.getVersion());
							}
						}
					}
				}

			}

			if (!invalidatedRecords.isEmpty()) {
				eventBus.send(INVALIDATE_RECORDS_EVENT_TYPE, invalidatedRecords);
			}
			if (!insertedRecords.isEmpty()) {

				Map<String, Object> params = new HashMap<>();
				params.put("records", insertedRecords);
				params.put("reason", insertionReason);
				eventBus.send(INSERT_RECORDS_EVENT_TYPE, params);
			}

			if (!invalidatedRecordsWithOlderVersion.isEmpty()) {
				eventBus.send(INVALIDATE_RECORDS_WITH_OLDER_VERSION_EVENT_TYPE, invalidatedRecordsWithOlderVersion);
			}
		}
	}

	@Override
	public void invalidateRecordsOfType(String recordType) {
		nestedRecordsCache.invalidateRecordsOfType(recordType);
		eventBus.send(INVALIDATE_SCHEMA_TYPE_EVENT_TYPE, recordType);
	}

	@Override
	public void invalidate(List<String> recordIds) {
		nestedRecordsCache.invalidate(recordIds);
		eventBus.send(INVALIDATE_RECORDS_EVENT_TYPE, recordIds);
	}

	@Override
	public void invalidate(String recordId) {
		nestedRecordsCache.invalidate(recordId);
		this.invalidate(asList(recordId));
	}

	@Override
	public void invalidateAll() {
		nestedRecordsCache.invalidateAll();
	}

	@Override
	public void onEventReceived(Event event) {
		switch (event.getType()) {
			case INSERT_RECORDS_EVENT_TYPE:
				InsertionReason insertionReason = event.getData("reason");
				for (Record record : event.<List<Record>>getData("records")) {
					nestedRecordsCache.insert(record, insertionReason);
				}
				break;

			case INVALIDATE_SCHEMA_TYPE_EVENT_TYPE:
				nestedRecordsCache.invalidateRecordsOfType(event.<String>getData());
				break;

			case INVALIDATE_RECORDS_EVENT_TYPE:
				nestedRecordsCache.invalidate(event.<List<String>>getData());
				break;

			case INVALIDATE_RECORDS_WITH_OLDER_VERSION_EVENT_TYPE:
				invalidateRecordsWithOlderVersions(event.<Map<String, Long>>getData());
				break;

			default:
				throw new ImpossibleRuntimeException("Unsupported event type '" + event.getType()
													 + "' on record's cache event bus '" + eventBus.getName());

		}
	}

	private void invalidateRecordsWithOlderVersions(Map<String, Long> recordsToPossiballyInvalidate) {
		for (Map.Entry<String, Long> entry : recordsToPossiballyInvalidate.entrySet()) {
			Record record = get(entry.getKey());
			if (record != null && record.getVersion() < entry.getValue()) {
				nestedRecordsCache.invalidate(record.getId());
			}
		}
	}

}
