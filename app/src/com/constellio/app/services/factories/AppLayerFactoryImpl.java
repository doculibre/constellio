package com.constellio.app.services.factories;

import com.constellio.app.api.cmis.binding.global.CmisCacheManager;
import com.constellio.app.conf.AppLayerConfiguration;
import com.constellio.app.extensions.AppLayerExtensions;
import com.constellio.app.extensions.api.scripts.Scripts;
import com.constellio.app.extensions.impl.DefaultPagesComponentsExtension;
import com.constellio.app.modules.complementary.ESRMRobotsModule;
import com.constellio.app.modules.es.ConstellioESModule;
import com.constellio.app.modules.restapi.ConstellioRestApiModule;
import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplateManager;
import com.constellio.app.modules.robots.ConstellioRobotsModule;
import com.constellio.app.modules.tasks.TaskModule;
import com.constellio.app.services.appManagement.AppManagementService;
import com.constellio.app.services.appManagement.AppManagementServiceException;
import com.constellio.app.services.background.AppLayerBackgroundThreadsManager;
import com.constellio.app.services.collections.CollectionsManager;
import com.constellio.app.services.corrector.CorrectorExcluderManager;
import com.constellio.app.services.extensions.ConstellioModulesManagerImpl;
import com.constellio.app.services.extensions.plugins.ConstellioPluginConfigurationManager;
import com.constellio.app.services.extensions.plugins.ConstellioPluginManager;
import com.constellio.app.services.extensions.plugins.JSPFConstellioPluginManager;
import com.constellio.app.services.metadata.AppSchemasServices;
import com.constellio.app.services.migrations.ConstellioEIM;
import com.constellio.app.services.migrations.MigrationServices;
import com.constellio.app.services.records.SavedSearchRecordsCachesHook;
import com.constellio.app.services.records.SystemCheckManager;
import com.constellio.app.services.recovery.UpgradeAppRecoveryService;
import com.constellio.app.services.recovery.UpgradeAppRecoveryServiceImpl;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.app.services.systemSetup.SystemGlobalConfigsManager;
import com.constellio.app.services.systemSetup.SystemLocalConfigsManager;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.framework.containers.ContainerButtonListener;
import com.constellio.app.ui.i18n.i18n;
import com.constellio.app.ui.pages.base.EnterViewListener;
import com.constellio.app.ui.pages.base.InitUIListener;
import com.constellio.app.ui.pages.base.PresenterService;
import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.dao.managers.StatefullServiceDecorator;
import com.constellio.data.dao.managers.config.ConfigManagerException.OptimisticLockingConfiguration;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.dao.services.factories.LayerFactoryImpl;
import com.constellio.data.io.IOServicesFactory;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.utils.Delayed;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.data.utils.systemLogger.SystemLogger;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.conf.FoldersLocatorMode;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.RecordMigrationScript;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.extensions.ConstellioModulesManager;
import com.constellio.model.services.extensions.ConstellioModulesManagerException.ConstellioModulesManagerException_ModuleInstallationFailed;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.factories.ModelPostInitializationParams;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.reindexing.ReindexationMode;
import com.constellio.model.services.records.reindexing.ReindexationParams;
import com.constellio.model.services.records.reindexing.ReindexingServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static com.constellio.model.services.records.reindexing.ReindexationParams.recalculateAndRewriteSchemaTypesInBackground;

public class AppLayerFactoryImpl extends LayerFactoryImpl implements AppLayerFactory {

	private static final Logger LOGGER = LoggerFactory.getLogger(AppLayerFactoryImpl.class);

	private AppLayerBackgroundThreadsManager appLayerBackgroundThreadsManager;

	private FoldersLocator foldersLocator;

	private ConstellioPluginManager pluginManager;

	private ConstellioModulesManagerImpl modulesManager;

	private ModelLayerFactory modelLayerFactory;

	private DataLayerFactory dataLayerFactory;

	private CmisCacheManager cmisRepositoriesManager;

	private List<EnterViewListener> enterViewListeners;

