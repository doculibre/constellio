package com.constellio.model.services.records.cache2;

import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.dto.records.RecordDTOMode;
import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.dao.services.cache.InsertionReason;
import com.constellio.data.utils.ImpossibleRuntimeException;
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
import com.constellio.model.services.records.RecordImpl;
import com.constellio.model.services.records.cache.CacheInsertionStatus;
import com.constellio.model.services.records.cache.MassiveCacheInvalidationReason;
import com.constellio.model.services.records.cache.RecordsCache;
import com.constellio.model.services.records.cache.RecordsCaches;
import com.constellio.model.services.records.cache2.ByteArrayRecordDTO.ByteArrayRecordDTOWithIntegerId;
import com.constellio.model.services.records.cache2.ByteArrayRecordDTO.ByteArrayRecordDTOWithStringId;
import com.constellio.model.services.records.cache2.CacheRecordDTOUtils.CacheRecordDTOBytesArray;
import com.constellio.model.services.schemas.MetadataSchemaProvider;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.search.SearchServices;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.constellio.data.dao.dto.records.RecordDTOMode.CUSTOM;
import static com.constellio.data.dao.dto.records.RecordDTOMode.FULLY_LOADED;
import static com.constellio.data.dao.dto.records.RecordDTOMode.SUMMARY;
import static com.constellio.data.dao.services.cache.InsertionReason.LOADING_CACHE;
import static com.constellio.data.utils.LangUtils.isEqual;
import static com.constellio.model.entities.schemas.Schemas.COLLECTION;
import static com.constellio.model.entities.schemas.Schemas.SCHEMA;
import static com.constellio.model.services.records.RecordUtils.toPersistedSummaryRecordDTO;
import static com.constellio.model.services.records.cache.MassiveCacheInvalidationReason.KEEP_INTEGRITY;
import static com.constellio.model.services.records.cache2.CacheRecordDTOUtils.convertDTOToByteArrays;

public class RecordsCaches2Impl implements RecordsCaches, StatefulService {

	private static final Logger LOGGER = LoggerFactory.getLogger(RecordsCaches2Impl.class);

	private Map<String, RecordsCache> collectionCaches = new HashMap<>();

	protected ModelLayerFactory modelLayerFactory;
	MetadataSchemasManager metadataSchemasManager;
	private CollectionsListManager collectionsListManager;

	private FileSystemRecordsValuesCacheDataStore fileSystemDataStore;
	private RecordsCachesDataStore memoryDataStore;
	private DB memoryDiskDatabase;
	protected HTreeMap<String, RecordDTO> volatileCache;
	private boolean initialized;

	public RecordsCaches2Impl(ModelLayerFactory modelLayerFactory,
							  FileSystemRecordsValuesCacheDataStore fileSystemDataStore,
							  RecordsCachesDataStore memoryDataStore) {
		this.modelLayerFactory = modelLayerFactory;
		this.collectionsListManager = modelLayerFactory.getCollectionsListManager();
		this.metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
		this.fileSystemDataStore = fileSystemDataStore;
		this.memoryDataStore = memoryDataStore;
		this.memoryDiskDatabase = DBMaker.memoryDB().make();

		ScheduledExecutorService executor =
				Executors.newScheduledThreadPool(2);

		//Maximum 50K records or 100mo
		volatileCache = memoryDiskDatabase
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
				.create();

	}

	public Record get(String id, String collection) {
		RecordDTO recordDTO = memoryDataStore.get(id);
		if (recordDTO != null) {
			String collectionCode = (String) recordDTO.getFields().get(COLLECTION.getDataStoreCode());
			String schemaCode = (String) recordDTO.getFields().get(SCHEMA.getDataStoreCode());

			//The record is in an other collection, so null is returned
			if (!collectionCode.equals(collection)) {
				return null;
			}

			MetadataSchemaTypes schemaTypes = metadataSchemasManager.getSchemaTypes(collectionCode);
			MetadataSchema schema = schemaTypes.getSchema(schemaCode);
			MetadataSchemaType schemaType = schemaTypes.getSchemaType(SchemaUtils.getSchemaTypeCode(schemaCode));
			if (schemaType.getCacheType().isSummaryCache()) {
				if (schemaType.getCacheType().hasVolatileCache()) {
					recordDTO = volatileCache.get(id);
					return recordDTO == null ? null : toRecord(recordDTO);

				} else {
					return null;
				}

			} else {
				return toRecord(recordDTO);
			}
		}
		return null;
	}

