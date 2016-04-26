package com.constellio.model.services.users;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.users.XmlUserCredentialsManager.USER_CREDENTIALS_CONFIG;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.constellio.data.dao.dto.records.OptimisticLockingResolution;
import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.utils.BatchBuilderIterator;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.global.GlobalGroup;
import com.constellio.model.entities.security.global.SolrGlobalGroup;
import com.constellio.model.entities.security.global.SolrUserCredential;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

public class UserCredentialAndGlobalGroupsMigration {

	private final ModelLayerFactory modelLayerFactory;
	XmlUserCredentialsManager oldUserManager;
	XmlGlobalGroupsManager oldGroupManager;
	RecordServices recordServices;
	SearchServices searchServices;
	SchemasRecordsServices schemasRecordsServices;
	ConfigManager configManager;

	public UserCredentialAndGlobalGroupsMigration(ModelLayerFactory modelLayerFactory) {

		DataLayerFactory dataLayerFactory = modelLayerFactory.getDataLayerFactory();
		this.configManager = dataLayerFactory.getConfigManager();

		if (configManager.exist(XmlGlobalGroupsManager.CONFIG_FILE)) {
			this.oldGroupManager = new XmlGlobalGroupsManager(configManager);
			this.oldGroupManager.initialize();
		}

		if (configManager.exist(USER_CREDENTIALS_CONFIG)) {
			this.oldUserManager = new XmlUserCredentialsManager(dataLayerFactory, modelLayerFactory,
					modelLayerFactory.getConfiguration());
			this.oldUserManager.initialize();
		}

		this.modelLayerFactory = modelLayerFactory;
		this.recordServices = modelLayerFactory.newRecordServices();
		this.searchServices = modelLayerFactory.newSearchServices();
		this.schemasRecordsServices = new SchemasRecordsServices(Collection.SYSTEM_COLLECTION, modelLayerFactory);
	}

	public boolean isMigrationRequired() {
		return oldGroupManager != null || oldUserManager != null;
	}

	private List<String> getExistingGroups() {
		List<String> existingGroups = new ArrayList<>();
		LogicalSearchQuery query = new LogicalSearchQuery(from(schemasRecordsServices.globalGroupSchemaType()).returnAll());
		Iterator<Record> groupsIterator = searchServices.recordsIterator(query, 1000);
		while (groupsIterator.hasNext()) {
			existingGroups.add((String) groupsIterator.next().get(schemasRecordsServices.globalGroupCode()));
		}
		return existingGroups;
	}

	private List<String> getExistingUsers() {
		List<String> existingUsers = new ArrayList<>();
		LogicalSearchQuery query = new LogicalSearchQuery(from(schemasRecordsServices.credentialSchemaType()).returnAll());
		Iterator<Record> groupsIterator = searchServices.recordsIterator(query, 1000);
		while (groupsIterator.hasNext()) {
			existingUsers.add((String) groupsIterator.next().get(schemasRecordsServices.credentialUsername()));
		}
		return existingUsers;
	}

