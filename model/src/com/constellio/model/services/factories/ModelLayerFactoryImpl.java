package com.constellio.model.services.factories;

import com.constellio.data.conf.FoldersLocator;
import com.constellio.data.dao.dto.records.StringRecordId;
import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.dao.managers.StatefullServiceDecorator;
import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.services.DataStoreTypesFactory;
import com.constellio.data.dao.services.cache.ConstellioCacheManager;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.dao.services.factories.LayerFactoryImpl;
import com.constellio.data.dao.services.records.RecordDao;
import com.constellio.data.extensions.ModelReplicationFactorManagerExtension;
import com.constellio.data.io.IOServicesFactory;
import com.constellio.data.utils.Delayed;
import com.constellio.data.utils.Factory;
import com.constellio.model.conf.ModelLayerConfiguration;
import com.constellio.model.conf.email.EmailConfigurationsManager;
import com.constellio.model.conf.ldap.LDAPConfigurationManager;
import com.constellio.model.entities.records.RecordEventDataSerializerExtension;
import com.constellio.model.entities.records.wrappers.Collection;
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
import com.constellio.model.services.emails.EmailServices;
import com.constellio.model.services.emails.EmailTemplatesManager;
import com.constellio.model.services.encrypt.EncryptionKeyFactory;
import com.constellio.model.services.encrypt.EncryptionServices;
import com.constellio.model.services.extensions.ConstellioModulesManager;
import com.constellio.model.services.extensions.ModelLayerExtensions;
import com.constellio.model.services.factories.migration.SecurityMigrationService;
import com.constellio.model.services.logging.LoggingServices;
import com.constellio.model.services.logs.LogServices;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.migrations.RecordMigrationsManager;
import com.constellio.model.services.parser.FileParser;
import com.constellio.model.services.parser.ForkParsers;
import com.constellio.model.services.parser.LanguageDetectionManager;
import com.constellio.model.services.pdf.pdtron.AnnotationLockManager;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesImpl;
import com.constellio.model.services.records.StringRecordIdLegacyMemoryMapping;
import com.constellio.model.services.records.StringRecordIdLegacyPersistedMapping;
import com.constellio.model.services.records.cache.CachedRecordServices;
import com.constellio.model.services.records.cache.RecordsCaches;
import com.constellio.model.services.records.cache.cacheIndexHook.impl.RecordUsageCounterHook;
import com.constellio.model.services.records.cache.cacheIndexHook.impl.RecordUsageCounterHookRetriever;
import com.constellio.model.services.records.cache.cacheIndexHook.impl.TaxonomyRecordsHook;
import com.constellio.model.services.records.cache.cacheIndexHook.impl.TaxonomyRecordsHookRetriever;
import com.constellio.model.services.records.cache.cacheIndexHook.impl.UserCredentialServiceKeyCacheHook;
import com.constellio.model.services.records.cache.cacheIndexHook.impl.UserCredentialServiceKeyCacheHookRetriever;
import com.constellio.model.services.records.cache.cacheIndexHook.impl.UserCredentialTokenCacheHook;
import com.constellio.model.services.records.cache.cacheIndexHook.impl.UserCredentialTokenCacheHookRetriever;
import com.constellio.model.services.records.cache.dataStore.FileSystemRecordsValuesCacheDataStore;
import com.constellio.model.services.records.cache.dataStore.RecordsCachesDataStore;
import com.constellio.model.services.records.cache.eventBus.EventsBusRecordsCachesImpl;
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
import com.constellio.model.services.security.authentification.CombinedAuthenticationService;
import com.constellio.model.services.security.authentification.LDAPAuthenticationService;
import com.constellio.model.services.security.authentification.PasswordFileAuthenticationService;
import com.constellio.model.services.security.roles.RolesManager;
import com.constellio.model.services.taxonomies.EventBusTaxonomiesSearchServicesCache;
import com.constellio.model.services.taxonomies.MemoryTaxonomiesSearchServicesCache;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.model.services.taxonomies.TaxonomiesSearchServices;
import com.constellio.model.services.taxonomies.TaxonomiesSearchServicesCache;
import com.constellio.model.services.thesaurus.ThesaurusManager;
import com.constellio.model.services.trash.TrashQueueManager;
import com.constellio.model.services.users.UserPhotosServices;
import com.constellio.model.services.users.UserServices;
import com.constellio.model.services.users.sync.LDAPUserSyncManager;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.Map;

