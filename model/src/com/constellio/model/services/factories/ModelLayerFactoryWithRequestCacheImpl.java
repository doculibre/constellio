package com.constellio.model.services.factories;

import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.dao.services.factories.DataLayerFactory;
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
import com.constellio.model.services.caches.ModelLayerCachesManager;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.configs.UserConfigurationsManager;
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
import com.constellio.model.services.pdftron.AnnotationLockManager;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesImpl;
import com.constellio.model.services.records.cache.CachedRecordServices;
import com.constellio.model.services.records.cache.RecordsCaches;
import com.constellio.model.services.records.cache.cacheIndexHook.impl.RecordUsageCounterHookRetriever;
import com.constellio.model.services.records.cache.cacheIndexHook.impl.TaxonomyRecordsHookRetriever;
import com.constellio.model.services.records.extractions.RecordPopulateServices;
import com.constellio.model.services.records.reindexing.ReindexingServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.FreeTextSearchServices;
import com.constellio.model.services.search.SearchBoostManager;
import com.constellio.model.services.search.SearchConfigurationsManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.SynonymsConfigurationsManager;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.model.services.security.SecurityModelCache;
import com.constellio.model.services.security.SecurityTokenManager;
import com.constellio.model.services.security.authentification.AuthenticationService;
import com.constellio.model.services.security.authentification.LDAPAuthenticationService;
import com.constellio.model.services.security.authentification.PasswordFileAuthenticationService;
import com.constellio.model.services.security.roles.RolesManager;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.model.services.taxonomies.TaxonomiesSearchServices;
import com.constellio.model.services.taxonomies.TaxonomiesSearchServicesCache;
import com.constellio.model.services.thesaurus.ThesaurusManager;
import com.constellio.model.services.trash.TrashQueueManager;
import com.constellio.model.services.users.SolrGlobalGroupsManager;
import com.constellio.model.services.users.SolrUserCredentialsManager;
import com.constellio.model.services.users.UserPhotosServices;
import com.constellio.model.services.users.UserServices;
import com.constellio.model.services.users.sync.LDAPUserSyncManager;

import java.security.Key;

public class ModelLayerFactoryWithRequestCacheImpl implements ModelLayerFactory {

	RecordsCaches requestCache;
	ModelLayerFactoryImpl modelLayerFactory;

	public ModelLayerFactoryWithRequestCacheImpl(ModelLayerFactoryImpl modelLayerFactory, RecordsCaches requestCache) {
		this.modelLayerFactory = modelLayerFactory;
		this.requestCache = requestCache;
	}

	@Override
	public RecordMigrationsManager getRecordMigrationsManager() {
		return modelLayerFactory.getRecordMigrationsManager();
	}

	@Override
	public ModelLayerExtensions getExtensions() {
		return modelLayerFactory.getExtensions();
	}

	@Override
	public RecordServices newRecordServices() {
		return new CachedRecordServices(this, newCachelessRecordServices(requestCache), requestCache);
	}

	@Override
	public RecordServicesImpl newCachelessRecordServices() {
		return modelLayerFactory.newCachelessRecordServices();
	}

	@Override
	public RecordServicesImpl newCachelessRecordServices(RecordsCaches recordsCaches) {
		return modelLayerFactory.newCachelessRecordServices(recordsCaches);
	}

	@Override
	public SearchServices newSearchServices() {
		return new SearchServices(this, requestCache);
	}

	@Override
	public FreeTextSearchServices newFreeTextSearchServices() {
		return modelLayerFactory.newFreeTextSearchServices();
	}

	@Override
	public FileParser newFileParser() {
		return modelLayerFactory.newFileParser();
	}

	@Override
	public MetadataSchemasManager getMetadataSchemasManager() {
		return modelLayerFactory.getMetadataSchemasManager();
	}

	@Override
	public BatchProcessesManager getBatchProcessesManager() {
		return modelLayerFactory.getBatchProcessesManager();
	}

	@Override
	public FoldersLocator getFoldersLocator() {
		return modelLayerFactory.getFoldersLocator();
	}

