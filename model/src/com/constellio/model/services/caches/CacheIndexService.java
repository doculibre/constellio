package com.constellio.model.services.caches;

import com.constellio.model.entities.CollectionInfo;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.rometools.utils.Strings;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CacheIndexService {
    private Map<String, Map<String,Map<String,Map<String,Object>>>> cacheIndexMap;

	public CacheIndexService() {
		cacheIndexMap = new HashMap<>();
	}

    public List<String> search(Metadata metadata, String value) {

		if (metadata == null) {
			throw new IllegalArgumentException("metadata parameter cannot be null");
		}

		if (!metadata.isCacheIndex() && !metadata.isUniqueValue()
			&& (metadata.getType() != MetadataValueType.REFERENCE || metadata.getType() != MetadataValueType.STRING)
			|| metadata.getLocalCode().equals(Schemas.IDENTIFIER.getLocalCode())) {
			throw new IllegalArgumentException("Metadata in parameter must be a cacheIndex or unique and not ID to search on this cache");
		}

		if (Strings.isBlank(value)) {
			return null;
		}

		return getRecordWithValue(metadata.getCollection(), metadata.getSchemaTypeCode(), metadata.getLocalCode(), value);
    }

    public void addUpdate(Record oldVersion, Record newVersion, MetadataSchema metadataSchema) {
		validateParameters(oldVersion, newVersion, metadataSchema);

		String collection = metadataSchema.getCollection();
		String schemaType = metadataSchema.getCode().substring(0, metadataSchema.getCode().indexOf("_"));

		for (Metadata currentMetadata : metadataSchema.getMetadatas().onlyNoIdCacheIndexAndUniqueReferenceOrString()) {
			if (oldVersion == null) {
				Object newValue = newVersion.get(currentMetadata);
				if (!isObjectNullOrEmpty(newValue)) {
					MapWithKeyReturnValue mapWithKeyReturnValue = getValueHashToModify(collection, schemaType, currentMetadata.getLocalCode(), true);
					addRecordIdToMapByValue(newValue, newVersion.getId(), mapWithKeyReturnValue.getValueRecordIdMap(), currentMetadata);
				}
			} else if (newVersion == null) {
				MapWithKeyReturnValue mapWithKeyReturnValue = getValueHashToModify(collection, schemaType, currentMetadata.getLocalCode(), false);

				if (mapWithKeyReturnValue != null) {
 					removeRecordIdToMapByValue(oldVersion.get(currentMetadata), oldVersion.getId(), mapWithKeyReturnValue.getValueRecordIdMap(), currentMetadata);
					cleanUpEmptyMap(mapWithKeyReturnValue);
				}
			} else {
				Object newValue = newVersion.get(currentMetadata);
				boolean isNewValueNull = isObjectNullOrEmpty(newValue);

				MapWithKeyReturnValue mapWithKeyReturnValue = getValueHashToModify(collection, schemaType, currentMetadata.getLocalCode(), !isNewValueNull);

				Object oldValue = oldVersion.get(currentMetadata);
				if (mapWithKeyReturnValue != null) {
					removeRecordIdToMapByValue(oldValue, oldVersion.getId(), mapWithKeyReturnValue.getValueRecordIdMap(), currentMetadata);
				}

				if (!isNewValueNull) {
					addRecordIdToMapByValue(newValue, newVersion.getId(), mapWithKeyReturnValue.getValueRecordIdMap(), currentMetadata);
				}
				if (mapWithKeyReturnValue != null) {
					cleanUpEmptyMap(mapWithKeyReturnValue);
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

	private void cleanUpEmptyMap(MapWithKeyReturnValue mapWithKeyReturnValue) {
		if (mapWithKeyReturnValue.getValueRecordIdMap().isEmpty()) {
			mapWithKeyReturnValue.getMetadataMap().remove(mapWithKeyReturnValue.getMetadataKey());

			if (mapWithKeyReturnValue.getMetadataMap().isEmpty()) {
				mapWithKeyReturnValue.getSchemaTypeMap().remove(mapWithKeyReturnValue.getSchemaTypeKey());

				if (mapWithKeyReturnValue.getSchemaTypeMap().isEmpty()) {
					cacheIndexMap.remove(mapWithKeyReturnValue.getCollectionKey());
				}
			}
		}
	}

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

	@Getter
	@AllArgsConstructor
	private class MapWithKeyReturnValue {
		private Map<String, Map<String, Map<String, Object>>> schemaTypeMap;
		private String collectionKey;

		private Map<String, Map<String, Object>> metadataMap;
		private String schemaTypeKey;

		private Map<String, Object> valueRecordIdMap;
		private String metadataKey;
	}

	private MapWithKeyReturnValue getValueHashToModify(String collection, String schemaType, String metadataCode,
													   boolean createIfNotExisitant) {

		if (Strings.isBlank(collection) || Strings.isBlank(schemaType) || Strings.isBlank(metadataCode)) {
			throw new IllegalArgumentException("All the parameters must be not equal to null");
		}

		Map<String, Map<String, Map<String, Object>>> schemaTypeMap = cacheIndexMap.get(collection);

		if (schemaTypeMap == null) {
			if (createIfNotExisitant) {
				schemaTypeMap = new HashMap<>();

				cacheIndexMap.put(collection, schemaTypeMap);
			} else {
				return null;
			}
		}

		Map<String, Map<String, Object>> metadataMap = schemaTypeMap.get(schemaType);

		if (metadataMap == null) {
			if (createIfNotExisitant) {
				metadataMap = new HashMap();
				schemaTypeMap.put(schemaType, metadataMap);
			} else {
				return null;
			}
		}

		Map<String, Object> valueRecordIdMap = metadataMap.get(metadataCode);

		if (valueRecordIdMap == null) {
			if (createIfNotExisitant) {
				valueRecordIdMap = new HashMap<>();

				metadataMap.put(metadataCode, valueRecordIdMap);
			} else {
				return null;
			}
		}

		return new MapWithKeyReturnValue(schemaTypeMap, collection, metadataMap, schemaType, valueRecordIdMap, metadataCode);
	}

	public int countByIterating() {
		int counter = 0;

		for (String collectionKey : cacheIndexMap.keySet()) {
			Map<String, Map<String, Map<String, Object>>> schemaTypeMap =
					nullToEmptyMap(cacheIndexMap.get(collectionKey));

			for (String schemaTypeKey : schemaTypeMap.keySet()) {
				Map<String, Map<String, Object>> metadataMap = nullToEmptyMap(schemaTypeMap.get(schemaTypeKey));

				for (String metadataKey : metadataMap.keySet()) {
					Map<String, Object> valueRecordIdMap = nullToEmptyMap(metadataMap.get(metadataKey));

					for (String value : valueRecordIdMap.keySet()) {
						Object recordIdObj = valueRecordIdMap.get(value);

						if (recordIdObj instanceof List) {
							counter += ((List) recordIdObj).size();
						} else if (recordIdObj != null) {
							counter++;
						}
					}
				}
			}
		}

		return counter;
	}

	public int numberOfEmptyMap() {
		int counter = 0;

		for (String collectionKey : cacheIndexMap.keySet()) {
			Map<String, Map<String, Map<String, Object>>> schemaTypeMap =
					nullToEmptyMap(cacheIndexMap.get(collectionKey));

			if (schemaTypeMap.isEmpty()) {
				counter++;
			}

			for (String schemaTypeKey : schemaTypeMap.keySet()) {
				Map<String, Map<String, Object>> metadataMap = nullToEmptyMap(schemaTypeMap.get(schemaTypeKey));

				if (metadataMap.isEmpty()) {
					counter++;
				}

				for (String metadataKey : metadataMap.keySet()) {
					Map<String, Object> valueRecordIdMap = nullToEmptyMap(metadataMap.get(metadataKey));

					if (valueRecordIdMap.isEmpty()) {
						counter++;
					}

					for (String value : valueRecordIdMap.keySet()) {
						if (valueRecordIdMap.isEmpty()) {
							counter++;
						}
					}
				}
			}
		}

		return counter;
	}

	private Map nullToEmptyMap(Map map) {
		if (map == null) {
			return new HashMap();
		} else {
			return map;
		}
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


	private List<String> getRecordWithValue(String collection, String schemaType, String metadataCode, String value) {
		if (Strings.isBlank(collection) || Strings.isBlank(schemaType) || Strings.isBlank(metadataCode) || Strings.isBlank(value)) {
			throw new IllegalArgumentException("All the parameters must have a value");
		}

		Map<String, Map<String,Map<String, Object>>> schemaTypeMap = cacheIndexMap.get(collection);
		if(schemaTypeMap == null || schemaTypeMap.isEmpty()) {
			return null;
		}

		Map<String, Map<String,Object>>  metadataMap = schemaTypeMap.get(schemaType);

		if(metadataMap == null || schemaTypeMap.isEmpty()) {
			return null;
		}

		Map<String, Object> valueRecordIdMap = metadataMap.get(metadataCode);

		if (valueRecordIdMap == null || valueRecordIdMap.isEmpty()) {
			return null;
		}

		Object referencesToRecordAsObject = valueRecordIdMap.get(value);

		if (referencesToRecordAsObject == null) {
			return null;
		} else if (referencesToRecordAsObject instanceof List) {
			return (List<String>) referencesToRecordAsObject;
		} else {
			List<String> referenceToRecordAsList = new ArrayList<String>();
			referenceToRecordAsList.add((String) referencesToRecordAsObject);
			return referenceToRecordAsList;
		}
	}


    public void clear(CollectionInfo collectionInfo) {
		cacheIndexMap.remove(collectionInfo.getCode());
    }

    public void clear(MetadataSchemaType metadataSchemaType) {
		Map metadataSchemaTypeMap = cacheIndexMap.get(metadataSchemaType.getCollection());

		if (metadataSchemaTypeMap == null) {
			return;
		}

		metadataSchemaTypeMap.remove(metadataSchemaType.getCode());
    }
}