	public void migrateUserAndGroups() {
		Transaction transaction = new Transaction();

		List<String> existingGroups = getExistingGroups();
		List<String> existingUsers = getExistingUsers();

		try {

			for (GlobalGroup oldGroup : oldGroupManager.getAllGroups()) {
				if (!existingGroups.contains(oldGroup.getCode())) {
					SolrGlobalGroup newGroup = (SolrGlobalGroup) schemasRecordsServices.newGlobalGroup();
					newGroup.setCode(oldGroup.getCode());
					newGroup.setCode(oldGroup.getCode());
					newGroup.setName(oldGroup.getName());
					newGroup.setTitle(oldGroup.getName());
					newGroup.setStatus(oldGroup.getStatus());
					newGroup.setUsersAutomaticallyAddedToCollections(oldGroup.getUsersAutomaticallyAddedToCollections());
					if (oldGroup.getParent() != null) {
						newGroup.setParent(oldGroup.getParent());
					}
					transaction.add(newGroup);
				}
			}

			try {
				recordServices.execute(transaction);
			} catch (RecordServicesException e) {
				throw new RuntimeException(e);
			}

			Iterator<List<UserCredential>> userCredentialBatchesIterator = new BatchBuilderIterator<>(
					oldUserManager.getUserCredentials().iterator(), 100);

			Map<String, List<String>> invalidUsernameListMappedByCollection = newCollectionMapExceptSystem();
			while (userCredentialBatchesIterator.hasNext()) {
				transaction = new Transaction();
				for (UserCredential userCredential : userCredentialBatchesIterator.next()) {
					String correctedUsername = UserUtils.cleanUsername(userCredential.getUsername());
					if (!existingUsers.contains(correctedUsername)) {
						if (!correctedUsername.equals(userCredential.getUsername())) {
							for (String collection : userCredential.getCollections()) {
								List<String> invalidUsersForCollection = invalidUsernameListMappedByCollection.get(collection);
								invalidUsersForCollection.add(userCredential.getUsername());
								invalidUsernameListMappedByCollection.put(collection, invalidUsersForCollection);
							}
						}
						transaction.add(toSolrUserCredential(userCredential));
					}
				}

				try {
					recordServices.execute(transaction);
				} catch (RecordServicesException e) {
					throw new RuntimeException(e);
				}
			}
			correctUsernameInAllCollections(invalidUsernameListMappedByCollection);

		} finally {
			oldUserManager.close();
			oldGroupManager.close();

			configManager.move(USER_CREDENTIALS_CONFIG, USER_CREDENTIALS_CONFIG + ".old");
			configManager.move(XmlGlobalGroupsManager.CONFIG_FILE, XmlGlobalGroupsManager.CONFIG_FILE + ".old");
		}
	}

	private Map<String, List<String>> newCollectionMapExceptSystem() {
		Map<String, List<String>> returnMap = new HashMap<>();
		CollectionsListManager collectionManager = modelLayerFactory
				.getCollectionsListManager();
		for (String collection : collectionManager.getCollections()) {
			if (!collection.equals(Collection.SYSTEM_COLLECTION)) {
				returnMap.put(collection, new ArrayList<String>());
			}
		}
		return returnMap;
	}

	private void correctUsernameInAllCollections(Map<String, List<String>> invalidUsernameListMappedByCollection) {
		for (Entry<String, List<String>> entry : invalidUsernameListMappedByCollection.entrySet()) {
			String collection = entry.getKey();
			List<String> usersWithInvalidNamesInCollection = entry.getValue();
			if (!usersWithInvalidNamesInCollection.isEmpty()) {
				UserServices userServices = modelLayerFactory.newUserServices();
				Transaction transaction = new Transaction();
				for (String username : usersWithInvalidNamesInCollection) {
					User user = userServices
							.getUserInCollectionCaseSensitive(username, collection);
					if (user != null) {
						user.setUsername(UserUtils.cleanUsername(user.getUsername()));
						transaction.add(user);
					}

				}
				transaction.setOptimisticLockingResolution(OptimisticLockingResolution.EXCEPTION);
				transaction.setOptions(transaction.getRecordUpdateOptions().setValidationsEnabled(false));
				try {
					recordServices.execute(transaction);
				} catch (RecordServicesException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	private SolrUserCredential toSolrUserCredential(UserCredential userCredential) {
		SolrUserCredential newUserCredential = (SolrUserCredential) schemasRecordsServices.newCredential();
		newUserCredential.setFirstName(userCredential.getFirstName());
		newUserCredential.setLastName(userCredential.getLastName());
		newUserCredential.setUsername(UserUtils.cleanUsername(userCredential.getUsername()));
		newUserCredential.setDn(userCredential.getDn());
		newUserCredential.setDomain(userCredential.getDomain());
		newUserCredential.setEmail(userCredential.getEmail());
		newUserCredential.setAccessTokens(userCredential.getAccessTokens());
		newUserCredential.setCollections(userCredential.getCollections());
		newUserCredential.setMsExchDelegateListBL(userCredential.getMsExchDelegateListBL());
		newUserCredential.setStatus(userCredential.getStatus());
		newUserCredential.setServiceKey(userCredential.getServiceKey());
		newUserCredential.setSystemAdmin(userCredential.isSystemAdmin());

		newUserCredential.setGlobalGroups(userCredential.getGlobalGroups());

		return newUserCredential;
	}

}