	@Override
	public ContentManager getContentManager() {
		return modelLayerFactory.getContentManager();
	}

	@Override
	public BatchProcessController getBatchProcessesController() {
		return modelLayerFactory.getBatchProcessesController();
	}

	@Override
	public TaxonomiesManager getTaxonomiesManager() {
		return modelLayerFactory.getTaxonomiesManager();
	}

	@Override
	public TaxonomiesSearchServices newTaxonomiesSearchService() {
		return modelLayerFactory.newTaxonomiesSearchService();
	}

	@Override
	public RolesManager getRolesManager() {
		return modelLayerFactory.getRolesManager();
	}

	@Override
	public AnnotationLockManager getAnnotationLockManager() {
		return modelLayerFactory.getAnnotationLockManager();
	}

	@Override
	public AuthorizationsServices newAuthorizationsServices() {
		return new AuthorizationsServices(this);
	}

	@Override
	public AuthenticationService newAuthenticationService() {
		return modelLayerFactory.newAuthenticationService();
	}

	@Override
	public CollectionsListManager getCollectionsListManager() {
		return modelLayerFactory.getCollectionsListManager();
	}

	public SolrUserCredentialsManager getUserCredentialsManager() {
		return new SolrUserCredentialsManager(this);
	}

	@Override
	public StoredBatchProcessProgressionServices getStoredBatchProcessProgressionServices() {
		return modelLayerFactory.getStoredBatchProcessProgressionServices();
	}

	@Override
	public SolrGlobalGroupsManager getGlobalGroupsManager() {
		return modelLayerFactory.getGlobalGroupsManager();
	}

	@Override
	public UserServices newUserServices() {
		return new UserServices(this);
	}

	@Override
	public LanguageDetectionManager getLanguageDetectionManager() {
		return modelLayerFactory.getLanguageDetectionManager();
	}

	@Override
	public SystemConfigurationsManager getSystemConfigurationsManager() {
		return modelLayerFactory.getSystemConfigurationsManager();
	}

	@Override
	public ConstellioEIMConfigs getSystemConfigs() {
		return modelLayerFactory.getSystemConfigs();
	}

	@Override
	public UserConfigurationsManager getUserConfigurationsManager() {
		return modelLayerFactory.getUserConfigurationsManager();
	}

	@Override
	public LoggingServices newLoggingServices() {
		return modelLayerFactory.newLoggingServices();
	}

	@Override
	public IOServicesFactory getIOServicesFactory() {
		return modelLayerFactory.getIOServicesFactory();
	}

	@Override
	public ModelLayerConfiguration getConfiguration() {
		return modelLayerFactory.getConfiguration();
	}

	@Override
	public UserPhotosServices newUserPhotosServices() {
		return modelLayerFactory.newUserPhotosServices();
	}

	@Override
	public ReindexingServices newReindexingServices() {
		return modelLayerFactory.newReindexingServices();
	}

	@Override
	public DataLayerFactory getDataLayerFactory() {
		return modelLayerFactory.getDataLayerFactory();
	}

	@Override
	public LDAPConfigurationManager getLdapConfigurationManager() {
		return modelLayerFactory.getLdapConfigurationManager();
	}

	@Override
	public LDAPAuthenticationService getLdapAuthenticationService() {
		return modelLayerFactory.getLdapAuthenticationService();
	}

	@Override
	public PasswordFileAuthenticationService getPasswordFileAuthenticationService() {
		return modelLayerFactory.getPasswordFileAuthenticationService();
	}

	@Override
	public LDAPUserSyncManager getLdapUserSyncManager() {
		return modelLayerFactory.getLdapUserSyncManager();
	}

	@Override
	public EmailConfigurationsManager getEmailConfigurationsManager() {
		return modelLayerFactory.getEmailConfigurationsManager();
	}

	@Override
	public EmailTemplatesManager getEmailTemplatesManager() {
		return modelLayerFactory.getEmailTemplatesManager();
	}

	@Override
	public EmailQueueManager getEmailQueueManager() {
		return modelLayerFactory.getEmailQueueManager();
	}

