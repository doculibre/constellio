package com.constellio.model.services.records.cache;

import com.constellio.data.dao.services.cache.InsertionReason;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Authorization;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordUtils;
import com.constellio.model.services.records.cache.RecordsCacheImplRuntimeException.RecordsCacheImplRuntimeException_InvalidSchemaTypeCode;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.DataStoreFilters;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.search.query.logical.condition.SchemaFilters;
import com.constellio.model.services.search.query.logical.condition.SchemaTypesFilters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.constellio.data.dao.services.cache.InsertionReason.WAS_MODIFIED;
import static com.constellio.model.services.records.cache.CacheInsertionStatus.ACCEPTED;
import static com.constellio.model.services.records.cache.CacheInsertionStatus.REFUSED_OLD_VERSION;
import static com.constellio.model.services.records.cache.RecordsCachesUtils.evaluateCacheInsert;
import static com.constellio.model.services.records.cache.RecordsCachesUtils.hasNoUnsupportedFeatureOrFilter;
import static com.constellio.model.services.records.cache2.DeterminedHookCacheInsertion.DEFAULT_INSERT;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;

public class RecordsCacheImpl implements RecordsCache {

	private static final Logger LOGGER = LoggerFactory.getLogger(RecordsCacheImpl.class);

	String collection;
	SchemaUtils schemaUtils = new SchemaUtils();

	Map<String, RecordHolder> cacheById = new HashMap<>();

	Map<String, RecordByMetadataCache> recordByMetadataCache = new HashMap<>();
	Map<String, VolatileCache> volatileCaches = new HashMap<>();
	Map<String, PermanentCache> permanentCaches = new HashMap<>();

	Map<String, CacheConfig> cachedTypes = new HashMap<>();

	ModelLayerFactory modelLayerFactory;
	SearchServices searchServices;

	Set<String> doNotLog = new HashSet<>();

	AtomicBoolean enabled = new AtomicBoolean();

	Set<String> fullyLoadedSchemaTypes = new HashSet<>();

	public RecordsCacheImpl(String collection, ModelLayerFactory modelLayerFactory, AtomicBoolean enabled) {
		this.collection = collection;
		this.modelLayerFactory = modelLayerFactory;
		this.searchServices = modelLayerFactory.newSearchServices();
		this.enabled = enabled;
	}

	public RecordsCacheImpl(String collection, ModelLayerFactory modelLayerFactory) {
		this.collection = collection;
		this.modelLayerFactory = modelLayerFactory;
		this.searchServices = modelLayerFactory.newSearchServices();
		this.enabled = new AtomicBoolean(true);
	}

	public boolean isCached(String id) {
		RecordHolder holder = cacheById.get(id);
		return holder != null && holder.getCopy() != null;
	}

	@Override
	public Record getSummary(String id) {
		return getByIdNoMatterIfSummary(id);
	}

	@Override
	public Record get(String id) {
		return onlyIfNotSummary(getByIdNoMatterIfSummary(id));
	}

	private Record getByIdNoMatterIfSummary(String id) {

		if (!enabled.get()) {
			return null;
		}

		RecordHolder holder = cacheById.get(id);

		Record copy = null;
		if (holder != null) {
			copy = holder.getCopy();

			if (copy != null) {
				CacheConfig config = getCacheConfigOf(copy.getSchemaCode());
				if (config.isVolatile()) {
					VolatileCache cache = volatileCaches.get(config.getSchemaType());
					synchronized (config) {
						cache.hit(holder);
					}
				}

			}

		}

		return copy;
	}

	@Override
	public boolean isEmpty() {
		for (VolatileCache cache : volatileCaches.values()) {
			if (cache.recordsInCache > 0) {
				return false;
			}
		}

		for (PermanentCache cache : permanentCaches.values()) {
			if (!cache.holders.isEmpty()) {
				return false;
			}
		}

		return true;
	}

