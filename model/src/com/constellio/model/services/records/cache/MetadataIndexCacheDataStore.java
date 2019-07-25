package com.constellio.model.services.records.cache;

import com.constellio.model.entities.CollectionInfo;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.rometools.utils.Strings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Concurrency is handled at multiple levels.
 * <p>
 * - Map creation and iteration is synchronized on this
 * - Map get/put/iteration is synchronized on the given map
 */
public class MetadataIndexCacheDataStore {


	private Map<Short, Map<String, Object>>[][] cacheIndexMaps = new Map[256][];

	public List<String> search(MetadataSchemaType schemaType, Metadata metadata, String value) {

		if (metadata == null) {
			throw new IllegalArgumentException("metadata parameter cannot be null");
		}

		if (!metadata.isCacheIndex() && !metadata.isUniqueValue()
			&& (metadata.getType() != MetadataValueType.REFERENCE || metadata.getType() != MetadataValueType.STRING)
			|| metadata.getLocalCode().equals(Schemas.IDENTIFIER.getLocalCode())) {
			throw new IllegalArgumentException("Metadata in parameter must be a cacheIndex or unique and not ID to search on this cache");
		}

		if (Strings.isBlank(value)) {
			return Collections.emptyList();
		}

		Map<String, Object> metadataIndexMap = getMetadataIndexMap(schemaType, metadata, false);
		return getRecordsWithValue(metadataIndexMap, value);
	}

	public void addUpdate(Record oldVersion, Record newVersion, MetadataSchemaType schemaType, MetadataSchema schema) {
		validateParameters(oldVersion, newVersion, schema);

		if (schemaType.getCollectionInfo().getCollectionId() != schema.getCollectionInfo().getCollectionId()) {
			throw new IllegalArgumentException("Schema type and schema have different collection id");
		}

		for (Metadata currentMetadata : schema.getMetadatas().onlyNoIdCacheIndexAndUniqueReferenceOrString()) {
			if (oldVersion == null) {
				if (newVersion.getCollectionInfo().getCollectionId() != schemaType.getCollectionInfo().getCollectionId()) {
					throw new IllegalArgumentException("New version and schema type have different collection id");
				}

				Object newValue = newVersion.get(currentMetadata);
				if (!isObjectNullOrEmpty(newValue)) {
					Map<String, Object> metadataIndexMap = getMetadataIndexMap(schemaType, currentMetadata, true);
					addRecordIdToMapByValue(newValue, newVersion.getId(), metadataIndexMap, currentMetadata);
				}
			} else if (newVersion == null) {
				if (oldVersion.getCollectionInfo().getCollectionId() != schemaType.getCollectionInfo().getCollectionId()) {
					throw new IllegalArgumentException("New version and schema type have different collection id");
				}

				Map<String, Object> metadataIndexMap = getMetadataIndexMap(schemaType, currentMetadata, false);

				if (!metadataIndexMap.isEmpty()) {
					removeRecordIdToMapByValue(oldVersion.get(currentMetadata), oldVersion.getId(), metadataIndexMap, currentMetadata);
				}
			} else {
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

				Map<String, Object> metadataIndexMap = getMetadataIndexMap(schemaType, currentMetadata, !isNewValueNull);

				Object oldValue = oldVersion.get(currentMetadata);
				if (!metadataIndexMap.isEmpty()) {
					removeRecordIdToMapByValue(oldValue, oldVersion.getId(), metadataIndexMap, currentMetadata);
				}

				if (!isNewValueNull) {
					addRecordIdToMapByValue(newValue, newVersion.getId(), metadataIndexMap, currentMetadata);
				}
				if (!metadataIndexMap.isEmpty()) {
				}
			}
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

		if (oldVersion != null && (!oldVersion.getCollection().equals(metadataSchema.getCollection())
								   || !metadataSchema.getCode().startsWith(oldVersion.getTypeCode()))) {
			throw new IllegalArgumentException("oldVersion parameter is not in the same collection or same schemaType than metadataSchema parameter");
		}

		if (newVersion != null && (!newVersion.getCollection().equals(metadataSchema.getCollection())
								   || !metadataSchema.getCode().startsWith(newVersion.getTypeCode()))) {
			throw new IllegalArgumentException("newVersion parameter is not in the same collection or same schemaType than metadataSchema parameter");
		}
	}

	private Map<String, Object> getMetadataIndexMap(MetadataSchemaType schemaType, Metadata metadata,
													boolean createIfNotExisitant) {

		Map<Short, Map<String, Object>>[] typeMaps = this.cacheIndexMaps[schemaType.getCollectionInfo().getCollectionIndex()];
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
				return Collections.emptyMap();
			}
		}

