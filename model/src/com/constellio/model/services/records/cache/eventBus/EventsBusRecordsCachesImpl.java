package com.constellio.model.services.records.cache.eventBus;

import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.services.cache.InsertionReason;
import com.constellio.data.events.Event;
import com.constellio.data.events.EventBus;
import com.constellio.data.events.EventBusListener;
import com.constellio.data.events.EventBusManager;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.RecordCacheType;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.cache.CacheInsertionResponse;
import com.constellio.model.services.records.cache.CacheInsertionStatus;
import com.constellio.model.services.records.cache.dataStore.FileSystemRecordsValuesCacheDataStore;
import com.constellio.model.services.records.cache.RecordsCaches2Impl;
import com.constellio.model.services.records.cache.dataStore.RecordsCachesDataStore;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.data.events.EventBusEventsExecutionStrategy.ONLY_SENT_REMOTELY;
import static com.constellio.model.services.records.cache.MassiveCacheInvalidationReason.KEEP_INTEGRITY;
import static com.constellio.model.services.records.cache.RecordsCachesUtils.evaluateCacheInsert;
import static com.constellio.model.services.records.cache.hooks.DeterminedHookCacheInsertion.DEFAULT_INSERT;
import static java.util.Arrays.asList;

/**
 * Records caches implementations using event bus to update and removeFromAllCaches records.
 * <p>
 * Each collection has it's own event bus
 */
public class EventsBusRecordsCachesImpl extends RecordsCaches2Impl implements EventBusListener {

	private static final boolean ONLY_LOCALLY = true;

	Logger LOGGER = LoggerFactory.getLogger(EventsBusRecordsCachesImpl.class);

	public static final String RECORDS_CACHE_NAME = "recordsCache";

	public static final String INSERT_RECORDS = "insert";
	public static final String RELOAD_SCHEMA_TYPES = "reload";
	public static final String REMOVE_RECORDS = "remove";
	public static final String INVALIDATE_VOLATILE_RECORDS = "volatile";
	public static final String CLEAR_VOLATILE_CACHE = "clearVolatile";
	public static final String REMOVE_COLLECTION_RECORDS = "removeCollectionRecords";

	public EventsBusRecordsCachesImpl(ModelLayerFactory modelLayerFactory,
									  FileSystemRecordsValuesCacheDataStore fileSystemDataStore,
									  RecordsCachesDataStore memoryDataStore) {
		super(modelLayerFactory, fileSystemDataStore, memoryDataStore);
		this.eventBusManager = modelLayerFactory.getDataLayerFactory().getEventBusManager();
		this.metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
		this.collectionsListManager = modelLayerFactory.getCollectionsListManager();
		this.eventBus = eventBusManager.createEventBus(RECORDS_CACHE_NAME, ONLY_SENT_REMOTELY);
		this.eventBus.register(this);
	}


	private EventBus eventBus;
	private EventBusManager eventBusManager;
	private MetadataSchemasManager metadataSchemasManager;
	private CollectionsListManager collectionsListManager;


	@Override
	public void initialize() {
		super.initialize();
	}

	@Override
	public void close() {
		super.close();
	}

	@Override
	public CacheInsertionResponse insert(Record insertedRecord, InsertionReason insertionReason) {
		CacheInsertionResponse response = super.insert(insertedRecord, insertionReason);

		if (response.getStatus() == CacheInsertionStatus.ACCEPTED) {
			MetadataSchemaType schemaType = metadataSchemasManager.getSchemaTypeOf(insertedRecord);
			if (schemaType.getCacheType().isSummaryCache()) {
				Record record = response.getDeterminedHookCacheInsertion() != DEFAULT_INSERT || response.getSummaryRecordDTO() == null
								? insertedRecord : toRecord(response.getSummaryRecordDTO());
				handleRemotely(insertedRecord.getCollectionInfo().getCollectionId(), asList(record), insertionReason);
			} else if (schemaType.getCacheType().hasPermanentCache()) {
				handleRemotely(insertedRecord.getCollectionInfo().getCollectionId(), asList(insertedRecord), insertionReason);

			}
		}

		return response;
	}

	@Override
	protected void reload(byte collectionId, String collection, List<String> schemaTypes,
						  boolean isOnlyLocally, boolean forceVolatileCacheClear) {

		super.reload(collectionId, collection, schemaTypes, ONLY_LOCALLY, forceVolatileCacheClear);
		if (!isOnlyLocally) {
			Map<String, Object> params = new HashMap<>();
			params.put("collectionId", collectionId);
			params.put("collection", collection);
			params.put("forceVolatileCacheClear", forceVolatileCacheClear);
			params.put("schemaTypes", schemaTypes);
			eventBus.send(RELOAD_SCHEMA_TYPES, params);
		}
	}

