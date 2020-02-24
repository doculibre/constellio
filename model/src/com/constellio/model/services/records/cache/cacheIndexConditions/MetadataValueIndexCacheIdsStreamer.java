package com.constellio.model.services.records.cache.cacheIndexConditions;

import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.data.dao.dto.records.RecordId;
import com.constellio.model.services.records.cache.MetadataIndexCacheDataStore;

import java.util.Iterator;

public class MetadataValueIndexCacheIdsStreamer implements SortedIdsStreamer {

	private MetadataSchemaType schemaType;
	private Metadata metadata;
	private Object value;
	private MetadataIndexCacheDataStore dataStore;

	public MetadataValueIndexCacheIdsStreamer(MetadataSchemaType schemaType,
											  Metadata metadata, Object value, MetadataIndexCacheDataStore dataStore) {
		this.schemaType = schemaType;
		this.metadata = metadata;
		this.value = value;
		this.dataStore = dataStore;
	}

	public Iterator<RecordId> iterator() {
		return dataStore.iteratorIds(schemaType, metadata, value);

		//		dataStore.getLockMechanism().obtainSchemaTypeReadingPermit(schemaType);
		//		try {
		//			dataStore.search(schemaType, metadata, value);
		//
		//		} finally {
		//			dataStore.getLockMechanism().releaseSchemaTypeReadingPermit(schemaType);
		//		}


		//		Iterator<List<String>> batchIterator = new LazyIterator<List<String>>() {
		//
		//			@Override
		//			protected List<String> getNextOrNull() {
		//				dataStore.getLockMechanism().obtainSchemaTypeReadingPermit(schemaType);
		//				final List<String> batch = new ArrayList<>(READ_BATCH_LENGTH);
		//				try {
		//					return batch;
		//
		//				} finally {
		//					dataStore.getLockMechanism().releaseSchemaTypeReadingPermit(schemaType);
		//				}
		//
		//			}
		//		};
		//
		//		return new BatchConsumerIterator(batchIterator).stream();

	}
}