	private List<InitUIListener> initUIListeners;

	private SystemGlobalConfigsManager systemGlobalConfigsManager;

	private SystemLocalConfigsManager systemLocalConfigsManager;

	private List<ContainerButtonListener> containerButtonListeners;
	private SchemasDisplayManager metadataSchemasDisplayManager;

	private final AppLayerExtensions appLayerExtensions;

	private final CollectionsManager collectionsManager;

	private final LabelTemplateManager labelTemplateManager;

	private final AppLayerConfiguration appLayerConfiguration;

	private final Map<String, StatefulService> moduleManagers = new HashMap<>();
	final private NavigatorConfigurationService navigatorConfigService;

	private final SystemCheckManager systemCheckManager;

	private final UpgradeAppRecoveryService upgradeAppRecoveryService;

	private final CorrectorExcluderManager correctorExcluderManager;

	private boolean initializationFinished;


	public AppLayerFactoryImpl(AppLayerConfiguration appLayerConfiguration, ModelLayerFactory modelLayerFactory,
							   DataLayerFactory dataLayerFactory, StatefullServiceDecorator statefullServiceDecorator,
							   String instanceName, short instanceId) {
		super(modelLayerFactory, statefullServiceDecorator, instanceName, instanceId);

		this.appLayerExtensions = new AppLayerExtensions();
		this.modelLayerFactory = modelLayerFactory;
		this.dataLayerFactory = dataLayerFactory;
		this.enterViewListeners = new ArrayList<>();
		this.initUIListeners = new ArrayList<>();
		this.containerButtonListeners = new ArrayList<>();
		this.foldersLocator = new FoldersLocator();
		this.appLayerConfiguration = appLayerConfiguration;
		this.setDefaultLocale();
		this.metadataSchemasDisplayManager = add(new SchemasDisplayManager(dataLayerFactory.getConfigManager(),
				modelLayerFactory.getCollectionsListManager(), modelLayerFactory.getMetadataSchemasManager(),
				dataLayerFactory.getLocalCacheManager(), modelLayerFactory.getSystemConfigurationsManager()));

		String warVersion = newApplicationService().getWarVersion();
		dataLayerFactory.setConstellioVersion(warVersion);

		IOServices ioServices = modelLayerFactory.getIOServicesFactory().newIOServices();
		pluginManager = add(new JSPFConstellioPluginManager(appLayerConfiguration.getPluginsFolder(),
				appLayerConfiguration.getPluginsManagementOnStartupFile(), ioServices,
				new ConstellioPluginConfigurationManager(dataLayerFactory)));
		pluginManager.registerModule(new ConstellioRMModule());

		pluginManager.registerModule(new ConstellioESModule());
		pluginManager.registerModule(new TaskModule());
		pluginManager.registerModule(new ConstellioRobotsModule());
		pluginManager.registerModule(new ESRMRobotsModule());
		pluginManager.registerModule(new ConstellioRestApiModule());

		Delayed<MigrationServices> migrationServicesDelayed = new Delayed<>();
		this.modulesManager = add(new ConstellioModulesManagerImpl(this, pluginManager, migrationServicesDelayed));


		this.systemGlobalConfigsManager = add(new SystemGlobalConfigsManager(modelLayerFactory.getDataLayerFactory()));
		this.systemLocalConfigsManager = add(new SystemLocalConfigsManager(new FoldersLocator().getLocalConfigsFile(), systemGlobalConfigsManager));
		this.collectionsManager = add(
				new CollectionsManager(this, modulesManager, migrationServicesDelayed, systemGlobalConfigsManager));
		migrationServicesDelayed.set(newMigrationServices());
		try {
			newMigrationServices().migrate(null, false);
		} catch (OptimisticLockingConfiguration e) {
			throw new RuntimeException(e);

		} catch (ConstellioModulesManagerException_ModuleInstallationFailed e) {
			throw new RuntimeException(e);
		}
		labelTemplateManager = new LabelTemplateManager(dataLayerFactory.getConfigManager(), this);
		this.navigatorConfigService = new NavigatorConfigurationService();
		this.systemCheckManager = add(new SystemCheckManager(this));
		this.upgradeAppRecoveryService = new UpgradeAppRecoveryServiceImpl(this,
				dataLayerFactory.getIOServicesFactory().newIOServices());

		dataLayerFactory.getConfigManager().keepInCache(MigrationServices.VERSION_PROPERTIES_FILE);

		correctorExcluderManager = add(new CorrectorExcluderManager(modelLayerFactory));

		this.appLayerBackgroundThreadsManager = add(new AppLayerBackgroundThreadsManager(this));

		initializationFinished = false;

	}

