package com.constellio.model.services.records.reindexing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.model.entities.schemas.Metadata;

public class InMemoryReindexingAggregatedValuesTempStorage implements ReindexingAggregatedValuesTempStorage {

	private static final Logger LOGGER = LoggerFactory.getLogger(InMemoryReindexingAggregatedValuesTempStorage.class);

	Map<String, Map<String, Map<String, List<Object>>>> entries = new HashMap<>();

	@Override
	public void addOrReplace(String recordIdAggregatingValues, String recordId, Metadata inputMetadata, List<Object> values) {
		//		LOGGER.info("addOrReplace(aggregatingId=" + recordIdAggregatingValues + ", recordId=" + recordId + ", inputMetadata="
		//				+ inputMetadata.getCode() + ", values=" + values);
		Map<String, Map<String, List<Object>>> entriesOfAggregatingRecord = entries.get(recordIdAggregatingValues);
		if (entriesOfAggregatingRecord == null) {
			entriesOfAggregatingRecord = new HashMap<>();
			entries.put(recordIdAggregatingValues, entriesOfAggregatingRecord);
		}

		Map<String, List<Object>> entriesOfAggregatingRecordInputMetadata = entriesOfAggregatingRecord
				.get(inputMetadata.getLocalCode());
		if (entriesOfAggregatingRecordInputMetadata == null) {
			entriesOfAggregatingRecordInputMetadata = new HashMap<>();
			entriesOfAggregatingRecord.put(inputMetadata.getLocalCode(), entriesOfAggregatingRecordInputMetadata);
		}

		entriesOfAggregatingRecordInputMetadata.put(recordId, values);
//		LOGGER.info("addOrReplace(aggregatingId=" + recordIdAggregatingValues + ", recordId=" + recordId + ", inputMetadata="
		//				+ inputMetadata.getCode() + ", values=" + entriesOfAggregatingRecordInputMetadata.g);
	}

	@Override
	public List<Object> getAllValues(String recordIdAggregatingValues, Metadata inputMetadata) {

		Map<String, Map<String, List<Object>>> entriesOfAggregatingRecord = entries.get(recordIdAggregatingValues);

		List<Object> returnedValues;
		if (entriesOfAggregatingRecord == null) {
			returnedValues = Collections.emptyList();

		} else {
			Map<String, List<Object>> entriesOfAggregatingRecordInputMetadata = entriesOfAggregatingRecord
					.get(inputMetadata.getLocalCode());
			if (entriesOfAggregatingRecordInputMetadata == null) {
				returnedValues = Collections.emptyList();

			} else {
				returnedValues = new ArrayList<>();
				for (List<Object> values : entriesOfAggregatingRecordInputMetadata.values()) {
					returnedValues.addAll(values);
				}
			}
		}

		//		LOGGER.info("getAllValues(aggregatingId=" + recordIdAggregatingValues + ", inputMetadata=" + inputMetadata.getCode()
		//				+ " =>" + returnedValues);
		return returnedValues;
	}

	@Override
	public void clear() {
		entries.clear();
	}
}
