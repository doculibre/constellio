package com.constellio.model.entities.batchprocess;

import com.constellio.model.frameworks.validation.ValidationException;

import java.util.Map;

public interface AsyncTaskExecutionParams {

	String getCollection();

	void logWarning(String code, Map<String, Object> parameters);

	void logError(String code, Map<String, Object> parameters) throws ValidationException;

	void incrementProgression(int numberToAdd);

	void setProgressionUpperLimit(long progressionUpperLimit);

	AsyncTaskBatchProcess getBatchProcess();

}
