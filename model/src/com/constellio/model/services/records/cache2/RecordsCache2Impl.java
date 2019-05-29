package com.constellio.model.services.records.cache2;

import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.services.cache.InsertionReason;
import com.constellio.model.entities.CollectionInfo;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.RecordCacheType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordImpl;
import com.constellio.model.services.records.cache.CacheConfig;
import com.constellio.model.services.records.cache.CacheInsertionStatus;
import com.constellio.model.services.records.cache.RecordsCache;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.SchemaUtils;
import org.mapdb.HTreeMap;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.constellio.data.utils.LangUtils.isEqual;
import static com.constellio.model.entities.schemas.Schemas.COLLECTION;
import static com.constellio.model.entities.schemas.Schemas.SCHEMA;
import static java.util.Arrays.asList;

public class RecordsCache2Impl implements RecordsCache {

	private String collection;
	private byte collectionId;

	private CollectionsListManager collectionsListManager;
	private ModelLayerFactory modelLayerFactory;
	private MetadataSchemasManager metadataSchemasManager;
	private FileSystemRecordsValuesCacheDataStore fileSystemDataStore;
	private RecordsCachesDataStore memoryDataStore;
	private HTreeMap<String, RecordDTO> volatileCache;

	private RecordsCaches2Impl recordsCaches2;

	public RecordsCache2Impl(String collection,
							 byte collectionId,
							 ModelLayerFactory modelLayerFactory,
							 FileSystemRecordsValuesCacheDataStore fileSystemDataStore,
							 RecordsCachesDataStore memoryDataStore,
							 HTreeMap<String, RecordDTO> volatileCache,
							 RecordsCaches2Impl recordsCaches2) {
		this.collection = collection;
		this.memoryDataStore = memoryDataStore;
		this.collectionId = collectionId;
		this.modelLayerFactory = modelLayerFactory;
		this.collectionsListManager = modelLayerFactory.getCollectionsListManager();
		this.metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
		this.fileSystemDataStore = fileSystemDataStore;
		this.volatileCache = volatileCache;
		this.recordsCaches2 = recordsCaches2;
	}

	@Override
	public Record get(String id) {
		RecordDTO recordDTO = memoryDataStore.get(id);
		if (recordDTO != null) {
			String collectionCode = (String) recordDTO.getFields().get(COLLECTION.getDataStoreCode());
			String schemaCode = (String) recordDTO.getFields().get(SCHEMA.getDataStoreCode());

			if (collectionCode.equals(collection) && !recordDTO.isSummary()) {
				MetadataSchema schema = metadataSchemasManager.getSchemaTypes(collection).getSchema(schemaCode);
				return new RecordImpl(schema, recordDTO);
			}
		}
		return null;
	}

	@Override
	public Record getSummary(String id) {
		RecordDTO recordDTO = memoryDataStore.get(id);
		if (recordDTO != null) {
			String collectionCode = (String) recordDTO.getFields().get(COLLECTION.getDataStoreCode());
			String schemaCode = (String) recordDTO.getFields().get(SCHEMA.getDataStoreCode());

			if (collectionCode.equals(collection)) {
				MetadataSchema schema = metadataSchemasManager.getSchemaTypes(collection).getSchema(schemaCode);
				return new RecordImpl(schema, recordDTO);
			}
		}
		return null;
	}

	@Override
	public List<Record> getAllValues(String schemaType) {
		return stream(schemaType).collect(Collectors.toList());
	}

	private Stream<Record> stream() {
		return memoryDataStore.stream(collectionId).map(recordDTO -> {
			CollectionInfo collectionInfo = collectionsListManager.getCollectionInfo(recordDTO.getCollection());
			return (Record) new RecordImpl(recordDTO, collectionInfo);
		});
	}

	private Stream<Record> stream(String schemaType) {
		short typeId = metadataSchemasManager.getSchemaTypes(collectionId).getSchemaType(schemaType).getId();
		return memoryDataStore.stream(collectionId, typeId).filter(recordDTO -> collection.equals(recordDTO.getCollection())).map(recordDTO -> {
			CollectionInfo collectionInfo = collectionsListManager.getCollectionInfo(recordDTO.getCollection());
			return (Record) new RecordImpl(recordDTO, collectionInfo);
		});
	}

