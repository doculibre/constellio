package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaDisplayManagerTransaction;
import com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.records.ActionExecutorInBatch;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.security.global.GlobalGroup;
import com.constellio.model.entities.security.global.GlobalGroupStatus;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import com.constellio.model.entities.security.global.UserSyncMode;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.schemas.builders.CommonMetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.users.SystemWideUserInfos;
import com.constellio.model.services.users.UserServices;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_CannotExcuteTransaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class CoreMigrationTo_9_2 extends MigrationHelper implements MigrationScript {

	private Map<String, Map<String, Object>> credentialMetadataMap = new HashMap<>();

	@Override
	public String getVersion() {
		return "9.2";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory) throws Exception {

		if (collection.equals(Collection.SYSTEM_COLLECTION)) {
			new SchemaSystemAlterationFor_9_2(collection, migrationResourcesProvider, appLayerFactory).migrate();
			//chaque utilisateur migré aura la valeur LOCALLY_CREATED si il existe une entrée dans le fichier de mot de passe, sinon SYNCED
			updateAllUserSyncMode(appLayerFactory.getModelLayerFactory());
		} else {
			new SchemaAlterationFor_9_2(collection, migrationResourcesProvider, appLayerFactory).migrate();
			transferDomainsAndMsDelegatesFromCredentialsToUser(collection, appLayerFactory.getModelLayerFactory());
		}
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
			//Les UserCredential sans collection seront supprimés
			UserServices userServices = appLayerFactory.getModelLayerFactory().newUserServices();
			//userServices.safePhysicalDeleteAllUserCredentialsWithEmptyCollections();
			try {
				safePhysicalDeleteAllUserCredentialsWithEmptyCollections(appLayerFactory.getModelLayerFactory());
			} catch (Exception e) {
				e.printStackTrace();
			}

			//Les User auront 4 statuts : active, pending, suspended, disabled (au lieu de deleted) (nothing to do here)
			//Une métadonnée enum "syncMode" dans Usercredential avec les choix SYNCED, NOT_SYNCED, LOCALLY_CREATED
			MetadataSchemaBuilder userCredentialSchema = typesBuilder.getSchemaType(UserCredential.SCHEMA_TYPE).getDefaultSchema();
			MetadataBuilder userSyncModeBuilder = userCredentialSchema.createUndeletable("syncMode")
					.defineAsEnum(UserSyncMode.class).setDefaultValue(UserSyncMode.LOCALLY_CREATED);

			//Les utilisateurs qui ne sont pas actifs sont supprimés logiquement

			try {
				logicallyRemoveAllNonActiveUsers(modelLayerFactory);
			} catch (Exception e) {
				e.printStackTrace();
			}

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
			logicallyRemoveAllNonActiveGroups(modelLayerFactory);

			MetadataSchemaBuilder glGroupSchema = typesBuilder.getSchemaType(GlobalGroup.SCHEMA_TYPE).getDefaultSchema();

			//NOT READY YET, STOP USING THEM IN USERSERVICES BEFORE
			//			glGroupSchema.deleteMetadataWithoutValidation("usersAutomaticallyAddedToCollections");
			//			glGroupSchema.deleteMetadataWithoutValidation("status");
			//			glGroupSchema.deleteMetadataWithoutValidation("locallyCreated");
			//			glGroupSchema.deleteMetadataWithoutValidation("hierarchy");

			List<GlobalGroup> glGroups = getAllGlobalGroups(modelLayerFactory);

			//GlobalGroup est supprimé.
			try {
				physicallyRemoveGlobalGroup(modelLayerFactory);
			} catch (Exception e) {
				e.printStackTrace();
			}


			//Les métadonnées suivantes seront déplacées dans User par le script de migration : domain, msExchangeDelegateList
			//sauvegarder en map pour l'instant
			transferDomainsAndMsDelegatesFromCredentialsToUser(userServices);

			userCredentialSchema.deleteMetadataWithoutValidation("domain");
			userCredentialSchema.deleteMetadataWithoutValidation("msExchangeDelegateList");
		}

		private void transferDomainsAndMsDelegatesFromCredentialsToUser(UserServices userServices) {
			List<User> users = new ArrayList<>();

			users.addAll(userServices.getAllUsersInCollection(collection));

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
				credentialMetadataMap.put(credential.getUsername(), metadatas);
			}
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
			//Les UserCredential sans collection seront supprimés
			UserServices userServices = appLayerFactory.getModelLayerFactory().newUserServices();

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
			groupSchema.createUndeletable("status")
					.defineAsEnum(GlobalGroupStatus.class)
					.setDefaultRequirement(true).setDefaultValue(GlobalGroupStatus.ACTIVE);
			groupSchema.createUndeletable("locallyCreated")
					.setType(MetadataValueType.BOOLEAN).setDefaultValue(true);
			groupSchema.createUndeletable("hierarchy")
					.setType(MetadataValueType.STRING);

			//Les utilisateurs désactivés qui ne sont pas utilisés sont supprimé physiquement
			userServices.safePhysicalDeleteAllUnusedUsers(collection);
			configureTableMetadatas(collection, appLayerFactory);
		}

	}

	private void transferDomainsAndMsDelegatesFromCredentialsToUser(String collection,
																	ModelLayerFactory modelLayerFactory)
			throws Exception {
		//List<User> users = new ArrayList<>();
		RecordServices recordServices = modelLayerFactory.newRecordServices();
		SearchServices searchServices = modelLayerFactory.newSearchServices();
		SchemasRecordsServices schemas = new SchemasRecordsServices(collection, modelLayerFactory);
		//users.addAll(getAllUsersInCollection(modelLayerFactory, collection));
		MetadataSchema userMetadataSchema = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection).getDefaultSchema(User.SCHEMA_TYPE);
		if (userMetadataSchema.hasMetadataWithCode(User.DOMAIN) && userMetadataSchema.hasMetadataWithCode(User.MS_EXCHANGE_DELEGATE_LIST)) {

			new ActionExecutorInBatch(searchServices, "Removing non active users", 250) {
				@Override
				public void doActionOnBatch(List<Record> records)
						throws Exception {
					for (Record record : records) {
						User user = schemas.wrapUser(record);
						if (credentialMetadataMap.containsKey(user.getUsername())) {
							Map<String, Object> metadatas = credentialMetadataMap.get(user.getUsername());
							user.setDomain((String) metadatas.get("domain"));
							user.setMsExchDelegateListBL((List) metadatas.get("msExchangeDelegateList"));
							try {
								recordServices.update(user);
							} catch (RecordServicesException e) {
								throw new UserServicesRuntimeException_CannotExcuteTransaction(e);
							}
						}
					}
				}
			}.execute(from(schemas.user.schemaType()).returnAll());
		}
	}

	private void logicallyRemoveAllNonActiveUsers(ModelLayerFactory modelLayerFactory) throws Exception {
		RecordServices recordServices = modelLayerFactory.newRecordServices();
		SearchServices searchServices = modelLayerFactory.newSearchServices();
		SchemasRecordsServices systemSchemas = new SchemasRecordsServices(Collection.SYSTEM_COLLECTION, modelLayerFactory);

		new ActionExecutorInBatch(searchServices, "Removing non active users", 250) {
			@Override
			public void doActionOnBatch(List<Record> records)
					throws Exception {
				for (Record record : records) {
					UserCredential userCredential = systemSchemas.wrapUserCredential(record);
					if (userCredential.getStatus() != UserCredentialStatus.ACTIVE) {
						userCredential.setStatus(UserCredentialStatus.DISABLED);
						try {
							recordServices.update(userCredential);
						} catch (RecordServicesException e) {
							throw new UserServicesRuntimeException_CannotExcuteTransaction(e);
						}
					}
				}
			}
		}.execute(from(systemSchemas.credentialSchemaType()).returnAll());

	}

	private void updateAllUserSyncMode(ModelLayerFactory modelLayerFactory) throws Exception {
		RecordServices recordServices = modelLayerFactory.newRecordServices();

		SearchServices searchServices = modelLayerFactory.newSearchServices();
		SchemasRecordsServices systemSchemas = new SchemasRecordsServices(Collection.SYSTEM_COLLECTION, modelLayerFactory);

		new ActionExecutorInBatch(searchServices, "Update sync mode credentials", 250) {
			@Override
			public void doActionOnBatch(List<Record> records)
					throws Exception {
				for (Record record : records) {
					UserCredential credential = systemSchemas.wrapUserCredential(record);
					if (credential.getDn() != null) {
						credential.setSyncMode(UserSyncMode.SYNCED);
					} else {
						credential.setSyncMode(UserSyncMode.LOCALLY_CREATED);
					}

					try {
						recordServices.update(credential);
					} catch (RecordServicesException e) {
						throw new UserServicesRuntimeException_CannotExcuteTransaction(e);
					}
				}
			}
		}.execute(from(systemSchemas.credentialSchemaType()).returnAll());
	}

	private void physicallyRemoveGlobalGroup(ModelLayerFactory modelLayerFactory) throws Exception {
		RecordServices recordServices = modelLayerFactory.newRecordServices();
		SearchServices searchServices = modelLayerFactory.newSearchServices();
		SchemasRecordsServices systemSchemas = new SchemasRecordsServices(Collection.SYSTEM_COLLECTION, modelLayerFactory);

		new ActionExecutorInBatch(searchServices, "Removing global groups", 250) {
			@Override
			public void doActionOnBatch(List<Record> records)
					throws Exception {
				for (Record record : records) {
					GlobalGroup globalGroup = systemSchemas.wrapOldGlobalGroup(record);
					recordServices.logicallyDelete(globalGroup, User.GOD);
					recordServices.physicallyDelete(globalGroup, User.GOD);
				}
			}
		}.execute(from(systemSchemas.globalGroupSchemaType()).returnAll());

	}

	public void logicallyRemoveAllNonActiveGroups(ModelLayerFactory modelLayerFactory) {
		RecordServices recordServices = modelLayerFactory.newRecordServices();
		List<GlobalGroup> groups = getAllGlobalGroups(modelLayerFactory);
		List<GlobalGroup> nonActiveGroups = groups.stream()
				.filter(gr -> gr.getStatus().equals(GlobalGroupStatus.INACTIVE))
				.collect(Collectors.toList());
		for (GlobalGroup group : nonActiveGroups) {
			try {
				recordServices.update(group);
			} catch (RecordServicesException e) {
				throw new UserServicesRuntimeException_CannotExcuteTransaction(e);
			}
		}
	}

	private void safePhysicalDeleteAllUserCredentialsWithEmptyCollections(ModelLayerFactory modelLayerFactory)
			throws Exception {
		RecordServices recordServices = modelLayerFactory.newRecordServices();
		SearchServices searchServices = modelLayerFactory.newSearchServices();
		SchemasRecordsServices systemSchemas = new SchemasRecordsServices(Collection.SYSTEM_COLLECTION, modelLayerFactory);

		//		new ActionExecutorInBatch(searchServices, "Removing user credentials with empty collections", 250) {
		//			@Override
		//			public void doActionOnBatch(List<Record> records)
		//					throws Exception {
		//				for (Record record : records) {
		//					UserCredential userCredential = systemSchemas.wrapUserCredential(record);
		//					if (userCredential.getCollections() == null || userCredential.getCollections().isEmpty()) {
		//						recordServices.logicallyDelete(userCredential, User.GOD);
		//						recordServices.physicallyDelete(userCredential, User.GOD);
		//					}
		//				}
		//			}
		//		}.execute(from(systemSchemas.credentialSchemaType()).returnAll());

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


}
