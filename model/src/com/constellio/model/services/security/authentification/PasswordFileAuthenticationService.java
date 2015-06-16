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

import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.managers.config.PropertiesAlteration;
import com.constellio.data.dao.managers.config.values.PropertiesConfiguration;
import com.constellio.data.utils.hashing.HashingService;
import com.constellio.data.utils.hashing.HashingServiceException;
import com.constellio.model.services.security.authentification.PasswordFileAuthenticationServiceRuntimeException.IncorrectPassword;
import com.constellio.model.services.users.UserUtils;

public class PasswordFileAuthenticationService implements AuthenticationService {

	private static final Logger LOGGER = LoggerFactory.getLogger(PasswordFileAuthenticationService.class);
	final String AUTHENTIFICATION_PROPERTIES = "/authentification.properties";
	private final ConfigManager configManager;

	private final HashingService hashingService;

	public PasswordFileAuthenticationService(ConfigManager configManager, HashingService hashingService) {
		this.configManager = configManager;
		this.hashingService = hashingService;
		configManager.createPropertiesDocumentIfInexistent(AUTHENTIFICATION_PROPERTIES, new PropertiesAlteration() {

			@Override
			public void alter(Map<String, String> properties) {
			}
		});
	}

	@Override
	public boolean supportPasswordChange() {
		return true;
	}

	@Override
	public boolean authenticate(String username, String password) {
		try {
			validateCurrentPassword(username, password);
		} catch (IncorrectPassword e) {
			LOGGER.warn("Cannot authenticate. Incorrect password", e);
			return false;
		}
		return true;
	}

	@Override
	public void changePassword(final String username, String password, final String newPassword) {
		validatePasswords(username, password, newPassword);
		final String newPasswordHash = calculeHash(newPassword);
		updatePassword(username, newPasswordHash);
	}

	@Override
	public void changePassword(String username, String newPassword) {
		validateNewPassword(newPassword);
		final String newPasswordHash = calculeHash(newPassword);
		updatePassword(username, newPasswordHash);
	}

	@Override
	public void reloadServiceConfiguration() {
		//not supported

	}

	void validatePasswords(String username, String password, String newPassword) {
		validateCurrentPassword(username, password);
		validateNewPassword(newPassword);

	}

	void validateNewPassword(String newPassword) {
		if (newPassword == null || newPassword.equals("")) {
			throw new PasswordFileAuthenticationServiceRuntimeException.InvalidPasswordException();
		}
	}

	void validateCurrentPassword(String username, String password) {
		if (username == null || password == null) {
			throw new IncorrectPassword();
		}

		String currentPasswordHash = getPasswordHash(username);
		String passwordHash = calculeHash(password);
		if (currentPasswordHash == null || !currentPasswordHash.equals(passwordHash)) {
			throw new IncorrectPassword();
		}
	}

	private String getPasswordHash(String username) {
		PropertiesConfiguration propertiesConfiguration = configManager.getProperties(AUTHENTIFICATION_PROPERTIES);
		Map<String, String> properties = propertiesConfiguration.getProperties();
		String usernameKey = UserUtils.toCacheKey(username);
		for (Entry<String, String> entry : properties.entrySet()) {
			String entryKey = UserUtils.toCacheKey(entry.getKey());
			if (entryKey.equals(usernameKey)) {
				return entry.getValue();
			}
		}
		return null;
	}

	private void updatePassword(final String username, final String newPasswordHash) {
		configManager.updateProperties(AUTHENTIFICATION_PROPERTIES, new PropertiesAlteration() {
			@Override
			public void alter(Map<String, String> properties) {
				properties.put(username, newPasswordHash);
			}
		});
	}

	private String calculeHash(String text) {
		final String textHash;
		try {
			textHash = hashingService.getHashFromBytes(text.getBytes());
		} catch (HashingServiceException e) {
			throw new PasswordFileAuthenticationServiceRuntimeException.CannotCalculateHash(text, e);
		}
		return textHash;
	}
}
