package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.model.calculators.group.GroupCaptionCalculator;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaDisplayManagerTransaction;
import com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.data.dao.dto.records.TransactionDTO;
import com.constellio.data.dao.services.bigVault.RecordDaoException.OptimisticLocking;
import com.constellio.data.dao.services.bigVault.SearchResponseIterator;
import com.constellio.data.services.tenant.TenantLocal;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.conf.email.BaseEmailServerConfiguration;
import com.constellio.model.conf.email.EmailConfigurationsManager;
import com.constellio.model.conf.email.EmailServerConfiguration;
import com.constellio.model.conf.ldap.LDAPConfigurationManager;
import com.constellio.model.conf.ldap.config.LDAPUserSyncConfiguration;
import com.constellio.model.entities.enums.DecryptionVersion;
import com.constellio.model.entities.records.ActionExecutorInBatch;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Record.GetMetadataOption;
import com.constellio.model.entities.records.Record.SetMetadataOption;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.records.wrappers.ExternalAccessUrl;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.records.wrappers.structure.ExternalAccessUrlStatus;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.global.GlobalGroup;
import com.constellio.model.entities.security.global.GlobalGroupStatus;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.entities.security.global.UserSyncMode;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.encrypt.EncryptionKeyFactory;
import com.constellio.model.services.encrypt.EncryptionServices;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordDeleteServices;
import com.constellio.model.services.records.RecordPhysicalDeleteOptions;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.records.UnhandledRecordModificationImpactHandler;
import com.constellio.model.services.schemas.builders.CommonMetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.users.SystemWideUserInfos;
import com.constellio.model.services.users.UserServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Key;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.constellio.model.entities.security.global.UserCredentialStatus.DISABLED;
import static com.constellio.model.entities.security.global.UserSyncMode.SYNCED;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.stream.Collectors.toList;

public class CoreMigrationTo_9_2 extends MigrationHelper implements MigrationScript {

	private static final Logger LOGGER = LoggerFactory.getLogger(CoreMigrationTo_9_2.class);

	private static TenantLocal<Key> OLD_KEY = new TenantLocal<>();
	private static TenantLocal<CoreMigrationTo_9_2_UserMigrationData> USER = new TenantLocal<>();

	private static class CoreMigrationTo_9_2_UserMigrationData {
		Map<String, Map<String, Object>> credentialMetadataMap = new HashMap<>();
		Map<String, Map<String, Object>> globalGroupMetadataMap = new HashMap<>();
	}

	@Override
	public String getVersion() {
		return "9.2";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory) throws Exception {
		SystemConfigurationsManager systemConfigurationsManager = appLayerFactory.getModelLayerFactory().getSystemConfigurationsManager();

		try {
			setDecrytionVersion(appLayerFactory.getModelLayerFactory(), systemConfigurationsManager, DecryptionVersion.VERSION1);
			migrate_1(collection, migrationResourcesProvider, appLayerFactory);
			migrate_2(collection, migrationResourcesProvider, appLayerFactory);
			setDecrytionVersion(appLayerFactory.getModelLayerFactory(),systemConfigurationsManager, DecryptionVersion.VERSION2);
		} catch (Exception e) {
			setDecrytionVersion(appLayerFactory.getModelLayerFactory(),systemConfigurationsManager, DecryptionVersion.VERSION1);
			throw e;
		}
	}

	private void setDecrytionVersion(ModelLayerFactory modelLayerFactory, SystemConfigurationsManager systemConfigurationsManager,
									 DecryptionVersion version1) {
		systemConfigurationsManager.setValue(ConstellioEIMConfigs.DECRYPTION_VERSION, version1);
		modelLayerFactory.resetEncryptionServices();
	}

