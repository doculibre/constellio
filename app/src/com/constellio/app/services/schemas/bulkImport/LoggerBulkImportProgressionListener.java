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
	public void onRecordValidation(int addUpdateCount, String legacyId) {
		String progression = currentStepName + " [" + (addUpdateCount + 1) + "/" + currentStepTotal + "] - ";

		LOGGER.info(progression + legacyId);
	}

	@Override
	public void onRecordImport(int addUpdateCount, String legacyId, String title) {
		String progression = currentStepName + " [" + (addUpdateCount + 1) + "/" + currentStepTotal + "] - ";

		if (title != null) {
			LOGGER.info(progression + legacyId + " (" + title + ")");
		} else {
			LOGGER.info(progression + legacyId);
		}
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
