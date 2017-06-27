package com.constellio.model.services.records.cache.ignite;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.ignite.cache.query.annotations.QuerySqlField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.data.dao.services.cache.ConstellioCache;
import com.constellio.data.dao.services.cache.ConstellioCacheManager;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.cache.CacheConfig;
import com.constellio.model.services.records.cache.RecordsCache;
import com.constellio.model.services.records.cache.RecordsCacheImplRuntimeException.RecordsCacheImplRuntimeException_InvalidSchemaTypeCode;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQuerySignature;
import com.constellio.model.services.search.query.logical.condition.DataStoreFilters;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.search.query.logical.condition.SchemaFilters;

public class RecordsCacheIgniteImpl implements RecordsCache {

	private static final Logger LOGGER = LoggerFactory.getLogger(RecordsCacheIgniteImpl.class);

	String collection;
	SchemaUtils schemaUtils = new SchemaUtils();

	Map<String, RecordByMetadataCache> recordByMetadataCache = new HashMap<>();

	Map<String, CacheConfig> cachedTypes = new HashMap<>();

	ModelLayerFactory modelLayerFactory;
	SearchServices searchServices;
	ConstellioCacheManager recordsCacheManager;
	
	ConstellioCache queryResultsCache;
	ConstellioCache permanentRecordHoldersCache;
	ConstellioCache volatileRecordHoldersCache;

	public RecordsCacheIgniteImpl(String collection, ModelLayerFactory modelLayerFactory) {
		this.collection = collection;
		this.modelLayerFactory = modelLayerFactory;
		this.searchServices = modelLayerFactory.newSearchServices();
		this.recordsCacheManager = modelLayerFactory.getDataLayerFactory().getRecordsCacheManager();
		
		this.queryResultsCache = recordsCacheManager.getCache(collection + ".queryResults");
		this.permanentRecordHoldersCache = recordsCacheManager.getCache(collection + ".recordHolders");
		this.volatileRecordHoldersCache = recordsCacheManager.getCache(collection + ".recordHolders.volatile");
	}
	
	private void putInPermanentCache(RecordHolder recordHolder) {
		
	}
	
	private void putInVolatileCache(RecordHolder recordHolder) {
		
	}
	
	private void putQueryResults(String schemaTypeCode, LogicalSearchQuerySignature signature, List<String> recordIds) {
		
	}
	
	private List<String> getQueryResults(String schemaTypeCode, LogicalSearchQuerySignature signature) {
		return null;
	}
	
	private void clearQueryResults(String schemaTypeCode) {
		
	}
	
	private void clearPermanentCache(String schemaTypeCode) {
		
	}
	
	private void clearVolatileCache(String schemaTypeCode) {
		
	}
	
	public boolean isCached(String id) {
		RecordHolder holder = permanentRecordHoldersCache.get(id);
		return holder != null && holder.getCopy() != null;
	}

	static AtomicInteger compteur = new AtomicInteger();

	static Set<String> ids = new HashSet<>();

