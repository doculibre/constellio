package com.constellio.app.services.recovery;

import java.io.File;

import com.constellio.data.dao.services.recovery.RecoveryService;

public interface UpgradeAppRecoveryService extends RecoveryService {
	InvalidWarCause isUpdateWithRecoveryPossible();
	void afterWarUpload();
}
