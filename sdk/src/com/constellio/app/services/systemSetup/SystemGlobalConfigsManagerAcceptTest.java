package com.constellio.app.services.systemSetup;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.services.systemSetup.SystemGlobalConfigsManager;
import com.constellio.app.services.systemSetup.SystemSetupService;
import com.constellio.sdk.tests.ConstellioTest;

public class SystemGlobalConfigsManagerAcceptTest extends ConstellioTest {

	@Mock SystemSetupService systemSetupService;

	@Test
	public void givenSystemAlreadySettedUpThenDoNotSetupAgain()
			throws Exception {

		getModelLayerFactory();

		new SystemGlobalConfigsManager(getDataLayerFactory().getConfigManager(), systemSetupService).initialize();

		verify(systemSetupService, never()).setup();
	}
}
