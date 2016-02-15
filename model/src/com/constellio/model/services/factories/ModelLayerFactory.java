package com.constellio.model.services.factories;

import java.security.Key;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.constellio.data.dao.managers.StatefullServiceDecorator;
import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.services.DataStoreTypesFactory;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.dao.services.factories.LayerFactory;
import com.constellio.data.dao.services.records.RecordDao;
import com.constellio.data.io.IOServicesFactory;
import com.constellio.data.utils.Delayed;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.conf.ModelLayerConfiguration;
import com.constellio.model.conf.email.EmailConfigurationsManager;
import com.constellio.model.conf.ldap.LDAPConfigurationManager;
import com.constellio.model.services.batch.controller.BatchProcessController;
import com.constellio.model.services.batch.manager.BatchProcessesManager;
import com.constellio.model.services.batch.state.StoredBatchProcessProgressionServices;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.emails.EmailQueueManager;
import com.constellio.model.services.emails.EmailServices;
import com.constellio.model.services.emails.EmailTemplatesManager;
import com.constellio.model.services.encrypt.EncryptionKeyFactory;
import com.constellio.model.services.encrypt.EncryptionServices;
import com.constellio.model.services.extensions.ConstellioModulesManager;
import com.constellio.model.services.extensions.ModelLayerExtensions;
import com.constellio.model.services.logging.LoggingServices;
import com.constellio.model.services.parser.FileParser;
import com.constellio.model.services.parser.ForkParsers;
import com.constellio.model.services.parser.LanguageDetectionManager;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesImpl;
import com.constellio.model.services.records.cache.CachedRecordServices;
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
import com.constellio.model.services.security.authentification.CombinedAuthenticationService;
import com.constellio.model.services.security.authentification.LDAPAuthenticationService;
import com.constellio.model.services.security.authentification.PasswordFileAuthenticationService;
import com.constellio.model.services.security.roles.RolesManager;
import com.constellio.model.services.tasks.TaskServices;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.model.services.taxonomies.TaxonomiesSearchServices;
import com.constellio.model.services.users.GlobalGroupsManager;
import com.constellio.model.services.users.UserCredentialsManager;
import com.constellio.model.services.users.UserPhotosServices;
import com.constellio.model.services.users.UserServices;
import com.constellio.model.services.users.sync.LDAPUserSyncManager;
import com.constellio.model.services.workflows.WorkflowExecutor;
import com.constellio.model.services.workflows.bpmn.WorkflowBPMNDefinitionsService;
import com.constellio.model.services.workflows.config.WorkflowsConfigManager;
import com.constellio.model.services.workflows.execution.WorkflowExecutionIndexManager;
import com.constellio.model.services.workflows.execution.WorkflowExecutionService;

public class ModelLayerFactory extends LayerFactory {
	private static final Logger LOGGER = LogManager.getLogger(ModelLayerFactory.class);
	private final DataLayerFactory dataLayerFactory;
	private final IOServicesFactory ioServicesFactory;
	private final FoldersLocator foldersLocator;
	private final ForkParsers forkParsers;
	private final MetadataSchemasManager schemasManager;
	private final BatchProcessController batchProcessesController;
	private final BatchProcessesManager batchProcessesManager;
	private final TaxonomiesManager taxonomiesManager;
	private final AuthorizationDetailsManager authorizationDetailsManager;
	private final RolesManager rolesManager;
	private final EmailConfigurationsManager emailConfigurationsManager;
	private final CollectionsListManager collectionsListManager;
	private final UserCredentialsManager userCredentialsManager;
	private final GlobalGroupsManager globalGroupsManager;
	private final SystemConfigurationsManager systemConfigurationsManager;
	private final LanguageDetectionManager languageDetectionManager;
	private final WorkflowsConfigManager workflowsConfigManager;
	private final WorkflowExecutionIndexManager workflowExecutionIndexManager;
	private final WorkflowExecutor workflowExecutor;
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
	private final RecordsCaches recordsCaches = new RecordsCaches();
	private final SecurityTokenManager securityTokenManager;
	protected Key applicationEncryptionKey;
	private final StoredBatchProcessProgressionServices storedBatchProcessProgressionServices;
	private final SearchBoostManager searchBoostManager;
	private final ModelLayerLogger modelLayerLogger;
	private EncryptionServices encryptionServices;

