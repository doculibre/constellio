package com.constellio.model.services.users;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.users.XmlUserCredentialsManager.USER_CREDENTIALS_CONFIG;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.schemas.validators.EmailValidator;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.SearchServicesRuntimeException;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_NoSuchUser;

public class UserCredentialAndGlobalGroupsMigration {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserCredentialAndGlobalGroupsMigration.class);
	private final ModelLayerFactory modelLayerFactory;
	XmlUserCredentialsManager oldUserManager;
	XmlGlobalGroupsManager oldGroupManager;
	RecordServices recordServices;
	SearchServices searchServices;
	SchemasRecordsServices schemasRecordsServices;
	MetadataSchemasManager schemasManager;
	ConfigManager configManager;

	public UserCredentialAndGlobalGroupsMigration(ModelLayerFactory modelLayerFactory) {

		DataLayerFactory dataLayerFactory = modelLayerFactory.getDataLayerFactory();
		this.configManager = dataLayerFactory.getConfigManager();
		this.schemasManager = modelLayerFactory.getMetadataSchemasManager();

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

		if (schemasRecordsServices.credentialSchema().get(SolrUserCredential.EMAIL).isDefaultRequirement()) {

			schemasManager.modify(Collection.SYSTEM_COLLECTION, new MetadataSchemaTypesAlteration() {
				@Override
				public void alter(MetadataSchemaTypesBuilder types) {
					types.getSchema(SolrUserCredential.DEFAULT_SCHEMA).get(SolrUserCredential.EMAIL)
							.setDefaultRequirement(false).setUniqueValue(false);
				}
			});
			schemasRecordsServices = new SchemasRecordsServices(Collection.SYSTEM_COLLECTION, modelLayerFactory);

		}

		for (String collection : modelLayerFactory.getCollectionsListManager().getCollectionsExcludingSystem()) {
			SchemasRecordsServices collectionSchemas = new SchemasRecordsServices(collection, modelLayerFactory);

			if (collectionSchemas.userEmail().isDefaultRequirement()) {

				schemasManager.modify(Collection.SYSTEM_COLLECTION, new MetadataSchemaTypesAlteration() {
					@Override
					public void alter(MetadataSchemaTypesBuilder types) {
						types.getSchema(User.DEFAULT_SCHEMA).get(User.EMAIL)
								.setDefaultRequirement(false).setUniqueValue(false);
					}
				});
				schemasRecordsServices = new SchemasRecordsServices(Collection.SYSTEM_COLLECTION, modelLayerFactory);

			}
		}
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

	private Map<String, SolrUserCredential> getExistingUsers(List<String> validUsernames, List<String> invalidUsernames) {
		Map<String, SolrUserCredential> existingUsers = new HashMap<>();
		LogicalSearchQuery query = new LogicalSearchQuery(from(schemasRecordsServices.credentialSchemaType()).returnAll());
		Iterator<Record> usersIterator = searchServices.recordsIterator(query, 1000);
		while (usersIterator.hasNext()) {
			SolrUserCredential userCredential = (SolrUserCredential) schemasRecordsServices
					.wrapUserCredential(usersIterator.next());

			String cleanedUsername = UserUtils.cleanUsername(userCredential.getUsername());

			if (!cleanedUsername.equals(userCredential.getUsername())) {
				invalidUsernames.add(userCredential.getUsername());
			} else {
				validUsernames.add(userCredential.getUsername());
			}

			existingUsers.put(cleanedUsername, userCredential);
			userCredential.setUsername(UserUtils.cleanUsername(userCredential.getUsername()));
		}
		return existingUsers;
	}

	public void migrateUserAndGroups() {
		Transaction transaction = new Transaction();

		List<String> existingGroups = getExistingGroups();
		List<String> invalidUsernames = new ArrayList<>();
		List<String> validUsernames = new ArrayList<>();
		Map<String, SolrUserCredential> existingUsers = getExistingUsers(validUsernames, invalidUsernames);

		if (oldGroupManager != null) {
			for (GlobalGroup oldGroup : oldGroupManager.getAllGroups()) {
				if (!existingGroups.contains(oldGroup.getCode())) {
					SolrGlobalGroup newGroup = (SolrGlobalGroup) schemasRecordsServices.newGlobalGroup();
					newGroup.setCode(oldGroup.getCode());
					newGroup.setName(oldGroup.getName());
					newGroup.setTitle(oldGroup.getName());
					newGroup.setStatus(oldGroup.getStatus());
					newGroup.setUsersAutomaticallyAddedToCollections(oldGroup.getUsersAutomaticallyAddedToCollections());
					if (oldGroup.getParent() != null) {
						newGroup.setParent(oldGroup.getParent());
					}
					if (isValid(newGroup)) {
						transaction.add(newGroup);
					}
				}
			}
		}

		try {
			transaction.setOptimisticLockingResolution(OptimisticLockingResolution.EXCEPTION);
			recordServices.execute(transaction);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}

		Iterator<List<UserCredential>> userCredentialBatchesIterator = new BatchBuilderIterator<>(
				oldUserManager.getUserCredentials().iterator(), 100);

		while (userCredentialBatchesIterator.hasNext()) {
			transaction = new Transaction();
			for (UserCredential userCredential : userCredentialBatchesIterator.next()) {
				SolrUserCredential solrUserCredential = toSolrUserCredential(userCredential, existingUsers);
				if (isValid(solrUserCredential)) {
					transaction.add(solrUserCredential);
					if (!solrUserCredential.getUsername().equals(userCredential.getUsername())) {
						invalidUsernames.add(userCredential.getUsername());
					}
				}

			}

			try {
				transaction.setOptimisticLockingResolution(OptimisticLockingResolution.EXCEPTION);
				recordServices.execute(transaction);
			} catch (RecordServicesException e) {
				throw new RuntimeException(e);
			}
		}
		correctUsernameInAllCollections(validUsernames, invalidUsernames);

		Iterator<Record> userCredentialIterator = searchServices.recordsIterator(new LogicalSearchQuery(
				from(schemasRecordsServices.credentialSchemaType()).returnAll()), 100);

		long nbUsers = searchServices.getResultsCount(from(schemasRecordsServices.credentialSchemaType()).returnAll());

		int done = 0;
		UserServices userServices = modelLayerFactory.newUserServices();
		while (userCredentialIterator.hasNext()) {
			UserCredential userCredential = schemasRecordsServices.wrapCredential(userCredentialIterator.next());
			if (validUsernames.contains(userCredential.getUsername())) {
				for (String collection : userCredential.getCollections()) {
					try {
						if (userServices.getUserInCollection(userCredential.getUsername(), collection) == null) {
							userServices.sync(userCredential);
						}
					} catch (SearchServicesRuntimeException.TooManyRecordsInSingleSearchResult e) {
						SchemasRecordsServices collectionSchemas = new SchemasRecordsServices(collection, modelLayerFactory);
						List<Record> userRecords = searchServices.search(new LogicalSearchQuery(
								from(collectionSchemas.userSchemaType())
										.where(collectionSchemas.userUsername()).isEqualTo(userCredential.getUsername())));
						List<User> users = collectionSchemas.wrapUsers(userRecords);
						Transaction transaction2 = new Transaction();

						for (int i = 1; i < users.size(); i++) {
							transaction2.add(users.get(i)).setUsername(userCredential.getUsername() + "-disabled");
						}

						try {
							transaction2.getRecordUpdateOptions().setValidationsEnabled(false);
							transaction.setOptimisticLockingResolution(OptimisticLockingResolution.EXCEPTION);
							recordServices.execute(transaction2);
						} catch (RecordServicesException e1) {
							throw new RuntimeException(e1);
						}
						userServices.sync(userCredential);
					}
				}
			}
			LOGGER.info("Validating users after migration : " + ++done + "/" + nbUsers);
		}

		if (oldGroupManager != null) {
			oldGroupManager.close();
		}

		configManager.move(USER_CREDENTIALS_CONFIG, USER_CREDENTIALS_CONFIG + ".old");
		if (oldGroupManager != null) {
			configManager.move(XmlGlobalGroupsManager.CONFIG_FILE, XmlGlobalGroupsManager.CONFIG_FILE + ".old");
		}
	}

	private boolean isValid(SolrGlobalGroup group) {
		return StringUtils.isNotBlank(group.getCode());
	}

	private boolean isValid(UserCredential user) {
		return StringUtils.isNotBlank(user.getUsername());
	}

	private void correctUsernameInAllCollections(List<String> validUsernames, List<String> invalidUsernames) {
		UserServices userServices = modelLayerFactory.newUserServices();
		CollectionsListManager collectionManager = modelLayerFactory.getCollectionsListManager();
		for (String collection : collectionManager.getCollections()) {
			Transaction transaction = new Transaction();
			for (String username : invalidUsernames) {
				try {
					User user = userServices.getUserInCollectionCaseSensitive(username, collection);
					if (user != null) {
						String cleanedUsername = UserUtils.cleanUsername(user.getUsername());
						if (!validUsernames.contains(cleanedUsername)) {
							user.setUsername(cleanedUsername);
							transaction.add(user);
							validUsernames.add(cleanedUsername);
						}
					}
				} catch (UserServicesRuntimeException_NoSuchUser e) {
					//OK
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

	private SolrUserCredential toSolrUserCredential(UserCredential userCredential,
			Map<String, SolrUserCredential> existingUsers) {
		String correctedUsername = UserUtils.cleanUsername(userCredential.getUsername());
		SolrUserCredential newUserCredential = existingUsers.get(correctedUsername);

		if (newUserCredential == null) {
			newUserCredential = (SolrUserCredential) schemasRecordsServices.newCredential();
		}

		newUserCredential.setFirstName(userCredential.getFirstName());
		newUserCredential.setLastName(userCredential.getLastName());
		newUserCredential.setUsername(correctedUsername);
		newUserCredential.setDn(userCredential.getDn());
		newUserCredential.setDomain(userCredential.getDomain());
		if (EmailValidator.isValid(userCredential.getEmail())) {
			newUserCredential.setEmail(userCredential.getEmail());
		}
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