	private void setDefaultLocale() {
		Locale locale;
		String mainDataLanguage = modelLayerFactory.getConfiguration().getMainDataLanguage();
		if ("en".equals(mainDataLanguage)) {
			locale = Locale.ENGLISH;
		} else if ("fr".equals(mainDataLanguage)) {
			locale = Locale.FRENCH;
		} else if ("ar".equals(mainDataLanguage)) {
			locale = new Locale(Language.Arabic.getCode());
		} else {
			throw new ImpossibleRuntimeException("Invalid language " + mainDataLanguage);
		}
		i18n.setLocale(locale);

	}

	public void registerSystemWideManager(String module, String id, StatefulService manager) {
		String key = module + "-" + id;
		add(manager);
		moduleManagers.put(key, manager);
	}

	public void registerManager(String collection, String module, String id, StatefulService manager) {
		String key = collection + "-" + module + "-" + id;
		add(manager);
		moduleManagers.put(key, manager);
	}

	public <T> T getRegisteredManager(String collection, String module, String id) {
		String key = collection + "-" + module + "-" + id;
		return (T) moduleManagers.get(key);
	}

	public <T> T getSystemWideRegisteredManager(String module, String id) {
		String key = module + "-" + id;
		return (T) moduleManagers.get(key);
	}

	public AppLayerExtensions getExtensions() {
		return appLayerExtensions;
	}

	public AppManagementService newApplicationService() {
		IOServicesFactory ioServicesFactory = dataLayerFactory.getIOServicesFactory();
		return new AppManagementService(this, foldersLocator);
	}

	public UpgradeAppRecoveryService newUpgradeAppRecoveryService() {
		return upgradeAppRecoveryService;
	}

	public SystemCheckManager getSystemCheckManager() {
		return systemCheckManager;
	}

	@Override
	public void initialize() {
		UpgradeAppRecoveryServiceImpl upgradeAppRecoveryService = new UpgradeAppRecoveryServiceImpl(this,
				dataLayerFactory.getIOServicesFactory().newIOServices());
		upgradeAppRecoveryService.deletePreviousWarCausingFailure();
		SystemConfigurationsManager configManager = modelLayerFactory
				.getSystemConfigurationsManager();
		configManager.initialize();
		ConstellioEIMConfigs constellioConfigs = new ConstellioEIMConfigs(configManager);
		boolean recoveryModeActive = constellioConfigs.isInUpdateProcess();
		if (Toggle.FORCE_ROLLBACK.isEnabled() || recoveryModeActive) {
			LOGGER.info("Launching in rollback mode");
			startupWithPossibleRecovery(upgradeAppRecoveryService);
		} else {
			LOGGER.info("Launching in normal mode");
			normalStartup(false);
		}
		if (dataLayerFactory.getDataLayerConfiguration().isBackgroundThreadsEnabled()) {
			dataLayerFactory.getBackgroundThreadsManager().onSystemStarted();
			dataLayerFactory.getConstellioJobManager().onSystemStarted();
		}
		dataLayerFactory.getEventBusManager().resume();
		upgradeAppRecoveryService.close();

		initializationFinished = true;

		getModelLayerFactory().getRecordsCaches().register(new SavedSearchRecordsCachesHook(10_000));

		SystemLogger.info("Application started");
	}

