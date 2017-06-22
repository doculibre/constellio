package com.constellio.model.services.records.cache;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.data.dao.services.cache.ConstellioCache;
import com.constellio.data.dao.services.cache.ConstellioCacheManager;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.cache.RecordsCacheImplRuntimeException.RecordsCacheImplRuntimeException_InvalidSchemaTypeCode;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQuerySignature;
import com.constellio.model.services.search.query.logical.condition.DataStoreFilters;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.search.query.logical.condition.SchemaFilters;

public class RecordsCacheImpl implements RecordsCache {

	private static final Logger LOGGER = LoggerFactory.getLogger(RecordsCacheImpl.class);

	String collection;
	SchemaUtils schemaUtils = new SchemaUtils();

//	Map<String, RecordHolder> cacheById = new HashMap<>();

	Map<String, RecordByMetadataCache> recordByMetadataCache = new HashMap<>();
	Map<String, VolatileCache> volatileCaches = new HashMap<>();
	Map<String, PermanentCache> permanentCaches = new HashMap<>();

	Map<String, CacheConfig> cachedTypes = new HashMap<>();

	ModelLayerFactory modelLayerFactory;
	SearchServices searchServices;
	ConstellioCacheManager recordsCacheManager;
	ConstellioCache cacheById;

	public RecordsCacheImpl(String collection, ModelLayerFactory modelLayerFactory) {
		this.collection = collection;
		this.modelLayerFactory = modelLayerFactory;
		this.searchServices = modelLayerFactory.newSearchServices();
		this.recordsCacheManager = modelLayerFactory.getDataLayerFactory().getRecordsCacheManager();
		cacheById = recordsCacheManager.getCache(collection + ".cacheById");
	}

	public boolean isCached(String id) {
		RecordHolder holder = cacheById.get(id);
		return holder != null && holder.getCopy() != null;
	}

	static AtomicInteger compteur = new AtomicInteger();

	static Set<String> ids = new HashSet<>();

	@Override
	public Record get(String id) {

		compteur.incrementAndGet();
		synchronized (RecordsCacheImpl.class) {
			ids.add(id);
		}

		RecordHolder holder = cacheById.get(id);

		Record copy = null;
		if (holder != null) {
			copy = holder.getCopy();

			if (copy != null) {
				CacheConfig config = getCacheConfigOf(copy.getSchemaCode());
				if (config.isVolatile()) {
					VolatileCache cache = volatileCaches.get(config.getSchemaType());
					synchronized (this) {
						cache.hit(holder);
					}
				}

			}

		}

		return copy;
	}

	public synchronized void insert(List<Record> records) {
		if (records != null) {
			for (Record record : records) {
				insert(record);
			}
		}
	}

	@Override
	public void insertQueryResults(LogicalSearchQuery query, List<Record> records) {

		PermanentCache cache = getCacheFor(query);
		if (cache != null) {
			LogicalSearchQuerySignature signature = LogicalSearchQuerySignature.signature(query);

			List<String> recordIds = new ArrayList<>();
			for (Record record : records) {
				recordIds.add(record.getId());
				insert(record);
			}

			modelLayerFactory.getExtensions().getSystemWideExtensions().onPutQueryResultsInCache(signature, recordIds, 0);
			cache.putQueryResults(signature.toStringSignature(), recordIds);
		}
	}

	PermanentCache getCacheFor(LogicalSearchQuery query) {
		LogicalSearchCondition condition = query.getCondition();
		DataStoreFilters filters = condition.getFilters();
		if (filters instanceof SchemaFilters) {
			SchemaFilters schemaFilters = (SchemaFilters) filters;

			if (schemaFilters.getSchemaTypeFilter() != null
					&& hasNoUnsupportedFeatureOrFilter(query)) {
				CacheConfig cacheConfig = getCacheConfigOf(schemaFilters.getSchemaTypeFilter().getCode());
				if (cacheConfig != null && cacheConfig.isPermanent()) {
					return permanentCaches.get(cacheConfig.getSchemaType());
				}
			}

		}
		return null;
	}

	private boolean hasNoUnsupportedFeatureOrFilter(LogicalSearchQuery query) {
		return query.getFacetFilters().toSolrFilterQueries().isEmpty()
				&& query.getFieldBoosts().isEmpty()
				&& query.getQueryBoosts().isEmpty()
				&& query.getStartRow() == 0
				&& query.getNumberOfRows() == 100000
				&& query.getStatisticFields().isEmpty()
				&& !query.isPreferAnalyzedFields()
				&& query.getResultsProjection() == null
				&& query.getFieldFacets().isEmpty()
				&& query.getQueryFacets().isEmpty()
				&& query.getReturnedMetadatas().isFullyLoaded()
				&& query.getUserFilter() == null
				&& !query.isHighlighting();
	}