import static com.constellio.data.conf.HashingEncoding.BASE64;

public class ModelLayerFactoryImpl extends LayerFactoryImpl implements ModelLayerFactory {
	private static final Logger LOGGER = LogManager.getLogger(ModelLayerFactoryImpl.class);

	private final DataLayerFactory dataLayerFactory;
	private final IOServicesFactory ioServicesFactory;
	private final FoldersLocator foldersLocator;
	private final ForkParsers forkParsers;
	private final MetadataSchemasManager schemasManager;
	private final BatchProcessController batchProcessesController;
	private final BatchProcessesManager batchProcessesManager;
	private final TaxonomiesManager taxonomiesManager;
	private final RolesManager rolesManager;
	private final EmailConfigurationsManager emailConfigurationsManager;
	private final CollectionsListManager collectionsListManager;
	private final SystemConfigurationsManager systemConfigurationsManager;
	private final UserConfigurationsManager userConfigurationsManager;
	private final LanguageDetectionManager languageDetectionManager;
	private final ModelLayerConfiguration modelLayerConfiguration;
	private final ContentManager contentsManager;
	private AuthenticationService authenticationManager;
	private final EmailTemplatesManager emailTemplatesManager;
	private final ModelLayerExtensions modelLayerExtensions;
	private final LDAPConfigurationManager ldapConfigurationManager;
	private final LDAPAuthenticationService ldapAuthenticationService;
	private final LDAPUserSyncManager ldapUserSyncManager;
	private final PasswordFileAuthenticationService passwordFileAuthenticationService;
	private final EmailQueueManager emailQueueManager;
	private final TrashQueueManager trashQueueManager;
	private final RecordsCaches recordsCaches;
	private final SecurityTokenManager securityTokenManager;
	protected Key applicationEncryptionKey;
	private final StoredBatchProcessProgressionServices storedBatchProcessProgressionServices;
	private final SearchBoostManager searchBoostManager;
	private final ModelLayerLogger modelLayerLogger;
	private EncryptionServices encryptionServices;
	private final Factory<ModelLayerFactory> modelLayerFactoryFactory;
	private final SecurityModelCache securityModelCache;

	private final ModelLayerBackgroundThreadsManager modelLayerBackgroundThreadsManager;
	private final RecordMigrationsManager recordMigrationsManager;
	private final SearchConfigurationsManager searchConfigurationsManager;
	private final SynonymsConfigurationsManager synonymsConfigurationsManager;
	private final AnnotationLockManager annotationLockManager;

	private ThesaurusManager thesaurusManager;

	private final TaxonomiesSearchServicesCache taxonomiesSearchServicesCache;
	private final Map<String, TaxonomyRecordsHookRetriever> taxonomyRecordsHookRetrieverMap = new HashMap<>();
	private final Map<String, RecordUsageCounterHookRetriever> recordUsageCounterHookRetrieverMap = new HashMap<>();
	UserCredentialTokenCacheHookRetriever userCredentialTokenCacheHookRetriever;
	UserCredentialServiceKeyCacheHookRetriever userCredentialServiceKeyCacheHookRetriever;

	private final ModelLayerCachesManager modelLayerCachesManager;

	private final Runnable markForReindexingRunnable, markForCacheRebuild;

	private SearchServices searchServices;
	private RecordServices recordServices;
	private RecordServicesImpl cachelessRecordServices;

