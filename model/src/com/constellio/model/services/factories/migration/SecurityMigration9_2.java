package com.constellio.model.services.factories.migration;

import com.constellio.data.dao.services.bigVault.SearchResponseIterator;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.conf.email.BaseEmailServerConfiguration;
import com.constellio.model.conf.email.EmailConfigurationsManager;
import com.constellio.model.conf.email.EmailServerConfiguration;
import com.constellio.model.conf.ldap.LDAPConfigurationManager;
import com.constellio.model.conf.ldap.config.LDAPUserSyncConfiguration;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Record.GetMetadataOption;
import com.constellio.model.entities.records.Record.SetMetadataOption;
import com.constellio.model.entities.records.RecordUpdateOptions;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.encrypt.EncryptionKeyFactory;
import com.constellio.model.services.encrypt.EncryptionServices;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;

import java.security.Key;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SecurityMigration9_2 {
	private ModelLayerFactory modelLayerFactory;
	private static final String CRYPTED_PREFIX = "crypted:";

	public SecurityMigration9_2(ModelLayerFactory modelLayerFactory) {
		this.modelLayerFactory = modelLayerFactory;
	}

	public void migrateAllCollections() {
		if (!Toggle.LOST_PRIVATE_KEY.isEnabled()) {
			List<String> collectionList = this.modelLayerFactory.getCollectionsListManager().getCollections();

			Key encryptionKey = EncryptionKeyFactory.getApplicationKey(modelLayerFactory);

			if (collectionList.contains(Collection.SYSTEM_COLLECTION)) {
				collectionList = new ArrayList<>(collectionList);
				collectionList.remove(Collection.SYSTEM_COLLECTION);
				collectionList.add(0, Collection.SYSTEM_COLLECTION);
			}

			systemCollectionMigration(encryptionKey);
			for (String collection : collectionList) {
				doAnyCollectionMigration(collection, encryptionKey);
			}

			modelLayerFactory.resetEncryptionServices();
		}
	}

	public void systemCollectionMigration(Key oldKey) {

		try {
			EncryptionServices oldEncryptionServices = new EncryptionServices(false);
			oldEncryptionServices.withKeyAndIV(oldKey);

			LDAPConfigurationManager ldapConfigurationManager = modelLayerFactory.getLdapConfigurationManager();

			LDAPUserSyncConfiguration ldapUserSyncConfiguration = ldapConfigurationManager.getLDAPUserSyncConfiguration(false);

			String password = ldapUserSyncConfiguration.getPassword();


			if (password != null && !password.equals("")) {
				password = oldEncryptionServices.decryptVersion1(password);

				LDAPUserSyncConfiguration newLdapUserSyncConfiguration = new LDAPUserSyncConfiguration(ldapUserSyncConfiguration.getUser(), password, ldapUserSyncConfiguration.getUserFilter(), ldapUserSyncConfiguration.getGroupFilter(), ldapUserSyncConfiguration.getDurationBetweenExecution(),
						ldapUserSyncConfiguration.getScheduleTime(), ldapUserSyncConfiguration.getGroupBaseContextList(),
						ldapUserSyncConfiguration.getUsersWithoutGroupsBaseContextList(), ldapUserSyncConfiguration.getUserFilterGroupsList(),
						ldapUserSyncConfiguration.isMembershipAutomaticDerivationActivated(), ldapUserSyncConfiguration.getSelectedCollectionsCodes(),
						ldapUserSyncConfiguration.isFetchSubGroups(), ldapUserSyncConfiguration.isIgnoreRegexForSubGroups(), ldapUserSyncConfiguration.isSyncUsersOnlyIfInAcceptedGroups());

				ldapConfigurationManager.saveLDAPConfiguration(ldapConfigurationManager.getLDAPServerConfiguration(), newLdapUserSyncConfiguration, false);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected void doAnyCollectionMigration(String collection, Key oldKey) {
		EmailConfigurationsManager emailConfigurationsManager = modelLayerFactory.getEmailConfigurationsManager();
		EmailServerConfiguration emailServerConfiguration = emailConfigurationsManager.getEmailConfiguration(collection, false);

		try {
			boolean lostPrivateKey = false;
			EncryptionServices oldEncryptionServices = new EncryptionServices(lostPrivateKey);
			oldEncryptionServices.withKeyAndIV(oldKey);
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
								Object decryptedData = isDataFakeEncrypted(data) ? parseFakeEncryptedContent(data) : oldEncryptionServices.decryptVersion1(data);
								currentRecord.set(currentMetadata, SetMetadataOption.NO_DECRYPTION, decryptedData);
							}

							if (hasModifications) {
								executeTransaction = true;
								transaction.update(currentRecord);
							}
						}

						if (executeTransaction) {
							transaction.setOptions(new RecordUpdateOptions().setValidationsEnabled(false));
							recordServices.execute(transaction);
						}
					}


				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	private boolean isDataFakeEncrypted(Object data) {
		if (data instanceof String) {
			if (((String) data).startsWith(CRYPTED_PREFIX)) {
				return true;
			}
		}
		return false;
	}

	private Object parseFakeEncryptedContent(Object data) {
		if (isDataFakeEncrypted(data)) {
			return ((String) data).substring(CRYPTED_PREFIX.length());
		}
		return data;
	}
}
