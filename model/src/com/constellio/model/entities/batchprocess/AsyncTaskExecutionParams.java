package com.constellio.model.entities.batchprocess;

import com.constellio.model.services.factories.ModelLayerFactory;

import java.util.Map;

public interface AsyncTaskExecutionParams {

	String getCollection();

	void logWarning(String code, Map<String, Object> parameters);

	void logError(String code, Map<String, Object> parameters);

	void incrementProgression(int numberToAdd);

	void resetProgression();

	void setProgressionUpperLimit(long progressionUpperLimit);

	AsyncTaskBatchProcess getBatchProcess();

	default ModelLayerFactory getModelLayerFactory() {
		return null;
	}

}
