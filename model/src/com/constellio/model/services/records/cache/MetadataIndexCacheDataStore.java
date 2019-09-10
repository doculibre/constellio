package com.constellio.model.services.records.cache;

import com.constellio.data.utils.LangUtils;
import com.constellio.model.entities.CollectionInfo;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.records.RecordUtils;
import com.constellio.model.services.records.cache.offHeapCollections.SortedIdsList;
import com.constellio.model.services.records.cache.offHeapCollections.SortedIntIdsList;
import com.constellio.model.services.records.cache.offHeapCollections.SortedStringIdsList;
import com.rometools.utils.Strings;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.model.entities.schemas.MetadataValueType.REFERENCE;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static com.constellio.model.entities.schemas.Schemas.IDENTIFIER;

/**
 * Concurrency is handled at multiple levels.
 * <p>
 * - Map creation and iteration is synchronized on this
 * - Map get/put/iteration is synchronized on the given map
 */
public class MetadataIndexCacheDataStore {

	private static MetadataIndex EMPTY_INDEX = new MetadataIndex(Collections.emptyMap());


	private static class MetadataIndex {

		Map<Integer, SortedIdsList> map;

		public MetadataIndex(
				Map<Integer, SortedIdsList> map) {
			this.map = map;
		}

		public MetadataIndex() {
			this.map = new HashMap<>();
		}

		void add(String value, String id) {
			SortedIdsList list = map.get(value.hashCode());
			if (list == null) {
				list = new SortedIntIdsList();
				map.put(value.hashCode(), list);
			}

			int intId = RecordUtils.toIntKey(id);
			if (intId == RecordUtils.KEY_IS_NOT_AN_INT) {
				if (list instanceof SortedIntIdsList) {
					list = new SortedStringIdsList((SortedIntIdsList) list);
					map.put(value.hashCode(), list);
				}
				list.add(id);
			} else {
				list.add(intId);
			}
		}

		void add(List<String> values, String id) {
			if (values != null) {
				for (String value : values) {
					if (value != null) {
						add(value, id);
					}
				}
			}
		}

		void remove(String value, String id) {
			SortedIdsList list = map.get(value.hashCode());
			if (list != null) {
				list.remove(id);
			}
		}

		void remove(List<String> values, String id) {
			if (values != null) {
				for (String value : values) {
					if (value != null) {
						remove(value, id);
					}
				}
			}
		}

		public boolean isEmpty() {
			return map.isEmpty();
		}

		public List<String> getIds(String value) {

			SortedIdsList list = map.get(value.hashCode());
			return list == null ? Collections.emptyList() : list.getValues();
		}

		public int getIdsCount(String value) {
			SortedIdsList list = map.get(value.hashCode());
			return list == null ? 0 : list.size();
		}
	}

	private Map<Short, MetadataIndex>[][] cacheIndexMaps = new Map[256][];

	public List<String> search(MetadataSchemaType schemaType, Metadata metadata, String value) {

		ensureSearchable(metadata);

		if (Strings.isBlank(value)) {
			return Collections.emptyList();
		}

		MetadataIndex metadataIndex = getMetadataIndexMap(schemaType, metadata, false);
		return metadataIndex.getIds(value);
	}

	private void ensureSearchable(Metadata metadata) {
		if (metadata == null) {
			throw new IllegalArgumentException("metadata parameter cannot be null");
		}

		if (!metadata.isCacheIndex() && !metadata.isUniqueValue()
			&& (metadata.getType() != REFERENCE || metadata.getType() != STRING)
			|| metadata.getLocalCode().equals(IDENTIFIER.getLocalCode())) {
			throw new IllegalArgumentException("Metadata in parameter must be a cacheIndex or unique and not ID to search on this cache");
		}
	}


	public int estimateMaxResultSizeUsingIndexedMetadata(MetadataSchemaType schemaType, Metadata metadata,
														 String value) {
		ensureSearchable(metadata);

		if (Strings.isBlank(value)) {
			return -1;
		}

		MetadataIndex metadataIndex = getMetadataIndexMap(schemaType, metadata, false);
		return metadataIndex.getIdsCount(value);
	}

	public void addUpdate(Record oldVersion, Record newVersion, MetadataSchemaType schemaType, MetadataSchema schema) {
		long start = new Date().getTime();
		validateParameters(oldVersion, newVersion, schema);

		if (schemaType.getCollectionInfo().getCollectionId() != schema.getCollectionInfo().getCollectionId()) {
			throw new IllegalArgumentException("Schema type and schema have different collection id");
		}

		for (Metadata currentMetadata : schema.getCacheIndexMetadatas()) {
			if (oldVersion == null) {
				addRecordMetadata(newVersion, schemaType, currentMetadata);
			} else if (newVersion == null) {
				removeRecordMetadata(oldVersion, schemaType, currentMetadata);
			} else {
				updateRecordMetadata(oldVersion, newVersion, schemaType, currentMetadata);
			}
		}
	}

