package com.constellio.model.services.records.cache.cacheIndexHook;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaType;

import java.util.Set;

public interface MetadataIndexCacheDataStoreHook<K> {

	boolean isHooked(MetadataSchemaType schemaType);

	boolean requiresDataUpdate(Record record);

	Set<K> getKeys(Record record);

	Class<?> getKeyType();

}
