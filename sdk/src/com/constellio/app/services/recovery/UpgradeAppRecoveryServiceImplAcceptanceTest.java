package com.constellio.app.services.recovery;

import static com.constellio.app.services.recovery.InvalidWarCause.TOO_SHORT_MEMORY;
import static com.constellio.app.services.recovery.InvalidWarCause.TOO_SHORT_SPACE;
import static com.constellio.app.services.recovery.UpgradeAppRecoveryServiceImpl.REQUIRED_MEMORY_IN_MO;
import static com.constellio.app.services.recovery.UpgradeAppRecoveryServiceImpl.REQUIRED_SPACE_IN_GIG;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.constellio.sdk.tests.ConstellioTest;

public class UpgradeAppRecoveryServiceImplAcceptanceTest extends ConstellioTest {

	UpgradeAppRecoveryServiceImpl upgradeAppRecoveryService;

	@Before
	public void setUp()
			throws Exception {
		upgradeAppRecoveryService = new UpgradeAppRecoveryServiceImpl(getAppLayerFactory(), getIOLayerFactory().newIOServices());
	}

	@Test
	public void whenIsValidWarThen()
			throws Exception {
		REQUIRED_MEMORY_IN_MO = 1;
		REQUIRED_SPACE_IN_GIG = 1;
		assertThat(upgradeAppRecoveryService.isUpdateWithRecoveryPossible()).isNull();

		REQUIRED_MEMORY_IN_MO = 1000*1024;
		assertThat(upgradeAppRecoveryService.isUpdateWithRecoveryPossible()).isEqualTo(TOO_SHORT_MEMORY);

		REQUIRED_MEMORY_IN_MO = 1;
		REQUIRED_SPACE_IN_GIG = 500*1024*1024;
		assertThat(upgradeAppRecoveryService.isUpdateWithRecoveryPossible()).isEqualTo(TOO_SHORT_SPACE);
	}

	//afterWarUpload
	//prepareNextStartup
	//deletePreviousWarCausingFailure
	//afterWarUpload
	//getTransactionLogFileSize

}
