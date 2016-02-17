package com.constellio.app.services.recovery;

import java.io.File;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.services.recovery.TransactionLogRecoveryManager;

public class UpgradeAppRecoveryServiceImpl implements UpgradeAppRecoveryService{
	private final TransactionLogRecoveryManager transactionLogRecoveryManager;
	private final AppLayerFactory appLayerFactory;

	public UpgradeAppRecoveryServiceImpl(AppLayerFactory appLayerFactory) {
		this.appLayerFactory = appLayerFactory;
		this.transactionLogRecoveryManager = appLayerFactory.getModelLayerFactory().getDataLayerFactory().getTransactionLogRecoveryManager();
	}

	private void prepareNextStartup(Throwable exception) {
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

	@Override
	public void startRollbackMode() {
		//TODO
		saveSettings();
		transactionLogRecoveryManager.startRollbackMode();
	}

	private void saveSettings() {

	}

	@Override
	public void stopRollbackMode() {
		deleteSavedSettings();
		transactionLogRecoveryManager.stopRollbackMode();
	}

	private void deleteSavedSettings() {
		//TODO
	}

	@Override
	public boolean isInRollbackMode() {
		return transactionLogRecoveryManager.isInRollbackMode();
	}

	public void rollback(Throwable t) {
		closeLayersExceptData();
		replaceSettingsByTheSavedOne();
		transactionLogRecoveryManager.rollback(t);
		prepareNextStartup(t);
		this.appLayerFactory.getModelLayerFactory().getDataLayerFactory().close(false);
	}

	private void closeLayersExceptData() {
		appLayerFactory.getModelLayerFactory().close(false);
		appLayerFactory.close(false);
	}

	private void replaceSettingsByTheSavedOne() {
		//TODO
	}

	@Override
	public InvalidWarCause isValidWar(File war) {
		//TODO
		return null;
	}

	@Override
	public void afterWarUpload() {
		//TODO invalidate reindexingMode
	}

	public void close() {
		this.transactionLogRecoveryManager.close();
	}
}
