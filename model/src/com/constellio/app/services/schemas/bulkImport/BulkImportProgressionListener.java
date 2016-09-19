package com.constellio.app.services.schemas.bulkImport;

import java.io.Serializable;

public interface BulkImportProgressionListener extends Serializable {

	public void updateTotal(int newTotal);

	public void updateProgression(int nFinishedRecordsCurrentStep, int nFinishedRecordsTotal);

	public void updateCurrentStepTotal(int newTotal);

	public void updateCurrentStepName(String stepName);

	void afterRecordValidations(String fromLegacyId, String toLegacyId, int totalValidated, int batchQty, int errorsCount);

	void afterRecordImports(String fromLegacyId, String toLegacyId, int totalImported, int batchQty, int errorsCount);

	void onRecordImportPostponed(String legacyId);
}