	@Override
	public RecordsCaches getRecordsCaches() {
		return modelLayerFactory.getRecordsCaches();
	}

	@Override
	public SecurityTokenManager getSecurityTokenManager() {
		return modelLayerFactory.getSecurityTokenManager();
	}

	@Override
	public ModelLayerLogger getModelLayerLogger() {
		return modelLayerFactory.getModelLayerLogger();
	}

	@Override
	public RecordPopulateServices newRecordPopulateServices() {
		return modelLayerFactory.newRecordPopulateServices();
	}

	@Override
	public Factory<ModelLayerFactory> getModelLayerFactoryFactory() {
		return modelLayerFactory.getModelLayerFactoryFactory();
	}

	@Override
	public void setEncryptionKey(Key key) {
		modelLayerFactory.setEncryptionKey(key);
	}

	@Override
	public EncryptionServices newEncryptionServices() {
		return modelLayerFactory.newEncryptionServices();
	}

	@Override
	public SearchBoostManager getSearchBoostManager() {
		return modelLayerFactory.getSearchBoostManager();
	}

	@Override
	public void setAuthenticationService(
			AuthenticationService authenticationService) {
		modelLayerFactory.setAuthenticationService(authenticationService);
	}

	@Override
	public TrashQueueManager getTrashQueueManager() {
		return modelLayerFactory.getTrashQueueManager();
	}

	@Override
	public ModelLayerBackgroundThreadsManager getModelLayerBackgroundThreadsManager() {
		return modelLayerFactory.getModelLayerBackgroundThreadsManager();
	}

	@Override
	public TaxonomiesSearchServicesCache getTaxonomiesSearchServicesCache() {
		return modelLayerFactory.getTaxonomiesSearchServicesCache();
	}

	@Override
	public ThesaurusManager getThesaurusManager() {
		return modelLayerFactory.getThesaurusManager();
	}

	@Override
	public SecurityModelCache getSecurityModelCache() {
		return modelLayerFactory.getSecurityModelCache();
	}

	@Override
	public ModelLayerCachesManager getCachesManager() {
		return modelLayerFactory.getCachesManager();
	}

	@Override
	public void postInitialization(ModelPostInitializationParams params) {
		modelLayerFactory.postInitialization(params);
	}

	@Override
	public void onCollectionInitialized(String collection) {
		this.modelLayerFactory.onCollectionInitialized(collection);
	}

	@Override
	public TaxonomyRecordsHookRetriever getTaxonomyRecordsHookRetriever(String collection) {
		return this.modelLayerFactory.getTaxonomyRecordsHookRetriever(collection);
	}

	@Override
	public RecordUsageCounterHookRetriever getRecordUsageCounterHookRetriever(String collection) {
		return this.modelLayerFactory.getRecordUsageCounterHookRetriever(collection);
	}

	@Override
	public void markForReindexing() {
		modelLayerFactory.markForReindexing();
	}

	@Override
	public void markLocalCachesAsRequiringRebuild() {
		modelLayerFactory.markLocalCachesAsRequiringRebuild();
	}

	@Override
	public SearchConfigurationsManager getSearchConfigurationsManager() {
		return modelLayerFactory.getSearchConfigurationsManager();
	}

	@Override
	public SynonymsConfigurationsManager getSynonymsConfigurationsManager() {
		return modelLayerFactory.getSynonymsConfigurationsManager();
	}

	@Override
	public short getInstanceId() {
		return modelLayerFactory.getInstanceId();
	}

	@Override
	public String getInstanceName() {
		return modelLayerFactory.getInstanceName();
	}

	@Override
	public <T extends StatefulService> T add(T statefulService) {
		return modelLayerFactory.add(statefulService);
	}

	@Override
	public void initialize() {
		modelLayerFactory.initialize();
	}

	@Override
	public void close() {
		modelLayerFactory.close();
	}

	@Override
	public void close(boolean closeBottomLayers) {
		modelLayerFactory.close(closeBottomLayers);
	}

	@Override
	public String toResourceName(String name) {
		return modelLayerFactory.toResourceName(name);
	}
}