	private void startupWithPossibleRecovery(UpgradeAppRecoveryServiceImpl recoveryService) {
		if (dataLayerFactory.getSecondTransactionLogManager() != null) {
			LOGGER.info("Second tlog manager detected");
			recoveryService.startRollbackMode();
			try {
				normalStartup(true);
				recoveryService.stopRollbackMode();
			} catch (Throwable exception) {
				if (recoveryService.isInRollbackMode()) {
					LOGGER.error("Error when trying to start application", exception);
					recoveryService.rollback(exception);
					LOGGER.info("rollbacked successfully");
					//this.appLayerFactory.getModelLayerFactory().getDataLayerFactory().close(false);
					try {
						newApplicationService().restart();
						LOGGER.info("Restart command launched");
					} catch (AppManagementServiceException e) {
						throw new RuntimeException(e);
					}
				} else {
					LOGGER.info("No more in rollback mode...");
					throw exception;
				}
			}
		} else {
			LOGGER.info("Second tlog manager not detected");
			//rare case in tests
			normalStartup(false);
		}

	}

	private void normalStartup(boolean recoveryMode) {
		appLayerExtensions.getSystemWideExtensions().pagesComponentsExtensions.add(new DefaultPagesComponentsExtension(this));
		this.pluginManager.detectPlugins();

		super.initialize();

		String warVersion = newApplicationService().getWarVersion();
		if (warVersion != null && !"5.0.0".equals(warVersion)) {
			LOGGER.info("----------- STARTING APPLICATION IN VERSION " + warVersion + " -----------");
		}

		try {
			try {
				collectionsManager.initializeModulesResources();
				newMigrationServices().migrate(null, false);
			} catch (OptimisticLockingConfiguration optimisticLockingConfiguration) {
				throw new RuntimeException(optimisticLockingConfiguration);
			}


			collectionsManager.initializeCollectionsAndGetInvalidModules();
			getModulesManager().enableComplementaryModules();
		} catch (ConstellioModulesManagerException_ModuleInstallationFailed e) {
			if (new FoldersLocator().getFoldersLocatorMode() == FoldersLocatorMode.WRAPPER && !recoveryMode) {
				LOGGER.warn("System is restarting because of failure to install/update module '" + e.getFailedModule()
							+ "' in collection '" + e.getFailedCollection() + "'", e);
				try {
					restart();
				} catch (AppManagementServiceException e2) {
					throw new RuntimeException(e2);
				}
			} else {
				throw new RuntimeException(e);
			}
		}

		Map<String, Set<String>> typesWithNewScriptsInCollections = new HashMap<>();
		for (String collection : getModelLayerFactory().getCollectionsListManager().getCollectionsExcludingSystem()) {
			List<RecordMigrationScript> scripts = newMigrationServices().getAllRecordMigrationScripts(collection);
			//TODO Check if master node
			Set<String> typesWithNewScripts = getModelLayerFactory().getRecordMigrationsManager()
					.registerReturningTypesWithNewScripts(collection, scripts, true);
			typesWithNewScriptsInCollections.put(collection, typesWithNewScripts);
		}

		modelLayerFactory.getBatchProcessesController().start();

		ReindexingServices reindexingServices = modelLayerFactory.newReindexingServices();
		MetadataSchemasManager schemasManager = modelLayerFactory.getMetadataSchemasManager();
		for (Map.Entry<String, Set<String>> entry : typesWithNewScriptsInCollections.entrySet()) {
			List<MetadataSchemaType> types = schemasManager.getSchemaTypes(entry.getKey(), new ArrayList<>(entry.getValue()));
			reindexingServices.reindexCollections(recalculateAndRewriteSchemaTypesInBackground(types));
		}

	}

