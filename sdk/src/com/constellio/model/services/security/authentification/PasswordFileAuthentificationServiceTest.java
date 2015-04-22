/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
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
