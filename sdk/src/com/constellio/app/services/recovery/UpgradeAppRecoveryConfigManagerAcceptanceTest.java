package com.constellio.app.services.recovery;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.constellio.sdk.tests.ConstellioTest;
import com.sun.star.uno.RuntimeException;

public class UpgradeAppRecoveryConfigManagerAcceptanceTest extends ConstellioTest {

	UpgradeAppRecoveryConfigManager upgradeAppRecoveryConfigManager;

	@Before
	public void setUp()
			throws Exception {
		upgradeAppRecoveryConfigManager = new UpgradeAppRecoveryConfigManager(getDataLayerFactory().getConfigManager());
	}


	@Test
	public void whenOnVersionUploadedCorrectlyThenBehavesAsExpected() {
		upgradeAppRecoveryConfigManager.onVersionUploadedCorrectly(new ConstellioVersionInfo("zeVersion", "zeVersionPath"), new ConstellioVersionInfo("", ""));
		assertThat(upgradeAppRecoveryConfigManager.getLastValidVersion()).isEqualTo("zeVersion");
		assertThat(upgradeAppRecoveryConfigManager.getLastValidVersionDirectoryPath()).isEqualTo("zeVersionPath");
		assertThat(upgradeAppRecoveryConfigManager.getLastVersionCausingExceptionDirectoryPath()).isEqualTo("");
		assertThat(upgradeAppRecoveryConfigManager.getUpgradeException()).isEqualTo("");
	}

	@Test
	public void whenOnVersionMigratedCorrectlyThenBehavesAsExpected() {
		upgradeAppRecoveryConfigManager.onVersionUploadedCorrectly(new ConstellioVersionInfo("", ""), new ConstellioVersionInfo("zeVersion", "zeVersionPath"));
		upgradeAppRecoveryConfigManager.onVersionMigratedCorrectly();
		assertThat(upgradeAppRecoveryConfigManager.getLastValidVersion()).isEqualTo("zeVersion");
		assertThat(upgradeAppRecoveryConfigManager.getLastValidVersionDirectoryPath()).isEqualTo("zeVersionPath");
		assertThat(upgradeAppRecoveryConfigManager.getLastVersionCausingExceptionDirectoryPath()).isEqualTo("");
		assertThat(upgradeAppRecoveryConfigManager.getUpgradeException()).isEqualTo("");
	}

	@Test
	public void whenOnVersionMigratedWithExceptionThenBehavesAsExpected() {
		upgradeAppRecoveryConfigManager.onVersionMigratedWithException(new Throwable("aa"));
		assertThat(upgradeAppRecoveryConfigManager.getLastValidVersion()).isEqualTo("");
		assertThat(upgradeAppRecoveryConfigManager.getLastValidVersionDirectoryPath()).isEqualTo("");
		assertThat(upgradeAppRecoveryConfigManager.getLastVersionCausingExceptionDirectoryPath()).isEqualTo("");
		assertThat(upgradeAppRecoveryConfigManager.getUpgradeException()).isEqualTo("java.lang.Throwable: aa");
	}

	@Test
	public void givenPreviousPropertiesOnVersionUploadedCorrectlyThenBehavesAsExpected() {
		populateManagerProperties();
		upgradeAppRecoveryConfigManager.onVersionUploadedCorrectly(new ConstellioVersionInfo("zeVersion", "zeVersionPath"), new ConstellioVersionInfo("", ""));
		assertThat(upgradeAppRecoveryConfigManager.getLastValidVersion()).isEqualTo("zeVersion");
		assertThat(upgradeAppRecoveryConfigManager.getLastValidVersionDirectoryPath()).isEqualTo("zeVersionPath");
		assertThat(upgradeAppRecoveryConfigManager.getLastVersionCausingExceptionDirectoryPath()).isEqualTo("");
		assertThat(upgradeAppRecoveryConfigManager.getUpgradeException()).isEqualTo("");
	}

	@Test
	public void givenPreviousPropertiesOnVersionMigratedCorrectlyThenBehavesAsExpected() {
		populateManagerProperties();
		upgradeAppRecoveryConfigManager.onVersionUploadedCorrectly(new ConstellioVersionInfo("", ""), new ConstellioVersionInfo("zeVersion", "zeVersionPath"));
		upgradeAppRecoveryConfigManager.onVersionMigratedCorrectly();
		assertThat(upgradeAppRecoveryConfigManager.getLastValidVersion()).isEqualTo("zeVersion");
		assertThat(upgradeAppRecoveryConfigManager.getLastValidVersionDirectoryPath()).isEqualTo("zeVersionPath");
		assertThat(upgradeAppRecoveryConfigManager.getLastVersionCausingExceptionDirectoryPath()).isEqualTo("");
		assertThat(upgradeAppRecoveryConfigManager.getUpgradeException()).isEqualTo("");
	}

	@Test
	public void givenPreviousPropertiesOnVersionMigratedWithExceptionThenBehavesAsExpected() {
		upgradeAppRecoveryConfigManager.onVersionUploadedCorrectly(new ConstellioVersionInfo("versionCorrect", "versionCorrectPath"), new ConstellioVersionInfo("zeVersion", "zeVersionPath"));
		upgradeAppRecoveryConfigManager.onVersionMigratedWithException(new Throwable("aa"));
		assertThat(upgradeAppRecoveryConfigManager.getLastValidVersion()).isEqualTo("versionCorrect");
		assertThat(upgradeAppRecoveryConfigManager.getLastValidVersionDirectoryPath()).isEqualTo("versionCorrectPath");
		assertThat(upgradeAppRecoveryConfigManager.getLastVersionCausingExceptionDirectoryPath()).isEqualTo("zeVersionPath");
		assertThat(upgradeAppRecoveryConfigManager.getUpgradeException()).isEqualTo("java.lang.Throwable: aa");
	}

	private void populateManagerProperties() {
		upgradeAppRecoveryConfigManager.onVersionUploadedCorrectly(new ConstellioVersionInfo("versionCorrect", "versionCorrectPath"),
				new ConstellioVersionInfo("", "versionBadPath"));
		upgradeAppRecoveryConfigManager.onVersionMigratedCorrectly();
		upgradeAppRecoveryConfigManager.onVersionMigratedWithException(new RuntimeException("badVersion"));
	}

}