	private void updateRecordMetadata(Record oldVersion, Record newVersion, MetadataSchemaType schemaType,
									  Metadata currentMetadata) {
		if (oldVersion.getCollectionInfo().getCollectionId() != schemaType.getCollectionInfo().getCollectionId()) {
			throw new IllegalArgumentException("New version and schema type have different collection id");
		}
		if (newVersion.getCollectionInfo().getCollectionId() != schemaType.getCollectionInfo().getCollectionId()) {
			throw new IllegalArgumentException("New version and schema type have different collection id");
		}
		if (!oldVersion.getId().equals(newVersion.getId())) {
			throw new IllegalArgumentException("Records have different ids");
		}

		Object newValue = newVersion.get(currentMetadata);
		boolean isNewValueNull = isObjectNullOrEmpty(newValue);

		MetadataIndex metadataIndexMap = getMetadataIndexMap(schemaType, currentMetadata, !isNewValueNull);

		Object oldValue = oldVersion.get(currentMetadata);

		if (!LangUtils.isEqual(newValue, oldValue)) {
			if (!metadataIndexMap.isEmpty()) {
				removeRecordIdToMapByValue(oldValue, oldVersion.getId(), metadataIndexMap, currentMetadata);
			}

			if (!isNewValueNull) {
				addRecordIdToMapByValue(newValue, newVersion.getId(), metadataIndexMap, currentMetadata);
			}
		}
	}

	private void removeRecordMetadata(Record oldVersion, MetadataSchemaType schemaType, Metadata currentMetadata) {
		if (oldVersion.getCollectionInfo().getCollectionId() != schemaType.getCollectionInfo().getCollectionId()) {
			throw new IllegalArgumentException("New version and schema type have different collection id");
		}

		MetadataIndex metadataIndexMap = getMetadataIndexMap(schemaType, currentMetadata, false);

		if (!metadataIndexMap.isEmpty()) {
			removeRecordIdToMapByValue(oldVersion.get(currentMetadata), oldVersion.getId(), metadataIndexMap, currentMetadata);
		}
	}

	private void addRecordMetadata(Record newVersion, MetadataSchemaType schemaType, Metadata currentMetadata) {
		if (newVersion.getCollectionInfo().getCollectionId() != schemaType.getCollectionInfo().getCollectionId()) {
			throw new IllegalArgumentException("New version and schema type have different collection id");
		}

		Object newValue = newVersion.get(currentMetadata);
		if (!isObjectNullOrEmpty(newValue)) {
			MetadataIndex metadataIndexMap = getMetadataIndexMap(schemaType, currentMetadata, true);

			addRecordIdToMapByValue(newValue, newVersion.getId(), metadataIndexMap, currentMetadata);
		}
	}

	private static boolean isObjectNullOrEmpty(Object object) {
		if (object == null) {
			return true;
		} else if (object instanceof List && ((List) object).isEmpty()) {
			return true;
		} else {
			return false;
		}
	}

	//	private void cleanUpEmptyMap(MetadataSchemaType schemaType, Metadata metadata,
	//								 Map<String, Object> metadataIndexMap) {
	//		if (metadataIndexMap.isEmpty()) {
	//			int collectionIndex = schemaType.getId() - Byte.MIN_VALUE;
	//			cacheIndexMaps[collectionIndex][schemaType.getId()].get()
	//			mapWithKeyReturnValue.getMetadataMap().remove(mapWithKeyReturnValue.getMetadataKey());
	//
	//			if (mapWithKeyReturnValue.getMetadataMap().isEmpty()) {
	//				mapWithKeyReturnValue.getSchemaTypeMap().remove(mapWithKeyReturnValue.getSchemaTypeKey());
	//
	//				if (mapWithKeyReturnValue.getSchemaTypeMap().isEmpty()) {
	//					cacheIndexMap.remove(mapWithKeyReturnValue.getCollectionKey());
	//				}
	//			}
	//		}
	//	}

