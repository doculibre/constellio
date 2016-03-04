package com.constellio.app.services.migrations.scripts;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;

import com.constellio.sdk.tests.annotations.SlowTest;
import org.jdom2.output.XMLOutputter;
import org.junit.Test;

import com.constellio.data.dao.managers.config.ConfigManagerException.OptimisticLockingConfiguration;
import com.constellio.model.conf.email.EmailServerConfiguration;
import com.constellio.model.services.emails.OldSmtpServerTestConfig;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.SDKFoldersLocator;

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
	public void givenSystemWithTokensWhenMigrationTo5_1_3ThenEncrypted()
			throws OptimisticLockingConfiguration, NoSuchAlgorithmException, IOException, InvalidKeySpecException {

		givenSystemAtVersion5_1_2withTokens();
		assertThat(new XMLOutputter().outputString(getDataLayerFactory().getConfigManager().getXML("userCredentialsConfig.xml")
				.getDocument())).doesNotContain("6f9b7e63-a6c1-4783-9143-1e69edf34b4c");

		List<String> adminTokens = getModelLayerFactory().newUserServices().getUserCredential("admin").getTokenKeys();
		List<String> bobTokens = getModelLayerFactory().newUserServices().getUserCredential("bob").getTokenKeys();
		assertThat(adminTokens).containsOnly("6f9b7e63-a6c1-4783-9143-1e69edf34b4c");
		assertThat(bobTokens).isEmpty();

		String newBobToken = getModelLayerFactory().newUserServices().generateToken("bob");

		assertThat(new XMLOutputter().outputString(getDataLayerFactory().getConfigManager().getXML("userCredentialsConfig.xml")
				.getDocument())).doesNotContain("6f9b7e63-a6c1-4783-9143-1e69edf34b4c").doesNotContain(newBobToken);
		adminTokens = getModelLayerFactory().newUserServices().getUserCredential("admin").getTokenKeys();
		bobTokens = getModelLayerFactory().newUserServices().getUserCredential("bob").getTokenKeys();
		assertThat(adminTokens).containsOnly("6f9b7e63-a6c1-4783-9143-1e69edf34b4c");
		assertThat(bobTokens).containsOnly(newBobToken);
	}

	@Test
	public void givenSystemWithEncryptedTokensWhenReadingUsersTokensThenTokensDecrypted()
			throws OptimisticLockingConfiguration, NoSuchAlgorithmException, IOException, InvalidKeySpecException {

		givenSystemWithEncryptedTokens();

		String validToken = "c84e2c14-f933-4399-aed2-95c538b2b7dd";
		assertThat(new XMLOutputter().outputString(getDataLayerFactory().getConfigManager().getXML("userCredentialsConfig.xml")
				.getDocument())).doesNotContain(validToken);

		List<String> adminTokens = getModelLayerFactory().newUserServices().getUserCredential("admin").getTokenKeys();
		assertThat(adminTokens).contains(validToken);
		assertThat(adminTokens).doesNotContain("invalidkey");
		String serviceKey = getModelLayerFactory().getUserCredentialsManager().getServiceKeyByToken(validToken);
		String userServiceKey = "adminkey";
		assertThat(serviceKey).isEqualTo(userServiceKey);
		boolean authenticated = getModelLayerFactory().newUserServices().isAuthenticated(userServiceKey, validToken);
		assertThat(authenticated).isTrue();
	}

	private void givenSystemWithEncryptedTokens() {
		givenTransactionLogIsEnabled();
		File statesFolder = new SDKFoldersLocator().getInitialStatesFolder();
		File state = new File(statesFolder, "saveStateWithEncryptedTokens.zip");

		getCurrentTestSession().getFactoriesTestFeatures().givenSystemInState(state);
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
		File statesFolder = new File(new SDKFoldersLocator().getInitialStatesFolder(), "olds");
		File state = new File(statesFolder, "given_system_in_5.1.2_with_ManualEmailServerConfiguration.zip");

		getCurrentTestSession().getFactoriesTestFeatures().givenSystemInState(state);
	}

	private void givenSystemAtVersion5_1_2WithManualInvalidSMTPConfiguration() {
		givenTransactionLogIsEnabled();
		File statesFolder = new File(new SDKFoldersLocator().getInitialStatesFolder(), "olds");
		File state = new File(statesFolder, "given_system_in_5.1.2_with_ManualInvalidEmailServerConfiguration.zip");

		getCurrentTestSession().getFactoriesTestFeatures().givenSystemInState(state);
	}

	private void givenSystemAtVersion5_1_2withTokens() {
		givenTransactionLogIsEnabled();
		File statesFolder = new File(new SDKFoldersLocator().getInitialStatesFolder(), "olds");
		File state = new File(statesFolder, "given_system_in_5.1.2.2_with_tasks,rm_modules__with_tokens.zip");

		getCurrentTestSession().getFactoriesTestFeatures().givenSystemInState(state);
	}

}
