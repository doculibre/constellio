package com.constellio.model.services.records.cache.cacheIndexHook.impl;

import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.ContentVersion;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.records.cache.cacheIndexHook.RecordIdsHookDataIndexRetriever;
import com.constellio.model.services.schemas.MetadataSchemasManager;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RecordContentVersionHashCacheHookRetriever {
	RecordIdsHookDataIndexRetriever<Integer> retriever;
	SchemasRecordsServices schemas;
	private MetadataSchemasManager metadataSchemasManager;
	private String collection;
	private RecordServices recordServices;

	public RecordContentVersionHashCacheHookRetriever(String collection,
													  RecordIdsHookDataIndexRetriever<Integer> retriever,
													  ModelLayerFactory modelLayerFactory) {
		this.retriever = retriever;
		this.schemas = new SchemasRecordsServices(collection, modelLayerFactory);
		this.metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
		this.recordServices = modelLayerFactory.newRecordServices();
		this.collection = collection;
	}

	public List<Record> getRecordsWithContent(String hashCode) {
		List<Record> recordFound = new ArrayList<>();

		retriever.recordIdsStreamerWithKey(hashCode.hashCode()).stream().forEach(recordId -> {

			Record record = recordServices.getRecordSummaryById(collection, recordId.stringValue());
			List<Metadata> contentMetadatas = metadataSchemasManager.getSchemaTypeOf(record).getContentMetadatas().stream().filter(m -> m.isStoredInSummaryCache()).collect(Collectors.toList());

			for (Metadata metadata : contentMetadatas) {
				for (Content content : record.<Content>getValues(metadata)) {

					if (addRecordWhenHashIsFound(hashCode, recordFound, record, content)) {
						break;
					}
				}
			}
		});

		return recordFound;
	}

	private boolean addRecordWhenHashIsFound(String hashCode, List<Record> recordFound, Record record,
											 Content content) {
		for (ContentVersion currentContentVersion : content.getVersions()) {
			if (hashCode.equals(currentContentVersion.getHash())) {
				recordFound.add(record);
				return true;
			}
		}

		return false;
	}
}