	private void validateParameters(Record oldVersion, Record newVersion, MetadataSchema metadataSchema) {
		if (metadataSchema == null) {
			throw new IllegalArgumentException("metadataSchema parameter cannot be null.");
		}

		if (oldVersion == null && newVersion == null) {
			throw new IllegalArgumentException("For these parameters : oldVersion and newVersion, one need to be not null.");
		}

		//		if (oldVersion != null && (!oldVersion.getCollection().equals(metadataSchema.getCollection())
		//								   || !metadataSchema.getCode().startsWith(oldVersion.getTypeCode()))) {
		//			throw new IllegalArgumentException("oldVersion parameter is not in the same collection or same schemaType than metadataSchema parameter");
		//		}
		//
		//		if (newVersion != null && (!newVersion.getCollection().equals(metadataSchema.getCollection())
		//								   || !metadataSchema.getCode().startsWith(newVersion.getTypeCode()))) {
		//			throw new IllegalArgumentException("newVersion parameter is not in the same collection or same schemaType than metadataSchema parameter");
		//		}
	}

	private MetadataIndex getMetadataIndexMap(MetadataSchemaType schemaType, Metadata metadata,
											  boolean createIfNotExisitant) {

		Map<Short, MetadataIndex>[] typeMaps = this.cacheIndexMaps[schemaType.getCollectionInfo().getCollectionIndex()];
		if (typeMaps == null) {
			if (createIfNotExisitant) {
				synchronized (this) {
					typeMaps = this.cacheIndexMaps[schemaType.getCollectionInfo().getCollectionIndex()];
					if (typeMaps == null) {
						typeMaps = this.cacheIndexMaps[schemaType.getCollectionInfo().getCollectionIndex()]
								= new Map[MetadataSchemaTypes.LIMIT_OF_TYPES_IN_COLLECTION];
					}
				}
			} else {
				return EMPTY_INDEX;
			}
		}

		Map<Short, MetadataIndex> metadataMaps = typeMaps[schemaType.getId()];
		if (metadataMaps == null) {
			if (createIfNotExisitant) {
				synchronized (this) {
					metadataMaps = typeMaps[schemaType.getId()];
					if (metadataMaps == null) {
						metadataMaps = typeMaps[schemaType.getId()] = new HashMap<>();
					}
				}
			} else {
				return EMPTY_INDEX;
			}
		}


		MetadataIndex metadataIndexMap = metadataMaps.get(metadata.getId());
		if (metadataIndexMap == null) {
			if (createIfNotExisitant) {
				synchronized (this) {
					metadataIndexMap = metadataMaps.computeIfAbsent(metadata.getId(), k -> new MetadataIndex());
				}
			} else {
				return EMPTY_INDEX;
			}
		}

		return metadataIndexMap;
	}

	public int countByIterating() {
		int counter = 0;

		for (int i = 0; i < cacheIndexMaps.length; i++) {
			Map<Short, MetadataIndex>[] typesMaps = cacheIndexMaps[i];
			if (typesMaps != null) {
				for (int j = 0; j < typesMaps.length; j++) {
					Map<Short, MetadataIndex> metadatasMaps = typesMaps[j];
					if (metadatasMaps != null) {
						synchronized (metadatasMaps) {
							for (MetadataIndex metadatasMap : metadatasMaps.values()) {

								synchronized (metadatasMap) {
									for (SortedIdsList value : metadatasMap.map.values()) {
										counter += value.size();
									}
								}
							}
						}
					}
				}
			}
		}
		return counter;
	}

	private void removeRecordIdToMapByValue(Object value, String recordId, MetadataIndex metadataIndex,
											Metadata metadata) {
		if (isObjectNullOrEmpty(value)) {
			return;
		}

		if (metadata.isUniqueValue()) {
			metadataIndex.remove((String) value, recordId);

		} else if (!metadata.isMultivalue()) {
			metadataIndex.remove((String) value, recordId);

		} else if (metadata.isMultivalue()) {
			metadataIndex.remove((List<String>) value, recordId);
		}
	}


	private void addRecordIdToMapByValue(Object value, String recordId, MetadataIndex metadataIndex,
										 Metadata metadata) {
		if (isObjectNullOrEmpty(value)) {
			return;
		}

		if (metadata.isUniqueValue()) {
			metadataIndex.add((String) value, recordId);
		} else if (!metadata.isMultivalue()) {
			metadataIndex.add((String) value, recordId);
		} else if (metadata.isMultivalue()) {
			List<String> valueList = (List<String>) value;
			metadataIndex.add(valueList, recordId);
		}
	}

	public void clear(CollectionInfo collectionInfo) {
		cacheIndexMaps[collectionInfo.getCollectionIndex()] = null;
	}

	public void clear(MetadataSchemaType metadataSchemaType) {

		Map[] typesMaps = cacheIndexMaps[metadataSchemaType.getCollectionInfo().getCollectionIndex()];
		if (typesMaps != null) {
			typesMaps[metadataSchemaType.getId()] = null;
		}
	}
}