	public List<CacheInsertionResponse> insert(List<Record> records, InsertionReason insertionReason) {
		List<CacheInsertionResponse> responses = new ArrayList<>();
		if (records != null) {
			for (Record record : records) {
				responses.add(insert(record, insertionReason));
			}
		}

		return responses;
	}


	@Override
	public List<Record> getAllValues(String schemaType) {
		CacheConfig cacheConfig = getCacheConfigOf(schemaType);

		if (cacheConfig.isPermanent()) {
			List<Record> records = new ArrayList<>();
			synchronized (cacheConfig) {
				for (RecordHolder holder : permanentCaches.get(schemaType).holders) {
					if (holder != null) {
						Record record = holder.record;
						if (record != null) {
							records.add(record.getCopyOfOriginalRecord());
						}
					}
				}
			}

			return records;
		} else {
			return null;
		}
	}

	@Override
	public List<Record> getAllValuesInUnmodifiableState(String schemaType) {
		CacheConfig cacheConfig = getCacheConfigOf(schemaType);

		if (cacheConfig.isPermanent()) {
			List<Record> records = new ArrayList<>();
			synchronized (cacheConfig) {
				for (RecordHolder holder : permanentCaches.get(schemaType).holders) {
					if (holder != null) {
						Record record = holder.record;
						if (record != null) {
							records.add(record);
						}

					}
				}

			}

			return records;
		} else {
			return null;
		}
	}

	PermanentCache getCacheFor(LogicalSearchQuery query, boolean onlyIds) {
		LogicalSearchCondition condition = query.getCondition();
		DataStoreFilters filters = condition.getFilters();

		MetadataSchemaType schemaType = null;

		if (filters instanceof SchemaFilters) {
			SchemaFilters schemaFilters = (SchemaFilters) filters;
			schemaType = schemaFilters.getSchemaTypeFilter();
		} else if (filters instanceof SchemaTypesFilters) {
			SchemaTypesFilters schemaTypesFilters = (SchemaTypesFilters) filters;
			if (((SchemaTypesFilters) filters).getSchemaTypes() != null) {
				schemaType = schemaTypesFilters.getSchemaTypes().size() == 1 ? schemaTypesFilters.getSchemaTypes().get(0) : null;
			} else if (((SchemaTypesFilters) filters).getSchemaTypesCodes() != null) {
				String schemaTypeCode = schemaTypesFilters.getSchemaTypesCodes().size() == 1 ?
										schemaTypesFilters.getSchemaTypesCodes().get(0) :
										null;
				if (schemaTypeCode != null) {
					schemaType = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection)
							.getSchemaType(schemaTypeCode);
				}
			}
		}

		if (schemaType != null && hasNoUnsupportedFeatureOrFilter(query, onlyIds)) {
			CacheConfig cacheConfig = getCacheConfigOf(schemaType.getCode());
			if (cacheConfig != null && cacheConfig.isPermanent()) {
				return permanentCaches.get(cacheConfig.getSchemaType());
			}
		}