	public ModelLayerFactory(DataLayerFactory dataLayerFactory, FoldersLocator foldersLocator,
			ModelLayerConfiguration modelLayerConfiguration, StatefullServiceDecorator statefullServiceDecorator,
			Delayed<ConstellioModulesManager> modulesManagerDelayed) {

		super(dataLayerFactory, statefullServiceDecorator);

		this.modelLayerLogger = new ModelLayerLogger();
		this.modelLayerExtensions = new ModelLayerExtensions();
		this.modelLayerConfiguration = modelLayerConfiguration;
		this.dataLayerFactory = dataLayerFactory;
		this.foldersLocator = foldersLocator;

		ConfigManager configManager = dataLayerFactory.getConfigManager();
		this.systemConfigurationsManager = add(new SystemConfigurationsManager(this, configManager, modulesManagerDelayed));
		this.ioServicesFactory = dataLayerFactory.getIOServicesFactory();

		this.forkParsers = add(new ForkParsers(modelLayerConfiguration.getForkParsersPoolSize()));
		this.collectionsListManager = add(new CollectionsListManager(configManager));
		this.batchProcessesManager = add(new BatchProcessesManager(newSearchServices(), configManager));
		this.taxonomiesManager = add(
				new TaxonomiesManager(configManager, newSearchServices(), batchProcessesManager, collectionsListManager,
						recordsCaches));

		this.schemasManager = add(new MetadataSchemasManager(this, modulesManagerDelayed));

		this.batchProcessesController = add(
				new BatchProcessController(this, modelLayerConfiguration.getNumberOfRecordsPerTask()));
		this.userCredentialsManager = add(
				new UserCredentialsManager(dataLayerFactory, this, modelLayerConfiguration));
		this.globalGroupsManager = add(new GlobalGroupsManager(configManager));
		this.authorizationDetailsManager = add(new AuthorizationDetailsManager(configManager, collectionsListManager));
		this.rolesManager = add(new RolesManager(configManager, collectionsListManager));

		languageDetectionManager = add(new LanguageDetectionManager(getFoldersLocator().getLanguageProfiles()));

		this.contentsManager = add(new ContentManager(this));

		workflowsConfigManager = add(new WorkflowsConfigManager(configManager, collectionsListManager,
				newWorkflowBPMNDefinitionsService()));
		workflowExecutionIndexManager = add(new WorkflowExecutionIndexManager(configManager, collectionsListManager));

		this.workflowExecutor = new WorkflowExecutor(this);

		securityTokenManager = add(new SecurityTokenManager());

		this.ldapConfigurationManager = add(new LDAPConfigurationManager(this, configManager));
		this.ldapUserSyncManager = add(
				new LDAPUserSyncManager(newUserServices(), globalGroupsManager, ldapConfigurationManager,
						dataLayerFactory.getBackgroundThreadsManager()));
		ldapAuthenticationService = add(
				new LDAPAuthenticationService(ldapConfigurationManager, configManager,
						ioServicesFactory.newHashingService()));
		passwordFileAuthenticationService = new PasswordFileAuthenticationService(configManager,
				ioServicesFactory.newHashingService());
		this.authenticationManager = new CombinedAuthenticationService(ldapConfigurationManager, ldapAuthenticationService,
				passwordFileAuthenticationService);
		this.emailConfigurationsManager = add(
				new EmailConfigurationsManager(configManager, collectionsListManager, this));

		this.emailTemplatesManager = add(
				new EmailTemplatesManager(configManager, collectionsListManager, ioServicesFactory.newIOServices()));
		this.emailQueueManager = add(new EmailQueueManager(this, new EmailServices()));
		this.storedBatchProcessProgressionServices = add(new StoredBatchProcessProgressionServices(configManager));
		this.searchBoostManager = add(
				new SearchBoostManager(configManager, collectionsListManager));

	}

	public ModelLayerExtensions getExtensions() {
		return modelLayerExtensions;
	}

	public RecordServices newRecordServices() {
		return new CachedRecordServices(this, newCachelessRecordServices(), recordsCaches);
	}

	public RecordServicesImpl newCachelessRecordServices() {
		RecordDao recordDao = dataLayerFactory.newRecordDao();
		RecordDao eventsDao = dataLayerFactory.newEventsDao();
		RecordDao notificationsDao = dataLayerFactory.newNotificationsDao();
		DataStoreTypesFactory typesFactory = dataLayerFactory.newTypesFactory();
		return new RecordServicesImpl(recordDao, eventsDao, notificationsDao, this, typesFactory,
				dataLayerFactory.getUniqueIdGenerator(), recordsCaches);
	}

