package com.constellio.data.dao.services.factories;

import com.constellio.data.dao.services.transactionLog.SecondTransactionLogManager;

public class RecoveryManager {
	final DataLayerFactory dataLayerFactory;
	private boolean inRollbackMode;

	public RecoveryManager(DataLayerFactory dataLayerFactory) {
		this.dataLayerFactory = dataLayerFactory;
	}

	public void startRollbackMode() {
		//TODO
		createRecovryFile();
		inRollbackMode = true;
		SecondTransactionLogManager transactionLogManager = dataLayerFactory
				.getSecondTransactionLogManager();
		transactionLogManager.regroupAndMoveInVault();
		transactionLogManager.setAutomaticLog(false);

	}

	private void createRecovryFile() {
		//TODO
	}

	public void stopRollbackMode() {
		inRollbackMode = false;
		//TODO
	}

	public boolean isInRollbackMode() {
		return inRollbackMode;
	}

	public void disableRollbackModeDuringSolrRestore() {
		this.inRollbackMode = false;
	}

	public void rollback() {

	}


}