		return null;
	}


	private CacheInsertionStatus executeInsert(Record insertedRecord, InsertionReason insertionReason) {

		if (Toggle.LOG_REQUEST_CACHE.isEnabled()) {
			if (!insertedRecord.getSchemaCode().startsWith("event")
				&& !doNotLog.contains(insertedRecord.getId() + "_" + insertedRecord.getVersion())) {
				new Exception("inserting in central cache " + insertedRecord.getIdTitle() + " in version "
							  + insertedRecord.getVersion()).printStackTrace();
			}
		}
		CacheConfig cacheConfig = getCacheConfigOf(insertedRecord.getSchemaCode());
		Record recordCopy = RecordsCachesUtils.prepareRecordForCacheInsert(insertedRecord, cacheConfig);

		if (cacheConfig != null) {
			Record previousRecord = null;

			synchronized (cacheConfig) {
				modelLayerFactory.getExtensions().getSystemWideExtensions().onPutInCache(recordCopy, 0);
				RecordHolder holder = cacheById.get(recordCopy.getId());
				if (holder != null) {
					previousRecord = holder.record;

					if (previousRecord == null || previousRecord.getVersion() < recordCopy.getVersion()) {
						if (cacheConfig.isVolatile()) {
							insertRecordIntoAnAlreadyExistingVolatileCacheHolder(recordCopy, cacheConfig, holder);
						}
						holder.set(recordCopy);
						if (cacheConfig.isPermanent() && insertionReason == WAS_MODIFIED) {
							permanentCaches.get(cacheConfig.getSchemaType()).queryResults.clear();
						}

					} else if (previousRecord.getVersion() == recordCopy.getVersion()) {
						if (cacheConfig.isPermanent() && insertionReason == WAS_MODIFIED) {
							permanentCaches.get(cacheConfig.getSchemaType()).queryResults.clear();
						}
						return REFUSED_OLD_VERSION;

					} else {
						return REFUSED_OLD_VERSION;
					}
				} else {
					holder = insertRecordIntoAnANewHolder(recordCopy, cacheConfig);
					if (cacheConfig.isPermanent() && insertionReason == WAS_MODIFIED) {
						permanentCaches.get(cacheConfig.getSchemaType()).queryResults.clear();
					}
				}
				this.recordByMetadataCache.get(cacheConfig.getSchemaType()).insert(previousRecord, holder);
			}

		}
		return ACCEPTED;
	}

	@Override
	public CacheInsertionResponse insert(Record insertedRecord, InsertionReason insertionReason) {

		if (insertedRecord == null) {
			return new CacheInsertionResponse(CacheInsertionStatus.REFUSED_NULL, null, DEFAULT_INSERT);
		}

		if ("savedSearch".equals(insertedRecord.getTypeCode())) {
			return new CacheInsertionResponse(executeInsert(insertedRecord, insertionReason), null, DEFAULT_INSERT);
		} else {

			CacheConfig cacheConfig = getCacheConfigOf(insertedRecord.getTypeCode());
			CacheInsertionStatus status = evaluateCacheInsert(insertedRecord);
			if (cacheConfig != null) {
				synchronized (cacheConfig) {

					if (status == CacheInsertionStatus.REFUSED_NOT_FULLY_LOADED) {
						removeFromAllCaches(insertedRecord.getId());
					}

					if (status == ACCEPTED) {
						return new CacheInsertionResponse(executeInsert(insertedRecord, insertionReason), null, DEFAULT_INSERT);
					} else {
						return new CacheInsertionResponse(status, null, DEFAULT_INSERT);
					}
				}
			} else {
				return new CacheInsertionResponse(status, null, DEFAULT_INSERT);
			}
		}
	}

	private RecordHolder insertRecordIntoAnANewHolder(Record record, CacheConfig cacheConfig) {
		RecordHolder holder = new RecordHolder(record);
		cacheById.put(record.getId(), holder);
		if (cacheConfig.isVolatile()) {
			VolatileCache cache = volatileCaches.get(cacheConfig.getSchemaType());
			cache.releaseFor(1);
			cache.insert(holder);
		} else {
			PermanentCache cache = permanentCaches.get(cacheConfig.getSchemaType());
			cache.insert(holder);
		}

		return holder;
	}

	private void insertRecordIntoAnAlreadyExistingVolatileCacheHolder(Record record, CacheConfig cacheConfig,
																	  RecordHolder currentHolder) {
		if (currentHolder.record == null) {
			VolatileCache cache = volatileCaches.get(cacheConfig.getSchemaType());
			cache.releaseFor(1);
			cache.insert(currentHolder);
		}

	}

	@Override
	public void reloadSchemaType(String recordType, boolean onlyLocally, boolean forceVolatileCacheClear) {
		CacheConfig cacheConfig = cachedTypes.get(recordType);

		if (cacheConfig != null) {
			synchronized (cacheConfig) {
				if (cacheConfig.isVolatile()) {
					volatileCaches.get(cacheConfig.getSchemaType()).invalidateAll();
				} else {
					permanentCaches.get(cacheConfig.getSchemaType()).invalidateAll();
				}
			}
		}
		fullyLoadedSchemaTypes.remove(recordType);
	}

	public void removeFromAllCaches(List<String> recordIds) {
		if (recordIds != null) {
			for (String recordId : recordIds) {
				removeFromAllCaches(recordId);
			}
		}
	}

	@Override
	public void removeFromAllCaches(String recordId) {
		if (recordId != null) {
			RecordHolder holder = cacheById.get(recordId);
			if (holder != null && holder.record != null) {
				CacheConfig cacheConfig = getCacheConfigOf(holder.record.getSchemaCode());
				synchronized (cacheConfig) {
					recordByMetadataCache.get(cacheConfig.getSchemaType()).invalidate(holder.record);
					holder.invalidate();

					if (cacheConfig.isPermanent()) {
						permanentCaches.get(cacheConfig.getSchemaType()).queryResults.clear();
					}
				}
			}
		}

	}

	public CacheConfig getCacheConfigOf(String schemaOrTypeCode) {
		String schemaTypeCode = schemaUtils.getSchemaTypeCode(schemaOrTypeCode);
		return cachedTypes.get(schemaTypeCode);
	}

	@Override
	public void configureCache(CacheConfig cacheConfig) {

		if (cacheConfig == null) {
			throw new IllegalArgumentException("Required parameter 'cacheConfig'");
		}
		if (cacheConfig.getSchemaType().contains("_")) {
			throw new RecordsCacheImplRuntimeException_InvalidSchemaTypeCode(cacheConfig.getSchemaType());
		}
		if (cachedTypes.containsKey(cacheConfig.getSchemaType())) {
			LOGGER.warn("Cache is already configured, nothing is changed");
			return;
		}

		cachedTypes.put(cacheConfig.getSchemaType(), cacheConfig);
		if (cacheConfig.isPermanent()) {
			permanentCaches.put(cacheConfig.getSchemaType(), new PermanentCache());
		} else {
			volatileCaches.put(cacheConfig.getSchemaType(),
					new VolatileCache(cacheConfig.getVolatileMaxSize(), cacheConfig.getInvalidationMethod()));
		}

		recordByMetadataCache.put(cacheConfig.getSchemaType(), new RecordByMetadataCache(cacheConfig));

		if (cacheConfig.isLoadedInitially()) {

			MetadataSchemaType schemaType = modelLayerFactory.getMetadataSchemasManager()
					.getSchemaTypes(collection).getSchemaType(cacheConfig.getSchemaType());
			long resultCount = searchServices.getResultsCount(from(schemaType).returnAll());
			if (resultCount > 0 && (resultCount < 10000 || asList(User.SCHEMA_TYPE,
					Group.SCHEMA_TYPE, Authorization.SCHEMA_TYPE).contains(cacheConfig.getSchemaType()))) {

				LOGGER.info("Loading cache of type '" + cacheConfig.getSchemaType() + "' of collection '" + collection + "'");
				searchServices.getAllRecords(schemaType);
			}
		}

	}

	@Override
	public Collection<CacheConfig> getConfiguredCaches() {
		return cachedTypes.values();
	}

	@Override
	public void invalidateVolatileReloadPermanent(List<String> schemaTypes, boolean onlyLocally) {
		cacheById.clear();
		for (VolatileCache cache : volatileCaches.values()) {
			cache.invalidateAll();
		}

		for (PermanentCache cache : permanentCaches.values()) {
			cache.invalidateAll();
		}

		fullyLoadedSchemaTypes.clear();
	}

	@Override
	public Record getByMetadata(Metadata metadata, String value) {
		return onlyIfNotSummary(getByMetadataNoMatterIfSummary(metadata, value));
	}

	@Override
	public Record getSummaryByMetadata(Metadata metadata, String value) {
		return getByMetadataNoMatterIfSummary(metadata, value);
	}

	private Record getByMetadataNoMatterIfSummary(Metadata metadata, String value) {

		if (!enabled.get()) {
			return null;
		}

		String schemaTypeCode = schemaUtils.getSchemaTypeCode(metadata);
		RecordByMetadataCache recordByMetadataCache = this.recordByMetadataCache.get(schemaTypeCode);

		Record foundRecord = null;
		if (recordByMetadataCache != null) {
			foundRecord = recordByMetadataCache.getByMetadata(metadata.getLocalCode(), value);
		}

		if (foundRecord == null) {
			modelLayerFactory.getExtensions().getSystemWideExtensions().onGetByUniqueMetadataCacheMiss(metadata, value, 0);
		} else {
			modelLayerFactory.getExtensions().getSystemWideExtensions()
					.onGetByUniqueMetadataCacheHit(foundRecord, metadata, value, 0);
		}

		return foundRecord;
	}

	//	@Override
	//	public void removeCache(String schemaType) {
	//		reloadSchemaType(schemaType);
	//		recordByMetadataCache.remove(schemaType);
	//		if (volatileCaches.containsKey(schemaType)) {
	//			volatileCaches.get(schemaType).invalidateAll();
	//			volatileCaches.remove(schemaType);
	//		}
	//		if (permanentCaches.containsKey(schemaType)) {
	//			permanentCaches.get(schemaType).invalidateAll();
	//			permanentCaches.remove(schemaType);
	//		}
	//
	//		cachedTypes.remove(schemaType);
	//		fullyLoadedSchemaTypes.remove(schemaType);
	//	}

	@Override
	public boolean isConfigured(MetadataSchemaType type) {
		return isConfigured(type.getCode());
	}

	public boolean isConfigured(String typeCode) {
		return cachedTypes.containsKey(typeCode);
	}

	static class VolatileCache {

		int maxSize;

		LinkedList<RecordHolder> holders = new LinkedList<>();

		int recordsInCache;

		VolatileCacheInvalidationMethod invalidationMethod;

		VolatileCache(int maxSize, VolatileCacheInvalidationMethod invalidationMethod) {
			this.maxSize = maxSize;
			this.invalidationMethod = invalidationMethod;
		}

		void insert(RecordHolder holder) {

			holder.volatileCacheOccurences = 1;
			holders.add(holder);
			recordsInCache++;
		}

		void hit(RecordHolder holder) {

			if (invalidationMethod == VolatileCacheInvalidationMethod.LRU) {
				if (holder != null && holder.getCopy() != null && holder.getCopy().getTypeCode() != null
					&& holder.getCopy().getTypeCode().equals("savedSearch")) {
				}

				if (holder.volatileCacheOccurences <= 2) {
					holder.volatileCacheOccurences++;
					holders.add(holder);
				}
			}
		}

		void releaseFor(int qty) {
			while (recordsInCache + qty > maxSize) {
				releaseNext();
			}
		}

		void releaseNext() {
			RecordHolder recordHolder = holders.removeFirst();
			if (recordHolder.volatileCacheOccurences > 1) {
				recordHolder.volatileCacheOccurences--;
				releaseNext();
			} else {
				recordHolder.invalidate();
				recordsInCache--;
			}
		}

		void invalidateAll() {

			this.recordsInCache = 0;
			for (RecordHolder holder : holders) {
				holder.invalidate();
			}
			holders.clear();
		}

		public int getCacheObjectsCount() {
			return recordsInCache;
		}

		public long getCacheObjectsSize() {

			long size = 0;

			for (RecordHolder holder : holders) {
				size += RecordUtils.estimateRecordSize(holder.record);
			}

			return size;
		}

	}

	static class PermanentCache {

		Map<String, List<String>> queryResults = new HashMap<>();
		LinkedList<RecordHolder> holders = new LinkedList<>();

		void insert(RecordHolder holder) {
			holders.add(holder);
		}

		void invalidateAll() {
			for (RecordHolder holder : holders) {
				holder.invalidate();
			}
			queryResults.clear();
		}

		public int getCacheObjectsCount() {
			int size = holders.size();

			for (List<String> aQueryResults : queryResults.values()) {
				size += 1 + aQueryResults.size();
			}

			return size;
		}

		public long getCacheObjectsSize() {

			long size = 0;

			for (RecordHolder holder : holders) {
				size += RecordUtils.estimateRecordSize(holder.record);
			}

			return size;
		}

	}

	static class RecordByMetadataCache {

		Map<String, Map<String, RecordHolder>> map = new HashMap<>();
		Map<String, Metadata> supportedMetadatas = new HashMap<>();

		RecordByMetadataCache(CacheConfig cacheConfig) {
			for (Metadata indexedMetadata : cacheConfig.getIndexes()) {
				supportedMetadatas.put(indexedMetadata.getLocalCode(), indexedMetadata);
				map.put(indexedMetadata.getLocalCode(), new HashMap<String, RecordHolder>());
			}
		}

		Record getByMetadata(String localCode, String value) {
			Map<String, RecordHolder> metadataMap = map.get(localCode);
			RecordHolder recordHolder = null;
			if (metadataMap != null) {
				recordHolder = metadataMap.get(value);
			}
			return recordHolder == null ? null : recordHolder.getCopy();
		}

		void insert(Record previousRecord, RecordHolder recordHolder) {

			for (Metadata supportedMetadata : supportedMetadatas.values()) {
				Object value = null;
				Object previousValue = null;

				if (previousRecord != null) {
					previousValue = previousRecord.get(supportedMetadata);
				}
				if (recordHolder.record != null) {
					value = recordHolder.record.get(supportedMetadata);
				}
				if (previousValue != null && !previousValue.equals(value)) {
					map.get(supportedMetadata.getLocalCode()).remove(previousValue);
				}
				if (value != null && !value.equals(previousValue)) {
					map.get(supportedMetadata.getLocalCode()).put(value.toString(), recordHolder);
				}
			}
		}

		void invalidate(Record record) {
			for (Metadata supportedMetadata : supportedMetadatas.values()) {
				Object value = record.get(supportedMetadata);

				if (value != null) {
					map.get(supportedMetadata.getLocalCode()).remove(value);
				}
			}
		}

		public int getCacheObjectsCount() {
			int cacheSize = map.size() + supportedMetadatas.size();
			for (Map<String, RecordHolder> aMap : map.values()) {
				cacheSize += aMap.size();
			}

			return cacheSize;
		}
	}

	static class RecordHolder {

		private Record record;

		private int volatileCacheOccurences;

		RecordHolder(Record record) {
			set(record);
		}

		Record getCopy() {
			Record copy = record;
			if (copy != null) {
				copy = copy.getCopyOfOriginalRecord();
			}
			return copy;
		}

		void set(Record record) {
			//Object logicallyDeletedStatus = record.get(Schemas.LOGICALLY_DELETED_STATUS);
			//			if (logicallyDeletedStatus == null
			//					|| (logicallyDeletedStatus instanceof Boolean && !(Boolean) logicallyDeletedStatus)
			//					|| (logicallyDeletedStatus instanceof String && logicallyDeletedStatus.equals("false"))) {
			this.record = record.getUnmodifiableCopyOfOriginalRecord();
			//			} else {
			//				this.record = null;
			//			}
		}

		void invalidate() {
			this.record = null;
		}

	}

	private Record onlyIfNotSummary(Record record) {
		if (record == null) {
			return null;

		} else {
			CacheConfig config = getCacheConfigOf(record.getSchemaCode());
			if (!config.getPersistedMetadatas().isEmpty()) {
				return null;
			} else {
				return record;
			}
		}
	}

}