	public SearchServices newSearchServices() {
		return new SearchServices(dataLayerFactory.newRecordDao(), this);
	}

	public FreeTextSearchServices newFreeTextSearchServices() {
		return new FreeTextSearchServices(dataLayerFactory.newRecordDao(), dataLayerFactory.newEventsDao(), newUserServices(),
				securityTokenManager);
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
		return new TaxonomiesSearchServices(newSearchServices(), taxonomiesManager, schemasManager);
	}

	public RolesManager getRolesManager() {
		return rolesManager;
	}

	public AuthorizationDetailsManager getAuthorizationDetailsManager() {
		return authorizationDetailsManager;
	}

	public AuthorizationsServices newAuthorizationsServices() {
		return new AuthorizationsServices(getAuthorizationDetailsManager(), getRolesManager(), getTaxonomiesManager(),
				newRecordServices(), newSearchServices(), newUserServices(), schemasManager, newLoggingServices(),
				dataLayerFactory.getUniqueIdGenerator());
	}

	//After rename get...
	public AuthenticationService newAuthenticationService() {
		return authenticationManager;
	}

	public CollectionsListManager getCollectionsListManager() {
		return collectionsListManager;
	}

	public UserCredentialsManager getUserCredentialsManager() {
		return userCredentialsManager;
	}

	public StoredBatchProcessProgressionServices getStoredBatchProcessProgressionServices() {
		return storedBatchProcessProgressionServices;
	}

	public GlobalGroupsManager getGlobalGroupsManager() {
		return globalGroupsManager;
	}

	public UserServices newUserServices() {
		return new UserServices(userCredentialsManager, globalGroupsManager, collectionsListManager, newRecordServices(),
				newSearchServices(), schemasManager, newAuthenticationService(), rolesManager, modelLayerConfiguration,
				ldapConfigurationManager);
	}

	public LanguageDetectionManager getLanguageDetectionManager() {

		return languageDetectionManager;
	}

	public SystemConfigurationsManager getSystemConfigurationsManager() {
		return systemConfigurationsManager;
	}

	public LoggingServices newLoggingServices() {
		return new LoggingServices(this);
	}

	public IOServicesFactory getIOServicesFactory() {
		return ioServicesFactory;
	}

	public WorkflowBPMNDefinitionsService newWorkflowBPMNDefinitionsService() {
		return new WorkflowBPMNDefinitionsService(getFoldersLocator(), getIOServicesFactory().newFileService());
	}

	public WorkflowExecutionService newWorkflowExecutionService() {
		return new WorkflowExecutionService(workflowExecutionIndexManager, dataLayerFactory);
	}

	public WorkflowsConfigManager getWorkflowsConfigManager() {
		return workflowsConfigManager;
	}

	public WorkflowExecutionIndexManager getWorkflowExecutionIndexManager() {
		return workflowExecutionIndexManager;
	}

	public WorkflowExecutor getWorkflowsManager() {
		return workflowExecutor;
	}

	public TaskServices newTaskServices() {
		return new TaskServices(newRecordServices(), newSearchServices(), newWorkflowExecutionService(),
				getMetadataSchemasManager());
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
		return new RecordPopulateServices(schemasManager, contentsManager, systemConfigurationsManager, modelLayerExtensions);
	}

	final void setEncryptionKey(Key key) {
		this.applicationEncryptionKey = key;
	}

	synchronized public EncryptionServices newEncryptionServices() {
		if (encryptionServices != null) {
			return encryptionServices;
		}
		if (applicationEncryptionKey == null) {
			this.applicationEncryptionKey = EncryptionKeyFactory.getApplicationKey(this);
		}
		return EncryptionServices.create(applicationEncryptionKey);
	}

	public SearchBoostManager getSearchBoostManager() {
		return searchBoostManager;
	}

	public void setEncryptionServices(EncryptionServices encryptionServices) {
		ensureNotYetInitialized();
		this.encryptionServices = encryptionServices;
	}

	public void setAuthenticationService(AuthenticationService authenticationService) {
		ensureNotYetInitialized();
		this.authenticationManager = authenticationService;
	}
}
