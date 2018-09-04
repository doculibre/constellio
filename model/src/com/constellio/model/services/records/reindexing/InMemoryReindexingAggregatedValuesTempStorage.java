package com.constellio.model.services.records.reindexing;

import com.constellio.data.utils.KeyIntMap;
import com.constellio.model.entities.schemas.entries.AggregatedValuesEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryReindexingAggregatedValuesTempStorage implements ReindexingAggregatedValuesTempStorage {

	private static final Logger LOGGER = LoggerFactory.getLogger(InMemoryReindexingAggregatedValuesTempStorage.class);

	private KeyIntMap<String> referenceCounts = new KeyIntMap<>();

	private Map<String, Map<String, Map<String, List<Object>>>> entries = new HashMap<>();

	@Override
	public void addOrReplace(String recordIdAggregatingValues, String recordId, String inputMetadataLocalCode,
							 List<Object> values) {
		Map<String, Map<String, List<Object>>> entriesOfAggregatingRecord = entries.get(recordIdAggregatingValues);
		if (entriesOfAggregatingRecord == null) {
			entriesOfAggregatingRecord = new HashMap<>();
			entries.put(recordIdAggregatingValues, entriesOfAggregatingRecord);
		}

		Map<String, List<Object>> entriesOfAggregatingRecordInputMetadata = entriesOfAggregatingRecord
				.get(inputMetadataLocalCode);
		if (entriesOfAggregatingRecordInputMetadata == null) {
			entriesOfAggregatingRecordInputMetadata = new HashMap<>();
			entriesOfAggregatingRecord.put(inputMetadataLocalCode, entriesOfAggregatingRecordInputMetadata);
		}

		entriesOfAggregatingRecordInputMetadata.put(recordId, values);
	}

	@Override
	public List<Object> getAllValues(String recordIdAggregatingValues, String inputMetadataLocalCode) {

		Map<String, Map<String, List<Object>>> entriesOfAggregatingRecord = entries.get(recordIdAggregatingValues);

		List<Object> returnedValues;
		if (entriesOfAggregatingRecord == null) {
			returnedValues = Collections.emptyList();

		} else {
			Map<String, List<Object>> entriesOfAggregatingRecordInputMetadata = entriesOfAggregatingRecord
					.get(inputMetadataLocalCode);
			if (entriesOfAggregatingRecordInputMetadata == null) {
				returnedValues = Collections.emptyList();

			} else {
				returnedValues = new ArrayList<>();
				for (List<Object> values : entriesOfAggregatingRecordInputMetadata.values()) {
					returnedValues.addAll(values);
				}
			}
		}

		return returnedValues;
	}

	@Override
	public void clear() {
		entries.clear();
		referenceCounts.clear();
	}

	@Override
	public List<AggregatedValuesEntry> getAllEntriesWithValues(String recordIdAggregatingValues) {

		Map<String, Map<String, List<Object>>> entriesOfAggregatingRecord = entries.get(recordIdAggregatingValues);

		if (entriesOfAggregatingRecord == null) {
			return Collections.emptyList();
		} else {
			List<AggregatedValuesEntry> returnedEntries = new ArrayList<>();
			for (Map.Entry<String, Map<String, List<Object>>> entry : entriesOfAggregatingRecord.entrySet()) {

				String metadata = entry.getKey();
				for (Map.Entry<String, List<Object>> entry2 : entry.getValue().entrySet()) {
					String recordId = entry2.getKey();
					List<Object> values = entry2.getValue();
					returnedEntries.add(new AggregatedValuesEntry(recordId, metadata, values));
				}

			}

			return returnedEntries;
		}
	}

	@Override
	public void incrementReferenceCount(String recordIdAggregatingValues) {
		referenceCounts.increment(recordIdAggregatingValues);
	}

	@Override
	public int getReferenceCount(String recordIdAggregatingValues) {
		return referenceCounts.get(recordIdAggregatingValues);
	}
}
