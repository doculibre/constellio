package com.constellio.app.services.schemas.bulkImport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggerBulkImportProgressionListener implements BulkImportProgressionListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(LoggerBulkImportProgressionListener.class);

	int totalProgression;

	int currentTotal;

	int currentStepTotal;

	String currentStepName;

	int stepProgression;

	@Override
	public void updateTotal(int newTotal) {
		this.currentTotal = newTotal;
	}

	@Override
	public void updateProgression(int stepProgression, int totalProgression) {
		this.totalProgression = totalProgression;
		this.stepProgression = stepProgression;
		//		LOGGER.info("Import progression : " + getPercentage() + "% [" + currentStepName + " " + stepProgression + "/"
		//				+ currentStepTotal + "]");
	}

	@Override
	public void updateCurrentStepTotal(int newTotal) {
		this.currentStepTotal = newTotal;
	}

	@Override
	public void updateCurrentStepName(String stepName) {
		if (this.currentStepName != null) {
			LOGGER.info(currentStepName + " finished.\n\n");

		}
		this.currentStepName = stepName;

		if (stepName != null) {
			LOGGER.info(currentStepName + " started.");
		}
	}

	@Override
	public void afterRecordValidations(String fromLegacyId, String toLegacyId, int totalValidated, int batchQty,
			int errorsCount) {

		String progression;
		if (errorsCount == 0) {
			progression = currentStepName + " No error found in batch of ";
		} else {
			progression = currentStepName + errorsCount + " errors found in batch of ";
		}

		progression += batchQty + " records [" + fromLegacyId + " - " + toLegacyId
				+ "] - Total of " + batchQty + " records validated";

		LOGGER.info(progression);
	}

	@Override
	public void afterRecordImports(String fromLegacyId, String toLegacyId, int totalImported, int batchQty, int errorsCount) {

		String progression = currentStepName + " Imported batch of " + batchQty + " records [" + fromLegacyId + " - "
				+ toLegacyId + "] for a total of " + totalImported + " records";

		if (errorsCount > 0) {
			progression += " - " + errorsCount + " records failed to import in this batch";
		}

		LOGGER.info(progression);
	}

	@Override
	public void onRecordImportPostponed(String legacyId) {
		LOGGER.info("Importation of record '" + legacyId + "' is postponed because of missing dependencies");
	}

	private double getPercentage() {
		if (currentTotal == 0) {
			return 0;
		}
		int pct10 = 1000 * totalProgression / currentTotal;
		return pct10 / 10.0;
	}
}