	public ModelLayerFactoryImpl(DataLayerFactory dataLayerFactory, FoldersLocator foldersLocator,
								 ModelLayerConfiguration modelLayerConfiguration,
								 StatefullServiceDecorator statefullServiceDecorator,
								 Delayed<ConstellioModulesManager> modulesManagerDelayed, String instanceName,
								 short instanceId,
								 Factory<ModelLayerFactory> modelLayerFactoryFactory,
								 Runnable markForReindexingRunnable, Runnable markForCacheRebuild) {

		super(dataLayerFactory, statefullServiceDecorator, instanceName, instanceId);

		this.markForReindexingRunnable = markForReindexingRunnable;
		this.markForCacheRebuild = markForCacheRebuild;
		dataLayerFactory.getEventBusManager().getEventDataSerializer().register(new RecordEventDataSerializerExtension(this));

		this.modelLayerCachesManager = new ModelLayerCachesManager();
		this.dataLayerFactory = dataLayerFactory;
		this.modelLayerConfiguration = modelLayerConfiguration;

		add(new StatefulService() {
			@Override
			public void initialize() {
				beforeInitialize();
			}

			@Override
			public void close() {
				afterClose();
			}
		});


		this.modelLayerFactoryFactory = modelLayerFactoryFactory;
		this.modelLayerLogger = new ModelLayerLogger();
		this.modelLayerExtensions = new ModelLayerExtensions();

		this.collectionsListManager = add(new CollectionsListManager(this));

		this.annotationLockManager = new AnnotationLockManager(dataLayerFactory.getConfigManager());

		this.foldersLocator = foldersLocator;

		this.securityModelCache = new SecurityModelCache(this);

		ConfigManager configManager = dataLayerFactory.getConfigManager();
		ConstellioCacheManager cacheManager = dataLayerFactory.getLocalCacheManager();
		this.searchConfigurationsManager = add(new SearchConfigurationsManager(configManager, collectionsListManager, cacheManager));
		this.securityTokenManager = add(new SecurityTokenManager(this));
		this.systemConfigurationsManager = add(
				new SystemConfigurationsManager(this, configManager, modulesManagerDelayed,
						dataLayerFactory.getDistributedCacheManager()));
		this.userConfigurationsManager = add(new UserConfigurationsManager(configManager));
		this.ioServicesFactory = dataLayerFactory.getIOServicesFactory();

		this.forkParsers = add(new ForkParsers(modelLayerConfiguration.getForkParsersPoolSize()));


		this.batchProcessesManager = add(new BatchProcessesManager(this));

		this.taxonomiesManager = add(
				new TaxonomiesManager(configManager, newSearchServices(), batchProcessesManager, collectionsListManager,
						cacheManager, getSystemConfigs()));

		this.schemasManager = add(new MetadataSchemasManager(this, modulesManagerDelayed));

		File workFolder = new FoldersLocator().getWorkFolder();
		workFolder.mkdirs();

		File localCacheConfigs = new File(new FoldersLocator().getWorkFolder(), (instanceName == null ? "constellio" : instanceName) + "-configs.json");
		File fileSystemCacheFolder = new File(new FoldersLocator().getWorkFolder(), (instanceName == null ? "constellio" : instanceName) + "-fsCache.db");
		File memoryCacheCopyCacheFolder = new File(new FoldersLocator().getWorkFolder(), (instanceName == null ? "constellio" : instanceName) + "-memoryCacheCopy.db");
		//FileUtils.deleteQuietly(fileSystemCacheFolder);
		FileSystemRecordsValuesCacheDataStore fileSystemRecordsValuesCacheDataStore
				= new FileSystemRecordsValuesCacheDataStore(fileSystemCacheFolder, memoryCacheCopyCacheFolder, localCacheConfigs);

		RecordsCachesDataStore memoryDataStore = new RecordsCachesDataStore(this);

		this.emailConfigurationsManager = add(
				new EmailConfigurationsManager(configManager, collectionsListManager, this, cacheManager));

		this.recordMigrationsManager = add(new RecordMigrationsManager(this));

		this.rolesManager = add(new RolesManager(this));

		add(new StatefulService() {
			@Override
			public void initialize() {
				new SecurityMigrationService(ModelLayerFactoryImpl.this).migrateIfRequired();
				configureRecordsCacheHooks();
			}

			@Override
			public void close() {

			}
		});

		this.recordsCaches = add(new EventsBusRecordsCachesImpl(this, fileSystemRecordsValuesCacheDataStore, memoryDataStore));

		this.batchProcessesController = add(
				new BatchProcessController(this, modelLayerConfiguration.getNumberOfRecordsPerTask()));


		languageDetectionManager = add(new LanguageDetectionManager(getFoldersLocator().getLanguageProfiles()));

		this.contentsManager = add(new ContentManager(this));

		this.ldapConfigurationManager = add(new LDAPConfigurationManager(this, configManager));
		this.ldapUserSyncManager = add(
				new LDAPUserSyncManager(this));
		ldapAuthenticationService = add(
				new LDAPAuthenticationService(ldapConfigurationManager, configManager,
						ioServicesFactory.newHashingService(BASE64), newUserServices()));
		passwordFileAuthenticationService = new PasswordFileAuthenticationService(configManager,
				ioServicesFactory.newHashingService(BASE64));
		this.authenticationManager = new CombinedAuthenticationService(ldapConfigurationManager, ldapAuthenticationService,
				passwordFileAuthenticationService, modelLayerExtensions);


		this.emailTemplatesManager = add(
				new EmailTemplatesManager(configManager, collectionsListManager, ioServicesFactory.newIOServices()));
		this.emailQueueManager = add(new EmailQueueManager(this, new EmailServices()));
		this.storedBatchProcessProgressionServices = add(new StoredBatchProcessProgressionServices(configManager));
		this.searchBoostManager = add(
				new SearchBoostManager(configManager, collectionsListManager, cacheManager));
		this.trashQueueManager = add(new TrashQueueManager(this));

		this.modelLayerBackgroundThreadsManager = add(new ModelLayerBackgroundThreadsManager(this));

		taxonomiesSearchServicesCache = new EventBusTaxonomiesSearchServicesCache(new MemoryTaxonomiesSearchServicesCache(),
				dataLayerFactory.getEventBusManager());

		this.synonymsConfigurationsManager = add(new SynonymsConfigurationsManager(configManager, collectionsListManager, cacheManager));

		dataLayerFactory.getExtensions().getSystemWideExtensions().replicationFactorManagerExtensions
				.add(new ModelReplicationFactorManagerExtension(this));


		add(new StatefulService() {
			@Override
			public void initialize() {
				afterInitialize();
			}

			@Override
			public void close() {
				beforeClose();
			}

		});

	}

