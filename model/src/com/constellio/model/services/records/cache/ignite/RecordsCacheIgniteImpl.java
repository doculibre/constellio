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

import javax.cache.Cache;

import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cache.eviction.lru.LruEvictionPolicy;
import org.apache.ignite.cache.query.SqlQuery;
import org.apache.ignite.cache.query.annotations.QuerySqlField;
import org.apache.ignite.configuration.CacheConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.data.dao.services.cache.ignite.ConstellioIgniteCache;
import com.constellio.data.dao.services.cache.ignite.ConstellioIgniteCacheManager;
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

	Map<String, CacheConfig> cachedTypes = new HashMap<>();

	ModelLayerFactory modelLayerFactory;
	SearchServices searchServices;
	ConstellioIgniteCacheManager recordsCacheManager;
	
	String queryResultsCacheName;
	String byIdRecordHoldersCacheName;
	String permanentRecordHoldersCacheName;
	String volatileRecordHoldersCacheName;
	String recordByMetadataCacheName;
	
	ConstellioIgniteCache queryResultsCache;
	ConstellioIgniteCache byIdRecordHoldersCache;
	ConstellioIgniteCache permanentRecordHoldersCache;
	ConstellioIgniteCache volatileRecordHoldersCache;
	ConstellioIgniteCache recordByMetadataCache;
	
	Map<String, List<Metadata>> cachedMetadatasBySchemaType = new HashMap<>();
	
	public RecordsCacheIgniteImpl(String collection, ModelLayerFactory modelLayerFactory) {
		this.collection = collection;
		this.modelLayerFactory = modelLayerFactory;
		this.searchServices = modelLayerFactory.newSearchServices();
		this.recordsCacheManager = (ConstellioIgniteCacheManager) modelLayerFactory.getDataLayerFactory().getRecordsCacheManager();
		
		this.queryResultsCacheName = collection + ".queryResults";
		this.byIdRecordHoldersCacheName = collection + ".recordHolders.byId";
		this.permanentRecordHoldersCacheName = collection + ".recordHolders.permanent";
		this.volatileRecordHoldersCacheName = collection + ".recordHolders.volatile";
		this.recordByMetadataCacheName = collection + ".recordsByMetadata";

        CacheConfiguration<String, Object> queryResultsCacheCfg = new CacheConfiguration<>(queryResultsCacheName);
        queryResultsCacheCfg.setCacheMode(CacheMode.PARTITIONED); // Default.
        queryResultsCacheCfg.setIndexedTypes(String.class, QueryResultsHolder.class);

        CacheConfiguration<String, Object> byIdRecordHoldersCacheCfg = new CacheConfiguration<>(byIdRecordHoldersCacheName);
        byIdRecordHoldersCacheCfg.setCacheMode(CacheMode.PARTITIONED); // Default.
        byIdRecordHoldersCacheCfg.setIndexedTypes(String.class, RecordHolder.class);

        CacheConfiguration<String, Object> permanentRecordHoldersCacheCfg = new CacheConfiguration<>(permanentRecordHoldersCacheName);
        permanentRecordHoldersCacheCfg.setCacheMode(CacheMode.PARTITIONED); // Default.
        permanentRecordHoldersCacheCfg.setIndexedTypes(String.class, RecordHolder.class);

        CacheConfiguration<String, Object> volatileRecordHoldersCacheCfg = new CacheConfiguration<>(volatileRecordHoldersCacheName);
        volatileRecordHoldersCacheCfg.setCacheMode(CacheMode.PARTITIONED); // Default.
        volatileRecordHoldersCacheCfg.setOnheapCacheEnabled(true);
        volatileRecordHoldersCacheCfg.setEvictionPolicy(new LruEvictionPolicy<String, Object>(30000));
        volatileRecordHoldersCacheCfg.setIndexedTypes(String.class, RecordHolder.class);

        CacheConfiguration<String, Object> recordByMetadataCacheCfg = new CacheConfiguration<>(recordByMetadataCacheName);
        recordByMetadataCacheCfg.setCacheMode(CacheMode.PARTITIONED); // Default.
        recordByMetadataCacheCfg.setIndexedTypes(String.class, RecordByMetadata.class);
		
		this.queryResultsCache = (ConstellioIgniteCache) recordsCacheManager.getCache(queryResultsCacheCfg);
		this.byIdRecordHoldersCache = (ConstellioIgniteCache) recordsCacheManager.getCache(byIdRecordHoldersCacheCfg);
		this.permanentRecordHoldersCache = (ConstellioIgniteCache) recordsCacheManager.getCache(permanentRecordHoldersCacheCfg);
		this.volatileRecordHoldersCache = (ConstellioIgniteCache) recordsCacheManager.getCache(volatileRecordHoldersCacheCfg);
		this.recordByMetadataCache = (ConstellioIgniteCache) recordsCacheManager.getCache(recordByMetadataCacheCfg);
	}
	
	private void putInPermanentCache(RecordHolder recordHolder) {
		permanentRecordHoldersCache.put(recordHolder.getRecordId(), recordHolder);
	}
	
	private void putInVolatileCache(RecordHolder recordHolder) {
		volatileRecordHoldersCache.put(recordHolder.getRecordId(), recordHolder);
	}
	
	private void putQueryResults(String schemaTypeCode, LogicalSearchQuerySignature signature, List<String> results) {
		String stringSignature = signature.toStringSignature();
		queryResultsCache.put(stringSignature, new QueryResultsHolder(schemaTypeCode, stringSignature, results));
	}
	
	private List<String> getQueryResults(String schemaTypeCode, LogicalSearchQuerySignature signature) {
		List<String> queryResults;

		String stringSignature = signature.toStringSignature();
        String sql = "querySignature = ? and schemaTypeCode = ?";
        SqlQuery<String, QueryResultsHolder> sqlQuery = new SqlQuery<>(QueryResultsHolder.class, sql);
        sqlQuery.setArgs(stringSignature, schemaTypeCode);

        ConstellioIgniteCache constellioIgniteCache = queryResultsCache;
		IgniteCache<String, Object> igniteCache = constellioIgniteCache.getIgniteCache();
        List<Cache.Entry<String, QueryResultsHolder>> igniteCacheQueryResults = igniteCache.query(sqlQuery).getAll();
		if (!igniteCacheQueryResults.isEmpty()) {
			Cache.Entry<String, QueryResultsHolder> igniteCacheQueryResult = igniteCacheQueryResults.get(0);
			queryResults = igniteCacheQueryResult.getValue().getResults();
		} else {
			queryResults = null;
		}
		return queryResults;
	}
	
	private void clearByIdCache(String schemaTypeCode) {
        String sql = "schemaTypeCode = ?";
        SqlQuery<String, RecordHolder> sqlQuery = new SqlQuery<>(RecordHolder.class, sql);
        sqlQuery.setArgs(schemaTypeCode);

        ConstellioIgniteCache constellioIgniteCache = byIdRecordHoldersCache;
		IgniteCache<String, Object> igniteCache = constellioIgniteCache.getIgniteCache();
        List<Cache.Entry<String, RecordHolder>> igniteCacheQueryResults = igniteCache.query(sqlQuery).getAll();
        Set<String> removedKeys = new HashSet<>();
        for (Cache.Entry<String, RecordHolder> entry : igniteCacheQueryResults) {
			String recordId = entry.getKey();
			removedKeys.add(recordId);
		}
        if (!removedKeys.isEmpty()) {
        	constellioIgniteCache.removeAll(removedKeys);
        }
	}
	
	int getByIdRecordHoldersCount(String schemaTypeCode) {
        String sql = "schemaTypeCode = ?";
        SqlQuery<String, RecordHolder> sqlQuery = new SqlQuery<>(RecordHolder.class, sql);
        sqlQuery.setArgs(schemaTypeCode);

        ConstellioIgniteCache constellioIgniteCache = byIdRecordHoldersCache;
		IgniteCache<String, Object> igniteCache = constellioIgniteCache.getIgniteCache();
        List<Cache.Entry<String, RecordHolder>> igniteCacheQueryResults = igniteCache.query(sqlQuery).getAll();
		return igniteCacheQueryResults.size();
	}
	
	int getPermanentRecordHoldersCount(String schemaTypeCode) {
        String sql = "schemaTypeCode = ?";
        SqlQuery<String, RecordHolder> sqlQuery = new SqlQuery<>(RecordHolder.class, sql);
        sqlQuery.setArgs(schemaTypeCode);

        ConstellioIgniteCache constellioIgniteCache = permanentRecordHoldersCache;
		IgniteCache<String, Object> igniteCache = constellioIgniteCache.getIgniteCache();
        List<Cache.Entry<String, RecordHolder>> igniteCacheQueryResults = igniteCache.query(sqlQuery).getAll();
		return igniteCacheQueryResults.size();
	}
	
	int getVolatileRecordHoldersCount(String schemaTypeCode) {
        String sql = "schemaTypeCode = ?";
        SqlQuery<String, RecordHolder> sqlQuery = new SqlQuery<>(RecordHolder.class, sql);
        sqlQuery.setArgs(schemaTypeCode);

        ConstellioIgniteCache constellioIgniteCache = volatileRecordHoldersCache;
		IgniteCache<String, Object> igniteCache = constellioIgniteCache.getIgniteCache();
        List<Cache.Entry<String, RecordHolder>> igniteCacheQueryResults = igniteCache.query(sqlQuery).getAll();
		return igniteCacheQueryResults.size();
	}
	
	int getQueryResultHoldersCount(String schemaTypeCode) {
        String sql = "schemaTypeCode = ?";
        SqlQuery<String, QueryResultsHolder> sqlQuery = new SqlQuery<>(QueryResultsHolder.class, sql);
        sqlQuery.setArgs(schemaTypeCode);

        ConstellioIgniteCache constellioIgniteCache = queryResultsCache;
		IgniteCache<String, Object> igniteCache = constellioIgniteCache.getIgniteCache();
        List<Cache.Entry<String, QueryResultsHolder>> igniteCacheQueryResults = igniteCache.query(sqlQuery).getAll();
		return igniteCacheQueryResults.size();
	}
	
	private void clearPermanentCache(String schemaTypeCode) {
        String sql = "schemaTypeCode = ?";
        SqlQuery<String, RecordHolder> sqlQuery = new SqlQuery<>(RecordHolder.class, sql);
        sqlQuery.setArgs(schemaTypeCode);

        ConstellioIgniteCache constellioIgniteCache = permanentRecordHoldersCache;
		IgniteCache<String, Object> igniteCache = constellioIgniteCache.getIgniteCache();
        List<Cache.Entry<String, RecordHolder>> igniteCacheQueryResults = igniteCache.query(sqlQuery).getAll();
        Set<String> removedKeys = new HashSet<>();
        for (Cache.Entry<String, RecordHolder> entry : igniteCacheQueryResults) {
			String recordId = entry.getKey();
			removedKeys.add(recordId);
		}
        if (!removedKeys.isEmpty()) {
        	constellioIgniteCache.removeAll(removedKeys);
        }
	}
	
	private void clearVolatileCache(String schemaTypeCode) {
        String sql = "schemaTypeCode = ?";
        SqlQuery<String, RecordHolder> sqlQuery = new SqlQuery<>(RecordHolder.class, sql);
        sqlQuery.setArgs(schemaTypeCode);

        ConstellioIgniteCache constellioIgniteCache = volatileRecordHoldersCache;
		IgniteCache<String, Object> igniteCache = constellioIgniteCache.getIgniteCache();
        List<Cache.Entry<String, RecordHolder>> igniteCacheQueryResults = igniteCache.query(sqlQuery).getAll();
        Set<String> removedKeys = new HashSet<>();
        for (Cache.Entry<String, RecordHolder> entry : igniteCacheQueryResults) {
			String recordId = entry.getKey();
			removedKeys.add(recordId);
		}
        if (!removedKeys.isEmpty()) {
        	constellioIgniteCache.removeAll(removedKeys);
        }
	}
	
	private void clearQueryResults(String schemaTypeCode) {
        String sql = "schemaTypeCode = ?";
        SqlQuery<String, QueryResultsHolder> sqlQuery = new SqlQuery<>(QueryResultsHolder.class, sql);
        sqlQuery.setArgs(schemaTypeCode);

        ConstellioIgniteCache constellioIgniteCache = queryResultsCache;
		IgniteCache<String, Object> igniteCache = constellioIgniteCache.getIgniteCache();
        List<Cache.Entry<String, QueryResultsHolder>> igniteCacheQueryResults = igniteCache.query(sqlQuery).getAll();
        Set<String> removedKeys = new HashSet<>();
        for (Cache.Entry<String, QueryResultsHolder> entry : igniteCacheQueryResults) {
			String recordId = entry.getKey();
			removedKeys.add(recordId);
		}
        if (!removedKeys.isEmpty()) {
        	constellioIgniteCache.removeAll(removedKeys);
        }
	}
	
	private void putInRecordByMetadataCache(Record previousRecord, Record record) {
		String schemaTypeCode = SchemaUtils.getSchemaTypeCode(record.getSchemaCode());
		List<Metadata> cachedMetadatas = cachedMetadatasBySchemaType.get(schemaTypeCode);
		if (cachedMetadatas != null) {
			String recordId = record.getId();
			for (Metadata metadata : cachedMetadatas) {
				String metadataCode = metadata.getLocalCode();
				String previousValue;
				String value = record.get(metadata);
				if (previousRecord != null) {
					previousValue = previousRecord.get(metadata);
					clearRecordByMetadataCacheForRecord(previousRecord);
					if (previousValue != null && !previousValue.equals(value)) {
						clearRecordByMetadataCacheForValue(schemaTypeCode, metadata, previousValue);
					}
				} else {
					previousValue = null;
				}
				if (value != null && !value.equals(previousValue)) {
					RecordByMetadata recordByMetadata = new RecordByMetadata(recordId, schemaTypeCode, metadataCode, value);
					recordByMetadataCache.put(recordByMetadata.getKey(), recordByMetadata);
				}
			}
		}
	}
	
	private Record getRecordByMetadataValue(String schemaTypeCode, Metadata metadata, String value) {
		Record record;
		String metadataCode = metadata.getLocalCode();

		ConstellioIgniteCache constellioIgniteCache = recordByMetadataCache;
		String key = RecordByMetadata.toKey(schemaTypeCode, metadataCode, value);
		RecordByMetadata recordByMetada = constellioIgniteCache.get(key);
		if (recordByMetada != null) {
			String recordId = recordByMetada.getRecordId();
			record = get(recordId);
		} else {
			record = null;
		}
		return record;
	}
	
	private void clearRecordByMetadataCacheForValue(String schemaTypeCode, Metadata metadata, String value) {
		String metadataCode = metadata.getLocalCode();

		ConstellioIgniteCache constellioIgniteCache = recordByMetadataCache;
		String key = RecordByMetadata.toKey(schemaTypeCode, metadataCode, value);
		constellioIgniteCache.remove(key);
	}
	
	private void clearRecordByMetadataCache(String schemaTypeCode) {
        String sql = "schemaTypeCode = ?";
        SqlQuery<String, RecordByMetadata> sqlQuery = new SqlQuery<>(RecordByMetadata.class, sql);
        sqlQuery.setArgs(schemaTypeCode);

        ConstellioIgniteCache constellioIgniteCache = recordByMetadataCache;
		IgniteCache<String, Object> igniteCache = constellioIgniteCache.getIgniteCache();
        List<Cache.Entry<String, RecordByMetadata>> igniteCacheQueryResults = igniteCache.query(sqlQuery).getAll();
        Set<String> removedKeys = new HashSet<>();
        for (Cache.Entry<String, RecordByMetadata> entry : igniteCacheQueryResults) {
			String recordId = entry.getKey();
			removedKeys.add(recordId);
		}
        if (!removedKeys.isEmpty()) {
        	constellioIgniteCache.removeAll(removedKeys);
        }
	}
	
	private void clearRecordByMetadataCacheForRecord(Record record) {
        String sql = "recordId=?";
        SqlQuery<String, RecordByMetadata> sqlQuery = new SqlQuery<>(RecordByMetadata.class, sql);
        sqlQuery.setArgs(record.getId());

        ConstellioIgniteCache constellioIgniteCache = recordByMetadataCache;
		IgniteCache<String, Object> igniteCache = constellioIgniteCache.getIgniteCache();
        List<Cache.Entry<String, RecordByMetadata>> igniteCacheQueryResults = igniteCache.query(sqlQuery).getAll();
        Set<String> removedKeys = new HashSet<>();
        for (Cache.Entry<String, RecordByMetadata> entry : igniteCacheQueryResults) {
			String recordId = entry.getKey();
			removedKeys.add(recordId);
		}
        if (!removedKeys.isEmpty()) {
        	constellioIgniteCache.removeAll(removedKeys);
        }
	}
	
	public boolean isCached(String id) {
		RecordHolder holder = byIdRecordHoldersCache.get(id);
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

		RecordHolder holder = byIdRecordHoldersCache.get(id);

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
					Record record = get(recordId);
					if (record != null) {
						cachedResults.add(record);
					}
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
					RecordHolder holder = byIdRecordHoldersCache.get(recordCopy.getId());
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
					putInRecordByMetadataCache(previousRecord, recordCopy);
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
				RecordHolder holder = byIdRecordHoldersCache.get(recordCopy.getId());
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
				putInRecordByMetadataCache(previousRecord, recordCopy);
			}

		}
		return insertedRecord;
	}

	private RecordHolder insertRecordIntoAnANewHolder(Record record, CacheConfig cacheConfig) {
		RecordHolder holder = new RecordHolder(record);
		byIdRecordHoldersCache.put(holder.getRecordId(), holder);
		if (cacheConfig.isVolatile()) {
			putInVolatileCache(holder);
		} else {
			putInPermanentCache(holder);
		}

		return holder;
	}

	private void insertRecordIntoAnAlreadyExistingHolder(Record record, CacheConfig cacheConfig, RecordHolder currentHolder) {
		currentHolder.set(record);

		if (currentHolder.record == null && cacheConfig.isVolatile()) {
			putInVolatileCache(currentHolder);
		} else if (cacheConfig.isPermanent()) {
			putInPermanentCache(currentHolder);
		}
	}

	@Override
	public synchronized void invalidateRecordsOfType(String recordType) {
		CacheConfig cacheConfig = cachedTypes.get(recordType);
		String schemaTypeCode = cacheConfig.getSchemaType();
		clearByIdCache(schemaTypeCode);
		if (cacheConfig.isVolatile()) {
			clearVolatileCache(schemaTypeCode);
		} else {
			clearPermanentCache(schemaTypeCode);
		}
		clearRecordByMetadataCache(schemaTypeCode);
		clearQueryResults(schemaTypeCode);
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
			RecordHolder holder = byIdRecordHoldersCache.get(recordId);
			if (holder != null && holder.record != null) {
				CacheConfig cacheConfig = getCacheConfigOf(holder.record.getSchemaCode());
				clearRecordByMetadataCacheForRecord(holder.record);
				holder.invalidate();

				if (cacheConfig != null && cacheConfig.isPermanent()) {
					String schemaTypeCode = cacheConfig.getSchemaType();
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

		cachedMetadatasBySchemaType.put(schemaTypeCode, cacheConfig.getIndexes());
		
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
		queryResultsCache.clear();
		byIdRecordHoldersCache.clear();
		permanentRecordHoldersCache.clear();
		volatileRecordHoldersCache.clear();
		recordByMetadataCache.clear();
	}

	@Override
	public Record getByMetadata(Metadata metadata, String value) {
		String schemaTypeCode = schemaUtils.getSchemaTypeCode(metadata);

		Record foundRecord = null;
		if (cachedTypes.containsKey(schemaTypeCode)) {
			foundRecord = getRecordByMetadataValue(schemaTypeCode, metadata, value);
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
		int cacheTotalSize = byIdRecordHoldersCache.size();
		
		cacheTotalSize += 1 + queryResultsCache.size();

		cacheTotalSize += 1 + permanentRecordHoldersCache.size();

		cacheTotalSize += 1 + volatileRecordHoldersCache.size();
		
		cacheTotalSize += 1 + recordByMetadataCache.size();

		for (CacheConfig aCacheConfig : cachedTypes.values()) {
			cacheTotalSize += 1 + aCacheConfig.getIndexes().size();
		}

		return cacheTotalSize;
	}
	
	static class RecordByMetadata implements Serializable {
		
		private String key;

	    @QuerySqlField(index = true)
		private String recordId;

	    @QuerySqlField(index = true)
		private String schemaTypeCode;

	    @QuerySqlField(index = true)
		private String metadataCode;

	    @QuerySqlField(index = true)
	    private String value;
	    
	    RecordByMetadata() {
	    	super();
	    }
	    
		RecordByMetadata(String recordId, String schemaTypeCode, String metadataCode, String value) {
			this.recordId = recordId;
			this.schemaTypeCode = schemaTypeCode;
			this.metadataCode = metadataCode;
			this.value = value;
			this.key = toKey(schemaTypeCode, metadataCode, value);
		}
		
		static String toKey(String schemaTypeCode, String metadataCode, String value) {
			return schemaTypeCode + "_" + metadataCode + "_" + value;
		}
		
		String getKey() {
			return key;
		}
		
		String getRecordId() {
			return recordId;
		}
		
		String getSchemaTypeCode() {
			return schemaTypeCode;
		}

		String getMetadataCode() {
			return metadataCode;
		}
		
	}
	
	static class QueryResultsHolder implements Serializable {

	    @QuerySqlField(index = true)
		private String schemaTypeCode;

	    @QuerySqlField(index = true)
		private String querySignature;
		
		private List<String> results;
		
		QueryResultsHolder() {
			super();
		}
		
		QueryResultsHolder(String schemaTypeCode, String querySignature, List<String> results) {
			this.schemaTypeCode = schemaTypeCode;
			this.querySignature = querySignature;
			this.results = results;
		}
		
		String getSchemaTypeCode() {
			return schemaTypeCode;
		}
		
		String getQuerySignature() {
			return querySignature;
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
		
		RecordHolder() {
			super();
		}

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

	@Override
	public int getCacheObjectsCount(String typeCode) {
		CacheConfig cacheConfig = getCacheConfigOf(typeCode);
		if (cacheConfig != null && cacheConfig.isVolatile()) {
			return getVolatileRecordHoldersCount(typeCode);
		} else if (cacheConfig != null && cacheConfig.isPermanent()) {
			return getPermanentRecordHoldersCount(typeCode);
		} else {
			return 0;
		}
	}

	@Override
	public long getCacheObjectsSize(String typeCode) {
		return 0;
	}

}
