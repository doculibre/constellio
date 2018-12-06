package com.constellio.app.services.migrations.scripts;

import com.constellio.data.dao.managers.config.ConfigManagerException.OptimisticLockingConfiguration;
import com.constellio.model.conf.email.EmailServerConfiguration;
import com.constellio.model.services.emails.OldSmtpServerTestConfig;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.SDKFoldersLocator;
import com.constellio.sdk.tests.annotations.SlowTest;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import static org.assertj.core.api.Assertions.assertThat;

@SlowTest
public class CoreMigrationTo_5_1_3AcceptanceTest extends ConstellioTest {

	@Test
	public void givenSystemWithEmailServerConfigurationWhenUpdatingFrom5_0_6ThenConfigurationReadOK()
			throws OptimisticLockingConfiguration, NoSuchAlgorithmException, IOException, InvalidKeySpecException {

		givenSystemAtVersion5_1_2WithManualSMTPConfiguration();

		EmailServerConfiguration expectedConfig = new OldSmtpServerTestConfig();
		EmailServerConfiguration serverConfiguration = getModelLayerFactory().getEmailConfigurationsManager()
				.getEmailConfiguration("ff", false);

		assertThat(serverConfiguration.getPassword())
				.isEqualTo(getModelLayerFactory().newEncryptionServices().encrypt(expectedConfig.getPassword()));
		assertThat(serverConfiguration.getDefaultSenderEmail()).isEqualTo(expectedConfig.getDefaultSenderEmail());

		serverConfiguration = getModelLayerFactory().getEmailConfigurationsManager().getEmailConfiguration("ff", true);
		assertThat(serverConfiguration.getPassword()).isEqualTo(expectedConfig.getPassword());

	}


	@Test
	public void givenSystemWithInvalidEmailServerConfigurationWhenUpdatingFrom5_0_6ThenConfigurationReadOK()
			throws OptimisticLockingConfiguration, NoSuchAlgorithmException, IOException, InvalidKeySpecException {

		givenSystemAtVersion5_1_2WithManualInvalidSMTPConfiguration();

		EmailServerConfiguration serverConfiguration = getModelLayerFactory().getEmailConfigurationsManager()
				.getEmailConfiguration("ff", false);
		assertThat(serverConfiguration.isEnabled()).isFalse();
	}

	private void givenSystemAtVersion5_1_2WithManualSMTPConfiguration() {
		givenTransactionLogIsEnabled();
		File statesFolder = new File(new SDKFoldersLocator().getInitialStatesFolder(), "veryOlds");
		File state = new File(statesFolder, "given_system_in_5.1.2_with_ManualEmailServerConfiguration.zip");

		getCurrentTestSession().getFactoriesTestFeatures().givenSystemInState(state);
	}

	private void givenSystemAtVersion5_1_2WithManualInvalidSMTPConfiguration() {
		givenTransactionLogIsEnabled();
		File statesFolder = new File(new SDKFoldersLocator().getInitialStatesFolder(), "veryOlds");
		File state = new File(statesFolder, "given_system_in_5.1.2_with_ManualInvalidEmailServerConfiguration.zip");

		getCurrentTestSession().getFactoriesTestFeatures().givenSystemInState(state);
	}


}