	private void afterInitialize() {
		searchServices = newSearchServices();
		recordServices = newRecordServices();
		cachelessRecordServices = newCachelessRecordServices();
	}


	private void beforeClose() {
	}

	private void afterClose() {
		StringRecordId.setMapping(null);
	}

	private void beforeInitialize() {
		if (!modelLayerConfiguration.isPersistingStringRecordIdLegacyMapping()) {
			//Faster for tests
			StringRecordId.setMapping(new StringRecordIdLegacyMemoryMapping(
					markForReindexingRunnable));

		} else {
			//Keeping mapping between runtimes
			StringRecordId.setMapping(new StringRecordIdLegacyPersistedMapping(
					dataLayerFactory.getConfigManager(), markForReindexingRunnable));

		}
	}

	public void configureRecordsCacheHooks() {
		if (getCollectionsListManager().getCollections().contains(Collection.SYSTEM_COLLECTION)) {
			userCredentialTokenCacheHookRetriever = new UserCredentialTokenCacheHookRetriever(
					recordsCaches.registerRecordIdsHook(Collection.SYSTEM_COLLECTION, new UserCredentialTokenCacheHook(this)), this);
			userCredentialServiceKeyCacheHookRetriever = new UserCredentialServiceKeyCacheHookRetriever(
					recordsCaches.registerRecordIdsHook(Collection.SYSTEM_COLLECTION, new UserCredentialServiceKeyCacheHook(this)), this);
		}
	}

	public void onCollectionInitialized(String collection) {

		taxonomyRecordsHookRetrieverMap.put(collection, new TaxonomyRecordsHookRetriever(
				recordsCaches.registerRecordCountHook(collection, new TaxonomyRecordsHook(collection, this)), this));

		recordUsageCounterHookRetrieverMap.put(collection, new RecordUsageCounterHookRetriever(
				recordsCaches.registerRecordCountHook(collection, new RecordUsageCounterHook(collection, this)), this));

		if (userCredentialTokenCacheHookRetriever == null && collection.equals(Collection.SYSTEM_COLLECTION)) {
			configureRecordsCacheHooks();
		}
	}

	@Override
	public TaxonomyRecordsHookRetriever getTaxonomyRecordsHookRetriever(String collection) {
		return taxonomyRecordsHookRetrieverMap.get(collection);
	}


	@Override
	public RecordUsageCounterHookRetriever getRecordUsageCounterHookRetriever(String collection) {
		return recordUsageCounterHookRetrieverMap.get(collection);
	}

	public RecordMigrationsManager getRecordMigrationsManager() {
		return recordMigrationsManager;
	}

