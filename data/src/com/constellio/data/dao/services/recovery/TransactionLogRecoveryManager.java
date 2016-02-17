package com.constellio.data.dao.services.recovery;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.dao.services.transactionLog.SecondTransactionLogManager;
import com.constellio.data.io.services.facades.IOServices;

public class TransactionLogRecoveryManager implements RecoveryService {
	private final static Logger LOGGER = LoggerFactory.getLogger(TransactionLogRecoveryManager.class);
	private static final String RECOVERY_WORK_DIR = TransactionLogRecoveryManager.class.getName() + "recoveryWorkDir";

	final DataLayerFactory dataLayerFactory;
	final File recoveryWorkDir;
	private final IOServices ioServices;
	private boolean inRollbackMode;
	Set<String> loadedRecordsIds, createdRecordsIds, deletedRecordsIds, updatedRecordsIds;

	public TransactionLogRecoveryManager(DataLayerFactory dataLayerFactory) {
		this.dataLayerFactory = dataLayerFactory;
		ioServices = this.dataLayerFactory.getIOServicesFactory().newIOServices();
		recoveryWorkDir = ioServices.newTemporaryFolder(RECOVERY_WORK_DIR);
	}

	@Override
	public void startRollbackMode() {
		if (!inRollbackMode) {
			realStartRollback();
		}
	}

	void realStartRollback() {
		loadedRecordsIds = new HashSet<>();
		createdRecordsIds = new HashSet<>();
		deletedRecordsIds = new HashSet<>();
		updatedRecordsIds = new HashSet<>();
		createRecoveryFile();
		inRollbackMode = true;
		SecondTransactionLogManager transactionLogManager = dataLayerFactory
				.getSecondTransactionLogManager();
		transactionLogManager.regroupAndMoveInVault();
		transactionLogManager.setAutomaticLog(false);
	}

	private void createRecoveryFile() {
	}


	@Override
	public void stopRollbackMode() {
		if (inRollbackMode) {
			realStopRollback();
		}
	}

	void realStopRollback() {
		deleteRecoveryFile();
		inRollbackMode = false;
		SecondTransactionLogManager transactionLogManager = dataLayerFactory
				.getSecondTransactionLogManager();
		transactionLogManager.regroupAndMoveInVault();
		transactionLogManager.setAutomaticLog(true);
	}

	private void deleteRecoveryFile() {
		//TODO
	}

	@Override
	public boolean isInRollbackMode() {
		return inRollbackMode;
	}

	public void disableRollbackModeDuringSolrRestore() {
		this.inRollbackMode = false;
	}

	@Override
	public void rollback(Throwable t) {
		if (inRollbackMode) {
			realRollback(t);
		}
	}

	void realRollback(Throwable t) {
		recover();
		deleteRecoveryFile();
		SecondTransactionLogManager transactionLogManager = dataLayerFactory
				.getSecondTransactionLogManager();
		transactionLogManager.deleteUnregroupedLog();
		transactionLogManager.setAutomaticLog(true);
		inRollbackMode = false;
	}

	private void recover() {
		//TODO
	}

}