	public void postInitialization() {
		modelLayerFactory.postInitialization(new ModelPostInitializationParams()
				.setRebuildCacheFromSolr(systemLocalConfigsManager.isMarkedForCacheRebuild())
				.setCacheLoadingFinishedCallback(() -> {
					if (systemLocalConfigsManager.isMarkedForCacheRebuild()) {
						systemLocalConfigsManager.setMarkedForCacheRebuild(false);
						systemLocalConfigsManager.markLocalCacheAsRebuilt();
					}
				}));

		pluginManager.configure();

		if (modelLayerFactory.newReindexingServices().isLockFileExisting()) {
			//Last reindexing was interrupted...
			systemGlobalConfigsManager.setLastReindexingFailed(true);
			if (dataLayerFactory.getSecondTransactionLogManager() != null) {
				dataLayerFactory.getSecondTransactionLogManager().moveLastBackupAsCurrentLog();
			}
			modelLayerFactory.newReindexingServices().removeLockFile();
		}

		if (systemLocalConfigsManager.isMarkedForReindexing()) {
			systemLocalConfigsManager.setMarkedForReindexing(false);

			try {
				modelLayerFactory.newReindexingServices().createLockFile();
				modelLayerFactory.newReindexingServices().reindexCollections(
						new ReindexationParams(ReindexationMode.RECALCULATE_AND_REWRITE).setRepopulate(false));
				modelLayerFactory.newReindexingServices().removeLockFile();

				systemGlobalConfigsManager.setReindexingRequired(false);
				systemGlobalConfigsManager.setLastReindexingFailed(false);
			} catch (Exception e) {
				LOGGER.error("Reindexing failed", e);
				systemGlobalConfigsManager.setReindexingRequired(true);
				systemGlobalConfigsManager.setLastReindexingFailed(true);
				dataLayerFactory.getSecondTransactionLogManager().moveLastBackupAsCurrentLog();
			}
		}
		systemLocalConfigsManager.setRestartRequired(false);
	}

	public void restart()
			throws AppManagementServiceException {
		newApplicationService().restart();
	}

	@Override
	public void close() {
		Scripts.removeScripts();
		super.close();
	}

	public boolean isInitializationFinished() {
		return initializationFinished;
	}

	public ConstellioPluginManager getPluginManager() {
		return pluginManager;
	}

	public ModelLayerFactory getModelLayerFactory() {
		return modelLayerFactory;
	}

	public NavigatorConfigurationService getNavigatorConfigurationService() {
		return navigatorConfigService;
	}

	public PresenterService newPresenterService() {
		return new PresenterService(modelLayerFactory);
	}

	public CmisCacheManager getConstellioCmisRepositoriesManager() {
		if (cmisRepositoriesManager == null) {
			cmisRepositoriesManager = add(new CmisCacheManager(this));
			cmisRepositoriesManager.initialize();
		}
		return cmisRepositoriesManager;
	}

	public List<EnterViewListener> getEnterViewListeners() {
		return enterViewListeners;
	}

	public List<InitUIListener> getInitUIListeners() {
		return initUIListeners;
	}

	public List<ContainerButtonListener> getContainerButtonListeners() {
		return containerButtonListeners;
	}

	public SchemasDisplayManager getMetadataSchemasDisplayManager() {
		return metadataSchemasDisplayManager;
	}

	public SystemGlobalConfigsManager getSystemGlobalConfigsManager() {
		return this.systemGlobalConfigsManager;
	}

	public SystemLocalConfigsManager getSystemLocalConfigsManager() {
		return systemLocalConfigsManager;
	}

	public CollectionsManager getCollectionsManager() {
		return collectionsManager;
	}

	public MigrationServices newMigrationServices() {
		return new MigrationServices(new ConstellioEIM(), this, modulesManager, pluginManager);
	}

	public ConstellioModulesManager getModulesManager() {
		return modulesManager;
	}

	public LabelTemplateManager getLabelTemplateManager() {
		return labelTemplateManager;
	}

	public AppLayerConfiguration getAppLayerConfiguration() {
		return appLayerConfiguration;
	}

	public AppSchemasServices newSchemasServices() {
		return new AppSchemasServices(this);
	}

	public CorrectorExcluderManager getCorrectorExcluderManager() {
		return correctorExcluderManager;
	}

}