	public ModelLayerExtensions getExtensions() {
		return modelLayerExtensions;
	}

	public RecordServices newRecordServices() {
		if (recordServices != null) {
			return recordServices;
		}

		return new CachedRecordServices(this, newCachelessRecordServices(), recordsCaches);
	}

	public synchronized ThesaurusManager getThesaurusManager() {
		if (thesaurusManager == null) {
			thesaurusManager = add(new ThesaurusManager(this));
		}
		return thesaurusManager;
	}

	@Override
	public SecurityModelCache getSecurityModelCache() {
		return securityModelCache;
	}

	@Override
	public ModelLayerCachesManager getCachesManager() {
		return modelLayerCachesManager;
	}

	@Override
	public void postInitialization(ModelPostInitializationParams params) {
		recordsCaches.onPostLayerInitialization(params);


	}

	public RecordServicesImpl newCachelessRecordServices(RecordsCaches recordsCaches) {
		RecordDao recordDao = dataLayerFactory.newRecordDao();
		RecordDao eventsDao = dataLayerFactory.newEventsDao();
		RecordDao searchDao = null;

		if (dataLayerFactory.getDataLayerConfiguration().isCopyingRecordsInSearchCollection()) {
			searchDao = dataLayerFactory.newSearchDao();
		}
		RecordDao notificationsDao = dataLayerFactory.newNotificationsDao();
		DataStoreTypesFactory typesFactory = dataLayerFactory.newTypesFactory();
		return new RecordServicesImpl(recordDao, eventsDao, searchDao, notificationsDao, this, typesFactory,
				dataLayerFactory.getUniqueIdGenerator(), recordsCaches);
	}

	public AnnotationLockManager getAnnotationLockManager() {
		return annotationLockManager;
	}

	public RecordServicesImpl newCachelessRecordServices() {
		if (cachelessRecordServices != null) {
			return cachelessRecordServices;
		}
		return newCachelessRecordServices(recordsCaches);
	}

	public SearchServices newSearchServices() {
		if (searchServices != null) {
			return searchServices;
		}
		return new SearchServices(dataLayerFactory.newRecordDao(), this);
	}

	public FreeTextSearchServices newFreeTextSearchServices() {
		return new FreeTextSearchServices(this);
	}

	public FileParser newFileParser() {
		return new FileParser(forkParsers, getLanguageDetectionManager(), ioServicesFactory.newIOServices(),
				systemConfigurationsManager, modelLayerConfiguration.isDocumentsParsedInForkProcess());
	}

	public MetadataSchemasManager getMetadataSchemasManager() {
		return schemasManager;
	}

	public BatchProcessesManager getBatchProcessesManager() {
		return batchProcessesManager;
	}

	public FoldersLocator getFoldersLocator() {
		return foldersLocator;
	}

	public ContentManager getContentManager() {
		return contentsManager;
	}

	public BatchProcessController getBatchProcessesController() {
		return batchProcessesController;
	}

	public TaxonomiesManager getTaxonomiesManager() {
		return taxonomiesManager;
	}

	public TaxonomiesSearchServices newTaxonomiesSearchService() {
		return new TaxonomiesSearchServices(this);
	}

	public RolesManager getRolesManager() {
		return rolesManager;
	}

	public AuthorizationsServices newAuthorizationsServices() {
		return new AuthorizationsServices(this);
	}

	//After rename get...
	public AuthenticationService newAuthenticationService() {
		return authenticationManager;
	}

	public CollectionsListManager getCollectionsListManager() {
		return collectionsListManager;
	}

	public StoredBatchProcessProgressionServices getStoredBatchProcessProgressionServices() {
		return storedBatchProcessProgressionServices;
	}

	public UserServices newUserServices() {
		return new UserServices(this);
	}

	public LanguageDetectionManager getLanguageDetectionManager() {
		return languageDetectionManager;
	}

	public SystemConfigurationsManager getSystemConfigurationsManager() {
		return systemConfigurationsManager;
	}

	public ConstellioEIMConfigs getSystemConfigs() {
		return new ConstellioEIMConfigs(getSystemConfigurationsManager());
	}

	public UserConfigurationsManager getUserConfigurationsManager() {
		return userConfigurationsManager;
	}