		Map<Short, Map<String, Object>> metadataMaps = typeMaps[schemaType.getId()];
		if (metadataMaps == null) {
			if (createIfNotExisitant) {
				synchronized (this) {
					metadataMaps = typeMaps[schemaType.getId()];
					if (metadataMaps == null) {
						metadataMaps = typeMaps[schemaType.getId()] = new HashMap<>();
					}
				}
			} else {
				return Collections.emptyMap();
			}
		}


		Map<String, Object> metadataIndexMap = metadataMaps.get(metadata.getId());
		if (metadataIndexMap == null) {
			if (createIfNotExisitant) {
				synchronized (this) {
					metadataIndexMap = metadataMaps.computeIfAbsent(metadata.getId(), k -> new HashMap<>());
				}
			} else {
				return Collections.emptyMap();
			}
		}

		return metadataIndexMap;
	}

	public int countByIterating() {
		int counter = 0;

		for (int i = 0; i < cacheIndexMaps.length; i++) {
			Map<Short, Map<String, Object>>[] typesMaps = cacheIndexMaps[i];
			if (typesMaps != null) {
				for (int j = 0; j < typesMaps.length; j++) {
					Map<Short, Map<String, Object>> metadatasMaps = typesMaps[j];
					if (metadatasMaps != null) {
						synchronized (metadatasMaps) {
							for (Map<String, Object> metadatasMap : metadatasMaps.values()) {

								synchronized (metadatasMap) {
									for (Object value : metadatasMap.values()) {
										if (value instanceof List) {
											counter += ((List) value).size();
										} else if (value != null) {
											counter++;
										}
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

	private void removeRecordIdToMapByValue(Object value, String recordId, Map<String, Object> valueRecordIdMap,
											Metadata metadata) {
		if (isObjectNullOrEmpty(value)) {
			return;
		}

		if (metadata.isUniqueValue()) {
			valueRecordIdMap.remove(value);
		} else if (!metadata.isMultivalue()) {
			removeValueToCache((String) value, recordId, valueRecordIdMap);
		} else if (metadata.isMultivalue()) {
			List<String> valueList = (List<String>) value;

			for (String currentValue : valueList) {
				removeValueToCache(currentValue, recordId, valueRecordIdMap);
			}
		}
	}

	private void removeValueToCache(String value, String recordId, Map<String, Object> valueRecordIdMap) {
		List<String> recordIdFromCache = (List<String>) valueRecordIdMap.get(value);

		if (recordIdFromCache == null) {
			return;
		}

		recordIdFromCache.remove(recordId);

		if (recordIdFromCache.isEmpty()) {
			valueRecordIdMap.remove(value);
		}
	}

	private void addRecordIdToMapByValue(Object value, String recordId, Map<String, Object> valueRecordIdMap,
										 Metadata metadata) {
		if (isObjectNullOrEmpty(value)) {
			return;
		}

		if (metadata.isUniqueValue()) {
			valueRecordIdMap.put((String) value, recordId);
		} else if (!metadata.isMultivalue()) {
			addValueToCache((String) value, recordId, valueRecordIdMap);
		} else if (metadata.isMultivalue()) {
			List<String> valueList = (List<String>) value;

			for (String currentValue : valueList) {
				addValueToCache(currentValue, recordId, valueRecordIdMap);
			}
		}
	}

	private void addValueToCache(String value, String recordId, Map<String, Object> valueRecordIdMap) {
		List<String> recordIdFromCache = (List<String>) valueRecordIdMap.get(value);

		if (recordIdFromCache == null) {
			recordIdFromCache = new ArrayList();
			valueRecordIdMap.put(value, recordIdFromCache);
		}

		if (!recordIdFromCache.contains(recordId)) {
			recordIdFromCache.add(recordId);
		}
	}


	private List<String> getRecordsWithValue(Map<String, Object> metadataIndexMap, String value) {

		Object referencesToRecordAsObject = metadataIndexMap.get(value);

		if (referencesToRecordAsObject == null) {
			return Collections.emptyList();

		} else if (referencesToRecordAsObject instanceof List) {
			return (List<String>) referencesToRecordAsObject;

		} else {
			return Collections.singletonList((String) referencesToRecordAsObject);
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
