package com.constellio.app.services.recovery;

import static com.constellio.app.services.recovery.InvalidWarCause.TOO_SHORT_MEMORY;
import static com.constellio.app.services.recovery.InvalidWarCause.TOO_SHORT_SPACE;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.systemProperties.SystemPropertiesServices;
import com.constellio.data.dao.services.recovery.TransactionLogRecoveryManager;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.model.conf.FoldersLocator;

public class UpgradeAppRecoveryServiceImpl implements UpgradeAppRecoveryService {
	private final static Logger LOGGER = LoggerFactory.getLogger(UpgradeAppRecoveryServiceImpl.class);
	public static long REQUIRED_MEMORY_IN_MO = 200;
	public static double REQUIRED_SPACE_IN_GIG = 0.5;
	private final TransactionLogRecoveryManager transactionLogRecoveryManager;
	private final AppLayerFactory appLayerFactory;
	private final SystemPropertiesServices systemPropertiesServices;

	public UpgradeAppRecoveryServiceImpl(AppLayerFactory appLayerFactory, IOServices ioServices) {
		this.appLayerFactory = appLayerFactory;
		this.transactionLogRecoveryManager = appLayerFactory.getModelLayerFactory().getDataLayerFactory()
				.getTransactionLogRecoveryManager();
		systemPropertiesServices = new SystemPropertiesServices(new FoldersLocator(), ioServices);
	}

	void prepareNextStartup(Throwable exception) {
		saveExceptionStack(exception);
		pointToPreviousWebApp();
		writeTheCurrentWebAppPathInAFileToInformTheNextRebootToDeleteIt();
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

	@Override
	public void stopRollbackMode() {
		deleteSavedSettings();
		transactionLogRecoveryManager.stopRollbackMode();
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

	@Override
	public InvalidWarCause isUpdateWithRecoveryPossible() {
		if(this.systemPropertiesServices.isAvailableMemoryLowerThan(REQUIRED_MEMORY_IN_MO)){
			return TOO_SHORT_MEMORY;
		}
		if(this.systemPropertiesServices.isFreeSpaceInTempFolderLowerThan(getTransactionLogFileSizeInGig() + REQUIRED_SPACE_IN_GIG)){
			return TOO_SHORT_SPACE;
		}
		return null;
	}

	double getTransactionLogFileSizeInGig() {
		return 0;
	}

	@Override
	public void afterWarUpload() {
		//TODO invalidate reindexingMode
	}

	public void close() {
		this.transactionLogRecoveryManager.close();
	}

	private void deleteSavedSettings() {
		//TODO
	}

	private void writeTheCurrentWebAppPathInAFileToInformTheNextRebootToDeleteIt() {
		//TODO
	}

	private void pointToPreviousWebApp() {
		//TODO
	}

	private void saveExceptionStack(Throwable exception) {
		//TODO
		if(exception != null){
			LOGGER.warn("Rollback without exception");
		}
		//Displayed in update center page(sauvegardé dans un fichier par exemple et est supprimée uniquement si un nouveau war est loadé)
	}

	void saveSettings() {

	}

	private void closeLayersExceptData() {
		appLayerFactory.getModelLayerFactory().close(false);
		appLayerFactory.close(false);
	}

	private void replaceSettingsByTheSavedOne() {
		//TODO
	}
}
