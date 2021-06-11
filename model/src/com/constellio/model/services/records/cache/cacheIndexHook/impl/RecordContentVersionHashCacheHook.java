package com.constellio.model.services.records.cache.cacheIndexHook.impl;

import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.ContentVersion;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.cache.cacheIndexHook.MetadataIndexCacheDataStoreHook;
import com.constellio.model.services.schemas.MetadataSchemasManager;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RecordContentVersionHashCacheHook implements MetadataIndexCacheDataStoreHook<Integer> {
	private String collection;
	private MetadataSchemasManager metadataSchemasManager;

	public RecordContentVersionHashCacheHook(String collection, ModelLayerFactory appLayerFactory) {
		this.collection = collection;
		this.metadataSchemasManager = appLayerFactory.getMetadataSchemasManager();
	}

	@Override
	public String getCollection() {
		return collection;
	}

	@Override
	public boolean isHooked(MetadataSchemaType schemaType) {
		List<Metadata> contentMetadatas = schemaType.getContentMetadatas().stream().filter(m -> m.isStoredInSummaryCache()).collect(Collectors.toList());

		return contentMetadatas.size() != 0;
	}

	@Override
	public boolean requiresDataUpdate(Record record) {
		return getContentMetdataInSummaryCache(metadataSchemasManager, record).anyMatch(m -> record.isModified(m));
	}

	public static Stream<Metadata> getContentMetdataInSummaryCache(MetadataSchemasManager metadataSchemasManager,
																   Record record) {
		return record.
				getModifiedMetadataList(metadataSchemasManager.getSchemaTypes(record.getCollection()))
				.stream().filter(m -> m.getType() == MetadataValueType.CONTENT && m.isStoredInSummaryCache());
	}

	@Override
	public Set<Integer> getKeys(Record record) {

		List<Metadata> summaryContentMetadatas = metadataSchemasManager.getSchemaTypeOf(record).getContentMetadatas().stream().filter(m -> m.isStoredInSummaryCache()).collect(Collectors.toList());

		Set<Integer> valueAsSet = new HashSet<>();

		for (Metadata metadata : summaryContentMetadatas) {
			List<Content> contents = record.<Content>getValues(metadata);
			for (Content content : contents) {
				if (content != null) {
					for (ContentVersion currentContentVersion : content.getVersions()) {
						valueAsSet.add(currentContentVersion.getHash().hashCode());
					}
				}
			}
		}

		return valueAsSet;
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
