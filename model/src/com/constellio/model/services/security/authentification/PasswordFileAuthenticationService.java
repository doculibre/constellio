package com.constellio.model.services.security.authentification;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
		String usernameKey = UserUtils.cleanUsername(username);
		for (Entry<String, String> entry : properties.entrySet()) {
			String entryKey = UserUtils.cleanUsername(entry.getKey());
			if (entryKey.equals(usernameKey)) {
				return entry.getValue();
			}
		}
		return null;
	}

	private void updatePassword(final String username, final String newPasswordHash) {
		final String usernameKey = UserUtils.cleanUsername(username);
		configManager.updateProperties(AUTHENTIFICATION_PROPERTIES, new PropertiesAlteration() {
			@Override
			public void alter(Map<String, String> properties) {
				Set<String> keysToRemove = new HashSet<String>();
				for (Map.Entry<String, String> entry : properties.entrySet()) {
					final String aUsernameKey = UserUtils.cleanUsername(entry.getKey());
					if (aUsernameKey.equals(usernameKey)) {
						keysToRemove.add(entry.getKey());
					}
				}

				for (String keyToRemove : keysToRemove) {
					properties.remove(keyToRemove);
				}

				properties.put(usernameKey, newPasswordHash);
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
