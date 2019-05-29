package com.constellio.model.services.records.cache2;

import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.dto.records.SolrRecordDTO;
import com.constellio.data.dao.services.cache.InsertionReason;
import com.constellio.model.entities.CollectionInfo;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.RecordCacheType;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordImpl;
import com.constellio.model.services.records.cache.CacheInsertionStatus;
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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static com.constellio.data.dao.services.cache.InsertionReason.WAS_OBTAINED;
import static com.constellio.model.entities.schemas.Schemas.COLLECTION;
import static com.constellio.model.entities.schemas.Schemas.SCHEMA;
import static com.constellio.model.services.records.cache2.CacheRecordDTOUtils.convertDTOToByteArrays;

public class RecordsCaches2Impl implements RecordsCaches {

	private static Logger LOGGER = LoggerFactory.getLogger(RecordsCaches2Impl.class);

	private Map<String, RecordsCache> collectionCaches = new HashMap<>();

	private ModelLayerFactory modelLayerFactory;
	private MetadataSchemasManager metadataSchemasManager;

	private FileSystemRecordsValuesCacheDataStore fileSystemDataStore;
	private RecordsCachesDataStore memoryDataStore;
	private DB memoryDiskDatabase;
	private HTreeMap<String, RecordDTO> volatileCache;

	public RecordsCaches2Impl(ModelLayerFactory modelLayerFactory,
							  FileSystemRecordsValuesCacheDataStore fileSystemDataStore,
							  RecordsCachesDataStore memoryDataStore) {
		this.modelLayerFactory = modelLayerFactory;
		this.metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
		this.fileSystemDataStore = fileSystemDataStore;
		this.memoryDataStore = memoryDataStore;
		this.memoryDiskDatabase = DBMaker.memoryDB().make();

		//Maximum 50K records or 100mo
		volatileCache = memoryDiskDatabase.hashMap("volatileCache")
				.keySerializer(Serializer.STRING)
				.valueSerializer(Serializer.JAVA)
				.expireMaxSize(50000)
				.expireStoreSize(100 * 1024 * 1024)
				.create();

	}


	private short nextCollectionId;

	@Override
	public RecordsCache getCache(String collection) {

		RecordsCache cache = collectionCaches.get(collection);
		if (cache == null) {
			synchronized (this) {
				cache = collectionCaches.get(collection);
				if (cache == null) {
					byte collectionId = (byte) (nextCollectionId++ + Byte.MIN_VALUE);
					cache = new RecordsCache2Impl(collection, collectionId, modelLayerFactory, fileSystemDataStore,
							memoryDataStore, volatileCache, this);
					collectionCaches.put(collection, cache);
				}
			}
		}

		return cache;
	}

	@Override
	public CacheInsertionStatus insert(Record record, InsertionReason insertionReason) {

		if (record == null) {
			return CacheInsertionStatus.REFUSED_NULL;
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
			if (record.isSummary()) {
				//TODO : Invalidate in volatile cache
			} else {
				RecordDTO cachedDto = volatileCache.get(record.getId());
				if (cachedDto == null || cachedDto.getVersion() < current.getVersion()) {
					volatileCache.put(record.getId(), ((RecordImpl)record).getRecordDTO());
				}

			}
		}

		if (schemaType.getCacheType() == RecordCacheType.FULLY_CACHED) {
			if (record.isSummary()) {
				LOGGER.error("Record '" + record.getId() + "' of type should not exist in summary state, since it is fully cached");
				return CacheInsertionStatus.REFUSED_NOT_FULLY_LOADED;
			}

			memoryDataStore.insert(((RecordImpl) record).getRecordDTO());
			return CacheInsertionStatus.ACCEPTED;


		} else if (schemaType.getCacheType().isSummaryCache()) {
			RecordDTO dto;
			if (record.isSummary()) {
				dto = ((RecordImpl) record).getRecordDTO();

			} else {
				dto = toPersistedSummaryRecordDTO(record);
			}

			memoryDataStore.insert(dto);
			return CacheInsertionStatus.ACCEPTED;

		} else {
			return CacheInsertionStatus.REFUSED_NOT_CACHED;
		}
	}

	private RecordDTO toPersistedSummaryRecordDTO(Record record) {
		RecordDTO dto = ((RecordImpl) record).getRecordDTO();
		MetadataSchema schema = metadataSchemasManager.getSchemaOf(record);

		Map<String, Object> fields = new HashMap<>();

		for (Metadata summaryMetadata : schema.getSummaryMetadatas()) {
			String summaryMetadataDataStoreCode = summaryMetadata.getDataStoreCode();
			fields.put(summaryMetadataDataStoreCode, dto.getFields().get(summaryMetadataDataStoreCode));
		}

		return new SolrRecordDTO(dto.getId(), dto.getVersion(), Collections.unmodifiableMap(fields), dto.isSummary());

	}

