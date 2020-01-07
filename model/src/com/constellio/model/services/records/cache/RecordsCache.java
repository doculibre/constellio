package com.constellio.model.services.records.cache;

import com.constellio.data.dao.services.cache.InsertionReason;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public interface RecordsCache {

	Record get(String id);

	Record getSummary(String id);

	List<Record> getAllValues(String schemaType);

	List<Record> getAllValuesInUnmodifiableState(String schemaType);

	CacheInsertionResponse insert(Record record, InsertionReason insertionReason);

	@Deprecated
	default void reloadSchemaType(String recordType, boolean forceVolatileCacheClear) {
		reloadSchemaType(recordType, false, forceVolatileCacheClear);
	}

	@Deprecated
	void reloadSchemaType(String recordType, boolean onlyLocally, boolean forceVolatileCacheClear);

	void removeFromAllCaches(List<String> recordIds);

	void removeFromAllCaches(String recordId);

	@Deprecated
	void configureCache(CacheConfig cacheConfig);

	@Deprecated
	Collection<CacheConfig> getConfiguredCaches();

	@Deprecated
	CacheConfig getCacheConfigOf(String schemaOrTypeCode);

	default void reloadAllSchemaTypes() {
		throw new UnsupportedOperationException("Unsupported");
	}

	default void invalidateVolatileReloadPermanent(List<String> schemaTypes) {
		invalidateVolatileReloadPermanent(schemaTypes, false);
	}

	default void invalidateVolatile(boolean onlyLocally) {
		invalidateVolatileReloadPermanent(Collections.emptyList(), onlyLocally);
	}

	void invalidateVolatileReloadPermanent(List<String> schemaTypes, boolean onlyLocally);

	Record getByMetadata(Metadata metadata, String value);

	Record getSummaryByMetadata(Metadata metadata, String value);

	@Deprecated
	boolean isConfigured(MetadataSchemaType type);

	@Deprecated
	boolean isConfigured(String typeCode);

	boolean isEmpty();

	@Deprecated
	default boolean isCached(String id) {
		return getSummary(id) != null;
	}

	default List<CacheInsertionResponse> insert(List<Record> records, InsertionReason insertionReason) {
		List<CacheInsertionResponse> statuses = new ArrayList<>(records.size());

		for (Record record : records) {
			statuses.add(insert(record, insertionReason));
		}

		return statuses;
	}

	default Stream<Record> streamVolatile(MetadataSchemaType schemaType) {
		throw new UnsupportedOperationException();
	}
}
