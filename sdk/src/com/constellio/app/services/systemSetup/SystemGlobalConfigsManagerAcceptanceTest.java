package com.constellio.app.services.systemSetup;

import com.constellio.sdk.tests.ConstellioTest;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import static com.constellio.sdk.tests.TestUtils.linkEventBus;
import static org.assertj.core.api.Assertions.assertThat;

public class SystemGlobalConfigsManagerAcceptanceTest extends ConstellioTest {

	SystemGlobalConfigsManager systemGlobalConfigsManager;
	SystemGlobalConfigsManager otherSystemGlobalConfigsManager;

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(withZeCollection());

		linkEventBus(getDataLayerFactory(), getDataLayerFactory("other-instance"));
		systemGlobalConfigsManager = getAppLayerFactory().getSystemGlobalConfigsManager();
		otherSystemGlobalConfigsManager = getAppLayerFactory("other-instance").getSystemGlobalConfigsManager();

	}

	@Test
	public void whenBlockingSystemDuringReindexingThenBlockedForAllInstances()
			throws Exception {

		assertThat(systemGlobalConfigsManager.getLastSystemReindexingSignal()).isNull();
		assertThat(otherSystemGlobalConfigsManager.getLastSystemReindexingSignal()).isNull();

		LocalDateTime now = LocalDateTime.now();
		givenTimeIs(now);
		systemGlobalConfigsManager.blockSystemDuringReindexing();

		assertThat(otherSystemGlobalConfigsManager.getLastSystemReindexingSignal()).isEqualTo(now);
		assertThat(systemGlobalConfigsManager.getLastSystemReindexingSignal()).isEqualTo(now);

		systemGlobalConfigsManager.unblockSystemDuringReindexing();

		assertThat(otherSystemGlobalConfigsManager.getLastSystemReindexingSignal()).isNull();
		assertThat(systemGlobalConfigsManager.getLastSystemReindexingSignal()).isNull();

	}

}