	@Override
	public List<Record> getQueryResults(LogicalSearchQuery query) {
		List<Record> cachedResults = null;
		PermanentCache cache = getCacheFor(query);
		if (cache != null) {
			LogicalSearchQuerySignature signature = LogicalSearchQuerySignature.signature(query);

			List<String> recordIds = cache.getQueryResults(signature.toStringSignature());
			if (recordIds != null) {
				cachedResults = new ArrayList<>();

				for (String recordId : recordIds) {
					cachedResults.add(get(recordId));
				}
				cachedResults = Collections.unmodifiableList(cachedResults);
				modelLayerFactory.getExtensions().getSystemWideExtensions().onQueryCacheHit(signature, 0);

			} else {
				modelLayerFactory.getExtensions().getSystemWideExtensions().onQueryCacheMiss(signature, 0);
			}

		}

		return cachedResults;
	}

	@Override
	public Record forceInsert(Record insertedRecord) {
		if (!insertedRecord.isFullyLoaded()) {
			invalidate(insertedRecord.getId());
			return insertedRecord;
		}

		try {
			Record recordCopy = insertedRecord.getCopyOfOriginalRecord();
			CacheConfig cacheConfig = getCacheConfigOf(recordCopy.getSchemaCode());
			if (cacheConfig != null) {
				String schemaTypeCode = cacheConfig.getSchemaType();
				Record previousRecord = null;

				synchronized (this) {
					modelLayerFactory.getExtensions().getSystemWideExtensions().onPutInCache(recordCopy, 0);
					RecordHolder holder = cacheById.get(recordCopy.getId());
					if (holder != null) {
						previousRecord = holder.record;

						insertRecordIntoAnAlreadyExistingHolder(recordCopy, cacheConfig, holder);
						if (cacheConfig.isPermanent() && (previousRecord == null || previousRecord.getVersion() != recordCopy
								.getVersion())) {
							permanentCaches.get(schemaTypeCode).clearQueryResults();
						}
					} else {
						holder = insertRecordIntoAnANewHolder(recordCopy, cacheConfig);
						if (cacheConfig.isPermanent()) {
							permanentCaches.get(schemaTypeCode).clearQueryResults();
						}
					}
					this.recordByMetadataCache.get(schemaTypeCode).insert(previousRecord, holder);
				}

			}
			return insertedRecord;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return insertedRecord;
	}

	@Override
	public Record insert(Record insertedRecord) {

		if (insertedRecord == null || insertedRecord.isDirty() || !insertedRecord.isSaved()) {
			return insertedRecord;
		}

		if (!insertedRecord.isFullyLoaded()) {
			invalidate(insertedRecord.getId());
			return insertedRecord;
		}

		Record recordCopy = insertedRecord.getCopyOfOriginalRecord();

		CacheConfig cacheConfig = getCacheConfigOf(recordCopy.getSchemaCode());
		if (cacheConfig != null) {
			String schemaTypeCode = cacheConfig.getSchemaType();
			Record previousRecord = null;

			synchronized (this) {
				modelLayerFactory.getExtensions().getSystemWideExtensions().onPutInCache(recordCopy, 0);
				RecordHolder holder = cacheById.get(recordCopy.getId());
				if (holder != null) {
					previousRecord = holder.record;

					insertRecordIntoAnAlreadyExistingHolder(recordCopy, cacheConfig, holder);
					if (cacheConfig.isPermanent() && (previousRecord == null || previousRecord.getVersion() != recordCopy
							.getVersion())) {
						permanentCaches.get(schemaTypeCode).clearQueryResults();
					}
				} else {
					holder = insertRecordIntoAnANewHolder(recordCopy, cacheConfig);
					if (cacheConfig.isPermanent()) {
						permanentCaches.get(schemaTypeCode).clearQueryResults();
					}
				}
				this.recordByMetadataCache.get(schemaTypeCode).insert(previousRecord, holder);
			}

		}
		return insertedRecord;
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

	private void insertRecordIntoAnAlreadyExistingHolder(Record record, CacheConfig cacheConfig, RecordHolder currentHolder) {
		if (currentHolder.record == null && cacheConfig.isVolatile()) {
			VolatileCache cache = volatileCaches.get(cacheConfig.getSchemaType());
			cache.releaseFor(1);
			cache.insert(currentHolder);
		}

		currentHolder.set(record);
	}

	@Override
	public synchronized void invalidateRecordsOfType(String recordType) {
		CacheConfig cacheConfig = cachedTypes.get(recordType);
		if (cacheConfig.isVolatile()) {
			volatileCaches.get(cacheConfig.getSchemaType()).invalidateAll();
		} else {
			permanentCaches.get(cacheConfig.getSchemaType()).invalidateAll();
		}
	}

	public synchronized void invalidate(List<String> recordIds) {
		if (recordIds != null) {
			for (String recordId : recordIds) {
				invalidate(recordId);
			}
		}
	}

	@Override
	public synchronized void invalidate(String recordId) {
		if (recordId != null) {
			RecordHolder holder = cacheById.get(recordId);
			if (holder != null && holder.record != null) {
				CacheConfig cacheConfig = getCacheConfigOf(holder.record.getSchemaCode());
				String schemaTypeCode = cacheConfig.getSchemaType();
				recordByMetadataCache.get(schemaTypeCode).invalidate(holder.record);
				holder.invalidate();

				if (cacheConfig.isPermanent()) {
					permanentCaches.get(schemaTypeCode).clearQueryResults();
				}
			}
		}

	}

	public CacheConfig getCacheConfigOf(String schemaOrTypeCode) {
		String schemaTypeCode = SchemaUtils.getSchemaTypeCode(schemaOrTypeCode);
		return cachedTypes.get(schemaTypeCode);
	}

	@Override
	public void configureCache(CacheConfig cacheConfig) {

		if (cacheConfig == null) {
			throw new IllegalArgumentException("Required parameter 'cacheConfig'");
		}
		String schemaTypeCode = cacheConfig.getSchemaType();
		if (schemaTypeCode.contains("_")) {
			throw new RecordsCacheImplRuntimeException_InvalidSchemaTypeCode(schemaTypeCode);
		}
		if (cachedTypes.containsKey(schemaTypeCode)) {
			removeCache(schemaTypeCode);
		}

		cachedTypes.put(schemaTypeCode, cacheConfig);
		if (cacheConfig.isPermanent()) {
			String holdersCacheKey = collection + "." + schemaTypeCode + ".permanentCache.holders"; 
			String queryResultsCacheKey = collection + "." + schemaTypeCode + ".permanentCache.queryResults"; 
			permanentCaches.put(schemaTypeCode, new PermanentCache(recordsCacheManager.getCache(holdersCacheKey), recordsCacheManager.getCache(queryResultsCacheKey)));
		} else {
			String holdersCacheKey = collection + "." + schemaTypeCode + ".volatileCache.holders"; 
			volatileCaches.put(schemaTypeCode, new VolatileCache(recordsCacheManager.getCache(holdersCacheKey), cacheConfig.getVolatileMaxSize()));
		}

		String recordsByMetadataCacheName = collection + "." + schemaTypeCode + ".recordsByMetadata";
		recordByMetadataCache.put(schemaTypeCode, new RecordByMetadataCache(recordsCacheManager.getCache(recordsByMetadataCacheName), cacheConfig));

		if (cacheConfig.isLoadedInitially()) {
			LOGGER.info("Loading cache of type '" + schemaTypeCode + "' of collection '" + collection + "'");
			MetadataSchemaType schemaType = modelLayerFactory.getMetadataSchemasManager()
					.getSchemaTypes(collection).getSchemaType(schemaTypeCode);
			if (searchServices.getResultsCount(from(schemaType).returnAll()) < 10000) {
				for (Iterator<Record> it = searchServices.recordsIterator(from(schemaType).returnAll(), 1000); it.hasNext(); ) {
					insert(it.next());
				}
			}
		}

	}

	@Override
	public Collection<CacheConfig> getConfiguredCaches() {
		return cachedTypes.values();
	}

	@Override
	public void invalidateAll() {
		cacheById.clear();
		for (VolatileCache cache : volatileCaches.values()) {
			cache.invalidateAll();
		}

		for (PermanentCache cache : permanentCaches.values()) {
			cache.invalidateAll();
		}
	}

	@Override
	public Record getByMetadata(Metadata metadata, String value) {
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

	@Override
	public synchronized void removeCache(String schemaType) {
		recordByMetadataCache.remove(schemaType);
		if (volatileCaches.containsKey(schemaType)) {
			volatileCaches.get(schemaType).invalidateAll();
			volatileCaches.remove(schemaType);
		}
		if (permanentCaches.containsKey(schemaType)) {
			permanentCaches.get(schemaType).invalidateAll();
			permanentCaches.remove(schemaType);
		}

		cachedTypes.remove(schemaType);
	}

	@Override
	public boolean isConfigured(MetadataSchemaType type) {
		return isConfigured(type.getCode());
	}

	public boolean isConfigured(String typeCode) {
		return cachedTypes.containsKey(typeCode);
	}

	@Override
	public int getCacheObjectsCount() {
		int cacheTotalSize = 0;

		cacheTotalSize += cacheById.size();

		for (RecordByMetadataCache aRecordByMetadataCache : recordByMetadataCache.values()) {
			cacheTotalSize += 1;
			cacheTotalSize += aRecordByMetadataCache.getCacheObjectsCount();
		}

		for (VolatileCache aVolatileCache : volatileCaches.values()) {
			cacheTotalSize += 1 + aVolatileCache.holdersSize();
		}

		for (PermanentCache aPermanentCache : permanentCaches.values()) {
			cacheTotalSize += 1 + aPermanentCache.getCacheObjectsCount();
		}

		for (CacheConfig aCacheConfig : cachedTypes.values()) {
			cacheTotalSize += 1 + aCacheConfig.getIndexes().size();
		}

		return cacheTotalSize;
	}

	static class VolatileCache {

		int maxSize;

		private ConstellioCache holdersCache;

		int recordsInCache;

		VolatileCache(ConstellioCache holdersCache, int maxSize) {
			this.holdersCache = holdersCache;
			this.maxSize = maxSize;
		}

		void insert(RecordHolder holder) {
			holder.volatileCacheOccurences = 1;
//			holders.add(holder);
			holdersCache.put(holder.getRecordId(), holder);
			recordsInCache++;
		}

		void hit(RecordHolder holder) {
			if (holder.volatileCacheOccurences <= 2) {
				holder.volatileCacheOccurences++;
//				holders.add(holder);
				holdersCache.put(holder.getRecordId(), holder);
			}
		}

		void releaseFor(int qty) {
			while (recordsInCache + qty > maxSize) {
				releaseNext();
			}
		}

		void releaseNext() {
			String holderId = holdersCache.keySet().next();
			RecordHolder recordHolder = holdersCache.get(holderId);
			holdersCache.remove(holderId);
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
			for (Iterator<String> it = holdersCache.keySet(); it.hasNext();) {
				String holdId = it.next();
				RecordHolder holder = holdersCache.get(holdId);
				holder.invalidate();
			}
			holdersCache.clear();
		}
		
		int holdersSize() {
			return holdersCache.size();
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (Iterator<String> it = holdersCache.keySet(); it.hasNext();) {
				String holdId = it.next();
				RecordHolder holder = holdersCache.get(holdId);
				if (holder.record != null) {
					if (sb.length() > 0) {
						sb.append(", ");
					}
					sb.append(holder.record.getId());
				}
			}
			return sb.toString();
		}

	}

	static class VolatileCache2 {

		int maxSize;

		LinkedList<RecordHolder> holders = new LinkedList<>();

		int recordsInCache;

		VolatileCache2(ConstellioCache holdersCache, int maxSize) {
			this.maxSize = maxSize;
		}

		void insert(RecordHolder holder) {
			holder.volatileCacheOccurences = 1;
			holders.add(holder);
			recordsInCache++;
		}

		void hit(RecordHolder holder) {
			if (holder.volatileCacheOccurences <= 2) {
				holder.volatileCacheOccurences++;
				holders.add(holder);
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
		
		int holdersSize() {
			return holders.size();
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (RecordHolder holder : holders) {
				if (holder.record != null) {
					if (sb.length() > 0) {
						sb.append(", ");
					}
					sb.append(holder.record.getId());
				}
			}
			return sb.toString();
		}

	}

	static class PermanentCache {

//		Map<String, List<String>> queryResults = new HashMap<>();
//		LinkedList<RecordHolder> holders = new LinkedList<>();
		
		private ConstellioCache holdersCache;
		
		private ConstellioCache queryResultsCache;
		
		private PermanentCache(ConstellioCache holdersCache, ConstellioCache queryResultsCache) {
			this.holdersCache = holdersCache;
			this.queryResultsCache = queryResultsCache;
		}
		
		void insert(RecordHolder holder) {
			holdersCache.put(holder.getRecordId(), holder);
		}
		
		List<String> getQueryResults(String schemaType) {
			return queryResultsCache.get(schemaType);
		}
		
		void putQueryResults(String schemaType, List<String> queryResults) {
			queryResultsCache.put(schemaType, (Serializable) queryResults);
		}
		
		void clearQueryResults(String schemaType) {
			List<String> queryResults = queryResultsCache.get(schemaType);
			queryResults.clear();
			putQueryResults(schemaType, queryResults);
		}
		
		void clearQueryResults() {
			queryResultsCache.clear();
		}

		void invalidateAll() {
			for (Iterator<String> it = holdersCache.keySet(); it.hasNext();) {
				String recordId = it.next();
				RecordHolder holder = holdersCache.get(recordId);
				holder.invalidate();
			}
			clearQueryResults();
		}

		public int getCacheObjectsCount() {
			int size = holdersCache.size();

			for (Iterator<String> it = queryResultsCache.keySet(); it.hasNext();) {
				String schemaType = it.next();
				List<String> queryResults = queryResultsCache.get(schemaType);
				size += 1 + queryResults.size();
			}

			return size;
		}
		
		int holdersSize() {
			return holdersCache.size();
		}
		
		int queryResultsSize() {
			return queryResultsCache.size();
		}
	}

	static class RecordByMetadataCache {
		
		private ConstellioCache recordHolderCache;
		
//		Map<String, Map<String, RecordHolder>> recordHolderMap = new HashMap<>();
		Map<String, Metadata> supportedMetadatas = new HashMap<>();

		RecordByMetadataCache(ConstellioCache recordHolderCache, CacheConfig cacheConfig) {
			this.recordHolderCache = recordHolderCache;
			for (Metadata indexedMetadata : cacheConfig.getIndexes()) {
				supportedMetadatas.put(indexedMetadata.getLocalCode(), indexedMetadata);
				putInCache(indexedMetadata.getLocalCode(), new HashMap<String, RecordHolder>());
			}
		}
		
		private Map<String, RecordHolder> getFromCache(String key) {
			return recordHolderCache.get(key);
		}
		
		private void putInCache(String key, Map<String, RecordHolder> value) {
			recordHolderCache.put(key, (Serializable) value);
		}
		
		private int getCacheSize() {
			return recordHolderCache.size();
		}

		Record getByMetadata(String localCode, String value) {
			Map<String, RecordHolder> metadataMap = getFromCache(localCode);
			RecordHolder recordHolder = null;
			if (metadataMap != null) {
				recordHolder = metadataMap.get(value);
			}
			return recordHolder == null ? null : recordHolder.getCopy();
		}

		void insert(Record previousRecord, RecordHolder recordHolder) {

			for (Metadata supportedMetadata : supportedMetadatas.values()) {
				String value = null;
				String previousValue = null;

				if (previousRecord != null) {
					previousValue = previousRecord.get(supportedMetadata);
				}
				if (recordHolder.record != null) {
					value = recordHolder.record.get(supportedMetadata);
				}
				if (previousValue != null && !previousValue.equals(value)) {
					getFromCache(supportedMetadata.getLocalCode()).remove(previousValue);
				}
				if (value != null && !value.equals(previousValue)) {
					getFromCache(supportedMetadata.getLocalCode()).put(value, recordHolder);
				}
			}
		}

		void invalidate(Record record) {
			for (Metadata supportedMetadata : supportedMetadatas.values()) {
				String value = record.get(supportedMetadata);

				if (value != null) {
					getFromCache(supportedMetadata.getLocalCode()).remove(value);
				}
			}
		}

		public int getCacheObjectsCount() {
			int cacheSize = getCacheSize() + supportedMetadatas.size();
			for (Iterator<String> it = recordHolderCache.keySet(); it.hasNext();) {
				String key = it.next();
				Map<String, RecordHolder> aMap = getFromCache(key);
				cacheSize += aMap.size();
			}
			return cacheSize;
		}
	}

	static class RecordHolder implements Serializable {
		
		private String recordId;

		private Record record;

		private int volatileCacheOccurences;

		RecordHolder(Record record) {
			set(record);
		}
		
		String getRecordId() {
			return recordId;
		}

		Record getCopy() {
			Record copy = record;
			if (copy != null) {
				copy = copy.getCopyOfOriginalRecord();
			}
			return copy;
		}

		void set(Record record) {
			this.recordId = record.getId();
			Object logicallyDeletedStatus = record.get(Schemas.LOGICALLY_DELETED_STATUS);
			if (logicallyDeletedStatus == null
					|| (logicallyDeletedStatus instanceof Boolean && !(Boolean) logicallyDeletedStatus)
					|| (logicallyDeletedStatus instanceof String && logicallyDeletedStatus.equals("false"))) {
				this.record = record.getCopyOfOriginalRecord();
			} else {
				this.record = null;
			}
		}

		void invalidate() {
			this.record = null;
		}

	}
}

