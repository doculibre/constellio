package com.constellio.model.services.caches;

import com.constellio.model.entities.CollectionInfo;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.rometools.utils.Strings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CacheIndexService {
    private Map<String, Map<String,Map<String,Map<String,Object>>>> cacheIndexMap;

    public List<String> search(Metadata metadata, String value) {
		if (!metadata.isCacheIndex()) {
			throw new IllegalArgumentException("Metadata in parameter must be a cacheIndex to search on this cache");
		}

		return getRecordWithValue(metadata.getCollection(), metadata.getSchemaTypeCode(), metadata.getLocalCode(), value);
    }

    public void addUpdate(Record oldVersion, Record newVersion, MetadataSchema metadataSchema) {
		validateRecords(oldVersion, newVersion, metadataSchema);

		String collection = metadataSchema.getCollection();
		String schemaType = metadataSchema.getCode();

		for (Metadata currentCacheIndexMetadata : metadataSchema.getMetadatas().onlyCacheIndex()) {
			Map recordIdByValue = getValueHashToModify(collection, schemaType, currentCacheIndexMetadata.getLocalCode());

			if (oldVersion == null) {
				addRecordIdToMapByValue(newVersion.get(currentCacheIndexMetadata), newVersion.getId(), recordIdByValue);
			} else if (newVersion == null) {
				removeRecordIdToMapByValue(oldVersion.get(currentCacheIndexMetadata), oldVersion.getId(), recordIdByValue);
			} else {
				removeRecordIdToMapByValue(oldVersion.get(currentCacheIndexMetadata), oldVersion.getId(), recordIdByValue);
				addRecordIdToMapByValue(newVersion.get(currentCacheIndexMetadata), newVersion.getId(), recordIdByValue);
			}
		}
    }

	private void validateRecords(Record oldVersion, Record newVersion, MetadataSchema metadataSchema) {
		if (oldVersion == null && newVersion == null) {
			throw new IllegalArgumentException("For these parameters : oldVersion and newVersion, one need to be not null.");
		}

		if (oldVersion != null && (!oldVersion.getCollection().equals(metadataSchema.getCollection())
								   || !oldVersion.getTypeCode().equals(metadataSchema.getCode()))) {
			throw new IllegalArgumentException("oldVersion parameter is not in the same collection or same schemaType than metadataSchema parameter");
		}

		if (newVersion != null && (!newVersion.getCollection().equals(metadataSchema.getCollection())
								   || !newVersion.getTypeCode().equals(metadataSchema.getCode()))) {
			throw new IllegalArgumentException("newVersion parameter is not in the same collection or same schemaType than metadataSchema parameter");
		}
	}

	private Map<String, Object> getValueHashToModify(String collection, String schemaType, String metadataCode) {

		if (Strings.isBlank(collection) || Strings.isBlank(schemaType) || Strings.isBlank(metadataCode)) {
			throw new IllegalArgumentException("All the parameters must be not equal to null");
		}

		Map<String, Map<String, Map<String, Object>>> schemaTypeMap = cacheIndexMap.get(collection);

		if (schemaTypeMap == null) {
			schemaTypeMap = new HashMap<>();

			cacheIndexMap.put(collection, schemaTypeMap);
		}

		Map<String, Map<String, Object>> metadataMap = schemaTypeMap.get(schemaType);

		if (metadataMap == null) {
			metadataMap = new HashMap();
			schemaTypeMap.put(schemaType, metadataMap);
		}

		Map<String, Object> valueRecordIdMap = metadataMap.get(metadataCode);

		if (valueRecordIdMap == null) {
			valueRecordIdMap = new HashMap<>();

			metadataMap.put(metadataCode, valueRecordIdMap);
		}

		return valueRecordIdMap;
	}

	private void removeRecordIdToMapByValue(String value, String recordId, Map<String, Object> valueRecordIdMap) {
		Object recordIdFromCache = valueRecordIdMap.get(value);

		if (recordIdFromCache == null) {

		} else if (recordIdFromCache instanceof List) {
			List listOfRecordWithValueInCache = ((List) recordIdFromCache);
			listOfRecordWithValueInCache.remove(recordId);
		} else {
			valueRecordIdMap.remove(value);
		}
	}

	private void addRecordIdToMapByValue(String value, String recordId, Map<String, Object> valueRecordIdMap) {
		Object recordIdFromCache = valueRecordIdMap.get(value);

		if (recordIdFromCache == null) {
			valueRecordIdMap.put(value, valueRecordIdMap);
		} else if (recordIdFromCache instanceof List) {
			List listOfRecordWithValueInCache = ((List) recordIdFromCache);
			if (!listOfRecordWithValueInCache.contains(recordId)) {
				listOfRecordWithValueInCache.add(recordId);
			}
		} else {
			String valueAsString = (String) recordIdFromCache;

			if (!valueAsString.equals(recordId)) {
				List listToPutInCache = new ArrayList();
				listToPutInCache.add(valueAsString);
				listToPutInCache.add(recordId);

				valueRecordIdMap.put(value, listToPutInCache);
			}
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

		if (referencesToRecordAsObject instanceof List) {
			return (List<String>) referencesToRecordAsObject;
		} else {
			List<String> referenceToRecordAsList = new ArrayList<String>();
			referenceToRecordAsList.add((String) referencesToRecordAsObject);
			return referenceToRecordAsList;
		}
	}

    private Map<String, Map<String,Map<String, Object>>> getMapOfCollection(String collection) {
        return cacheIndexMap.get(collection);
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
