package com.constellio.app.services.schemas.bulkImport;

import java.io.Serializable;

public interface BulkImportProgressionListener extends Serializable {

	public void updateTotal(int newTotal);

	public void updateProgression(int nFinishedRecordsCurrentStep, int nFinishedRecordsTotal);

	public void updateCurrentStepTotal(int newTotal);

	public void updateCurrentStepName(String stepName);

	void onRecordValidation(int addUpdateCount, String legacyId);

	void onRecordImport(int addUpdateCount, String legacyId, String title);

	void onRecordImportPostponed(String legacyId);
}
