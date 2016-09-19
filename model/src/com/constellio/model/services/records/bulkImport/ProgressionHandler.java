package com.constellio.model.services.records.bulkImport;

import java.util.HashMap;
import java.util.Map;

import com.constellio.app.services.schemas.bulkImport.BulkImportProgressionListener;

public class ProgressionHandler {

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

	public void validatingRecord(int validatedRecords, String legacyId) {
		listener.onRecordValidation(validatedRecords, legacyId);
	}

	public void afterValidationOfSchema(String schema, int recordCount) {
		counts.put(schema, recordCount);
		totalCount += recordCount;
		listener.updateTotal(totalCount);
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

	public void onRecordImport(int addUpdateCount, String legacyId, String title) {
		listener.onRecordImport(addUpdateCount, legacyId, title);
	}
}