	public Record getSummary(String id, String collection) {
		RecordDTO recordDTO = memoryDataStore.get(id);
		if (recordDTO != null) {
			String collectionCode = (String) recordDTO.getFields().get(COLLECTION.getDataStoreCode());
			String schemaCode = (String) recordDTO.getFields().get(SCHEMA.getDataStoreCode());

			if (collectionCode.equals(collection)) {
				return toRecord(recordDTO);
			}
		}
		return null;
	}


	protected void reload(byte collectionId, String collection, List<String> schemaTypes,
						  boolean onlyLocally, boolean forceVolatileCacheClear) {

		boolean clearVolatileCache = forceVolatileCacheClear;
		for (String schemaTypeCode : schemaTypes) {
			MetadataSchemaType schemaType = metadataSchemasManager.getSchemaTypes(collection).getSchemaType(schemaTypeCode);
			if (schemaType.getCacheType().hasVolatileCache()) {
				clearVolatileCache = true;
			}

			memoryDataStore.invalidate(schemaType.getCollectionInfo().getCollectionId(), schemaType.getId(), (r) -> true);
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
					cache = new RecordsCache2Impl(collection, collectionId, modelLayerFactory, fileSystemDataStore,
							memoryDataStore, volatileCache, this);
					collectionCaches.put(collection, cache);
				}
			}
		}

		return cache;
	}


	public CacheInsertionStatus insert(Record record, InsertionReason insertionReason) {

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

		//TODO Validate if record is insertable

		MetadataSchemaType schemaType = metadataSchemasManager.getSchemaTypeOf(record);

		RecordDTO current = memoryDataStore.get(record.getId());
		if (current != null && current.getVersion() > record.getVersion()) {
			return CacheInsertionStatus.REFUSED_OLD_VERSION;
		}

		if (schemaType.getCacheType().hasVolatileCache()) {
			if (record.getRecordDTOMode() != FULLY_LOADED) {
				volatileCache.remove(record.getId());

			} else {
				RecordDTO volatileCacheDto = volatileCache.get(record.getId());
				if (volatileCacheDto == null || volatileCacheDto.getVersion() < record.getVersion()) {
					volatileCache.put(record.getId(), ((RecordImpl) record).getRecordDTO());
				}

			}
		}

		if (schemaType.getCacheType() == RecordCacheType.FULLY_CACHED) {

			RecordDTO dto = ((RecordImpl) record).getRecordDTO();
			if (dto.getLoadingMode() != RecordDTOMode.FULLY_LOADED) {
				LOGGER.error("Record '" + record.getId() + "' of type should not exist in summary state, since it is fully cached");
				return CacheInsertionStatus.REFUSED_NOT_FULLY_LOADED;
			}

			memoryDataStore.insert(dto);
			return CacheInsertionStatus.ACCEPTED;


		} else if (schemaType.getCacheType().isSummaryCache()) {
			MetadataSchema schema = metadataSchemasManager.getSchemaOf(record);
			RecordDTO dto = toPersistedSummaryRecordDTO(record, schema);

			//			if (record.isSummary()) {
			//				dto = ((RecordImpl) record).getRecordDTO();
			//
			//			} else {
			//				dto = toPersistedSummaryRecordDTO(record);
			//			}

			memoryDataStore.insert(dto);
			return CacheInsertionStatus.ACCEPTED;

		} else {
			return CacheInsertionStatus.REFUSED_NOT_CACHED;
		}
	}

	@Override
	public Record getRecord(String id) {
		RecordDTO recordDTO = memoryDataStore.get(id);
		if (recordDTO != null) {
			String collectionCode = (String) recordDTO.getFields().get(COLLECTION.getDataStoreCode());
			String schemaCode = (String) recordDTO.getFields().get(SCHEMA.getDataStoreCode());

			MetadataSchemaTypes schemaTypes = metadataSchemasManager.getSchemaTypes(collectionCode);
			MetadataSchema schema = schemaTypes.getSchema(schemaCode);
			MetadataSchemaType schemaType = schemaTypes.getSchemaType(SchemaUtils.getSchemaTypeCode(schemaCode));

			if (schemaType.getCacheType().isSummaryCache()) {

				if (schemaType.getCacheType().hasVolatileCache()) {
					recordDTO = volatileCache.get(id);
					return recordDTO == null ? null : toRecord(recordDTO);

				} else {
					return null;
				}

			} else {
				return toRecord(recordDTO);
			}
		}
		return null;
	}

	@Override
	public Record getRecordSummary(String id) {
		RecordDTO recordDTO = memoryDataStore.get(id);
		if (recordDTO != null) {
			String collectionCode = (String) recordDTO.getFields().get(COLLECTION.getDataStoreCode());
			String schemaCode = (String) recordDTO.getFields().get(SCHEMA.getDataStoreCode());

			return toRecord(recordDTO);
		}
		return null;
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
		byte collectionId = modelLayerFactory.getCollectionsListManager().getCollectionInfo(collection).getCollectionId();
		memoryDataStore.invalidateAll(collectionId);
	}

	@Override
	public Stream<Record> stream(MetadataSchemaType type) {
		return memoryDataStore.stream(type.getCollectionInfo().getCollectionId(), type.getId()).map(this::toRecord);
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
	public boolean isInitialized() {
		return initialized;
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


	private void loadCache(String collection) {
		for (MetadataSchemaType type : metadataSchemasManager.getSchemaTypes(collection).getSchemaTypes()) {
			loadSchemaType(type);
		}
	}

	private void loadSchemaType(MetadataSchemaType type) {
		SearchServices searchServices = modelLayerFactory.newSearchServices();

		if (type.getCacheType().hasPermanentCache()) {
			AtomicInteger added = new AtomicInteger();

			long count = searchServices.streamFromSolr(type, type.getCacheType().isSummaryCache()).count();
			searchServices.streamFromSolr(type, type.getCacheType().isSummaryCache()).forEach((record) -> {
				CacheInsertionStatus status = (insert(record, LOADING_CACHE));
				LOGGER.info("Adding records " + record.getTypeCode() + " : " + added.incrementAndGet() + "/" + count);
				if (status != CacheInsertionStatus.ACCEPTED) {
					LOGGER.warn("Could not load record '" + record.getId() + "' in cache : " + status);
				}
			});
		}
	}


	public RecordDTO prepareForCache(RecordDTO dto) {

		if (dto.getLoadingMode() == CUSTOM) {
			throw new IllegalStateException("Cannot create summary record from a customly loaded Record");
		}

		String collection = (String) dto.getFields().get("collection_s");
		String schemaCode = (String) dto.getFields().get("schema_s");
		MetadataSchemaType type = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection)
				.getSchemaType(SchemaUtils.getSchemaTypeCode(schemaCode));

		MetadataSchemaProvider schemaProvider = modelLayerFactory.getMetadataSchemasManager();

		MetadataSchema schema = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection).getSchema(schemaCode);
		CollectionInfo collectionInfo = schema.getCollectionInfo();

		//TODO Handle Holder
		CacheRecordDTOBytesArray bytesArray = convertDTOToByteArrays(dto, schema);

		int intId = CacheRecordDTOUtils.toIntKey(dto.getId());

		if (intId == CacheRecordDTOUtils.KEY_IS_NOT_AN_INT) {
			if (bytesArray.bytesToPersist != null && bytesArray.bytesToPersist.length > 0) {
				SummaryCacheSingletons.dataStore.saveStringKey(dto.getId(), bytesArray.bytesToPersist);
			} else {
				//SummaryCacheSingletons.dataStore.removeStringKey(dto.getId());
			}
			return new ByteArrayRecordDTOWithStringId(dto.getId(), schemaProvider, dto.getVersion(), dto.getLoadingMode() == SUMMARY,
					collectionInfo.getCode(), collectionInfo.getCollectionId(), type.getCode(), type.getId(),
					schema.getCode(), schema.getId(), bytesArray.bytesToKeepInMemory);
		} else {
			if (bytesArray.bytesToPersist != null && bytesArray.bytesToPersist.length > 0) {
				SummaryCacheSingletons.dataStore.saveIntKey(intId, bytesArray.bytesToPersist);
			} else {
				//SummaryCacheSingletons.dataStore.removeIntKey(intId);
			}
			return new ByteArrayRecordDTOWithIntegerId(intId, schemaProvider, dto.getVersion(), dto.getLoadingMode() == SUMMARY,
					collectionInfo.getCode(), collectionInfo.getCollectionId(), type.getCode(), type.getId(),
					schema.getCode(), schema.getId(), bytesArray.bytesToKeepInMemory);
		}

	}

	@Deprecated
	protected List<Record> getAllValuesInUnmodifiableState(byte collectionId, String collection, String schemaType) {
		CollectionInfo collectionInfo = collectionsListManager.getCollectionInfo(collection);
		short typeId = metadataSchemasManager.getSchemaTypes(collectionId).getSchemaType(schemaType).getId();
		return memoryDataStore.stream(collectionId, typeId).map(this::toRecord).collect(Collectors.toList());
	}

	@Override
	public void initialize() {
		for (String collection : modelLayerFactory.getCollectionsListManager().getCollections()) {
			LOGGER.info("Loading cache of '" + collection);
			loadCache(collection);
		}
		initialized = true;
	}

	@Override
	public void close() {
		fileSystemDataStore.close();
	}

	protected void removeFromAllCaches(byte collectionId, List<String> recordIds) {
		memoryDataStore.stream(collectionId, recordIds).forEach(this::remove);
	}


	private void reloadSchemaType(byte collectionId, String collection, String recordType, boolean onlyLocally) {
		short typeId = metadataSchemasManager.getSchemaTypes(collectionId).getSchemaType(recordType).getId();
		memoryDataStore.stream(collectionId, typeId).filter((record) -> Boolean.TRUE).forEach(this::remove);
		MetadataSchemaType type = metadataSchemasManager.getSchemaTypes(collection).getSchemaType(recordType);

		if (type.getCacheType().hasVolatileCache()) {
			volatileCache.clear();
		}

		loadSchemaType(type);
	}

	protected Stream<Record> stream(byte collectionId, String schemaType) {
		short typeId = metadataSchemasManager.getSchemaTypes(collectionId).getSchemaType(schemaType).getId();
		return memoryDataStore.stream(collectionId, typeId).map(this::toRecord);
	}


	private void remove(RecordDTO recordDTO) {
		int intId = CacheRecordDTOUtils.toIntKey(recordDTO.getId());
		if (intId == CacheRecordDTOUtils.KEY_IS_NOT_AN_INT) {
			memoryDataStore.remove(recordDTO);
			fileSystemDataStore.removeStringKey(recordDTO.getId());
		} else {
			memoryDataStore.remove(recordDTO);
			fileSystemDataStore.removeIntKey(intId);

		}
		volatileCache.remove(recordDTO.getId());
	}


	private Record toRecord(RecordDTO dto) {
		return modelLayerFactory.newRecordServices().toRecord(dto, dto.getLoadingMode() == FULLY_LOADED);
	}


	protected Record getByMetadata(byte collectionId, Metadata metadata, String value) {
		if (metadata.isSameLocalCode(Schemas.IDENTIFIER)) {
			return get(value, metadata.getCollection());
		}

		MetadataSchemaType schemaType = metadataSchemasManager.getSchemaTypes(collectionId)
				.getSchemaType(metadata.getSchemaTypeCode());

		if (!metadata.getCollection().equals(schemaType.getCollection())) {
			throw new ImpossibleRuntimeException("Searching with a metadata from collection '" + metadata.getCollection() + "' in cache of collection '" + schemaType.getCollection() + "'");
		}

		if (schemaType.getCacheType() == RecordCacheType.FULLY_CACHED) {
			return stream(collectionId, metadata.getSchemaTypeCode())
					.filter(record -> isEqual(value, record.get(metadata))).findFirst().orElse(null);

			//			List<Record> allRecords = stream(schemaType.getCollection()).collect(Collectors.toList());
			//
			//			List<Record> records = stream(collectionId, metadata.getSchemaTypeCode())
			//					.collect(Collectors.toList());
			//
			//			for (Record record : records) {
			//				if (isEqual(value, record.get(metadata))) {
			//					return record;
			//				}
			//			}
			//
			//			for (Record record : allRecords) {
			//				if (record.getSchemaCode().startsWith(schemaType.getCode() + "_") && isEqual(value, record.get(metadata))) {
			//					return record;
			//				}
			//			}
			//
			//			return null;

		} else {
			throw new ImpossibleRuntimeException("getByMetadata cannot be used for schema type '" + schemaType.getCode() + "' which is not fully cached. If the schema type has a summary cache, try using getSummaryByMetadata instead");
		}
	}

	protected Record getSummaryByMetadata(byte collectionId, Metadata metadata, String value) {

		if (metadata.isSameLocalCode(Schemas.IDENTIFIER)) {
			return getSummary(value, metadata.getCollection());
		}

		MetadataSchemaType schemaType = metadataSchemasManager.getSchemaTypes(collectionId)
				.getSchemaType(metadata.getSchemaTypeCode());

		if (schemaType.getCacheType().isSummaryCache() || schemaType.getCacheType() == RecordCacheType.FULLY_CACHED) {
			return stream(schemaType)
					.filter(record -> isEqual(value, record.get(metadata))).findFirst().orElse(null);
		} else {
			return null;
		}
	}

	public Stream<Record> streamVolatile(MetadataSchemaType schemaType) {
		return StreamSupport.stream(volatileCache.getEntries().spliterator(), false)
				.filter((e -> e.getValue().getCollection().equals(schemaType.getCollection()) &&
							  e.getValue().getSchemaCode().startsWith(schemaType.getCode() + "_")))
				.map((e) -> toRecord(e.getValue()));

	}

	@Override
	public void invalidateVolatile(MassiveCacheInvalidationReason reason) {
		//TODO Distribute query to keep integrity
		volatileCache.clear();
	}

}