	@Override
	public Record get(String id) {

		compteur.incrementAndGet();
		synchronized (RecordsCacheIgniteImpl.class) {
			ids.add(id);
		}

		RecordHolder holder = permanentRecordHoldersCache.get(id);

		Record copy = null;
		if (holder != null) {
			copy = holder.getCopy();
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
		String schemaTypeCodeForStorageInCache = getSchemaTypeCodeForStorageInCache(query);
		if (schemaTypeCodeForStorageInCache != null) {
			LogicalSearchQuerySignature signature = LogicalSearchQuerySignature.signature(query);

			List<String> recordIds = new ArrayList<>();
			for (Record record : records) {
				recordIds.add(record.getId());
				insert(record);
			}

			modelLayerFactory.getExtensions().getSystemWideExtensions().onPutQueryResultsInCache(signature, recordIds, 0);
			putQueryResults(schemaTypeCodeForStorageInCache, signature, recordIds);
		}
	}
	
	private String getSchemaTypeCodeForStorageInCache(LogicalSearchQuery query) {
		String schemaTypeCodeForStorageInCache;
		LogicalSearchCondition condition = query.getCondition();
		DataStoreFilters filters = condition.getFilters();
		if (filters instanceof SchemaFilters) {
			SchemaFilters schemaFilters = (SchemaFilters) filters;

			if (schemaFilters.getSchemaTypeFilter() != null
					&& hasNoUnsupportedFeatureOrFilter(query)) {
				String schemaTypeCode = schemaFilters.getSchemaTypeFilter().getCode();
				CacheConfig cacheConfig = getCacheConfigOf(schemaTypeCode);
				if (cacheConfig != null && cacheConfig.isPermanent()) {
					schemaTypeCodeForStorageInCache = schemaTypeCode;
				} else {
					schemaTypeCodeForStorageInCache = null;
				}
			} else {
				schemaTypeCodeForStorageInCache = null;
			}
		} else {
			schemaTypeCodeForStorageInCache = null;
		}
		return schemaTypeCodeForStorageInCache;
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
		String schemaTypeCodeForStorageInCache = getSchemaTypeCodeForStorageInCache(query);
		if (schemaTypeCodeForStorageInCache != null) {
			LogicalSearchQuerySignature signature = LogicalSearchQuerySignature.signature(query);

			List<String> recordIds = getQueryResults(schemaTypeCodeForStorageInCache, signature);
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
					RecordHolder holder = permanentRecordHoldersCache.get(recordCopy.getId());
					if (holder != null) {
						previousRecord = holder.record;

						insertRecordIntoAnAlreadyExistingHolder(recordCopy, cacheConfig, holder);
						if (cacheConfig.isPermanent() && (previousRecord == null || previousRecord.getVersion() != recordCopy
								.getVersion())) {
							clearQueryResults(schemaTypeCode);
						}
					} else {
						holder = insertRecordIntoAnANewHolder(recordCopy, cacheConfig);
						if (cacheConfig.isPermanent()) {
							clearQueryResults(schemaTypeCode);
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
				RecordHolder holder = permanentRecordHoldersCache.get(recordCopy.getId());
				if (holder != null) {
					previousRecord = holder.record;

					insertRecordIntoAnAlreadyExistingHolder(recordCopy, cacheConfig, holder);
					if (cacheConfig.isPermanent() && (previousRecord == null || previousRecord.getVersion() != recordCopy
							.getVersion())) {
						clearQueryResults(schemaTypeCode);
					}
				} else {
					holder = insertRecordIntoAnANewHolder(recordCopy, cacheConfig);
					if (cacheConfig.isPermanent()) {
						clearQueryResults(schemaTypeCode);
					}
				}
				this.recordByMetadataCache.get(schemaTypeCode).insert(previousRecord, holder);
			}

		}
		return insertedRecord;
	}

	private RecordHolder insertRecordIntoAnANewHolder(Record record, CacheConfig cacheConfig) {
		RecordHolder holder = new RecordHolder(record);
		permanentRecordHoldersCache.put(record.getId(), holder);
		if (cacheConfig.isVolatile()) {
			putInVolatileCache(holder);
		} else {
			putInPermanentCache(holder);
		}

		return holder;
	}

	private void insertRecordIntoAnAlreadyExistingHolder(Record record, CacheConfig cacheConfig, RecordHolder currentHolder) {
		if (currentHolder.record == null && cacheConfig.isVolatile()) {
			putInVolatileCache(currentHolder);
		}

		currentHolder.set(record);
	}

	@Override
	public synchronized void invalidateRecordsOfType(String recordType) {
		CacheConfig cacheConfig = cachedTypes.get(recordType);
		String schemaTypeCode = cacheConfig.getSchemaType();
		if (cacheConfig.isVolatile()) {
			clearVolatileCache(schemaTypeCode);
		} else {
			clearPermanentCache(schemaTypeCode);
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
			RecordHolder holder = permanentRecordHoldersCache.get(recordId);
			if (holder != null && holder.record != null) {
				CacheConfig cacheConfig = getCacheConfigOf(holder.record.getSchemaCode());
				String schemaTypeCode = cacheConfig.getSchemaType();
				recordByMetadataCache.get(schemaTypeCode).invalidate(holder.record);
				holder.invalidate();

				if (cacheConfig.isPermanent()) {
					clearQueryResults(schemaTypeCode);
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
		permanentRecordHoldersCache.clear();
		volatileRecordHoldersCache.clear();
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
		clearVolatileCache(schemaType);
		clearPermanentCache(schemaType);		
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

		cacheTotalSize += permanentRecordHoldersCache.size();

		for (RecordByMetadataCache aRecordByMetadataCache : recordByMetadataCache.values()) {
			cacheTotalSize += 1;
			cacheTotalSize += aRecordByMetadataCache.getCacheObjectsCount();
		}

		cacheTotalSize += 1 + volatileRecordHoldersCache.size();
		
		cacheTotalSize += 1 + permanentRecordHoldersCache.size();
		
		cacheTotalSize += 1 + queryResultsCache.size();

		for (CacheConfig aCacheConfig : cachedTypes.values()) {
			cacheTotalSize += 1 + aCacheConfig.getIndexes().size();
		}

		return cacheTotalSize;
	}
	
	static class RecordByMetadata implements Serializable {
		
	    @QuerySqlField(index = true)
		private String recordId;

	    @QuerySqlField(index = true)
		private String metadataCode;
		
		RecordByMetadata(String recordId, String metadataCode) {
			this.recordId = recordId;
			this.metadataCode = metadataCode;
		}
		
		String getRecordId() {
			return recordId;
		}
		
		String getMetadataCode() {
			return metadataCode;
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
	
	static class QueryResultsHolder implements Serializable {
		
		@QuerySqlField
		private String schemaTypeCode;
		
		private LogicalSearchQuery query;
		
		private List<String> results;
		
		QueryResultsHolder(String schemaTypeCode, LogicalSearchQuery query, List<String> results) {
			this.schemaTypeCode = schemaTypeCode;
			this.query = query;
			this.results = results;
		}
		
		String getSchemaTypeCode() {
			return schemaTypeCode;
		}
		
		LogicalSearchQuery getQuery() {
			return query;
		}
		
		List<String> getResults() {
			return results;
		}
		
	}

	static class RecordHolder implements Serializable {

	    @QuerySqlField(index = true)
		private String recordId;

	    @QuerySqlField(index = true)
		private String schemaTypeCode;

		private Record record;

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
			this.schemaTypeCode = SchemaUtils.getSchemaTypeCode(record.getSchemaCode());
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
