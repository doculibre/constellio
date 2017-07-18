package com.constellio.model.services.factories;

import java.security.Key;
import java.util.List;

import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.dao.services.factories.LayerFactory;
import com.constellio.data.io.IOServicesFactory;
import com.constellio.data.utils.Factory;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.conf.ModelLayerConfiguration;
import com.constellio.model.conf.email.EmailConfigurationsManager;
import com.constellio.model.conf.ldap.LDAPConfigurationManager;
import com.constellio.model.services.background.ModelLayerBackgroundThreadsManager;
import com.constellio.model.services.batch.controller.BatchProcessController;
import com.constellio.model.services.batch.manager.BatchProcessesManager;
import com.constellio.model.services.batch.state.StoredBatchProcessProgressionServices;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.emails.EmailQueueManager;
import com.constellio.model.services.emails.EmailTemplatesManager;
import com.constellio.model.services.encrypt.EncryptionServices;
import com.constellio.model.services.extensions.ModelLayerExtensions;
import com.constellio.model.services.logging.LoggingServices;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.migrations.RecordMigrationsManager;
import com.constellio.model.services.parser.FileParser;
import com.constellio.model.services.parser.LanguageDetectionManager;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesImpl;
import com.constellio.model.services.records.cache.RecordsCaches;
import com.constellio.model.services.records.extractions.RecordPopulateServices;
import com.constellio.model.services.records.reindexing.ReindexingServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.FreeTextSearchServices;
import com.constellio.model.services.search.SearchBoostManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.security.AuthorizationDetailsManager;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.model.services.security.SecurityTokenManager;
import com.constellio.model.services.security.authentification.AuthenticationService;
import com.constellio.model.services.security.authentification.LDAPAuthenticationService;
import com.constellio.model.services.security.authentification.PasswordFileAuthenticationService;
import com.constellio.model.services.security.roles.RolesManager;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.model.services.taxonomies.TaxonomiesSearchServices;
import com.constellio.model.services.trash.TrashQueueManager;
import com.constellio.model.services.users.GlobalGroupsManager;
import com.constellio.model.services.users.UserCredentialsManager;
import com.constellio.model.services.users.UserPhotosServices;
import com.constellio.model.services.users.UserServices;
import com.constellio.model.services.users.sync.LDAPUserSyncManager;

public interface ModelLayerFactory extends LayerFactory {

	RecordMigrationsManager getRecordMigrationsManager();

	List<SystemCollectionListener> getSystemCollectionListeners();

	void addSystemCollectionListener(SystemCollectionListener listener);

	ModelLayerExtensions getExtensions();

	RecordServices newRecordServices();

	RecordServicesImpl newCachelessRecordServices();

	RecordServicesImpl newCachelessRecordServices(RecordsCaches recordsCaches);

	SearchServices newSearchServices();

	FreeTextSearchServices newFreeTextSearchServices();

	FileParser newFileParser();

	MetadataSchemasManager getMetadataSchemasManager();

	BatchProcessesManager getBatchProcessesManager();

	BatchProcessesManager newBatchProcessesManager();

	FoldersLocator getFoldersLocator();

	ContentManager getContentManager();

	BatchProcessController getBatchProcessesController();

	TaxonomiesManager getTaxonomiesManager();

	TaxonomiesSearchServices newTaxonomiesSearchService();

	RolesManager getRolesManager();

	AuthorizationDetailsManager getAuthorizationDetailsManager();

	AuthorizationsServices newAuthorizationsServices();

	//After rename get...
	AuthenticationService newAuthenticationService();

	CollectionsListManager getCollectionsListManager();

	UserCredentialsManager getUserCredentialsManager();

	StoredBatchProcessProgressionServices getStoredBatchProcessProgressionServices();

	GlobalGroupsManager getGlobalGroupsManager();

	UserServices newUserServices();

	LanguageDetectionManager getLanguageDetectionManager();

	SystemConfigurationsManager getSystemConfigurationsManager();

	ConstellioEIMConfigs getSystemConfigs();

	LoggingServices newLoggingServices();

	IOServicesFactory getIOServicesFactory();

	ModelLayerConfiguration getConfiguration();

	UserPhotosServices newUserPhotosServices();

	ReindexingServices newReindexingServices();

	DataLayerFactory getDataLayerFactory();

	LDAPConfigurationManager getLdapConfigurationManager();

	LDAPAuthenticationService getLdapAuthenticationService();

	PasswordFileAuthenticationService getPasswordFileAuthenticationService();

	LDAPUserSyncManager getLdapUserSyncManager();

	EmailConfigurationsManager getEmailConfigurationsManager();

	EmailTemplatesManager getEmailTemplatesManager();

	EmailQueueManager getEmailQueueManager();

	RecordsCaches getRecordsCaches();

	SecurityTokenManager getSecurityTokenManager();

	ModelLayerLogger getModelLayerLogger();

	RecordPopulateServices newRecordPopulateServices();

	Factory<ModelLayerFactory> getModelLayerFactoryFactory();

	void setEncryptionKey(Key key);

	EncryptionServices newEncryptionServices();

	SearchBoostManager getSearchBoostManager();

	void setAuthenticationService(AuthenticationService authenticationService);

	TrashQueueManager getTrashQueueManager();

	ModelLayerBackgroundThreadsManager getModelLayerBackgroundThreadsManager();
}
