package com.constellio.model.services.records.bulkImport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.constellio.app.services.schemas.bulkImport.BulkImportProgressionListener;

public class ProgressionHandler {

	int currentBatchErrors;
	List<String> currentBatch = new ArrayList<>();

	int totalProgression;
	int stepProgression;
	int totalCount = 0;
	Map<String, Integer> counts = new HashMap<>();
	BulkImportProgressionListener listener;

	public ProgressionHandler(BulkImportProgressionListener listener) {
		this.listener = listener;
	}

	public void beforeValidationOfSchema(String schema) {
		listener.updateCurrentStepName("Validating '" + schema + "'");

	}

	public void afterRecordValidation(String legacyId, boolean hasErrors) {
		stepProgression++;
		currentBatch.add(legacyId);
		if (hasErrors) {
			currentBatchErrors++;
		}

		if (currentBatch.size() >= 1000) {
			logValidationBatch();
		}
	}

	public void afterValidationOfSchema(String schema, int recordCount) {
		logValidationBatch();

		counts.put(schema, recordCount);
		totalCount += recordCount;
		listener.updateTotal(totalCount);
		currentBatch.clear();
	}

	private void logValidationBatch() {
		if (!currentBatch.isEmpty()) {
			String firstId = currentBatch.get(0);
			String lastId = currentBatch.get(currentBatch.size() - 1);
			listener.afterRecordValidations(firstId, lastId, stepProgression, currentBatch.size(), currentBatchErrors);
			currentBatch.clear();
			currentBatchErrors = 0;
		}
	}

	public void beforeImportOf(String schema) {
		listener.updateCurrentStepName("Import of '" + schema + "'");
		listener.updateCurrentStepTotal(counts.get(schema));
		this.stepProgression = 0;
	}

	public void onImportFinished() {
		listener.updateCurrentStepName(null);
	}

	public void incrementProgression() {
		totalProgression++;
		stepProgression++;
		listener.updateProgression(stepProgression, totalProgression);
	}

	public void onRecordImportPostponed(String legacyId) {
		listener.onRecordImportPostponed(legacyId);
	}

	public void afterRecordImports(String fromLegacyId, String toLegacyId, int batchQty, int errorsCount) {
		listener.afterRecordImports(fromLegacyId, toLegacyId, stepProgression, batchQty, errorsCount);
	}
}
