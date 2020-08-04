package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.services.bigVault.SearchResponseIterator;
import com.constellio.data.services.tenant.TenantLocal;
import com.constellio.model.conf.email.BaseEmailServerConfiguration;
import com.constellio.model.conf.email.EmailConfigurationsManager;
import com.constellio.model.conf.email.EmailServerConfiguration;
import com.constellio.model.conf.ldap.LDAPConfigurationManager;
import com.constellio.model.conf.ldap.config.LDAPUserSyncConfiguration;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Record.GetMetadataOption;
import com.constellio.model.entities.records.Record.SetMetadataOption;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.records.wrappers.ExternalAccessUrl;
import com.constellio.model.entities.records.wrappers.structure.ExternalAccessUrlStatus;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.encrypt.EncryptionKeyFactory;
import com.constellio.model.services.encrypt.EncryptionServices;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;

import java.security.Key;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CoreMigrationTo_9_2 extends MigrationHelper implements MigrationScript {

	private static TenantLocal<Key> OLD_KEY = new TenantLocal<>();

	@Override
	public String getVersion() {
		return "9.2";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {
		new SchemaAlterationFor_9_2(collection, migrationResourcesProvider, appLayerFactory).migrate();

		if (collection.equals(Collection.SYSTEM_COLLECTION)) {
			new EncryptionSystemCollectionMigration_9_2(collection, appLayerFactory).doMigration();
		}

		new EncryptionMigration_9_2(collection, appLayerFactory).doMigration();
	}

	class SchemaAlterationFor_9_2 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationFor_9_2(String collection, MigrationResourcesProvider migrationResourcesProvider,
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

	class EncryptionSystemCollectionMigration_9_2 {
		private String collection;
		private AppLayerFactory appLayerFactory;
		private ModelLayerFactory modelLayerFactory;

		protected EncryptionSystemCollectionMigration_9_2(String collection,
														  AppLayerFactory appLayerFactory) {
			this.collection = collection;
			this.appLayerFactory = appLayerFactory;
			this.modelLayerFactory = appLayerFactory.getModelLayerFactory();
		}

		protected void doMigration() {
			Key encryptionKey = EncryptionKeyFactory.getApplicationKey(appLayerFactory.getModelLayerFactory());
			OLD_KEY.set(encryptionKey);

			//			modelLayerFactory.getDataLayerFactory().saveEncryptionKey();
			modelLayerFactory.resetEncryptionServices();

			try {
				EncryptionServices oldEncryptionServices = new EncryptionServices(modelLayerFactory.getConfiguration().isPreviousPrivateKeyLost()).withKeyAndIV(OLD_KEY.get());

				LDAPConfigurationManager ldapConfigurationManager = modelLayerFactory.getLdapConfigurationManager();

				LDAPUserSyncConfiguration ldapUserSyncConfiguration = ldapConfigurationManager.getLDAPUserSyncConfiguration(false);

				String password = ldapUserSyncConfiguration.getPassword();


				if (password != null) {
					password = oldEncryptionServices.decryptWithOldWayAppKey(password);

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

	class EncryptionMigration_9_2 {
		private String collection;
		private AppLayerFactory appLayerFactory;
		private ModelLayerFactory modelLayerFactory;

		protected EncryptionMigration_9_2(String collection,
										  AppLayerFactory appLayerFactory) {
			this.collection = collection;
			this.appLayerFactory = appLayerFactory;
			this.modelLayerFactory = appLayerFactory.getModelLayerFactory();
		}

		protected void doMigration() {
			EmailConfigurationsManager emailConfigurationsManager = modelLayerFactory.getEmailConfigurationsManager();
			EmailServerConfiguration emailServerConfiguration = emailConfigurationsManager.getEmailConfiguration(this.collection, false);


			try {
				EncryptionServices oldEncryptionServices = new EncryptionServices(modelLayerFactory.getConfiguration().isPreviousPrivateKeyLost()).withKeyAndIV(OLD_KEY.get());
				EncryptionServices newEncryptionServices = modelLayerFactory.newEncryptionServices();

				if (emailServerConfiguration != null) {
					String password = emailServerConfiguration.getPassword();

					String decriptedPassword = oldEncryptionServices.decryptWithOldWayAppKey(password);

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

						LogicalSearchQuery logicalSearchQuery = new LogicalSearchQuery(LogicalSearchQueryOperators.from(metadataSchema).whereAll(metadataToReEncrypt).isNotNull());

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
									Object decriptedData = oldEncryptionServices.decryptWithOldWayAppKey(data);
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
