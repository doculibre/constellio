package com.constellio.model.services.records.cache;

import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.dto.records.RecordDTOMode;
import com.constellio.data.dao.dto.records.SolrRecordDTO;
import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.dao.services.Stats;
import com.constellio.data.dao.services.cache.InsertionReason;
import com.constellio.data.utils.Holder;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.data.utils.LangUtils;
import com.constellio.data.utils.LangUtils.ListComparisonResults;
import com.constellio.data.utils.LazyIterator;
import com.constellio.data.utils.ParallelIterator;
import com.constellio.data.utils.ThreadList;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.data.utils.systemLogger.SystemLogger;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.entities.CollectionInfo;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.RecordCacheType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.factories.ModelPostInitializationParams;
import com.constellio.model.services.records.RecordId;
import com.constellio.model.services.records.RecordImpl;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordUtils;
import com.constellio.model.services.records.cache.ByteArrayRecordDTO.ByteArrayRecordDTOWithIntegerId;
import com.constellio.model.services.records.cache.ByteArrayRecordDTO.ByteArrayRecordDTOWithStringId;
import com.constellio.model.services.records.cache.CacheRecordDTOUtils.CacheRecordDTOBytesArray;
import com.constellio.model.services.records.cache.cacheIndexConditions.SortedIdsStreamer;
import com.constellio.model.services.records.cache.cacheIndexHook.MetadataIndexCacheDataStoreHook;
import com.constellio.model.services.records.cache.cacheIndexHook.RecordCountHookDataIndexRetriever;
import com.constellio.model.services.records.cache.cacheIndexHook.RecordIdsHookDataIndexRetriever;
import com.constellio.model.services.records.cache.dataStore.CollectionSchemaTypeObjectHolder;
import com.constellio.model.services.records.cache.dataStore.FileSystemRecordsValuesCacheDataStore;
import com.constellio.model.services.records.cache.dataStore.RecordsCachesDataStore;
import com.constellio.model.services.records.cache.hooks.DeterminedHookCacheInsertion;
import com.constellio.model.services.records.cache.hooks.HookCacheInsertionResponse;
import com.constellio.model.services.records.cache.hooks.RecordsCachesHook;
import com.constellio.model.services.records.cache.hooks.RecordsCachesHooks;
import com.constellio.model.services.records.cache.offHeapCollections.OffHeapMemoryAllocator;
import com.constellio.model.services.schemas.MetadataSchemaProvider;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.MetadataSchemasManagerListener;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.SearchServices.RecordIdVersion;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.search.VisibilityStatusFilter;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.QueryExecutionMethod;
import com.constellio.model.utils.Lazy;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joda.time.LocalDateTime;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.VM;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.constellio.data.dao.dto.records.RecordDTOMode.CUSTOM;
import static com.constellio.data.dao.dto.records.RecordDTOMode.FULLY_LOADED;
import static com.constellio.data.dao.services.cache.InsertionReason.LOADING_CACHE;
import static com.constellio.data.utils.LangUtils.compare;
import static com.constellio.data.utils.ThreadList.running;
import static com.constellio.model.entities.schemas.Schemas.COLLECTION;
import static com.constellio.model.entities.schemas.Schemas.SCHEMA;
import static com.constellio.model.services.records.cache.CacheRecordDTOUtils.convertDTOToByteArrays;
import static com.constellio.model.services.records.cache.MassiveCacheInvalidationReason.KEEP_INTEGRITY;
import static com.constellio.model.services.records.cache.hooks.DeterminedHookCacheInsertion.DEFAULT_INSERT;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class RecordsCaches2Impl implements RecordsCaches, StatefulService {

	private static final Logger LOGGER = LoggerFactory.getLogger(RecordsCaches2Impl.class);

	private Map<String, RecordsCache> collectionCaches = new HashMap<>();

	private RecordsCachesHooks hooks;
	protected ModelLayerFactory modelLayerFactory;
	MetadataSchemasManager metadataSchemasManager;
	private CollectionsListManager collectionsListManager;

	private FileSystemRecordsValuesCacheDataStore fileSystemDataStore;
	private RecordsCachesDataStore memoryDataStore;
	private DB memoryDiskDatabase;
	protected VolatileCache volatileCache;
	private MetadataIndexCacheDataStore metadataIndexCacheDataStore;
	private boolean fullyPermanentInitialized;
	private boolean summaryPermanentInitialized;
	private Lazy<RecordServices> recordServices;
	private CollectionSchemaTypeObjectHolder<Boolean> schemaTypeLoadedStatuses
			= new CollectionSchemaTypeObjectHolder<>(() -> false);

	private boolean summaryCacheInitialized;

	private CacheLoadingProgression cacheLoadingProgression;

	public RecordsCaches2Impl(ModelLayerFactory modelLayerFactory,
							  FileSystemRecordsValuesCacheDataStore fileSystemDataStore,
							  RecordsCachesDataStore memoryDataStore) {
		this.modelLayerFactory = modelLayerFactory;
		this.collectionsListManager = modelLayerFactory.getCollectionsListManager();
		this.metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
		this.fileSystemDataStore = fileSystemDataStore;
		SummaryCacheSingletons.dataStore.put(modelLayerFactory.getInstanceId(), fileSystemDataStore);
		this.memoryDataStore = memoryDataStore;
		this.recordServices = new Lazy<RecordServices>() {
			@Override
			protected RecordServices load() {
				return modelLayerFactory.newRecordServices();
			}
		};

		this.memoryDiskDatabase = DBMaker.memoryDB().make();
		this.hooks = new RecordsCachesHooks(modelLayerFactory);
		this.metadataIndexCacheDataStore = new MetadataIndexCacheDataStore(modelLayerFactory);

		ScheduledExecutorService executor =
				Executors.newScheduledThreadPool(2);

		//Maximum 50K records or 100mo
		volatileCache = new VolatileCache(memoryDiskDatabase
				.hashMap("volatileCache")
				.keySerializer(Serializer.STRING)
				.valueSerializer(Serializer.JAVA)
				//.expireMaxSize(50000)
				//.expireExecutorPeriod(1)
				.expireStoreSize(modelLayerFactory.getConfiguration().getRecordsVolatileCacheMemorySize())
				//				.expireExecutor(executor)
				//				.expireMaxSize(15)
				.expireAfterGet()
				.expireAfterCreate()
				//.expireStoreSize(modelLayerFactory.getConfiguration().getRecordsVolatileCacheMemorySize())
				.create());

	}

	@Override
	public void enableVolatileCache() {
		volatileCache.setEnabled(true);
	}

	@Override
	public void disableVolatileCache() {
		volatileCache.setEnabled(false);

	}

	@Override
	public MetadataIndexCacheDataStore getMetadataIndexCacheDataStore() {
		return metadataIndexCacheDataStore;
	}

	public RecordsCachesDataStore getRecordsCachesDataStore() {
		return memoryDataStore;
	}


	public void register(RecordsCachesHook hook) {
		hooks.register(hook);
	}


	protected void reload(byte collectionId, String collection, List<String> schemaTypes,
						  boolean onlyLocally, boolean forceVolatileCacheClear) {

		boolean clearVolatileCache = forceVolatileCacheClear;
		for (String schemaTypeCode : schemaTypes) {
			MetadataSchemaType schemaType = metadataSchemasManager.getSchemaTypes(collection).getSchemaType(schemaTypeCode);
			schemaTypeLoadedStatuses.set(schemaType, false);
			if (schemaType.getCacheType().hasVolatileCache()) {
				clearVolatileCache = true;
			}

			memoryDataStore.invalidate(schemaType.getCollectionInfo().getCollectionId(), schemaType.getId(), (r) -> true);
			metadataIndexCacheDataStore.clear(schemaType);

			reloadSchemaType(collectionId, collection, schemaType.getCode(), true);
		}

		if (clearVolatileCache) {
			invalidateVolatile(KEEP_INTEGRITY);
		}

	}

	@Override
	public RecordsCache getCache(String collection) {

		RecordsCache cache = collectionCaches.get(collection);
		if (cache == null) {
			synchronized (this) {
				cache = collectionCaches.get(collection);
				if (cache == null) {
					byte collectionId = modelLayerFactory.getCollectionsListManager().getCollectionInfo(collection).getCollectionId();
					cache = new RecordsCache2Impl(collection, collectionId, this);
					collectionCaches.put(collection, cache);
				}
			}
		}

		return cache;
	}

	public CacheInsertionResponse insert(Record record, InsertionReason insertionReason) {

		CacheInsertionStatus problemo = validateInsertable(record, insertionReason);
		if (problemo != null) {
			return new CacheInsertionResponse(problemo, null, DEFAULT_INSERT);
		}

		RecordDTO current = insertionReason == LOADING_CACHE ? null : memoryDataStore.get(record.getId());
		if (current != null && current.getVersion() > record.getVersion()) {
			return new CacheInsertionResponse(CacheInsertionStatus.REFUSED_OLD_VERSION, null, DEFAULT_INSERT);
		}
		if (current instanceof ByteArrayRecordDTO) {
			//Since persisted values will change during cache insert, we copy the current status of the DTO in a SolrRecordDTO
			current = new SolrRecordDTO(current);
		}

		MetadataSchemaTypes schemaTypes = metadataSchemasManager.getSchemaTypes(record);
		MetadataSchemaType schemaType = schemaTypes.getSchemaType(record.getTypeCode());

		RecordsCachesHook hook = hooks.getSchemaTypeHook(schemaTypes, schemaType.getId());
		DeterminedHookCacheInsertion insertion = DeterminedHookCacheInsertion.DEFAULT_INSERT;
		HookCacheInsertionResponse hookInsertionResponse = null;
		if (hook != null) {
			insertion = hook.determineCacheInsertion(record, schemaTypes);
			if (insertion.isInsertingUsingHook()) {
				hookInsertionResponse = hook.insert(record, schemaTypes, insertionReason);
			}
		}

		if (schemaType.getCacheType().hasVolatileCache()) {
			if (insertion.isContinuingVolatileCacheInsertion()) {
				insertInVolatileCache(record);
			} else {
				volatileCache.remove(record.getId());
			}
		}

		if (schemaType.getCacheType().hasPermanentCache()) {
			if (insertion.isContinuingPermanentCacheInsertion()) {
				Record oldRecord = null;
				if (current != null) {
					oldRecord = toRecord(schemaType, current);
				}

				return insertInPermanentCache(oldRecord, record, schemaType, insertion, insertionReason);
			} else {
				if (current != null) {
					memoryDataStore.remove(current);
				}
				return new CacheInsertionResponse(hookInsertionResponse.getStatus(), null, insertion);
			}
		}

		return hookInsertionResponse == null ? new CacheInsertionResponse(CacheInsertionStatus.REFUSED_NOT_CACHED, null, insertion)
											 : new CacheInsertionResponse(hookInsertionResponse.getStatus(), null, insertion);

	}

	@NotNull
	private CacheInsertionResponse insertInPermanentCache(Record oldRecord, Record record,
														  MetadataSchemaType schemaType,
														  DeterminedHookCacheInsertion insertion,
														  InsertionReason insertionReason) {
		if (schemaType.getCacheType() == RecordCacheType.FULLY_CACHED) {

			RecordDTO dto = ((RecordImpl) record).getRecordDTO();
			if (dto.getLoadingMode() != RecordDTOMode.FULLY_LOADED) {
				LOGGER.error("Record '" + record.getId() + "' of type should not exist in summary state, since it is fully cached");
				return new CacheInsertionResponse(CacheInsertionStatus.REFUSED_NOT_FULLY_LOADED, null, insertion);
			}

			MetadataSchema metadataSchema = oldRecord != null ? schemaType.getSchema(oldRecord.getSchemaCode()) : schemaType.getSchema(record.getSchemaCode());

			metadataIndexCacheDataStore.addUpdate(oldRecord, record, schemaType, metadataSchema);
			memoryDataStore.insert(dto);
			return new CacheInsertionResponse(CacheInsertionStatus.ACCEPTED, null, insertion);


		} else if (schemaType.getCacheType().isSummaryCache()) {
			RecordDTO dto = null;

			MetadataSchema metadataSchema = oldRecord != null ? schemaType.getSchema(oldRecord.getSchemaCode()) : schemaType.getSchema(record.getSchemaCode());

			if (insertionReason == LOADING_CACHE && record.getRecordDTO() instanceof ByteArrayRecordDTO) {
				dto = record.getRecordDTO();
			} else {
				int sortValue = ByteArrayRecordDTO.MAIN_SORT_UNDEFINED;
				if (oldRecord != null) {
					if (LangUtils.isEqual(oldRecord.get(Schemas.TITLE), record.get(Schemas.TITLE))) {
						sortValue = ByteArrayRecordDTO.MAIN_SORT_UNCHANGED;
					}
				}

				dto = toPersistedSummaryRecordDTO(record, insertionReason, sortValue);
			}


			metadataIndexCacheDataStore.addUpdate(oldRecord, record, schemaType, metadataSchema);
			memoryDataStore.insert(dto);
			return new CacheInsertionResponse(CacheInsertionStatus.ACCEPTED, dto, insertion);

		} else {
			return new CacheInsertionResponse(CacheInsertionStatus.REFUSED_NOT_CACHED, null, insertion);
		}
	}

	private void insertInVolatileCache(Record record) {
		if (record.getRecordDTOMode() != FULLY_LOADED) {
			volatileCache.remove(record.getId());

		} else {
			RecordDTO volatileCacheDto = volatileCache.get(record.getId());
			if (volatileCacheDto == null || volatileCacheDto.getVersion() < record.getVersion()) {
				volatileCache.put(record.getId(), ((RecordImpl) record).getRecordDTO());
			}

		}
	}

	@Nullable
	private CacheInsertionStatus validateInsertable(Record record, InsertionReason insertionReason) {
		if (record == null) {
			return CacheInsertionStatus.REFUSED_NULL;
		}

		if (((RecordImpl) record).getRecordDTO().getLoadingMode() == CUSTOM && insertionReason != LOADING_CACHE) {
			return CacheInsertionStatus.REFUSED_NOT_FULLY_LOADED;
		}

		//TODO Remove coupling!
		if (!"savedSearch".equals(record.getTypeCode())) {
			if (!record.isSaved()) {
				return CacheInsertionStatus.REFUSED_UNSAVED;
			}

			if (record.isDirty()) {
				return CacheInsertionStatus.REFUSED_DIRTY;
			}
		}
		return null;
	}

	@Override
	public Record getRecord(RecordId id, String optionnalCollection, String optionnalSchemaType) {

		RecordDTO recordDTO = memoryDataStore.get(id);
		Record returnedRecord = null;
		if (recordDTO != null) {
			String collectionCode = (String) recordDTO.getFields().get(COLLECTION.getDataStoreCode());
			String schemaCode = (String) recordDTO.getFields().get(SCHEMA.getDataStoreCode());

			MetadataSchemaTypes schemaTypes = metadataSchemasManager.getSchemaTypes(collectionCode);
			MetadataSchemaType schemaType = schemaTypes.getSchemaType(SchemaUtils.getSchemaTypeCode(schemaCode));

			if (schemaType.getCacheType().isSummaryCache()) {

				if (schemaType.getCacheType().hasVolatileCache()) {
					recordDTO = volatileCache.get(id.stringValue());
					returnedRecord = recordDTO == null ? null : toRecord(schemaType, recordDTO);
				}

			} else {
				returnedRecord = toRecord(schemaType, recordDTO);
			}
		} else {
			recordDTO = volatileCache.get(id.stringValue());
			if (recordDTO != null) {
				String collectionCode = (String) recordDTO.getFields().get(COLLECTION.getDataStoreCode());
				String schemaCode = (String) recordDTO.getFields().get(SCHEMA.getDataStoreCode());

				MetadataSchemaTypes schemaTypes = metadataSchemasManager.getSchemaTypes(collectionCode);
				MetadataSchemaType schemaType = schemaTypes.getSchemaType(SchemaUtils.getSchemaTypeCode(schemaCode));
				returnedRecord = toRecord(schemaType, recordDTO);
			}
		}


		if (returnedRecord == null) {
			if (optionnalCollection != null && optionnalSchemaType != null) {
				MetadataSchemaTypes schemaTypes = metadataSchemasManager.getSchemaTypes(optionnalCollection);
				MetadataSchemaType schemaType = schemaTypes.getSchemaType(optionnalSchemaType);
				RecordsCachesHook hook = hooks.getSchemaTypeHook(schemaTypes, schemaType.getId());
				if (hook != null) {
					returnedRecord = hook.getById(id);
				}
			} else {
				for (RecordsCachesHook hook : hooks.getRegisteredHooks()) {
					Record record = hook.getById(id);
					if (record != null) {
						returnedRecord = record;
					}
				}
			}
		}

		return returnedRecord;
	}

	public Record getRecordSummary(RecordId id, String optionnalCollection, String optionnalSchemaType) {
		RecordDTO recordDTO = memoryDataStore.get(id);
		Record returnedRecord = null;
		if (recordDTO != null) {
			String collectionCode = (String) recordDTO.getFields().get(COLLECTION.getDataStoreCode());

			if (optionnalCollection == null || collectionCode.equals(optionnalCollection)) {
				returnedRecord = toRecord(recordDTO);
			}
		}


		if (returnedRecord == null) {

			if (optionnalCollection != null && optionnalSchemaType != null) {
				MetadataSchemaTypes schemaTypes = metadataSchemasManager.getSchemaTypes(optionnalCollection);
				MetadataSchemaType schemaType = schemaTypes.getSchemaType(optionnalSchemaType);
				RecordsCachesHook hook = hooks.getSchemaTypeHook(schemaTypes, schemaType.getId());
				if (hook != null) {
					returnedRecord = hook.getById(id);
				}
			} else {
				for (RecordsCachesHook hook : hooks.getRegisteredHooks()) {
					Record record = hook.getById(id);
					if (record != null) {
						returnedRecord = record;
					}
				}
			}

			if (returnedRecord != null && returnedRecord.getLoadedFieldsMode() == FULLY_LOADED) {
				RecordId recordId = returnedRecord.getRecordId();
				int mainSortValue = memoryDataStore.getMainSortValue(recordId);

				returnedRecord = toRecord(toPersistedSummaryRecordDTO(returnedRecord, null, mainSortValue));
			}
		}

		return returnedRecord;
	}

	//	@Override
	//	public synchronized void reload(List<String> schemaTypes) {
	//		volatileCache.clear();
	//		for (String collection : modelLayerFactory.getCollectionsListManager().getCollections()) {
	//			for (String schemaTypeCode : schemaTypes) {
	//				MetadataSchemaType schemaType = metadataSchemasManager.getSchemaTypes(collection).getSchemaType(schemaTypeCode);
	//
	//				memoryDataStore.removeRecordsOfCollection(schemaType.getCollectionInfo().getCollectionId(), schemaType.getId(), (R) -> true);
	//				loadSchemaType(schemaType);
	//			}
	//
	//		}
	//	}

	@Override
	public void removeRecordsOfCollection(String collection, boolean onlyLocally) {
		volatileCache.clear();
		CollectionInfo collectionInfo = modelLayerFactory.getCollectionsListManager().getCollectionInfo(collection);
		byte collectionId = collectionInfo.getCollectionId();
		metadataIndexCacheDataStore.clear(collectionInfo);
		memoryDataStore.invalidateAll(collectionId);
	}

	@Override
	public Stream<Record> stream(MetadataSchemaType type) {
		return memoryDataStore.stream(type.getCollectionInfo().getCollectionId(), type.getId()).map(this::toRecord);
	}


	@Override
	public Stream<Record> stream(SortedIdsStreamer streamer) {

		final Iterator<RecordId> recordIdStream = streamer.iterator();
		return LangUtils.stream(new LazyIterator<Record>() {
			@Override
			protected Record getNextOrNull() {
				if (recordIdStream.hasNext()) {
					Record record = getRecord(recordIdStream.next());
					if (record == null) {
						record = getNextOrNull();
					}
					return record;
				} else {
					return null;
				}
			}
		});
	}


	@Override
	public Stream<Record> stream(String collection) {
		CollectionInfo collectionInfo = this.metadataSchemasManager.getSchemaTypes(collection).getCollectionInfo();
		return memoryDataStore.stream(collectionInfo.getCollectionId()).map(this::toRecord);
	}

	@Override
	public Stream<Record> stream() {
		return memoryDataStore.stream().map(this::toRecord);
	}

	@Override
	public boolean isCacheInitialized(MetadataSchemaType schemaType) {
		return schemaTypeLoadedStatuses.get(schemaType, true);
	}

	@Override
	public boolean areSummaryCachesInitialized() {
		return summaryCacheInitialized;
	}

	@Override
	public void reloadAllSchemaTypes(String collection) {
		MetadataSchemaTypes schemaTypes = this.metadataSchemasManager.getSchemaTypes(collection);
		for (MetadataSchemaType schemaType : schemaTypes.getSchemaTypes()) {
			if (schemaType.getCacheType() != RecordCacheType.NOT_CACHED) {
				reloadSchemaType(schemaTypes.getCollectionInfo().getCollectionId(), collection, schemaType.getCode(), false);
			}
		}
	}

	@Override
	public Stream<Record> getRecordsByIndexedMetadata(MetadataSchemaType schemaType, Metadata metadata, String value) {
		return getRecordWithMetadataValue(schemaType, metadata, value, false);
	}

	@Override
	public Stream<Record> getRecordsSummaryByIndexedMetadata(MetadataSchemaType schemaType, Metadata metadata,
															 String value) {
		return getRecordWithMetadataValue(schemaType, metadata, value, true);
	}

	private Stream<Record> getRecordWithMetadataValue(MetadataSchemaType schemaType,
													  Metadata metadata, String value,
													  boolean summary) {
		if (metadata.isSameLocalCode(Schemas.IDENTIFIER)) {
			Record record = getRecord(value, metadata.getCollection(), null);
			if (record == null) {
				return Stream.empty();
			}
		}

		if (metadata.getCollection() != null && !metadata.getCollection().equals(schemaType.getCollection())) {
			throw new ImpossibleRuntimeException("Searching with a metadata from collection '" + metadata.getCollection() + "' in cache of collection '" + schemaType.getCollection() + "'");
		}


		if (metadata.isSameLocalCode(Schemas.IDENTIFIER)) {
			Record record;
			if (schemaType.getCacheType().isSummaryCache()) {
				record = getRecordSummary(value);
			} else {
				record = getRecord(value);
			}

			if (record == null) {
				return Stream.empty();
			} else {
				return Stream.of(record);
			}
		}
		//if (schemaType.getCacheType() == RecordCacheType.FULLY_CACHED) {
		List<String> potentialIds = metadataIndexCacheDataStore.search(schemaType, metadata, value);

		if (potentialIds != null && !potentialIds.isEmpty()) {
			return potentialIds.stream().map((id) -> {
				return toRecord(schemaType, memoryDataStore.get(id));
			}).filter((r) -> r != null && metadata.isMultivalue() ? r.getList(metadata).contains(value) : value.equals(r.get(metadata)));
		} else {
			return Stream.empty();
		}

		//		} else {
		//			throw new ImpossibleRuntimeException("getByMetadata cannot be used for schema type '" + schemaType.getCode() + "' which is not fully cached. If the schema type has a summary cache, try using getSummaryByMetadata instead");
		//		}
	}


	private List<MetadataSchemaType> loadSummaryPermanentCache(String collection) {

		List<MetadataSchemaType> schemaTypes = metadataSchemasManager.getSchemaTypes(collection).getSchemaTypes();
		List<MetadataSchemaType> typesToLoadAsync = new ArrayList<>();
		SearchServices searchServices = modelLayerFactory.newSearchServices();
		for (MetadataSchemaType schemaType : schemaTypes) {
			if (schemaType.getCacheType().isSummaryCache() && schemaType.getCacheType().hasPermanentCache()) {
				long count = searchServices.streamFromSolr(schemaType, schemaType.getCacheType().isSummaryCache()).count();
				//TODO improve with extension
				if (count > 0) {
					if (count <= 10_000 && !FoldersLocator.usingAppWrapper()) {
						loadSchemaType(schemaType, !fileSystemDataStore.isRecreated());

					} else {
						typesToLoadAsync.add(schemaType);
					}
				} else {
					schemaTypeLoadedStatuses.set(schemaType, true);
				}
			}

		}
		return typesToLoadAsync;
	}


	private void loadFullyPermanentCache(String collection) {

		List<MetadataSchemaType> schemaTypes = metadataSchemasManager.getSchemaTypes(collection).getSchemaTypes();
		schemaTypes.forEach((schemaType) -> {
			if (schemaType.getCacheType() == RecordCacheType.FULLY_CACHED) {
				loadSchemaType(schemaType, false);
			}
		});

	}

	private void loadSchemaType(MetadataSchemaType type, boolean usePersistedSummaryDatabase) {

		SearchServices searchServices = modelLayerFactory.newSearchServices();

		AtomicInteger added = new AtomicInteger();

		long count = searchServices.streamFromSolr(type, type.getCacheType().isSummaryCache()).count();
		String stepName = "Loading '" + type.getCode() + "' of collection '" + type.getCollection() + "'";
		cacheLoadingProgression = new CacheLoadingProgression(stepName, 0, count);
		if (count > 0) {

			boolean loadUsingSolr = true;
			if (usePersistedSummaryDatabase && !modelLayerFactory.getConfiguration().isForcingCacheLoadingFromSolr()) {

				Iterator<RecordIdVersion> recordIdVersionIterator = getRecordIdVersionIteratorFromSolr(type, searchServices);
				try {
					insertRecordsUsingMapDb(type, added, count, recordIdVersionIterator);
					loadUsingSolr = false;
				} catch (LoadingUsingMapDBAbortedException ignored) {
					LOGGER.info("Cache loading using Map.DB was aborted, restarting using solr...");
				}

			}

			if (loadUsingSolr) {
				LOGGER.info("Loading records of schema type " + type.getCode() + " from Solr");
				searchServices.streamFromSolr(type, type.getCacheType().isSummaryCache()).parallel().forEach((record) -> {
					CacheInsertionResponse response = (insert(record, LOADING_CACHE));

					int inserted = added.incrementAndGet();
					if (inserted % 100_000 == 0 || inserted == count) {

						long mb = OffHeapMemoryAllocator.getAllocatedMemory() / (1024 * 1024);
						LOGGER.info("Adding records " + record.getTypeCode() + " : " + inserted + "/" + count
									+ " (" + mb + "mb loaded in memory / " + VM.maxDirectMemory() + ")");
						cacheLoadingProgression = new CacheLoadingProgression(stepName, inserted, count);
					}
					if (response.getStatus() != CacheInsertionStatus.ACCEPTED) {
						LOGGER.warn("Could not load record '" + record.getId() + "' in cache : " + response.getStatus());
					}
				});
			}
		}

		schemaTypeLoadedStatuses.set(type, true);
	}

	private void insertRecordsUsingMapDb(MetadataSchemaType type, AtomicInteger added, long count,
										 Iterator<RecordIdVersion> recordIdVersionIterator)
			throws LoadingUsingMapDBAbortedException {

		try {
			int threadCount = 1;
			LinkedBlockingQueue<Holder<Record>> recordsQueue = new LinkedBlockingQueue<>(1000);

			ThreadList<Thread> threadList = running(threadCount, () -> {
				boolean finished = false;
				String stepName = "Loading '" + type.getCode() + "' of collection '" + type.getCollection() + "'";
				while (!finished) {
					try {
						Holder<Record> recordHolder = recordsQueue.take();

						if (recordHolder.get() == null) {
							finished = true;
						} else {
							Record record = recordHolder.get();
							CacheInsertionResponse response = (insert(record, LOADING_CACHE));

							int inserted = added.incrementAndGet();
							if (inserted % 10000 == 0 || inserted == count) {

								long mb = OffHeapMemoryAllocator.getAllocatedMemory() / (1024 * 1024);
								LOGGER.info("Adding records " + record.getTypeCode() + " : " + inserted + "/" + count
											+ " (" + mb + "mb loaded in memory / " + VM.maxDirectMemory() + ")");

								cacheLoadingProgression = new CacheLoadingProgression(stepName, inserted, count);
							}
							if (response.getStatus() != CacheInsertionStatus.ACCEPTED) {
								LOGGER.warn("Could not load record '" + record.getId() + "' in cache : " + response.getStatus());
							}

						}

					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
				}

			}).startAll();

			boolean aborted = false;

			int problems = 0;
			int success = 0;

			while (recordIdVersionIterator.hasNext() && !aborted) {
				RecordIdVersion idVersion = recordIdVersionIterator.next();
				Record record = null;
				if (idVersion.getRecordId().isInteger()) {
					try {
						RecordDTO recordDTO = fileSystemDataStore.loadRecordDTOIfVersion(
								idVersion.getRecordId().intValue(), idVersion.getVersion(), metadataSchemasManager, type);
						if (recordDTO != null) {
							MetadataSchema schema = metadataSchemasManager.getSchemaOf(recordDTO);
							record = new RecordImpl(schema, recordDTO);
						}

					} catch (Throwable t) {
						t.printStackTrace();
					}
				}

				if (record == null) {
					problems++;
					record = recordServices.get().realtimeGetRecordById(idVersion.getRecordId());

					if (problems > 5000 && ((problems) * 100 / ((problems) + success)) > 25) {
						//25% of records are problematic, it's better to abort and use solr
						aborted = true;
					}
				} else {
					success++;
				}

				recordsQueue.put(new Holder<>(record));

			}
			for (int i = 0; i < threadCount; i++) {
				recordsQueue.put(new Holder<>(null));

			}
			threadList.joinAll();

			if (aborted) {
				throw new LoadingUsingMapDBAbortedException();
			}

		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	@NotNull
	private Iterator<RecordIdVersion> getRecordIdVersionIteratorFromSolr(MetadataSchemaType type,
																		 SearchServices searchServices) {
		Iterator<RecordIdVersion> recordIdVersionIterator;
		LOGGER.info("Loading records of schema type " + type.getCode() + " from locally persited database");
		recordIdVersionIterator = searchServices.recordsIdVersionIteratorUsingSolr(type);
		int batchSize = 10_000;
		recordIdVersionIterator = new ParallelIterator<>(recordIdVersionIterator, batchSize);
		return recordIdVersionIterator;
	}

	@NotNull
	private Iterator<RecordIdVersion> getRecordIdVersionIteratorFromMapDb(MetadataSchemaType type,
																		  SearchServices searchServices) {
		Iterator<RecordIdVersion> recordIdVersionIterator;
		LogicalSearchQuery query = new LogicalSearchQuery(from(type).returnAll());
		query.setQueryExecutionMethod(QueryExecutionMethod.USE_SOLR);
		query.sortDesc(Schemas.MODIFIED_ON);
		query.setNumberOfRows(100_000);
		query.setReturnedMetadatas(ReturnedMetadatasFilter.onlyMetadatas(Schemas.MODIFIED_ON));
		query.filteredByStatus(StatusFilter.ALL);
		query.filteredByVisibilityStatus(VisibilityStatusFilter.ALL);

		List<Record> last100K = searchServices.search(query);

		if (last100K.size() < 100_000) {
			recordIdVersionIterator = last100K.stream().map((r) -> new RecordIdVersion(r.getRecordId(), r.getVersion()))
					.collect(toList()).iterator();

		} else {

			LocalDateTime localDateBeforeConsideringSuspicious =
					last100K.get(last100K.size() - 1).get(Schemas.MODIFIED_ON);
			Set<RecordId> ids = last100K.stream().map((r) -> r.getRecordId()).collect(toSet());

			List<RecordIdVersion> idVersions = this.fileSystemDataStore
					.retrieveIdVersionForRecordOfType(metadataSchemasManager, type,
							localDateBeforeConsideringSuspicious, ids);

			last100K.stream().forEach((r) -> idVersions.add(new RecordIdVersion(r.getRecordId(), r.getVersion())));

			idVersions.sort(comparing(RecordIdVersion::getRecordId));

			recordIdVersionIterator = idVersions.iterator();

		}
		return recordIdVersionIterator;
	}


	@Deprecated
	protected List<Record> getAllValuesInUnmodifiableState(byte collectionId, String collection, String schemaType) {
		short typeId = metadataSchemasManager.getSchemaTypes(collectionId).getSchemaType(schemaType).getId();
		return memoryDataStore.stream(collectionId, typeId).map(this::toRecord).collect(toList());
	}

	@Override
	public void initialize() {
		CacheRecordDTOUtils.startCompilingDTOsStats();
		for (String collection : modelLayerFactory.getCollectionsListManager().getCollections()) {
			LOGGER.info("Loading cache of '" + collection);
			loadFullyPermanentCache(collection);
		}

		metadataSchemasManager.registerListener(new MetadataSchemasManagerListener() {
			@Override
			public void onCollectionSchemasModified(String collection) {
				MetadataSchemaTypes types = metadataSchemasManager.getSchemaTypes(collection);
				metadataIndexCacheDataStore.onTypesModified(types);
			}
		});

		fullyPermanentInitialized = true;
	}

	public void updateRecordsMainSortValue() {
		memoryDataStore.setRecordsMainSortValue(recordsIdSortedByTheirDefaultSort());
		cacheLoadingProgression = null;
	}

	public List<RecordId> recordsIdSortedByTheirDefaultSort() {

		//Trier par code s'il n'y a pas ddv dans le type de schéma
		//Sinon par titre

		List<RecordId> returnedIds = new ArrayList<>();

		for (String collection : collectionsListManager.getCollections()) {
			for (MetadataSchemaType schemaType : metadataSchemasManager.getSchemaTypes(collection).getSchemaTypesInDisplayOrder()) {

				if (schemaType.getMainSortMetadata() != null) {

					boolean useTupleStream = modelLayerFactory.getSystemConfigs().isRunningWithSolr6()
											 && modelLayerFactory.getDataLayerFactory().getDataLayerConfiguration()
													 .useSolrTupleStreamsIfSupported();

					String stepName = "Loading sort values of '" + schemaType.getCode() + "' of collection '" + schemaType.getCollection() + "'"
									  + (useTupleStream ? " (using tuple streams)" : " (using iterator)");

					final long total = modelLayerFactory.newSearchServices().getResultsCount(from(schemaType).returnAll());
					Consumer<Integer> progressionConsumer = (current) -> {

						if (current % 10000 == 0 || current == total) {
							cacheLoadingProgression = new CacheLoadingProgression(stepName, current, total);
						}
						if (current % 50000 == 0 || current == total) {
							LOGGER.info(stepName + " - " + current + "/" + total);
						}
					};


					if (useTupleStream) {
						returnedIds.addAll(modelLayerFactory.newSearchServices()
								.recordsIdSortedByTitleUsingTupleStream(schemaType, schemaType.getMainSortMetadata(), progressionConsumer));


					} else {
						returnedIds.addAll(modelLayerFactory.newSearchServices()
								.recordsIdSortedByTitleUsingIterator(schemaType, schemaType.getMainSortMetadata(), progressionConsumer));
					}
				}

			}
		}
		return returnedIds;
	}

	public void onPostLayerInitialization(ModelPostInitializationParams params) {

		if (modelLayerFactory.getConfiguration().isSummaryCacheEnabled()) {
			List<MetadataSchemaType> typesLoadedAsync = new ArrayList<>();
			for (String collection : modelLayerFactory.getCollectionsListManager().getCollections()) {
				LOGGER.info("Loading cache of '" + collection);
				typesLoadedAsync.addAll(loadSummaryPermanentCache(collection));
			}


			if (!typesLoadedAsync.isEmpty()) {
				new Thread(() -> {
					Stats.compilerFor("SummaryCacheLoading").log(() -> {
						//One loading at a time
						synchronized (RecordsCaches2Impl.class) {
							boolean useMapDb = !fileSystemDataStore.isRecreated() && !params.isRebuildCacheFromSolr();
							typesLoadedAsync.forEach(type -> loadSchemaType(type, useMapDb));
						}
						memoryDataStore.setRecordsMainSortValue(recordsIdSortedByTheirDefaultSort());
						summaryCacheInitialized = true;
						CacheRecordDTOUtils.stopCompilingDTOsStats();
						LOGGER.info("\n" + RecordsCachesUtils.buildCacheDTOStatsReport(modelLayerFactory));
						cacheLoadingProgression = null;
					});

					if (Toggle.USE_MMAP_WITHMAP_DB_FOR_LOADING.isEnabled() && !Toggle.USE_MMAP_WITHMAP_DB_FOR_RUNTIME.isEnabled()) {
						fileSystemDataStore.closeThenReopenWithoutMmap();
					}
					if (params.getCacheLoadingFinishedCallback() != null) {
						params.getCacheLoadingFinishedCallback().run();
					}
				}).start();


			} else {

				memoryDataStore.setRecordsMainSortValue(recordsIdSortedByTheirDefaultSort());

				summaryCacheInitialized = true;
				CacheRecordDTOUtils.stopCompilingDTOsStats();
				LOGGER.info("\n" + RecordsCachesUtils.buildCacheDTOStatsReport(modelLayerFactory));
				cacheLoadingProgression = null;
			}
		}
	}

	@Override
	public void markAsInitialized(MetadataSchemaType schemaType) {
		this.schemaTypeLoadedStatuses.set(schemaType, true);
	}

	@Override
	public void close() {
		memoryDataStore.close();
		fileSystemDataStore.close();
	}

	protected void removeFromAllCaches(byte collectionId, List<String> recordIds) {
		memoryDataStore.stream(collectionId, recordIds).forEach(recordDTO -> remove(recordDTO, false));

		for (String recordId : recordIds) {
			volatileCache.remove(recordId);
		}
	}


	private void reloadSchemaType(byte collectionId, String collection, String recordType, boolean onlyLocally) {
		short typeId = metadataSchemasManager.getSchemaTypes(collectionId).getSchemaType(recordType).getId();
		//memoryDataStore.stream(collectionId, typeId).filter((record) -> Boolean.TRUE).forEach(this::remove);
		MetadataSchemaType type = metadataSchemasManager.getSchemaTypes(collection).getSchemaType(recordType);

		//		if (type.getCacheType().hasPermanentCache()) {
		//			metadataIndexCacheDataStore.clear(type);
		//		}

		if (type.getCacheType().hasVolatileCache()) {
			volatileCache.clear();
		}

		loadSchemaType(type, false);
	}

	protected Stream<Record> stream(byte collectionId, String schemaType) {
		short typeId = metadataSchemasManager.getSchemaTypes(collectionId).getSchemaType(schemaType).getId();
		return memoryDataStore.stream(collectionId, typeId).map(this::toRecord);
	}


	private void remove(RecordDTO recordDTO) {
		remove(recordDTO, true);
	}

	private void remove(RecordDTO recordDTO, boolean removeFromVolatile) {
		int intId = RecordUtils.toIntKey(recordDTO.getId());
		if (intId == RecordUtils.KEY_IS_NOT_AN_INT) {
			memoryDataStore.remove(recordDTO);
		} else {
			memoryDataStore.remove(recordDTO);

		}
		if (removeFromVolatile) {
			volatileCache.remove(recordDTO.getId());
		}

		MetadataSchemaTypes types = metadataSchemasManager.getSchemaTypes(recordDTO.getCollection());
		MetadataSchemaType type = types.getSchemaType(SchemaUtils.getSchemaTypeCode(recordDTO.getSchemaCode()));
		metadataIndexCacheDataStore.addUpdate(toRecord(recordDTO), null, type, types.getSchema(recordDTO.getSchemaCode()));
		RecordsCachesHook hook = hooks.getSchemaTypeHook(types, type.getId());
		if (hook != null) {
			hook.removeRecordFromCache(recordDTO);
		}

		if (intId == RecordUtils.KEY_IS_NOT_AN_INT) {
			fileSystemDataStore.removeStringKey(recordDTO.getId());
		} else {
			fileSystemDataStore.removeIntKey(intId);

		}
	}


	protected Record toRecord(RecordDTO dto) {
		return dto == null ? null : recordServices.get().toRecord(dto, dto.getLoadingMode() == FULLY_LOADED);
	}

	protected Record toRecord(MetadataSchemaType schemaType, RecordDTO dto) {
		return dto == null ? null : recordServices.get().toRecord(schemaType, dto, dto.getLoadingMode() == FULLY_LOADED);
	}

	protected Record toRecord(MetadataSchema schema, RecordDTO dto) {
		return dto == null ? null : recordServices.get().toRecord(schema, dto, dto.getLoadingMode() == FULLY_LOADED);
	}

	@Override
	public int estimateMaxResultSizeUsingIndexedMetadata(MetadataSchemaType schemaType, Metadata metadata,
														 String value) {
		return metadataIndexCacheDataStore.estimateMaxResultSizeUsingIndexedMetadata(schemaType, metadata, value);
	}

	@Override
	public CacheLoadingProgression getLoadingProgression() {
		return cacheLoadingProgression;
	}

	protected Record getByMetadata(byte collectionId, Metadata metadata, String value) {
		if (metadata.isSameLocalCode(Schemas.IDENTIFIER)) {
			return getRecord(value, metadata.getCollection(), null);
		}

		if (!metadata.isUniqueValue()) {
			throw new IllegalArgumentException("Metadata must be unique to use this method");
		}

		MetadataSchemaType schemaType = metadataSchemasManager.getSchemaTypes(collectionId)
				.getSchemaType(metadata.getSchemaTypeCode());

		if (!metadata.getCollection().equals(schemaType.getCollection())) {
			throw new ImpossibleRuntimeException("Searching with a metadata from collection '" + metadata.getCollection() + "' in cache of collection '" + schemaType.getCollection() + "'");
		}

		if (schemaType.getCacheType() == RecordCacheType.FULLY_CACHED) {
			List<String> potentialIds = metadataIndexCacheDataStore.search(schemaType, metadata, value);

			for (String potentialId : potentialIds) {
				Record record = toRecord(schemaType, memoryDataStore.get(potentialId));
				if (value.equals(record.get(metadata))) {
					return record;
				}
			}
			return null;

		} else {
			throw new ImpossibleRuntimeException("getByMetadata cannot be used for schema type '" + schemaType.getCode() + "' which is not fully cached. If the schema type has a summary cache, try using getSummaryByMetadata instead");
		}
	}

	protected Record getSummaryByMetadata(byte collectionId, Metadata metadata, String value) {

		if (!metadata.isUniqueValue()) {
			throw new IllegalArgumentException("Metadata must be unique to use this method");
		}

		if (metadata.isSameLocalCode(Schemas.IDENTIFIER)) {
			return getRecordSummary(value, metadata.getCollection());
		}

		if (StringUtils.isBlank(value)) {
			return null;
		}

		MetadataSchemaType schemaType = metadataSchemasManager.getSchemaTypes(collectionId)
				.getSchemaType(metadata.getSchemaTypeCode());

		if (schemaType.getCacheType().isSummaryCache() || schemaType.getCacheType() == RecordCacheType.FULLY_CACHED) {
			List<String> potentialIds = metadataIndexCacheDataStore.search(schemaType, metadata, value);

			for (String potentialId : potentialIds) {
				Record record = toRecord(schemaType, memoryDataStore.get(potentialId));
				if (value.equals(record.get(metadata))) {
					return record;
				}
			}
			return null;

		} else {
			return null;
		}
	}

	public Stream<Record> streamVolatile(MetadataSchemaType schemaType) {
		return StreamSupport.stream(volatileCache.getEntries().spliterator(), false)
				.filter((e -> e.getValue().getCollection().equals(schemaType.getCollection()) &&
							  e.getValue().getSchemaCode().startsWith(schemaType.getCode() + "_")))
				.map((e) -> toRecord(schemaType, e.getValue()));

	}

	@Override
	public void invalidateVolatile(MassiveCacheInvalidationReason reason) {
		//TODO Distribute query to keep integrity
		volatileCache.clear();
	}

	@Override
	public RecordsCachesHook getHook(MetadataSchemaType schemaType) {
		return hooks.getSchemaTypeHook(
				metadataSchemasManager.getSchemaTypes(schemaType.getCollection()), schemaType.getId());
	}


	private RecordDTO toPersistedSummaryRecordDTO(Record record, InsertionReason reason, int mainSortValue) {

		if (Toggle.USE_BYTE_ARRAY_DTOS_FOR_SUMMARY_CACHE.isEnabled()) {
			RecordDTO byteArrayRecordDTO = prepareForCache(((RecordImpl) record).getRecordDTO(), reason, mainSortValue);

			if (Toggle.VALIDATE_BYTE_ARRAY_DTOS_AFTER_CREATION.isEnabled()) {
				validate(record, byteArrayRecordDTO);
			}

			return byteArrayRecordDTO;

		} else {
			MetadataSchema schema = metadataSchemasManager.getSchemaOf(record);
			return RecordUtils.toPersistedSummaryRecordDTO(record, schema);
		}
	}

	private void validate(Record record, RecordDTO byteArrayRecordDTO) {
		MetadataSchema schema = metadataSchemasManager.getSchemaOf(record);
		RecordDTO comparisonRecordDTO = RecordUtils.toPersistedSummaryRecordDTO(record, schema);

		if (!byteArrayRecordDTO.getId().equals(comparisonRecordDTO.getId())) {
			handleByteArrayDTOIntegrityError(record.getId(), "Id not equal");
			return;

		} else if (byteArrayRecordDTO.getVersion() != comparisonRecordDTO.getVersion()) {
			handleByteArrayDTOIntegrityError(record.getId(), "Version not equal");

		} else if (byteArrayRecordDTO.getLoadingMode() != comparisonRecordDTO.getLoadingMode()) {
			handleByteArrayDTOIntegrityError(record.getId(), "Loading mode not equal");

		} else if (!byteArrayRecordDTO.getCollection().equals(comparisonRecordDTO.getCollection())) {
			handleByteArrayDTOIntegrityError(record.getId(), "Collection not equal");

		} else if (!byteArrayRecordDTO.getSchemaCode().equals(comparisonRecordDTO.getSchemaCode())) {
			handleByteArrayDTOIntegrityError(record.getId(), "Schema not equal");

		} else if (!byteArrayRecordDTO.getFields().keySet().equals(comparisonRecordDTO.getFields().keySet())) {

			Set<String> byteArrayFields = new HashSet<>(byteArrayRecordDTO.getFields().keySet());
			Set<String> comparisonFields = new HashSet<>(comparisonRecordDTO.getFields().keySet());

			for (String field : byteArrayRecordDTO.getFields().keySet()) {
				//System.out.println(field + ":" + byteArrayRecordDTO.getFields().get(field));
				Object value = byteArrayRecordDTO.getFields().get(field);
				if (value == null || ((value instanceof List) && ((List) value).isEmpty())) {
					byteArrayFields.remove(field);
				}
			}

			for (String field : comparisonRecordDTO.getFields().keySet()) {
				//	System.out.println(field + ":" + comparisonRecordDTO.getFields().get(field));
				Object value = comparisonRecordDTO.getFields().get(field);
				if (value == null || ((value instanceof List) && ((List) value).isEmpty())) {
					comparisonFields.remove(field);
				}
			}

			ListComparisonResults<String> diff = compare(byteArrayFields, comparisonFields);

			StringBuilder stringBuilder = new StringBuilder();
			if (!diff.getNewItems().isEmpty()) {
				stringBuilder.append("\nFields that should be in byte array DTO : " + diff.getNewItems());
			}
			if (!diff.getRemovedItems().isEmpty()) {
				stringBuilder.append("\nFields that should not be in byte array DTO : " + diff.getRemovedItems());
			}

			if (stringBuilder.length() > 0) {
				handleByteArrayDTOIntegrityError(record.getId(), "Not same fields : " + stringBuilder.toString());
			}

		} else {
			Set<String> fields = byteArrayRecordDTO.getFields().keySet();

			for (String field : fields) {
				Object byteArrayFieldValue = byteArrayRecordDTO.getFields().get(field);
				Object comparisonRecordFieldValue = comparisonRecordDTO.getFields().get(field);

				if (!LangUtils.isEqual(byteArrayFieldValue, comparisonRecordFieldValue)) {
					handleByteArrayDTOIntegrityError(record.getId(), "Field '" + field + "' is different"
																	 + "\nByte array DTO value : " + byteArrayFieldValue
																	 + "\nObject DTO value : " + comparisonRecordFieldValue);
				}
			}

		}
	}

	private void handleByteArrayDTOIntegrityError(String recordId, String errorMessage) {
		if (FoldersLocator.usingAppWrapper()) {
			SystemLogger.error("Validation of record '" + recordId + "' failed : " + errorMessage);
		} else {
			throw new IllegalArgumentException("Validation of record '" + recordId + "' failed : " + errorMessage);
		}

	}


	private RecordDTO prepareForCache(RecordDTO dto, InsertionReason reason, int mainSortValue) {

		if (dto.getLoadingMode() == CUSTOM && reason != LOADING_CACHE) {
			throw new IllegalStateException("Cannot create summary record from a customly loaded Record");
		}

		String collection = (String) dto.getFields().get("collection_s");
		String schemaCode = (String) dto.getFields().get("schema_s");
		short instanceId = modelLayerFactory.getInstanceId();
		MetadataSchemaType type = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection)
				.getSchemaType(SchemaUtils.getSchemaTypeCode(schemaCode));

		MetadataSchemaProvider schemaProvider = modelLayerFactory.getMetadataSchemasManager();

		MetadataSchema schema = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection).getSchema(schemaCode);
		CollectionInfo collectionInfo = schema.getCollectionInfo();

		//TODO Handle Holder
		if (Toggle.USE_ONLY_SUMMARY_SOLR_RECORD_DTO.isEnabled()) {
			return ((SolrRecordDTO) dto).createSummaryKeeping(schema.getSummaryMetadatas()
					.stream().map(Metadata::getDataStoreCode).collect(Collectors.toList()));
		} else {
			CacheRecordDTOBytesArray bytesArray = null;
			bytesArray = convertDTOToByteArrays(dto, schema);

			int intId = RecordUtils.toIntKey(dto.getId());

			if (intId == RecordUtils.KEY_IS_NOT_AN_INT) {
				if (bytesArray.bytesToPersist != null && bytesArray.bytesToPersist.length > 0) {
					SummaryCacheSingletons.dataStore.get(instanceId).saveStringKey(dto.getId(), bytesArray.bytesToPersist);

				} else if (reason != LOADING_CACHE) {
					SummaryCacheSingletons.dataStore.get(instanceId).removeStringKey(dto.getId());
				}
				return new ByteArrayRecordDTOWithStringId(dto.getId(), schemaProvider, dto.getVersion(), true,
						instanceId, collectionInfo.getCode(), collectionInfo.getCollectionId(), type.getCode(), type.getId(),
						schema.getCode(), schema.getId(), bytesArray.bytesToKeepInMemory, mainSortValue);
			} else {

				ByteArrayRecordDTOWithIntegerId recordDTO = new ByteArrayRecordDTOWithIntegerId(intId, schemaProvider, dto.getVersion(), true,
						instanceId, collectionInfo.getCode(), collectionInfo.getCollectionId(), type.getCode(), type.getId(),
						schema.getCode(), schema.getId(), bytesArray.bytesToKeepInMemory, mainSortValue);

				SummaryCacheSingletons.dataStore.get(instanceId).saveIntKeyPersistedAndMemoryData(intId, bytesArray.bytesToPersist, recordDTO);
				return recordDTO;
			}

		}
	}

	public <K> RecordCountHookDataIndexRetriever<K> registerRecordCountHook(
			String collection, MetadataIndexCacheDataStoreHook hook) {
		byte collectionId = collectionsListManager.getCollectionId(collection);
		return metadataIndexCacheDataStore.registerRecordCountHook(collectionId, hook);
	}

	public <K> RecordIdsHookDataIndexRetriever<K> registerRecordIdsHook(
			String collection, MetadataIndexCacheDataStoreHook hook) {
		byte collectionId = collectionsListManager.getCollectionId(collection);
		return metadataIndexCacheDataStore.registerRecordIdsHook(collectionId, hook);
	}

	private static class LoadingUsingMapDBAbortedException extends Exception {
	}

}
