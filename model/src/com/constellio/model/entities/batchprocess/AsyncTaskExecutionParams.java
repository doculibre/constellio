package com.constellio.model.entities.batchprocess;

import java.util.Map;

public interface AsyncTaskExecutionParams {

	String getCollection();

	void logWarning(String code, Map<String, Object> parameters);

	void logError(String code, Map<String, Object> parameters);

}
