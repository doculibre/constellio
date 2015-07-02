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
package com.constellio.model.services.users;

import static com.constellio.data.threads.BackgroundThreadExceptionHandling.CONTINUE;
import static com.constellio.model.services.users.UserUtils.toCacheKey;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Document;
import org.joda.time.LocalDateTime;

import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.managers.config.DocumentAlteration;
import com.constellio.data.dao.managers.config.events.ConfigUpdatedEventListener;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.threads.BackgroundThreadConfiguration;
import com.constellio.data.threads.BackgroundThreadsManager;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.conf.ModelLayerConfiguration;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.entities.security.global.UserCredentialStatus;

public class UserCredentialsManager implements StatefulService, ConfigUpdatedEventListener {

	private static final String USER_CREDENTIALS_CONFIG = "/userCredentialsConfig.xml";

	private final ConfigManager configManager;

	private final BackgroundThreadsManager backgroundThreadsManager;

	private final ModelLayerConfiguration configuration;

	private Map<String, UserCredential> cache = new LinkedHashMap<>();

	private List<String> usersWithServiceKey = new ArrayList<>();

	public UserCredentialsManager(DataLayerFactory dataLayerFactory, ModelLayerConfiguration configuration) {
		this.configManager = dataLayerFactory.getConfigManager();
		this.configuration = configuration;
		this.backgroundThreadsManager = dataLayerFactory.getBackgroundThreadsManager();
	}

	@Override
	public void initialize() {
		registerListener(configManager);

		Document document = configManager.getXML(USER_CREDENTIALS_CONFIG).getDocument();
		UserCredentialsReader reader = newUserCredencialsReader(document);
		cache = Collections.unmodifiableMap(reader.readAll());

		Runnable removedTimedOutTokens = new Runnable() {
			@Override
			public void run() {
				removedTimedOutTokens();
			}
		};

		this.backgroundThreadsManager.configure(BackgroundThreadConfiguration
				.repeatingAction("removedTimedOutTokens", removedTimedOutTokens)
				.handlingExceptionWith(CONTINUE)
				.executedEvery(configuration.getTokenRemovalThreadDelayBetweenChecks()));
	}

	public void addUpdate(UserCredential userCredential) {
		configManager.updateXML(USER_CREDENTIALS_CONFIG, newAddUpdateUserCredentialsDocumentAlteration(userCredential));
		if (userCredential.getServiceKey() != null) {
			usersWithServiceKey.add(userCredential.getUsername());
		}
	}

	public UserCredential getUserCredential(String username) {
		String cacheKey = toCacheKey(username);
		return cache.get(cacheKey);
	}

	public List<UserCredential> getUserCredentials() {
		List<UserCredential> userCredentials = new ArrayList<>(cache.values());
		sort(userCredentials);
		return Collections.unmodifiableList(userCredentials);
	}

	List<UserCredential> getUserCredentialsByStatus(UserCredentialStatus status) {
		List<UserCredential> userCredentials = new ArrayList<>();
		for (UserCredential userCredential : getUserCredentials()) {
			if (status == userCredential.getStatus()) {
				userCredentials.add(userCredential);
			}
		}
		return Collections.unmodifiableList(userCredentials);
	}

	public List<UserCredential> getActiveUserCredentials() {
		return Collections.unmodifiableList(getUserCredentialsByStatus(UserCredentialStatus.ACTIVE));
	}

	public List<UserCredential> getSuspendedUserCredentials() {
		return Collections.unmodifiableList(getUserCredentialsByStatus(UserCredentialStatus.SUPENDED));
	}

	public List<UserCredential> getPendingApprovalUserCredentials() {
		return Collections.unmodifiableList(getUserCredentialsByStatus(UserCredentialStatus.PENDING));
	}

	public List<UserCredential> getDeletedUserCredentials() {
		return Collections.unmodifiableList(getUserCredentialsByStatus(UserCredentialStatus.DELETED));
	}

	public List<UserCredential> getUserCredentialsInGlobalGroup(String group) {
		List<UserCredential> userCredentials = new ArrayList<>();
		for (UserCredential userCredential : getActiveUserCredentials()) {
			if (userCredential.getGlobalGroups().contains(group)) {
				userCredentials.add(userCredential);
			}
		}
		return Collections.unmodifiableList(userCredentials);
	}

	public void removeCollection(String collection) {
		configManager.updateXML(USER_CREDENTIALS_CONFIG, newRemoveCollectionDocumentAlteration(collection));
	}

	public void removeToken(String token) {
		configManager.updateXML(USER_CREDENTIALS_CONFIG, newRemoveTokenDocumentAlteration(token));
	}