	@Override
	public Record getRecord(String id) {
		RecordDTO recordDTO = memoryDataStore.get(id);
		if (recordDTO != null) {
			String collectionCode = (String) recordDTO.getFields().get(COLLECTION.getDataStoreCode());
			String schemaCode = (String) recordDTO.getFields().get(SCHEMA.getDataStoreCode());

			MetadataSchema schema = metadataSchemasManager.getSchemaTypes(collectionCode).getSchema(schemaCode);
			return new RecordImpl(schema, recordDTO);
		}
		return null;
	}

	@Override
	public Record getRecordSummary(String id) {
		RecordDTO recordDTO = memoryDataStore.get(id);
		if (recordDTO != null) {
			String collectionCode = (String) recordDTO.getFields().get(COLLECTION.getDataStoreCode());
			String schemaCode = (String) recordDTO.getFields().get(SCHEMA.getDataStoreCode());

			MetadataSchema schema = metadataSchemasManager.getSchemaTypes(collectionCode).getSchema(schemaCode);
			return new RecordImpl(schema, recordDTO);
		}
		return null;
	}

	@Override
	public synchronized void invalidateAll() {
		memoryDataStore.invalidate((R) -> true);
		fileSystemDataStore.clearAll();
	}

	@Override
	public void invalidate(String collection) {
		byte collectionId = modelLayerFactory.getCollectionsListManager().getCollectionInfo(collection).getCollectionId();
		memoryDataStore.stream(collectionId).filter((record) -> Boolean.TRUE).forEach(this::remove);
	}

	@Override
	public Stream<Record> stream(MetadataSchemaType type) {
		return memoryDataStore.stream(type.getCollectionInfo().getCollectionId(), type.getId()).map(dto -> new RecordImpl(dto, type.getCollectionInfo()));
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
	}


	private void loadCache(String collection) {

		for (MetadataSchemaType type : metadataSchemasManager.getSchemaTypes(collection).getSchemaTypes()) {
			SearchServices searchServices = modelLayerFactory.newSearchServices();

			if (type.getCacheType().hasPermanentCache()) {
				AtomicInteger added = new AtomicInteger();

				long count = searchServices.streamFromSolr(type, type.getCacheType().isSummaryCache()).count();
				searchServices.streamFromSolr(type, type.getCacheType().isSummaryCache()).forEach((record) -> {
					CacheInsertionStatus status = (insert(record, WAS_OBTAINED));
					LOGGER.info("Adding records " + record.getTypeCode() + " : " + added.incrementAndGet() + "/" + count);
					if (status != CacheInsertionStatus.ACCEPTED) {
						LOGGER.warn("Could not load record '" + record.getId() + "' in cache : " + status);
					}
				});
			}
		}
	}


	public static RecordsCaches2Impl createAndInitialize(ModelLayerFactory modelLayerFactory,
														 FileSystemRecordsValuesCacheDataStore fileSystemDataStore) {

		RecordsCachesDataStore memoryDataStore = new RecordsCachesDataStore(modelLayerFactory);
		RecordsCaches2Impl caches = new RecordsCaches2Impl(modelLayerFactory, fileSystemDataStore, memoryDataStore);

		for (String collection : modelLayerFactory.getCollectionsListManager().getCollections()) {
			caches.loadCache(collection);
		}

		return caches;

	}

	public RecordDTO prepareForCache(RecordDTO dto) {
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
			return new ByteArrayRecordDTOWithStringId(dto.getId(), schemaProvider, dto.getVersion(), dto.isSummary(),
					collectionInfo.getCode(), collectionInfo.getCollectionId(), type.getCode(), type.getId(),
					schema.getCode(), schema.getId(), bytesArray.bytesToKeepInMemory);
		} else {
			if (bytesArray.bytesToPersist != null && bytesArray.bytesToPersist.length > 0) {
				SummaryCacheSingletons.dataStore.saveIntKey(intId, bytesArray.bytesToPersist);
			} else {
				//SummaryCacheSingletons.dataStore.removeIntKey(intId);
			}
			return new ByteArrayRecordDTOWithIntegerId(intId, schemaProvider, dto.getVersion(), dto.isSummary(),
					collectionInfo.getCode(), collectionInfo.getCollectionId(), type.getCode(), type.getId(),
					schema.getCode(), schema.getId(), bytesArray.bytesToKeepInMemory);
		}

	}

}
