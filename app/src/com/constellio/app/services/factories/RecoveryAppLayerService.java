package com.constellio.app.services.factories;

import com.constellio.data.dao.services.factories.RecoveryManager;

public class RecoveryAppLayerService {
	private final RecoveryManager recoveryManager;

	public RecoveryAppLayerService(RecoveryManager recoveryManager) {
		this.recoveryManager = recoveryManager;
	}

	public void prepareNextStartup(Throwable exception) {
		saveExceptionStack(exception);
		pointToPreviousWebapp();
		writeTheCurrentWebappPathInAFileToInformTheNextRebootToDeleteIt();
	}

	private void writeTheCurrentWebappPathInAFileToInformTheNextRebootToDeleteIt() {
	}

	private void pointToPreviousWebapp() {
	}

	private void saveExceptionStack(Throwable exception) {
		//Displayed in update center page(sauvegardé dans un fichier par exemple et est supprimée uniquement si un nouveau war est loadé)
	}

	public void deletePreviousWarCausingFailure() {
		//TODO
	}

	public void startRollbackMode() {
		//TODO
		saveSettings();
		recoveryManager.startRollbackMode();
	}

	private void saveSettings() {

	}

	public void stopRollbackMode() {

		recoveryManager.stopRollbackMode();
		//TODO
	}

	public boolean isInRollbackMode() {
		return recoveryManager.isInRollbackMode();
	}

	public void disableRollbackModeDuringSolrRestore() {
		recoveryManager.disableRollbackModeDuringSolrRestore();
	}

	public void rollback() {

	}
}