	public void migrate_1(String collection, MigrationResourcesProvider migrationResourcesProvider,
						  AppLayerFactory appLayerFactory) throws Exception {

		CoreMigrationTo_9_2_UserMigrationData ctx = USER.get();
		if (ctx == null) {
			ctx = new CoreMigrationTo_9_2_UserMigrationData();
			USER.set(ctx);
		}
		if (collection.equals(Collection.SYSTEM_COLLECTION)) {
			//Les UserCredential sans collection seront supprimés
			//safePhysicalDeleteAllUserCredentialsWithEmptyCollections(appLayerFactory.getModelLayerFactory());
			transferDomainsAndMsDelegatesFromCredentialsToUser(appLayerFactory.getModelLayerFactory().newUserServices(), ctx);
			transferGroupStatusFromGlobalToGroup(appLayerFactory.getModelLayerFactory(), ctx);
			new SchemaSystemAlterationFor_9_2(collection, migrationResourcesProvider, appLayerFactory).migrate();
			//Les utilisateurs qui ne sont pas actifs sont supprimés logiquement
			//logicallyRemoveAllNonActiveUsers(appLayerFactory.getModelLayerFactory());
			//GlobalGroup est supprimé.
			physicallyRemoveGlobalGroup(appLayerFactory.getModelLayerFactory());
			//chaque utilisateur migré aura la valeur LOCALLY_CREATED si il existe une entrée dans le fichier de mot de passe, sinon SYNCED
			updateAllUserSyncMode(appLayerFactory.getModelLayerFactory());
		} else {
			new SchemaAlterationFor_9_2(collection, migrationResourcesProvider, appLayerFactory).migrate();
			//Les utilisateurs désactivés qui ne sont pas utilisés sont supprimé physiquement

			transferglGroupStatusToCollectionGroup(collection, appLayerFactory.getModelLayerFactory(), ctx);
			transferDomainsAndMsDelegatesFromCredentialsToUser(collection, appLayerFactory.getModelLayerFactory(), ctx);
			//deleteUnusedUsers(appLayerFactory, collection);
		}
	}

	private void deleteUnusedUsers(AppLayerFactory appLayerFactory, String collection) {
		UserServices userServices = appLayerFactory.getModelLayerFactory().newUserServices();

		List<RecordDTO> userIdsToDelete = new ArrayList<>();

		List<User> usersToEvaluate = userServices.streamUser(collection)
				.filter(u -> {
					UserCredential userCredential = userServices.getUserCredential(u.getUsername());

					return DISABLED == u.getStatus() &&
						   (userCredential == null || SYNCED == userCredential.getSyncMode());
				})
				.collect(toList());

		for (int i = 0; i < usersToEvaluate.size(); i++) {
			User userToEvaluate = usersToEvaluate.get(i);
			if(!userToEvaluate.getUsername().equals("admin")) {

				if (!userServices.hasUsedSystem(userToEvaluate)) {
					userIdsToDelete.add(userToEvaluate.getWrappedRecord().getRecordDTO());
				}

				if (i + 1 == usersToEvaluate.size() || (i + 1) % 100 == 0) {
					LOGGER.info("Evaluating users to delete in collection '" + collection + "' : " + (1 + i) + "/" + usersToEvaluate.size());
				}
			}
		}

		LOGGER.info("Deleting " + userIdsToDelete.size() +" users to delete in collection '" + collection + "'");
		try {
			appLayerFactory.getModelLayerFactory().getDataLayerFactory().newRecordDao().execute(
					new TransactionDTO(RecordsFlushing.NOW()).withDeletedRecords(userIdsToDelete));

		} catch (OptimisticLocking e) {
			LOGGER.warn("Problem while deleting unused user, just cancelling",e);
		}

		appLayerFactory.getModelLayerFactory().getRecordsCaches().getCache(collection)
				.removeFromAllCaches(userIdsToDelete.stream().map(RecordDTO::getId).collect(toList()));

	}

	class SchemaSystemAlterationFor_9_2 extends MetadataSchemasAlterationHelper {

