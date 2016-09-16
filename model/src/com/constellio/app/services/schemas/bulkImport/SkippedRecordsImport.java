package com.constellio.app.services.schemas.bulkImport;

import com.constellio.data.utils.KeySetMap;
import com.constellio.model.frameworks.validation.ValidationErrors;

public class SkippedRecordsImport {

	KeySetMap<String, String> skippedBecauseOfFailure = new KeySetMap<>();
	KeySetMap<String, String> skippedBecauseOfDependency = new KeySetMap<>();

	public void markAsSkippedBecauseOfFailure(String schemaType, String id) {
		skippedBecauseOfFailure.add(schemaType, id);
	}

	public boolean isSkipped(String schemaType, String id) {
		return (skippedBecauseOfFailure.contains(schemaType) && skippedBecauseOfFailure.get(schemaType).contains(id)) ||
				(skippedBecauseOfDependency.contains(schemaType) && skippedBecauseOfDependency.get(schemaType).contains(id));
	}

	public void markAsSkippedBecauseOfDependencyFailure(String schemaType, String id) {
		skippedBecauseOfDependency.add(schemaType, id);
	}

	public void addWarningForSkippedRecordsBecauseOfDependencies(ValidationErrors errors) {

	}

}
