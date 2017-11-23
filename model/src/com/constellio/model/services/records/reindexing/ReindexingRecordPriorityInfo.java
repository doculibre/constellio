package com.constellio.model.services.records.reindexing;

import java.util.HashMap;
import java.util.Map;

public class ReindexingRecordPriorityInfo {

	Map<String, Integer> lastDependencyLevels = new HashMap<>();

	Map<String, Integer> values = new HashMap<>();

	public Integer getLastIterationOf(int depedencyLevel, String schemaType) {
		return lastDependencyLevels.get(depedencyLevel + "-" + schemaType);
	}

	public void markHasHandledAtIteration(int depedencyLevel, String schemaType, String recordId, int iteration) {
		values.put(depedencyLevel + "-" + schemaType + "-" + recordId, iteration);
		Integer currentLastDependencyLevels = getLastIterationOf(depedencyLevel, schemaType);
		if (currentLastDependencyLevels == null || currentLastDependencyLevels < iteration) {
			values.put(depedencyLevel + "-" + schemaType, iteration);
		}
	}

	public Integer getIterationOf(int depedencyLevel, String schemaType, String recordId) {
		return values.get(depedencyLevel + "-" + schemaType + "-" + recordId);
	}

}