		protected SchemaSystemAlterationFor_9_2(String collection,
												MigrationResourcesProvider migrationResourcesProvider,
												AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {

			//USERS

			//Les User auront 4 statuts : active, pending, suspended, disabled (au lieu de deleted) (nothing to do here)
			//Une métadonnée enum "syncMode" dans Usercredential avec les choix SYNCED, NOT_SYNCED, LOCALLY_CREATED
			MetadataSchemaBuilder userCredentialSchema = typesBuilder.getSchemaType(UserCredential.SCHEMA_TYPE).getDefaultSchema();
			MetadataBuilder userSyncModeBuilder = userCredentialSchema.createUndeletable("syncMode")
					.defineAsEnum(UserSyncMode.class).setDefaultValue(UserSyncMode.LOCALLY_CREATED);

			//Les métadonnées suivantes sont retirées de UserCredential, car l'information existe dans User :
			// firstname, lastname, email, personalEmails, collections, globalGroups, phone, fax, jobTitle, address

			userCredentialSchema.getMetadata(CommonMetadataBuilder.TITLE).defineDataEntry().asManual();

			//NOT READY YET, STOP USING THEM IN USERSERVICES BEFORE
			//			userCredentialSchema.deleteMetadataWithoutValidation("title");
			//			userCredentialSchema.deleteMetadataWithoutValidation("firstname");
			//			userCredentialSchema.deleteMetadataWithoutValidation("lastname");
			//			userCredentialSchema.deleteMetadataWithoutValidation("email");
			//			userCredentialSchema.deleteMetadataWithoutValidation("personalEmails");
			//			userCredentialSchema.deleteMetadataWithoutValidation("collections");
			//			userCredentialSchema.deleteMetadataWithoutValidation("globalGroups");
			//			userCredentialSchema.deleteMetadataWithoutValidation("phone");
			//			userCredentialSchema.deleteMetadataWithoutValidation("fax");
			//			userCredentialSchema.deleteMetadataWithoutValidation("jobTitle");
			//			userCredentialSchema.deleteMetadataWithoutValidation("address");

			//Les groupes qui ne sont pas actifs sont supprimés logiquement

			MetadataSchemaBuilder glGroupSchema = typesBuilder.getSchemaType(GlobalGroup.SCHEMA_TYPE).getDefaultSchema();

			//NOT READY YET, STOP USING THEM IN USERSERVICES BEFORE
			//			glGroupSchema.deleteMetadataWithoutValidation("usersAutomaticallyAddedToCollections");
			//			glGroupSchema.deleteMetadataWithoutValidation("status");
			//			glGroupSchema.deleteMetadataWithoutValidation("locallyCreated");
			//			glGroupSchema.deleteMetadataWithoutValidation("hierarchy");

			//Les métadonnées suivantes seront déplacées dans User par le script de migration : domain, msExchangeDelegateList
			//sauvegarder en map pour l'instant

			//userCredentialSchema.deleteMetadataWithoutValidation("domain");
			//userCredentialSchema.deleteMetadataWithoutValidation("msExchangeDelegateList");
		}

	}


	class SchemaAlterationFor_9_2 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationFor_9_2(String collection,
										  MigrationResourcesProvider migrationResourcesProvider,
										  AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			//USERS

			//Les métadonnées suivantes sont déplacées dans User par le script de migration : domain, msExchangeDelegateList
			MetadataSchemaBuilder userSchema = typesBuilder.getSchemaType(User.SCHEMA_TYPE).getDefaultSchema();
			MetadataBuilder userCreateDomain = userSchema.createUndeletable(User.DOMAIN)
					.setType(MetadataValueType.STRING).setDefaultValue("");
			MetadataBuilder userCreateMsExchange = userSchema.createUndeletable(User.MS_EXCHANGE_DELEGATE_LIST).setType(MetadataValueType.STRING)
					.setMultivalue(true);

			//GROUPS
			//la métadonnée "usersAutomaticallyAddedToCollections" est supprimée
			MetadataSchemaBuilder groupSchema = typesBuilder.getSchemaType(Group.SCHEMA_TYPE).getDefaultSchema();

			//les attributs "locallyCreated", "hierarchy" et "status" sont déplacés dans Group
			groupSchema.createUndeletable(Group.STATUS)
					.defineAsEnum(GlobalGroupStatus.class).setDefaultRequirement(true);
			groupSchema.createUndeletable(Group.LOCALLY_CREATED)
					.setType(MetadataValueType.BOOLEAN).setDefaultValue(true);
			groupSchema.createUndeletable(Group.HIERARCHY)
					.setType(MetadataValueType.STRING);

			configureTableMetadatas(collection, appLayerFactory);

			//Calculator for Caption metadata
			typesBuilder.getDefaultSchema(Group.SCHEMA_TYPE).get(Schemas.CAPTION.getLocalCode())
					.defineDataEntry().asCalculated(GroupCaptionCalculator.class);
		}

	}

