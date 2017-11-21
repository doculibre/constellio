package com.constellio.model.services.records.reindexing;

import org.slf4j.Logger;

public class ReindexingLogger {

	Logger logger;
	String collection;
	String currentSchemaType;
	int currentDependencyLevel;
	String currentPrefix;

	public ReindexingLogger(String collection, Logger logger) {
		this.collection = collection;
		this.logger = logger;
	}

	public void startingToReindexSchemaType(String typeCode, int dependencyLevel) {
		this.currentSchemaType = typeCode;
		this.currentDependencyLevel = dependencyLevel;
		if (dependencyLevel == 0) {
			currentPrefix = "Collection '" + collection + "' - Indexing '" + typeCode + "'";
		} else {
			currentPrefix =
					"Collection '" + collection + "' - Indexing '" + typeCode + "' (Dependency level " + dependencyLevel + ")";
		}
		logger.info(currentPrefix);
	}

	public void updateProgression(long current, long qty) {
		if (current % 1000 == 0 || current == qty) {
			logger.info(currentPrefix + " : " + current + "/" + qty);
		}
	}

	public void updateSkipsCount(int skippedRecordsSize) {
		if (skippedRecordsSize % 100 == 0) {
			logger.info(currentPrefix + " : " + skippedRecordsSize + " records skipped");
		}

	}

	public void onEndOfIteration(int skippedRecordsCount, int currentIteration) {
		if (skippedRecordsCount > 0) {
			logger.info(currentPrefix + " : Iteration " + currentIteration + " has finished with " + skippedRecordsCount
					+ " records skipped, iterating an other time...");
		}

	}
}