	public LoggingServices newLoggingServices() {
		return new LoggingServices(this);
	}

	public LogServices newLogServices() {
		return new LogServices(this);
	}

	public IOServicesFactory getIOServicesFactory() {
		return ioServicesFactory;
	}

	public ModelLayerConfiguration getConfiguration() {
		return modelLayerConfiguration;
	}

	public UserPhotosServices newUserPhotosServices() {
		return new UserPhotosServices(dataLayerFactory);
	}

	public ReindexingServices newReindexingServices() {
		return new ReindexingServices(this);
	}

	public DataLayerFactory getDataLayerFactory() {
		return dataLayerFactory;
	}

	public LDAPConfigurationManager getLdapConfigurationManager() {
		return ldapConfigurationManager;
	}

	public LDAPAuthenticationService getLdapAuthenticationService() {
		return ldapAuthenticationService;
	}

	public PasswordFileAuthenticationService getPasswordFileAuthenticationService() {
		return passwordFileAuthenticationService;
	}

	public LDAPUserSyncManager getLdapUserSyncManager() {
		return ldapUserSyncManager;
	}

	public EmailConfigurationsManager getEmailConfigurationsManager() {
		return emailConfigurationsManager;
	}

	public EmailTemplatesManager getEmailTemplatesManager() {
		return emailTemplatesManager;
	}

	public EmailQueueManager getEmailQueueManager() {
		return emailQueueManager;
	}

	public RecordsCaches getRecordsCaches() {
		return recordsCaches;
	}

	public SecurityTokenManager getSecurityTokenManager() {
		return securityTokenManager;
	}

	public ModelLayerLogger getModelLayerLogger() {
		return modelLayerLogger;
	}

	public RecordPopulateServices newRecordPopulateServices() {
		return new RecordPopulateServices(schemasManager, contentsManager, systemConfigurationsManager, modelLayerExtensions, searchServices);
	}

	public Factory<ModelLayerFactory> getModelLayerFactoryFactory() {
		return modelLayerFactoryFactory;
	}

	public final void setEncryptionKey(Key key) {
		this.applicationEncryptionKey = key;
	}

	synchronized public EncryptionServices newEncryptionServices() {
		if (encryptionServices == null) {
			encryptionServices = modelLayerConfiguration.getEncryptionServicesFactory().get();
		}

		if (!encryptionServices.isInitialized()) {
			if (applicationEncryptionKey == null) {
				this.applicationEncryptionKey = EncryptionKeyFactory.getApplicationKey(this);
			}
			try {
				encryptionServices.withKey(applicationEncryptionKey);
			} catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException e) {
				throw new RuntimeException(e);
			}
		}

		return encryptionServices;
	}

	synchronized public void resetEncryptionServices() {
		this.encryptionServices = null;
		this.applicationEncryptionKey = null;
	}

	public SearchBoostManager getSearchBoostManager() {
		return searchBoostManager;
	}

	public void setAuthenticationService(AuthenticationService authenticationService) {
		ensureNotYetInitialized();
		this.authenticationManager = authenticationService;
	}

	public TrashQueueManager getTrashQueueManager() {
		return trashQueueManager;
	}

	public ModelLayerBackgroundThreadsManager getModelLayerBackgroundThreadsManager() {
		return modelLayerBackgroundThreadsManager;
	}

	@Override
	public SearchConfigurationsManager getSearchConfigurationsManager() {
		return searchConfigurationsManager;
	}

	@Override
	public SynonymsConfigurationsManager getSynonymsConfigurationsManager() {
		return synonymsConfigurationsManager;
	}

	@Override
	public TaxonomiesSearchServicesCache getTaxonomiesSearchServicesCache() {
		return taxonomiesSearchServicesCache;
	}

	@Override
	public void markForReindexing() {
		markForReindexingRunnable.run();
	}


	@Override
	public void markLocalCachesAsRequiringRebuild() {
		markForCacheRebuild.run();
	}

	public UserCredentialTokenCacheHookRetriever getUserCredentialTokenCacheHookRetriever() {
		return userCredentialTokenCacheHookRetriever;
	}

	@Override
	public UserCredentialServiceKeyCacheHookRetriever getUserCredentialServiceKeyCacheHookRetriever() {
		return userCredentialServiceKeyCacheHookRetriever;
	}
}
