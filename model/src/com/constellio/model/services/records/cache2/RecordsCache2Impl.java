package com.constellio.model.services.records.cache2;

import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.services.cache.InsertionReason;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.RecordCacheType;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.cache.CacheConfig;
import com.constellio.model.services.records.cache.CacheInsertionStatus;
import com.constellio.model.services.records.cache.RecordsCache;
import com.constellio.model.services.schemas.SchemaUtils;
import org.mapdb.HTreeMap;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

public final class RecordsCache2Impl implements RecordsCache {

	private String collection;
	private byte collectionId;

	RecordsCaches2Impl caches;

	public RecordsCache2Impl(String collection,
							 byte collectionId,
							 ModelLayerFactory modelLayerFactory,
							 FileSystemRecordsValuesCacheDataStore fileSystemDataStore,
							 RecordsCachesDataStore memoryDataStore,
							 HTreeMap<String, RecordDTO> volatileCache,
							 RecordsCaches2Impl caches) {
		this.collection = collection;
		this.caches = caches;
		this.collectionId = collectionId;
	}

	@Override
	public Record get(String id) {
		return caches.get(id, collection);
	}

	@Override
	public Record getSummary(String id) {
		return caches.getRecordSummary(id, collection, null);
	}

	@Override
	public List<Record> getAllValues(String schemaType) {
		return caches.stream(collectionId, schemaType).collect(Collectors.toList());
	}

	@Override
	@Deprecated
	public List<Record> getAllValuesInUnmodifiableState(String schemaType) {
		return caches.getAllValuesInUnmodifiableState(collectionId, collection, schemaType);
	}

	@Override
	public CacheInsertionStatus insert(Record record, InsertionReason insertionReason) {
		return caches.insert(record, insertionReason);
	}

	@Override
	@Deprecated
	public void reloadSchemaType(String recordType, boolean onlyLocally, boolean forceVolatileCacheClear) {
		caches.reload(collectionId, collection, singletonList(recordType), onlyLocally, forceVolatileCacheClear);
	}


	@Override
	public void removeFromAllCaches(List<String> recordIds) {
		caches.removeFromAllCaches(collectionId, recordIds);
	}

	@Override
	public void removeFromAllCaches(String recordId) {
		removeFromAllCaches(asList(recordId));

	}

	@Override
	public void configureCache(CacheConfig cacheConfig) {

		MetadataSchemaType type = caches.metadataSchemasManager.getSchemaTypes(collection).getSchemaType(cacheConfig.getSchemaType());
		if (cacheConfig.isPermanent()) {
			if (!type.getCacheType().hasPermanentCache() || !type.getCacheType().isSummaryCache()) {
				throw new ImpossibleRuntimeException("Cache is not configured as expected : no permanent cache");
			}
		}

		if (cacheConfig.isVolatile()) {
			if (!type.getCacheType().hasVolatileCache()) {
				throw new ImpossibleRuntimeException("Cache is not configured as expected : no volatile cache");
			}
		}

		//Nothing
	}

	@Override
	public Collection<CacheConfig> getConfiguredCaches() {
		return caches.metadataSchemasManager.getSchemaTypes(collectionId).streamTypes().map((s) -> getCacheConfigOf(s.getCode()))
				.filter(x -> x != null).collect(Collectors.toList());
	}

	@Override
	public CacheConfig getCacheConfigOf(String schemaOrTypeCode) {
		String schemaTypeCode = SchemaUtils.getSchemaTypeCode(schemaOrTypeCode);
		MetadataSchemaType schemaType = caches.metadataSchemasManager.getSchemaTypes(collectionId).getSchemaType(schemaTypeCode);

		switch (schemaType.getCacheType()) {
			case NOT_CACHED:
				return null;

			case SUMMARY_CACHED_WITHOUT_VOLATILE:
				return CacheConfig.permanentCache(schemaType);

			case SUMMARY_CACHED_WITH_VOLATILE:
				return CacheConfig.volatileCache(schemaType, 0);

			case FULLY_CACHED:
				return CacheConfig.permanentCache(schemaType);

			case HOOK_ONLY:
				break;
		}

		return null;
	}

	@Override
	public void reloadAllSchemaTypes() {
		caches.reloadAllSchemaTypes(collection);
	}

	@Override
	public void invalidateVolatileReloadPermanent(List<String> schemaTypes, boolean onlyLocally) {
		caches.reload(collectionId, collection, schemaTypes, onlyLocally, true);
	}

	@Override
	public Record getByMetadata(Metadata metadata, String value) {
		if (value == null) {
			return null;
		}

		return caches.getByMetadata(collectionId, metadata, value);
	}

	@Override
	public Record getSummaryByMetadata(Metadata metadata, String value) {
		return caches.getSummaryByMetadata(collectionId, metadata, value);
	}

	@Override
	public boolean isConfigured(MetadataSchemaType type) {
		return type.getCacheType() != RecordCacheType.NOT_CACHED;
	}

	@Override
	public boolean isConfigured(String typeCode) {
		MetadataSchemaTypes types = caches.metadataSchemasManager.getSchemaTypes(collectionId);
		return types.hasType(typeCode) && types.getSchemaType(typeCode).getCacheType() != RecordCacheType.NOT_CACHED;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	public Stream<Record> streamVolatile(MetadataSchemaType schemaType) {
		return caches.streamVolatile(schemaType);

	}

}
