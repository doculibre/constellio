package com.constellio.model.services.caches;

import com.constellio.model.entities.CollectionInfo;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.rometools.utils.Strings;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CacheIndexService {
    private Map<String, Map<String,Map<String,Map<String,Object>>>> cacheIndexMap;

    public List<String> search(Metadata metadata, String value) {
        throw new NotImplementedException();
    }

    public void addUpdate(Record oldVersion, Record newVersion, MetadataSchema metadataSchema) {
        if(oldVersion == null) {
//            cacheIndexMap.put()
        }
    }

	private void setValue(String collection, String schemaType, String metadataCode, String value) {

		if (Strings.isBlank(collection) || Strings.isBlank(schemaType) || Strings.isBlank(metadataCode) || Strings.isBlank(value)) {
			throw new IllegalArgumentException("All the parameters must have a value");
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

		Map<String, Object> valueRecordIdMap = metadataMap.get(value);

		if (valueRecordIdMap == null) {
			valueRecordIdMap = new HashMap();

			metadataMap.put(value, valueRecordIdMap);
		}
	}

	private Map<String, Object> getValueIdMap(String collection, String schemaType, String metadataCode, String value) {


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

        Map<String, Object> valueRecordIdMap = metadataMap.get(value);

        return valueRecordIdMap;
    }

    private Map<String, Map<String,Map<String, Object>>> getMapOfCollection(String collection) {
        return cacheIndexMap.get(collection);
    }

    public void clear(CollectionInfo collectionInfo) {

    }

    public void clear(MetadataSchemaType metadataSchemaType) {

    }
}
