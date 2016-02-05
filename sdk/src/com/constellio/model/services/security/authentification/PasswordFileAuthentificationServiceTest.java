package com.constellio.model.services.security.authentification;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.managers.config.PropertiesAlteration;
import com.constellio.data.utils.hashing.HashingService;
import com.constellio.sdk.tests.ConstellioTest;

public class
		PasswordFileAuthentificationServiceTest extends ConstellioTest {

	@Mock ConfigManager configManager;
	@Mock HashingService hashingService;
	PasswordFileAuthenticationService passwordFileAuthenticationService;

	@Before
	public void setup()
			throws Exception {
		passwordFileAuthenticationService = spy(new PasswordFileAuthenticationService(configManager, hashingService));

		doNothing().when(passwordFileAuthenticationService).validatePasswords("username", "password", "newPassword");
		doNothing().when(passwordFileAuthenticationService).validateNewPassword("newPassword");
		doNothing().when(passwordFileAuthenticationService).validateCurrentPassword("username", "password");
	}

	@Test
	public void whenChangeOldPasswordThenUpdatePropertiesIsCalled()
			throws Exception {

		passwordFileAuthenticationService.changePassword("username", "password", "newPassword");

		verify(passwordFileAuthenticationService).validatePasswords("username", "password", "newPassword");
		verify(configManager).updateProperties(anyString(), any(PropertiesAlteration.class));
	}

	@Test
	public void whenAuthenticateThenValidateCurrentPassword()
			throws Exception {

		passwordFileAuthenticationService.authenticate("username", "password");

		verify(passwordFileAuthenticationService).validateCurrentPassword("username", "password");
	}

	@Test
	public void whenChangePasswordThenUpdatePropertiesIsCalled()
			throws Exception {

		passwordFileAuthenticationService.changePassword("username", "newPassword");

		verify(passwordFileAuthenticationService).validateNewPassword("newPassword");
		verify(configManager).updateProperties(anyString(), any(PropertiesAlteration.class));
	}
}
