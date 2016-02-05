package com.constellio.model.services.security.authentification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

import org.junit.Before;
import org.junit.Test;

import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.managers.config.values.PropertiesConfiguration;
import com.constellio.data.utils.hashing.HashingService;
import com.constellio.data.utils.hashing.HashingServiceException;
import com.constellio.sdk.tests.ConstellioTest;

public class PasswordFileAuthentificationServiceAcceptanceTest extends ConstellioTest {

	private static final String NEW_PASSWORD = "newPassword";
	private static final String PASSWORD = "password";
	String authentificationPropertiesPath = "/authentification.properties";
	String usernameChuck = "chuck";
	String usernameDakota = "dakota";
	PasswordFileAuthenticationService passwordFileAuthenticationService;
	ConfigManager configManager;
	HashingService hashingService;

	@Before
	public void setup()
			throws Exception {
		configManager = getDataLayerFactory().getConfigManager();
		hashingService = spy(getIOLayerFactory().newHashingService());
		passwordFileAuthenticationService = getModelLayerFactory()
				.getPasswordFileAuthenticationService();
	}

	@Test
	public void givenPasswordHashPropertyWhenChangeOldPasswordThenItIsChanged()
			throws Exception {

		givenChuckPasswordProperty();
		String newPassword = NEW_PASSWORD;
		String newPasswordHash = hashingService.getHashFromBytes(newPassword.getBytes());

		passwordFileAuthenticationService.changePassword(usernameChuck, PASSWORD, NEW_PASSWORD);

		PropertiesConfiguration propertiesConfiguration = configManager.getProperties(authentificationPropertiesPath);
		assertThat(propertiesConfiguration.getProperties().get(usernameChuck)).isEqualTo(newPasswordHash);
	}

	@Test(expected = PasswordFileAuthenticationServiceRuntimeException.InvalidPasswordException.class)
	public void givenInvalidNewPasswordWhenChangeOldPasswordThenException()
			throws Exception {

		givenChuckPasswordProperty();
		String newPassword = null;

		passwordFileAuthenticationService.changePassword(usernameChuck, PASSWORD, newPassword);
	}

	@Test(expected = PasswordFileAuthenticationServiceRuntimeException.IncorrectPassword.class)
	public void givenDifferentCurrentPasswordWhenChangeOldPasswordThenException()
			throws Exception {

		givenChuckPasswordProperty();

		passwordFileAuthenticationService.changePassword(usernameChuck, "differentPassword", NEW_PASSWORD);
	}

	@Test(expected = PasswordFileAuthenticationServiceRuntimeException.IncorrectPassword.class)
	public void givenInexistentUsernameWhenChangeOldPasswordThenException()
			throws Exception {

		givenChuckPasswordProperty();

		passwordFileAuthenticationService.changePassword("inexistentUsername", PASSWORD, NEW_PASSWORD);
	}

	@Test(expected = PasswordFileAuthenticationServiceRuntimeException.CannotCalculateHash.class)
	public void givenHashingServiceExceptionWhenChangeOldPasswordThenException()
			throws Exception {
		passwordFileAuthenticationService = new PasswordFileAuthenticationService(configManager, hashingService);
		givenChuckPasswordProperty();
		doThrow(HashingServiceException.class).when(hashingService).getHashFromBytes(PASSWORD.getBytes());

		passwordFileAuthenticationService.changePassword(usernameChuck, PASSWORD, NEW_PASSWORD);
	}

	@Test
	public void wWhenChangePasswordThenItIsChanged()
			throws Exception {

		givenChuckPasswordProperty();
		String newPassword = NEW_PASSWORD;
		String newPasswordHash = hashingService.getHashFromBytes(newPassword.getBytes());

		passwordFileAuthenticationService.changePassword(usernameChuck, NEW_PASSWORD);

		PropertiesConfiguration propertiesConfiguration = configManager.getProperties(authentificationPropertiesPath);
		assertThat(propertiesConfiguration.getProperties().get(usernameChuck)).isEqualTo(newPasswordHash);
	}

	@Test(expected = PasswordFileAuthenticationServiceRuntimeException.InvalidPasswordException.class)
	public void givenInvalidNewPasswordWhenChangePasswordThenException()
			throws Exception {

		String newPassword = null;

		passwordFileAuthenticationService.changePassword(usernameChuck, newPassword);
	}

	@Test(expected = PasswordFileAuthenticationServiceRuntimeException.CannotCalculateHash.class)
	public void givenHashingServiceExceptionWhenChangePasswordThenException()
			throws Exception {
		passwordFileAuthenticationService = new PasswordFileAuthenticationService(configManager, hashingService);
		doThrow(HashingServiceException.class).when(hashingService).getHashFromBytes(NEW_PASSWORD.getBytes());
		String newPassword = NEW_PASSWORD;

		passwordFileAuthenticationService.changePassword(usernameChuck, newPassword);
	}

	@Test
	public void givenUsernameAndPasswordWhenAuthenticateThenReturnTrue()
			throws Exception {
		givenChuckPasswordProperty();

		boolean authenticated = passwordFileAuthenticationService.authenticate(usernameChuck, PASSWORD);

		assertThat(authenticated).isTrue();
	}

	@Test
	public void givenWrongUsernameWhenAuthenticateThenReturnFalse()
			throws Exception {
		givenChuckPasswordProperty();

		boolean authenticated = passwordFileAuthenticationService.authenticate("inexistentUsername", PASSWORD);

		assertThat(authenticated).isFalse();
	}

	@Test
	public void givenWrongPasswordWhenAuthenticateThenReturnFalse()
			throws Exception {
		givenChuckPasswordProperty();

		boolean authenticated = passwordFileAuthenticationService.authenticate(usernameChuck, "inexistentPassword");

		assertThat(authenticated).isFalse();
	}

	private void givenChuckPasswordProperty()
			throws HashingServiceException {
		passwordFileAuthenticationService.changePassword(usernameChuck, PASSWORD);
	}
}
