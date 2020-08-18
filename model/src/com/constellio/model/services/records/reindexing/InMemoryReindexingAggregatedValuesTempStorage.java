package com.constellio.model.services.records.reindexing;

import com.constellio.data.utils.KeyIntMap;
import com.constellio.model.entities.schemas.entries.AggregatedValuesEntry;
import com.constellio.model.services.records.reindexing.SystemReindexingConsumptionInfos.SystemReindexingConsumptionHeapInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.data.utils.LangUtils.sizeOf;

public class InMemoryReindexingAggregatedValuesTempStorage implements ReindexingAggregatedValuesTempStorage {

	private static final Logger LOGGER = LoggerFactory.getLogger(InMemoryReindexingAggregatedValuesTempStorage.class);

	private Map<String, KeyIntMap<String>> referenceCounts = new HashMap<>();

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
	public void incrementReferenceCount(String recordIdAggregatingValues, String aggregatedMetadataLocalCode) {
		if (!referenceCounts.containsKey(recordIdAggregatingValues)) {
			referenceCounts.put(recordIdAggregatingValues, new KeyIntMap<String>());
		}
		referenceCounts.get(recordIdAggregatingValues).increment(aggregatedMetadataLocalCode);
	}

	@Override
	public int getReferenceCount(String recordIdAggregatingValues, String aggregatedMetadataLocalCode) {
		KeyIntMap<String> keyIntMap = referenceCounts.get(recordIdAggregatingValues);
		return keyIntMap != null ? keyIntMap.get(aggregatedMetadataLocalCode) : 0;
	}

	@Override
	public void populateCacheConsumptionInfos(SystemReindexingConsumptionInfos infos) {
		infos.getHeapInfos().add(new SystemReindexingConsumptionHeapInfo(
				"InMemoryReindexingAggregatedValuesTempStorage.referenceCounts", sizeOf(referenceCounts), true));

		infos.getHeapInfos().add(new SystemReindexingConsumptionHeapInfo(
				"InMemoryReindexingAggregatedValuesTempStorage.entries", sizeOf(entries), true));
	}
}