	@Override
	public void removeRecordsOfCollection(String collection, boolean isOnlyLocally) {
		super.removeRecordsOfCollection(collection, ONLY_LOCALLY);
		if (!isOnlyLocally) {
			eventBus.send(REMOVE_COLLECTION_RECORDS, collection);
		}
	}

	private void handleRemotely(byte collectionId, List<Record> records, InsertionReason insertionReason) {

		if (insertionReason == InsertionReason.WAS_MODIFIED) {
			List<Record> insertedRecords = new ArrayList<>();
			//			List<String> invalidatedRecords = new ArrayList<>();
			Map<String, Long> invalidatedRecordsWithOlderVersion = new HashMap<>();
			for (Record insertedRecord : records) {
				if (insertedRecord != null) {
					RecordCacheType recordCacheType = metadataSchemasManager.getSchemaTypeOf(insertedRecord).getCacheType();

					if (recordCacheType.hasPermanentCache()) {
						CacheInsertionStatus status = evaluateCacheInsert(insertedRecord);
						insertedRecords.add(insertedRecord);
					} else if (recordCacheType.hasVolatileCache()) {
						invalidatedRecordsWithOlderVersion.put(insertedRecord.getId(), insertedRecord.getVersion());
					}
				}

			}

			//			if (!invalidatedRecords.isEmpty()) {
			//				Map<String, Object> params = new HashMap<>();
			//				params.put("collectionId", collectionId);
			//				params.put("ids", invalidatedRecords);
			//				eventBus.send(REMOVE_RECORDS, params);
			//			}
			if (!insertedRecords.isEmpty()) {

				Map<String, Object> params = new HashMap<>();
				params.put("collectionId", collectionId);
				params.put("records", insertedRecords);
				params.put("reason", insertionReason);
				eventBus.send(INSERT_RECORDS, params);
			}

			if (!invalidatedRecordsWithOlderVersion.isEmpty()) {
				invalidateVolatileRecordsWithOlderVersionsLocallyAndRemotely(invalidatedRecordsWithOlderVersion);
			}
		}
	}


	private void invalidateVolatileRecordsWithOlderVersionsLocally(Map<String, Long> recordsToPossiballyInvalidate) {
		for (Map.Entry<String, Long> entry : recordsToPossiballyInvalidate.entrySet()) {
			RecordDTO recordDTO = volatileCache.get(entry.getKey());
			if (recordDTO != null && recordDTO.getVersion() < entry.getValue()) {
				volatileCache.remove(entry.getKey());
			}
		}
	}

	private void invalidateVolatileRecordsWithOlderVersionsLocallyAndRemotely(Map<String, Long> idVersionsMap) {
		invalidateVolatileRecordsWithOlderVersionsLocally(idVersionsMap);
		Map<String, Object> params = new HashMap<>();
		params.put("idVersionsMap", idVersionsMap);
		eventBus.send(INVALIDATE_VOLATILE_RECORDS, params);
	}

	@Override
	protected void removeFromAllCaches(byte collectionId, List<String> recordIds) {
		super.removeFromAllCaches(collectionId, recordIds);

		Map<String, Object> params = new HashMap<>();
		params.put("ids", recordIds);
		params.put("collectionId", collectionId);
		eventBus.send(REMOVE_RECORDS, params);
	}

	@Override
	public void onEventReceived(Event event) {
		switch (event.getType()) {
			case INSERT_RECORDS:
				InsertionReason insertionReason = event.getData("reason");
				for (Record record : event.<List<Record>>getData("records")) {
					super.insert(record, insertionReason);
				}
				break;

			case RELOAD_SCHEMA_TYPES:
				byte collectionId = event.getData("collectionId");
				String collection = collectionsListManager.getCollectionCode(collectionId);
				List<String> schemaTypes = event.getData("schemaTypes");
				boolean forceVolatileCacheClear = event.getData("forceVolatileCacheClear");
				reload(collectionId, collection, schemaTypes, ONLY_LOCALLY, forceVolatileCacheClear);
				break;

			case REMOVE_RECORDS:
				collectionId = event.getData("collectionId");
				List<String> ids = event.<List<String>>getData("ids");
				super.removeFromAllCaches(collectionId, ids);
				break;

			case INVALIDATE_VOLATILE_RECORDS:
				Map<String, Long> idVersionsMap = event.getData("idVersionsMap");
				invalidateVolatileRecordsWithOlderVersionsLocally(idVersionsMap);
				break;


			case CLEAR_VOLATILE_CACHE:
				invalidateVolatile(KEEP_INTEGRITY);
				break;

			case REMOVE_COLLECTION_RECORDS:
				removeRecordsOfCollection(event.getData(), ONLY_LOCALLY);
				break;

			default:
				throw new ImpossibleRuntimeException("Unsupported event type '" + event.getType()
													 + "' on record's cache event bus '" + eventBus.getName());

		}
	}


}