	public void removeUserCredentialFromCollection(UserCredential userCredential, String collection) {
		configManager.updateXML(USER_CREDENTIALS_CONFIG, newRemoveUserDocumentAlteration(userCredential, collection));
	}

	public void removeGroup(String codeGroup) {
		configManager.updateXML(USER_CREDENTIALS_CONFIG, newRemoveGroupDocumentAlteration(codeGroup));
	}

	void registerListener(ConfigManager configManager) {
		if (!configManager.exist(USER_CREDENTIALS_CONFIG)) {
			createEmptyUserCredentialsConfig();
		}
		configManager.registerListener(USER_CREDENTIALS_CONFIG, this);
	}

	void createEmptyUserCredentialsConfig() {
		Document document = new Document();
		UserCredentialsWriter writer = new UserCredentialsWriter(document);
		writer.createEmptyUserCredentials();
		configManager.add(USER_CREDENTIALS_CONFIG, document);
	}

	@Override
	public void onConfigUpdated(String configPath) {
		Document document = configManager.getXML(USER_CREDENTIALS_CONFIG).getDocument();
		UserCredentialsReader reader = newUserCredencialsReader(document);
		cache = Collections.unmodifiableMap(reader.readAll());
	}

	DocumentAlteration newAddUpdateUserCredentialsDocumentAlteration(final UserCredential userCredential) {
		return new DocumentAlteration() {
			@Override
			public void alter(Document document) {
				newUserCredencialsWriter(document).addUpdate(userCredential);
			}
		};
	}

	DocumentAlteration newRemoveCollectionDocumentAlteration(final String collection) {
		return new DocumentAlteration() {
			@Override
			public void alter(Document document) {
				newUserCredencialsWriter(document).removeCollection(collection);
			}
		};
	}

	DocumentAlteration newRemoveTokenDocumentAlteration(final String token) {
		return new DocumentAlteration() {
			@Override
			public void alter(Document document) {
				newUserCredencialsWriter(document).removeToken(token);
			}
		};
	}

	DocumentAlteration newRemoveUserDocumentAlteration(final UserCredential userCredential, final String collection) {
		return new DocumentAlteration() {
			@Override
			public void alter(Document document) {
				newUserCredencialsWriter(document).removeUserFromCollection(userCredential, collection);
			}
		};
	}

	DocumentAlteration newRemoveGroupDocumentAlteration(final String groupCode) {
		return new DocumentAlteration() {
			@Override
			public void alter(Document document) {
				newUserCredencialsWriter(document).removeGroup(groupCode);
			}
		};
	}

	UserCredentialsWriter newUserCredencialsWriter(Document document) {
		return new UserCredentialsWriter(document);
	}

	UserCredentialsReader newUserCredencialsReader(Document document) {
		return new UserCredentialsReader(document);
	}

	ConfigManager getConfigManager() {
		return configManager;
	}

	public String getUserCredentialByServiceKey(String serviceKey) {
		for (String usernameWithServiceKey : usersWithServiceKey) {
			UserCredential userCredential = getUserCredential(usernameWithServiceKey);
			if (userCredential != null && serviceKey.equals(userCredential.getServiceKey())) {
				return userCredential.getUsername();
			}
		}
		return null;
	}

	public String getServiceKeyByToken(String token) {
		for (String usernameWithServiceKey : usersWithServiceKey) {
			UserCredential userCredential = getUserCredential(usernameWithServiceKey);
			if (userCredential.getTokens().containsKey(token) && !userCredential.getTokens().get(token)
					.isBefore(new LocalDateTime())) {
				return userCredential.getServiceKey();
			}
		}
		return null;
	}

	@Override
	public void close() {

	}

	public void removedTimedOutTokens() {
		for (UserCredential userCredential : getUserCredentials()) {
			UserCredential modifiedUserCredential = null;
			for (Map.Entry<String, LocalDateTime> token : userCredential.getTokens().entrySet()) {
				if (!token.getValue().isAfter(TimeProvider.getLocalDateTime())) {
					if (modifiedUserCredential == null) {
						modifiedUserCredential = userCredential;
					}
					modifiedUserCredential = modifiedUserCredential.withRemovedToken(token.getKey());
				}
			}
			if (modifiedUserCredential != null) {
				addUpdate(modifiedUserCredential);
			}
		}
	}

	private void sort(List<UserCredential> userCredentials) {
		Collections.sort(userCredentials, new Comparator<UserCredential>() {
			@Override
			public int compare(UserCredential o1, UserCredential o2) {
				return o1.getUsername().toLowerCase().compareTo(o2.getUsername().toLowerCase());
			}
		});
	}
}