	@Override
	@Deprecated
	public List<Record> getAllValuesInUnmodifiableState(String schemaType) {
		CollectionInfo collectionInfo = collectionsListManager.getCollectionInfo(collection);
		short typeId = metadataSchemasManager.getSchemaTypes(collectionId).getSchemaType(schemaType).getId();
		return memoryDataStore.stream(collectionId, typeId).map(dto -> new RecordImpl(dto, collectionInfo)).collect(Collectors.toList());
	}

	@Override
	public CacheInsertionStatus insert(Record record, InsertionReason insertionReason) {
		return recordsCaches2.insert(record, insertionReason);
	}

	@Override
	public void invalidateRecordsOfType(String recordType) {
		short typeId = metadataSchemasManager.getSchemaTypes(collectionId).getSchemaType(recordType).getId();
		memoryDataStore.stream(collectionId, typeId).filter((record) -> Boolean.TRUE).forEach(this::remove);
	}

	private void remove(RecordDTO recordDTO) {
		memoryDataStore.remove(recordDTO);
	}

	@Override
	public void invalidate(List<String> recordIds) {
		memoryDataStore.stream(collectionId, recordIds).forEach(this::remove);
	}

	@Override
	public void invalidate(String recordId) {
		invalidate(asList(recordId));

	}

	@Override
	public void configureCache(CacheConfig cacheConfig) {
		//Nothing
	}

	@Override
	public Collection<CacheConfig> getConfiguredCaches() {
		return metadataSchemasManager.getSchemaTypes(collectionId).streamTypes().map((s) -> getCacheConfigOf(s.getCode()))
				.filter(x -> x != null).collect(Collectors.toList());
	}

	@Override
	public CacheConfig getCacheConfigOf(String schemaOrTypeCode) {
		String schemaTypeCode = SchemaUtils.getSchemaTypeCode(schemaOrTypeCode);
		MetadataSchemaType schemaType = metadataSchemasManager.getSchemaTypes(collectionId).getSchemaType(schemaTypeCode);

		switch (schemaType.getCacheType()) {
			case NOT_CACHED:
				return null;

			case SUMMARY_CACHED_WITHOUT_VOLATILE:
				return CacheConfig.permanentCache(schemaType);

			case SUMMARY_CACHED_WITH_VOLATILE:
				return CacheConfig.volatileCache(schemaType, 0);

			case FULLY_CACHED:
				return CacheConfig.permanentCache(schemaType);

			case HOOK:
				break;
		}

		return null;
	}

	@Override
	public void invalidateAll() {
		memoryDataStore.stream().forEach(this::remove);
		fileSystemDataStore.clearAll();
	}

	@Override
	public Record getByMetadata(Metadata metadata, String value) {

		if (metadata.isSameLocalCode(Schemas.IDENTIFIER)) {
			return get(value);
		}

		MetadataSchemaType schemaType = metadataSchemasManager.getSchemaTypes(collectionId)
				.getSchemaType(metadata.getSchemaTypeCode());

		if (schemaType.getCacheType() == RecordCacheType.FULLY_CACHED) {
			return stream(metadata.getSchemaTypeCode())
					.filter(record -> isEqual(value, record.get(metadata))).findFirst().orElse(null);
		} else {
			return null;
		}

	}

	@Override
	public Record getSummaryByMetadata(Metadata metadata, String value) {

		if (metadata.isSameLocalCode(Schemas.IDENTIFIER)) {
			return getSummary(value);
		}

		MetadataSchemaType schemaType = metadataSchemasManager.getSchemaTypes(collectionId)
				.getSchemaType(metadata.getSchemaTypeCode());

		if (schemaType.getCacheType().isSummaryCache() || schemaType.getCacheType() == RecordCacheType.FULLY_CACHED) {
			return stream(metadata.getSchemaTypeCode())
					.filter(record -> isEqual(value, record.get(metadata))).findFirst().orElse(null);
		} else {
			return null;
		}
	}

	@Override
	public void removeCache(String schemaType) {
		//throw new UnsupportedOperationException("Unsupported");
	}

	@Override
	public boolean isConfigured(MetadataSchemaType type) {
		return type.getCacheType() != RecordCacheType.NOT_CACHED;
	}

	@Override
	public boolean isConfigured(String typeCode) {
		MetadataSchemaType schemaType = metadataSchemasManager.getSchemaTypes(collectionId).getSchemaType(typeCode);
		return isConfigured(schemaType);
	}

	@Override
	public boolean isEmpty() {
		return false;
	}
}
