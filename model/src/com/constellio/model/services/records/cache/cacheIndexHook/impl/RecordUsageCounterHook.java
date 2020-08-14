package com.constellio.model.services.records.cache.cacheIndexHook.impl;

import com.constellio.data.dao.dto.records.RecordId;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.cache.cacheIndexHook.MetadataIndexCacheDataStoreHook;
import com.constellio.model.services.schemas.MetadataSchemasManager;

import java.util.HashSet;
import java.util.Set;

public class RecordUsageCounterHook implements MetadataIndexCacheDataStoreHook<Integer> {

	private MetadataSchemasManager schemasManager;
	private String collection;

	public RecordUsageCounterHook(String collection, ModelLayerFactory modelLayerFactory) {
		this.collection = collection;
		this.schemasManager = modelLayerFactory.getMetadataSchemasManager();
	}

	@Override
	public String getCollection() {
		return collection;
	}

	@Override
	public boolean isHooked(MetadataSchemaType schemaType) {
		return schemaType.getCacheType().isSummaryCache()
			   && schemaType.getAllSchemas().stream().anyMatch(s -> !s.getReferencesToSummaryCachedType().isEmpty());
	}

	@Override
	public boolean requiresDataUpdate(Record record) {
		return record.isAnyModified(schemasManager.getSchemaOf(record).getReferencesToSummaryCachedType());
	}

	@Override
	public Set<Integer> getKeys(Record record) {

		Set<Integer> keys = new HashSet<>();
		for (Metadata metadata : schemasManager.getSchemaOf(record).getReferencesToSummaryCachedType()) {
			record.<String>getValues(metadata).forEach((stringId) -> keys.add(RecordId.toId(stringId).intValue()));
		}

		return keys;
	}


	@Override
	public Class<?> getKeyType() {
		return Integer.class;
	}

	@Override
	public int getKeyMemoryLength() {
		return Integer.BYTES;
	}

}