	private void transferDomainsAndMsDelegatesFromCredentialsToUser(String collection,
																	ModelLayerFactory modelLayerFactory,
																	CoreMigrationTo_9_2_UserMigrationData ctx)
			throws Exception {
		//List<User> users = new ArrayList<>();
		RecordServices recordServices = modelLayerFactory.newRecordServices();
		SearchServices searchServices = modelLayerFactory.newSearchServices();
		SchemasRecordsServices schemas = new SchemasRecordsServices(collection, modelLayerFactory);
		//users.addAll(getAllUsersInCollection(modelLayerFactory, collection));
		MetadataSchema userMetadataSchema = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection).getDefaultSchema(User.SCHEMA_TYPE);
		if (userMetadataSchema.hasMetadataWithCode(User.DOMAIN) && userMetadataSchema.hasMetadataWithCode(User.MS_EXCHANGE_DELEGATE_LIST)) {

			new ActionExecutorInBatch(searchServices, "Removing non active users", 1000) {
				@Override
				public void doActionOnBatch(List<Record> records)
						throws Exception {
					Transaction tx = new Transaction();
					for (Record record : records) {
						User user = schemas.wrapUser(record);
						if (ctx.credentialMetadataMap.containsKey(user.getUsername())) {
							Map<String, Object> metadatas = ctx.credentialMetadataMap.get(user.getUsername());
							user.setDomain((String) metadatas.get("domain"));
							user.setMsExchDelegateListBL((List) metadatas.get("msExchangeDelegateList"));
							tx.add(user);
						}
					}
					if(tx.getRecords().size() > 0) {
						tx.setSkippingRequiredValuesValidation(true);
						recordServices.executeWithImpactHandler(tx, new UnhandledRecordModificationImpactHandler());
					}
				}
			}.execute(from(schemas.user.schemaType()).returnAll());
		}
	}

	private void transferglGroupStatusToCollectionGroup(String collection,
														ModelLayerFactory modelLayerFactory,
														CoreMigrationTo_9_2_UserMigrationData ctx)
			throws Exception {
		//List<User> users = new ArrayList<>();
		RecordServices recordServices = modelLayerFactory.newRecordServices();
		SearchServices searchServices = modelLayerFactory.newSearchServices();
		SchemasRecordsServices schemas = new SchemasRecordsServices(collection, modelLayerFactory);
		//users.addAll(getAllUsersInCollection(modelLayerFactory, collection));
		MetadataSchema groupMetadataSchema = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection).getDefaultSchema(Group.SCHEMA_TYPE);
		if (groupMetadataSchema.hasMetadataWithCode(Group.LOCALLY_CREATED) && groupMetadataSchema.hasMetadataWithCode(Group.STATUS)) {

			new ActionExecutorInBatch(searchServices, "Removing non active users", 1000) {
				@Override
				public void doActionOnBatch(List<Record> records)
						throws Exception {
					Transaction tx = new Transaction();
					for (Record record : records) {
						Group group = schemas.wrapGroup(record);
						if (ctx.globalGroupMetadataMap.containsKey(group.getCode())) {
							Map<String, Object> metadatas = ctx.globalGroupMetadataMap.get(group.getCode());
							group.setHierarchy((String) metadatas.get("hierarchy"));
							group.setStatus((GlobalGroupStatus) metadatas.get("status"));
							group.setLocallyCreated((boolean) metadatas.get("locallyCreated"));
							tx.add(group);
						}
					}
					if(tx.getRecords().size() > 0) {
						tx.setSkippingRequiredValuesValidation(true);
						recordServices.executeWithImpactHandler(tx, new UnhandledRecordModificationImpactHandler());
					}
				}
			}.execute(from(schemas.group.schemaType()).returnAll());
		}
	}

	//	private void logicallyRemoveAllNonActiveUsers(ModelLayerFactory modelLayerFactory) throws Exception {
	//		RecordServices recordServices = modelLayerFactory.newRecordServices();
	//		SearchServices searchServices = modelLayerFactory.newSearchServices();
	//		SchemasRecordsServices systemSchemas = new SchemasRecordsServices(Collection.SYSTEM_COLLECTION, modelLayerFactory);
	//
	//		new ActionExecutorInBatch(searchServices, "Removing non active users", 250) {
	//			@Override
	//			public void doActionOnBatch(List<Record> records)
	//					throws Exception {
	//				Transaction tx = new Transaction();
	//				for (Record record : records) {
	//					UserCredential userCredential = systemSchemas.wrapUserCredential(record);
	//					if (userCredential.getStatus() != UserCredentialStatus.ACTIVE) {
	//						userCredential.set(Schemas.LOGICALLY_DELETED_STATUS, true);
	//						tx.add(userCredential);
	//					}
	//				}
	//				tx.setSkippingRequiredValuesValidation(true);
	//				recordServices.executeWithImpactHandler(tx, new UnhandledRecordModificationImpactHandler());
	//			}
	//		}.execute(from(systemSchemas.credentialSchemaType()).returnAll());
	//
	//	}

	private void updateAllUserSyncMode(ModelLayerFactory modelLayerFactory) throws Exception {
		RecordServices recordServices = modelLayerFactory.newRecordServices();

		SearchServices searchServices = modelLayerFactory.newSearchServices();
		SchemasRecordsServices systemSchemas = new SchemasRecordsServices(Collection.SYSTEM_COLLECTION, modelLayerFactory);

		new ActionExecutorInBatch(searchServices, "Update sync mode credentials", 1000) {
			@Override
			public void doActionOnBatch(List<Record> records)
					throws Exception {
				Transaction tx = new Transaction();
				for (Record record : records) {
					UserCredential credential = systemSchemas.wrapUserCredential(record);
					if (credential.getDn() != null) {
						credential.setSyncMode(SYNCED);
					} else {
						credential.setSyncMode(UserSyncMode.LOCALLY_CREATED);
					}
					tx.add(credential);
				}
				tx.setSkippingRequiredValuesValidation(true);
				recordServices.executeWithImpactHandler(tx, new UnhandledRecordModificationImpactHandler());
			}
		}.execute(from(systemSchemas.credentialSchemaType()).returnAll());
	}

	private void physicallyRemoveGlobalGroup(ModelLayerFactory modelLayerFactory) throws Exception {
//		RecordDeleteServices recordServices = new RecordDeleteServices(modelLayerFactory);
//		SearchServices searchServices = modelLayerFactory.newSearchServices();
//		SchemasRecordsServices systemSchemas = new SchemasRecordsServices(Collection.SYSTEM_COLLECTION, modelLayerFactory);
//
//		new ActionExecutorInBatch(searchServices, "Removing global groups", 250) {
//			@Override
//			public void doActionOnBatch(List<Record> records)
//					throws Exception {
//				for (Record record : records) {
//					GlobalGroup globalGroup = systemSchemas.wrapOldGlobalGroup(record);
//					recordServices.physicallyDeleteNoMatterTheStatus(globalGroup, User.GOD, new RecordPhysicalDeleteOptions());
//				}
//			}
//		}.execute(from(systemSchemas.globalGroupSchemaType()).returnAll());
//
//		new ActionExecutorInBatch(searchServices, "Removing groups (system group schema)", 250) {
//			@Override
//			public void doActionOnBatch(List<Record> records)
//					throws Exception {
//				for (Record record : records) {
//					recordServices.logicallyDelete(record, User.GOD);
//				}
//			}
//		}.execute(from(systemSchemas.group.schemaType()).returnAll());

		SearchServices searchServices = modelLayerFactory.newSearchServices();



		SchemasRecordsServices schemas = new SchemasRecordsServices(Collection.SYSTEM_COLLECTION, modelLayerFactory);
		List<RecordDTO> groupsToDelete = searchServices.search(new LogicalSearchQuery(from(schemas.globalGroupSchemaType()).returnAll()))
				.stream().map(r->r.getRecordDTO()).collect(toList());

		LOGGER.info("Deleting " + groupsToDelete.size() +" global groups");
		try {
			modelLayerFactory.getDataLayerFactory().newRecordDao().execute(
					new TransactionDTO(RecordsFlushing.NOW()).withDeletedRecords(groupsToDelete));

		} catch (OptimisticLocking e) {
			LOGGER.warn("Problem while deleting unused user, just cancelling",e);
		}

		modelLayerFactory.getRecordsCaches().getCache(Collection.SYSTEM_COLLECTION)
				.removeFromAllCaches(groupsToDelete.stream().map(RecordDTO::getId).collect(toList()));
	}

	private void safePhysicalDeleteAllUserCredentialsWithEmptyCollections(ModelLayerFactory modelLayerFactory)
			throws Exception {
		RecordDeleteServices recordServices = new RecordDeleteServices(modelLayerFactory);
		SearchServices searchServices = modelLayerFactory.newSearchServices();
		SchemasRecordsServices systemSchemas = new SchemasRecordsServices(Collection.SYSTEM_COLLECTION, modelLayerFactory);

		new ActionExecutorInBatch(searchServices, "Removing user credentials with empty collections", 1000) {
			@Override
			public void doActionOnBatch(List<Record> records)
					throws Exception {
				//Transaction tx = new Transaction();
				for (Record record : records) {
					UserCredential userCredential = systemSchemas.wrapUserCredential(record);
					if (!userCredential.getUsername().equals("admin") &&
						(userCredential.getCollections() == null || userCredential.getCollections().isEmpty())) {
						recordServices.physicallyDeleteNoMatterTheStatus(userCredential, User.GOD, new RecordPhysicalDeleteOptions());
					}
				}
				//tx.setSkippingRequiredValuesValidation(true);
				//recordServices.executeWithImpactHandler(tx, new UnhandledRecordModificationImpactHandler());
			}
		}.execute(from(systemSchemas.credentialSchemaType()).returnAll());

	}

	//helpers
	private List<GlobalGroup> getAllGlobalGroups(ModelLayerFactory modelLayerFactory) {
		SearchServices searchServices = modelLayerFactory.newSearchServices();
		SchemasRecordsServices systemSchemas = new SchemasRecordsServices(Collection.SYSTEM_COLLECTION, modelLayerFactory);

		List<Record> groupeRecord = searchServices.search(new LogicalSearchQuery(
				from(systemSchemas.globalGroupSchemaType()).returnAll()));
		return groupeRecord == null ? null : systemSchemas.wrapOldGlobalGroups(groupeRecord);
	}

	private List<UserCredential> getUserCredentials(ModelLayerFactory modelLayerFactory) {
		//This method may exist in UserServices for the moment, but we will try to remove it from the service
		SearchServices searchServices = modelLayerFactory.newSearchServices();
		SchemasRecordsServices systemSchemas = new SchemasRecordsServices(Collection.SYSTEM_COLLECTION, modelLayerFactory);

		List<Record> userCredentialRecord = searchServices.search(new LogicalSearchQuery(
				from(systemSchemas.credentialSchemaType()).returnAll()));
		return userCredentialRecord == null ? null : systemSchemas.wrapUserCredentials(userCredentialRecord);
	}

	private List<User> getAllUsersInCollection(ModelLayerFactory modelLayerFactory, String collection) {
		SearchServices searchServices = modelLayerFactory.newSearchServices();
		//This method may exist in UserServices for the moment, but we will try to remove it from the service
		SchemasRecordsServices schemas = new SchemasRecordsServices(collection, modelLayerFactory);
		List<Record> userRecord = searchServices.search(new LogicalSearchQuery(
				from(schemas.user.schemaType()).returnAll()));
		return userRecord == null ? null : schemas.wrapUsers(userRecord);
	}

	private void configureTableMetadatas(String collection, AppLayerFactory appLayerFactory) {
		SchemaDisplayManagerTransaction transaction = new SchemaDisplayManagerTransaction();
		SchemasDisplayManager manager = appLayerFactory.getMetadataSchemasDisplayManager();

		SchemasDisplayManager displayManager = appLayerFactory.getMetadataSchemasDisplayManager();
		SchemaTypesDisplayTransactionBuilder transactionBuilder = displayManager.newTransactionBuilderFor(collection);

		transactionBuilder.add(displayManager.getSchema(Collection.SYSTEM_COLLECTION, User.DEFAULT_SCHEMA)
				.withNewTableMetadatas(User.DEFAULT_SCHEMA + "_" + User.USERNAME)
				.withNewTableMetadatas(User.DEFAULT_SCHEMA + "_" + User.FIRSTNAME)
				.withNewTableMetadatas(User.DEFAULT_SCHEMA + "_" + User.LASTNAME)
				.withNewTableMetadatas(User.DEFAULT_SCHEMA + "_" + User.LAST_LOGIN));
		transactionBuilder.add(displayManager.getSchema(Collection.SYSTEM_COLLECTION, Group.DEFAULT_SCHEMA)
				.withNewTableMetadatas(Group.DEFAULT_SCHEMA + "_" + Group.CODE));

		displayManager.execute(transactionBuilder.build());
	}

	private void transferDomainsAndMsDelegatesFromCredentialsToUser(UserServices userServices,
																	CoreMigrationTo_9_2_UserMigrationData ctx) {

		List<SystemWideUserInfos> credentials = userServices.getAllUserCredentials();

		for (SystemWideUserInfos credential :
				credentials) {
			Map<String, Object> metadatas = new HashMap<>();
			if (credential.getDomain() != null) {
				metadatas.put("domain", credential.getDomain());
			}
			if (credential.getMsExchangeDelegateList() != null) {
				metadatas.put("msExchangeDelegateList", credential.getMsExchangeDelegateList());
			}
			ctx.credentialMetadataMap.put(credential.getUsername(), metadatas);
		}
	}

	private void transferGroupStatusFromGlobalToGroup(ModelLayerFactory modelLayerFactory,
													  CoreMigrationTo_9_2_UserMigrationData ctx) {

		List<GlobalGroup> groups = this.getAllGlobalGroups(modelLayerFactory);

		for (GlobalGroup gr : groups) {
			Map<String, Object> metadatas = new HashMap<>();
			if (gr.getStatus() != null) {
				metadatas.put("status", gr.getStatus());
			} else {
				metadatas.put("status", GlobalGroupStatus.ACTIVE);
			}
			metadatas.put("locallyCreated", gr.isLocallyCreated());

			ctx.globalGroupMetadataMap.put(gr.getCode(), metadatas);
		}
	}

	public void migrate_2(String collection, MigrationResourcesProvider migrationResourcesProvider,
						  AppLayerFactory appLayerFactory)
			throws Exception {
		new SchemaAlterationFor_9_2_2(collection, migrationResourcesProvider, appLayerFactory).migrate();

		if (collection.equals(Collection.SYSTEM_COLLECTION)) {
			new EncryptionSystemCollectionMigration_9_2_2(collection, appLayerFactory).doMigration();
		}

		new EncryptionMigration_9_2_2(collection, appLayerFactory).doMigration();

	}

	class SchemaAlterationFor_9_2_2 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationFor_9_2_2(String collection, MigrationResourcesProvider migrationResourcesProvider,
											AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			if (!typesBuilder.hasSchemaType(ExternalAccessUrl.SCHEMA_TYPE)) {
				MetadataSchemaTypeBuilder externalAccessUrlSchemaType =
						typesBuilder.createNewSchemaType(ExternalAccessUrl.SCHEMA_TYPE).setSecurity(false);
				MetadataSchemaBuilder externalAccessUrlSchema = externalAccessUrlSchemaType.getDefaultSchema();

				externalAccessUrlSchema.createUndeletable(ExternalAccessUrl.TOKEN)
						.setType(MetadataValueType.STRING);
				externalAccessUrlSchema.createUndeletable(ExternalAccessUrl.FULLNAME)
						.setType(MetadataValueType.STRING);
				externalAccessUrlSchema.createUndeletable(ExternalAccessUrl.EXPIRATION_DATE)
						.setType(MetadataValueType.DATE);
				externalAccessUrlSchema.createUndeletable(ExternalAccessUrl.STATUS)
						.setType(MetadataValueType.ENUM)
						.defineAsEnum(ExternalAccessUrlStatus.class);
				externalAccessUrlSchema.createUndeletable(ExternalAccessUrl.ACCESS_RECORD)
						.setType(MetadataValueType.STRING);
			}
		}
	}

	class EncryptionSystemCollectionMigration_9_2_2 {
		private String collection;
		private AppLayerFactory appLayerFactory;
		private ModelLayerFactory modelLayerFactory;

		protected EncryptionSystemCollectionMigration_9_2_2(String collection,
															AppLayerFactory appLayerFactory) {
			this.collection = collection;
			this.appLayerFactory = appLayerFactory;
			this.modelLayerFactory = appLayerFactory.getModelLayerFactory();
		}

		protected void doMigration() {

			try {
				Key encryptionKey = EncryptionKeyFactory.getApplicationKey(appLayerFactory.getModelLayerFactory());
				OLD_KEY.set(encryptionKey);
			} catch (Throwable t) {
				t.printStackTrace();
				throw t;
			}
			//			modelLayerFactory.getDataLayerFactory().saveEncryptionKey();
			modelLayerFactory.resetEncryptionServices();

			try {
				boolean lostPrivateKey = Toggle.LOST_PRIVATE_KEY.isEnabled() || OLD_KEY.get() == null
										 ||  modelLayerFactory.getConfiguration().isPreviousPrivateKeyLost();
				EncryptionServices oldEncryptionServices = new EncryptionServices(lostPrivateKey, DecryptionVersion.VERSION1);
				if (!lostPrivateKey) {
					oldEncryptionServices.withKeyAndIV(OLD_KEY.get());
				}

				LDAPConfigurationManager ldapConfigurationManager = modelLayerFactory.getLdapConfigurationManager();

				LDAPUserSyncConfiguration ldapUserSyncConfiguration = ldapConfigurationManager.getLDAPUserSyncConfiguration(false);

				String password = ldapUserSyncConfiguration.getPassword();


				if (password != null && !password.equals("")) {
					password = oldEncryptionServices.decryptVersion1(password);

					LDAPUserSyncConfiguration newLdapUserSyncConfiguration = new LDAPUserSyncConfiguration(ldapUserSyncConfiguration.getUser(), password, ldapUserSyncConfiguration.getUserFilter(), ldapUserSyncConfiguration.getGroupFilter(), ldapUserSyncConfiguration.getDurationBetweenExecution(),
							ldapUserSyncConfiguration.getScheduleTime(), ldapUserSyncConfiguration.getGroupBaseContextList(),
							ldapUserSyncConfiguration.getUsersWithoutGroupsBaseContextList(), ldapUserSyncConfiguration.getUserFilterGroupsList(),
							ldapUserSyncConfiguration.isMembershipAutomaticDerivationActivated(), ldapUserSyncConfiguration.getSelectedCollectionsCodes());

					ldapConfigurationManager.saveLDAPConfiguration(ldapConfigurationManager.getLDAPServerConfiguration(), newLdapUserSyncConfiguration);
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	class EncryptionMigration_9_2_2 {
		private String collection;
		private AppLayerFactory appLayerFactory;
		private ModelLayerFactory modelLayerFactory;

		protected EncryptionMigration_9_2_2(String collection,
											AppLayerFactory appLayerFactory) {
			this.collection = collection;
			this.appLayerFactory = appLayerFactory;
			this.modelLayerFactory = appLayerFactory.getModelLayerFactory();
		}

		protected void doMigration() {
			EmailConfigurationsManager emailConfigurationsManager = modelLayerFactory.getEmailConfigurationsManager();
			EmailServerConfiguration emailServerConfiguration = emailConfigurationsManager.getEmailConfiguration(this.collection, false);

			try {
				boolean lostPrivateKey = false;
				EncryptionServices oldEncryptionServices = new EncryptionServices(lostPrivateKey, DecryptionVersion.VERSION1);
				if (!lostPrivateKey) {
					oldEncryptionServices.withKeyAndIV(OLD_KEY.get());
				}
				EncryptionServices newEncryptionServices = modelLayerFactory.newEncryptionServices();

				if (emailServerConfiguration != null) {
					String password = emailServerConfiguration.getPassword();

					String decriptedPassword = oldEncryptionServices.decryptVersion1(password);

					String encryptedPassword = (String) newEncryptionServices.encryptWithAppKey(decriptedPassword);

					BaseEmailServerConfiguration baseEmailServerConfiguration = new BaseEmailServerConfiguration(emailServerConfiguration.getUsername(),
							encryptedPassword, emailServerConfiguration.getDefaultSenderEmail(),
							emailServerConfiguration.getProperties(), emailServerConfiguration.isEnabled());

					emailConfigurationsManager.updateEmailServerConfiguration(baseEmailServerConfiguration, collection, false);
				}
				MetadataSchemaTypes metadataSchemaTypes = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection);

				RecordServices recordServices = modelLayerFactory.newRecordServices();

				for (MetadataSchemaType metadataSchemaType : metadataSchemaTypes.getSchemaTypes()) {
					for (MetadataSchema metadataSchema : metadataSchemaType.getAllSchemas()) {
						List<Metadata> metadataToReEncrypt = new ArrayList<>();
						for (Metadata metadata : metadataSchema.getOnlyNonHerited()) {
							if (metadata.isEncrypted()) {
								metadataToReEncrypt.add(metadata);
							}
						}

						if (metadataToReEncrypt.size() == 0) {
							continue;
						}

						LogicalSearchQuery logicalSearchQuery = new LogicalSearchQuery(LogicalSearchQueryOperators.from(metadataSchema).whereAny(metadataToReEncrypt).isNotNull());

						SearchServices searchServices = modelLayerFactory.newSearchServices();
						SearchResponseIterator<Record> searchResponseIterator = searchServices.recordsIterator(logicalSearchQuery, 1000);


						for (SearchResponseIterator<List<Record>> batches = searchResponseIterator.inBatches(); batches.hasNext(); ) {
							Iterator<Record> currentBatch = batches.next().iterator();
							Transaction transaction = new Transaction();
							boolean executeTransaction = false;
							while (currentBatch.hasNext()) {
								Record currentRecord = currentBatch.next();

								boolean hasModifications = false;
								for (Metadata currentMetadata : metadataToReEncrypt) {
									hasModifications = true;
 									Object data = currentRecord.get(currentMetadata, GetMetadataOption.NO_DECRYPTION);
									Object decriptedData = oldEncryptionServices.decryptVersion1(data);
									currentRecord.set(currentMetadata, SetMetadataOption.NO_DECRYPTION, decriptedData);
								}

								if (hasModifications) {
									executeTransaction = true;
									transaction.update(currentRecord);
								}
							}

							if (executeTransaction) {
								recordServices.execute(transaction);
							}
						}


					}
				}

			} catch (Exception e) {
				new RuntimeException(e);
			}
		}
	}

}
