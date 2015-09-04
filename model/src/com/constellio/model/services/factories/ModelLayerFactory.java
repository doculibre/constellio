/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.model.services.factories;

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
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.emails.EmailQueueManager;
import com.constellio.model.services.emails.EmailServices;
import com.constellio.model.services.emails.EmailTemplatesManager;
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
import com.constellio.model.services.records.reindexing.ReindexingServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.FreeTextSearchServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.security.AuthorizationDetailsManager;
import com.constellio.model.services.security.AuthorizationsServices;
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
	private final AuthenticationService authenticationManager;
	private final EmailTemplatesManager emailTemplatesManager;
	//private final LDAPUserSyncManager ldapUserSyncManager;
	private final ModelLayerExtensions modelLayerExtensions;
	private final LDAPConfigurationManager ldapConfigurationManager;
	private final LDAPAuthenticationService ldapAuthenticationService;
	private final LDAPUserSyncManager ldapUserSyncManager;
	private final PasswordFileAuthenticationService passwordFileAuthenticationService;
	private final EmailQueueManager emailQueueManager;
	private final RecordsCaches recordsCaches = new RecordsCaches();

	public ModelLayerFactory(DataLayerFactory dataLayerFactory, FoldersLocator foldersLocator,
			ModelLayerConfiguration modelLayerConfiguration, StatefullServiceDecorator statefullServiceDecorator,
			Delayed<ConstellioModulesManager> modulesManagerDelayed) {

		super(dataLayerFactory, statefullServiceDecorator);

		this.modelLayerExtensions = new ModelLayerExtensions();
		this.modelLayerConfiguration = modelLayerConfiguration;
		this.dataLayerFactory = dataLayerFactory;
		this.foldersLocator = foldersLocator;

		ConfigManager configManager = dataLayerFactory.getConfigManager();
		this.systemConfigurationsManager = add(new SystemConfigurationsManager(this, configManager, modulesManagerDelayed));
		this.ioServicesFactory = dataLayerFactory.getIOServicesFactory();

		this.forkParsers = add(new ForkParsers(modelLayerConfiguration.getForkParsersPoolSize()));
		this.collectionsListManager = add(new CollectionsListManager(configManager));
		this.batchProcessesManager = add(new BatchProcessesManager(modelLayerConfiguration.getComputerName(),
				modelLayerConfiguration.getBatchProcessesPartSize(), newRecordServices(), newSearchServices(), configManager));
		this.taxonomiesManager = add(
				new TaxonomiesManager(configManager, newSearchServices(), batchProcessesManager, collectionsListManager,
						recordsCaches));

		this.schemasManager = add(new MetadataSchemasManager(configManager, dataLayerFactory.newTypesFactory(), taxonomiesManager,
				collectionsListManager, batchProcessesManager, newSearchServices()));

		this.batchProcessesController = add(new BatchProcessController(batchProcessesManager, newRecordServices(),
				modelLayerConfiguration.getNumberOfRecordsPerTask(), schemasManager, newSearchServices()));
		this.userCredentialsManager = add(
				new UserCredentialsManager(dataLayerFactory, collectionsListManager, modelLayerConfiguration));
		this.globalGroupsManager = add(new GlobalGroupsManager(configManager));
		this.authorizationDetailsManager = add(new AuthorizationDetailsManager(configManager, collectionsListManager));
		this.rolesManager = add(new RolesManager(configManager, collectionsListManager));

		languageDetectionManager = add(new LanguageDetectionManager(getFoldersLocator().getLanguageProfiles()));

		this.contentsManager = add(new ContentManager(newFileParser(), newSearchServices(), schemasManager, dataLayerFactory,
				modelLayerConfiguration));

		workflowsConfigManager = add(new WorkflowsConfigManager(configManager, collectionsListManager,
				newWorkflowBPMNDefinitionsService()));
		workflowExecutionIndexManager = add(new WorkflowExecutionIndexManager(configManager, collectionsListManager));

		this.workflowExecutor = new WorkflowExecutor(this);
		this.ldapConfigurationManager = add(new LDAPConfigurationManager(this, configManager));

		this.ldapUserSyncManager = add(new LDAPUserSyncManager(newUserServices(), globalGroupsManager, ldapConfigurationManager,
				dataLayerFactory.getBackgroundThreadsManager()));

		ldapAuthenticationService = add(
				new LDAPAuthenticationService(ldapConfigurationManager, configManager, ioServicesFactory.newHashingService()));
		passwordFileAuthenticationService = new PasswordFileAuthenticationService(configManager,
				ioServicesFactory.newHashingService());
		this.authenticationManager = new CombinedAuthenticationService(ldapConfigurationManager, ldapAuthenticationService,
				passwordFileAuthenticationService);
		this.emailConfigurationsManager = add(new EmailConfigurationsManager(configManager, collectionsListManager));
		this.emailTemplatesManager = add(
				new EmailTemplatesManager(configManager, collectionsListManager, ioServicesFactory.newIOServices()));
		this.emailQueueManager = add(new EmailQueueManager(this, new EmailServices()));
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
		return new SearchServices(dataLayerFactory.newRecordDao(), newRecordServices());
	}

	public FreeTextSearchServices newFreeTextSearchServices() {
		return new FreeTextSearchServices(dataLayerFactory.newRecordDao(), dataLayerFactory.newEventsDao(), newUserServices());
	}

	public FileParser newFileParser() {
		return new FileParser(forkParsers, getLanguageDetectionManager(),
				modelLayerConfiguration.isDocumentsParsedInForkProcess());
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
}
