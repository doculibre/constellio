package com.constellio.app.services.recovery;

import com.constellio.data.dao.services.recovery.RecoveryService;

public interface UpgradeAppRecoveryService extends RecoveryService {
	UpdateRecoveryImpossibleCause isUpdateWithRecoveryPossible();
	void afterWarUpload(ConstellioVersionInfo currentInstalledVersion, ConstellioVersionInfo uploadedVersion);
	String getLastUpgradeExceptionMessage();
}
