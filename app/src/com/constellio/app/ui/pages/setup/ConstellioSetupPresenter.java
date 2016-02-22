package com.constellio.app.ui.pages.setup;

import static com.constellio.app.ui.i18n.i18n.$;
import static java.util.Arrays.asList;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.constellio.app.entities.modules.InstallableModule;
import com.constellio.app.modules.rm.ui.builders.UserToVOBuilder;
import com.constellio.app.services.appManagement.AppManagementServiceException;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.i18n.i18n;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.app.ui.pages.setup.ConstellioSetupPresenterException.ConstellioSetupPresenterException_CannotLoadSaveState;
import com.constellio.app.ui.pages.setup.ConstellioSetupPresenterException.ConstellioSetupPresenterException_MustSelectAtLeastOneModule;
import com.constellio.app.ui.pages.setup.ConstellioSetupPresenterException.ConstellioSetupPresenterException_TasksCannotBeTheOnlySelectedModule;
import com.constellio.data.conf.DataLayerConfiguration;
import com.constellio.data.dao.services.DataLayerLogger;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServer;
import com.constellio.data.dao.services.transactionLog.TransactionLogReadWriteServices;
import com.constellio.data.dao.services.transactionLog.replay.TransactionLogReplayServices;
import com.constellio.data.extensions.DataLayerSystemExtensions;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.io.services.zip.ZipService;
import com.constellio.data.io.services.zip.ZipServiceException;
import com.constellio.model.entities.modules.Module;
import com.constellio.model.entities.modules.PluginUtil;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import com.constellio.model.entities.security.global.XmlUserCredential;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.extensions.ConstellioModulesManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.users.UserServices;

public class ConstellioSetupPresenter extends BasePresenter<ConstellioSetupView> {

	private static final String TEMP_UNZIP_FOLDER = "ConstellioSetupPresenter-TempUnzipFolder";

	private static final Logger LOGGER = LogManager.getLogger(ConstellioSetupPresenter.class);

	private ConstellioSetupView view;

	private UserToVOBuilder userToVOBuilder = new UserToVOBuilder();

	public ConstellioSetupPresenter(ConstellioSetupView view) {
		super(view);
		this.view = view;

		ConstellioFactories factories = view.getConstellioFactories();
		ConstellioModulesManager modulesManager = factories.getAppLayerFactory().getModulesManager();
		List<Module> installedModules = new ArrayList<>();

		for (Module module : modulesManager.getBuiltinModules()) {
			if (!module.isComplementary()) {
				installedModules.add(module);
			}
		}

		List<String> localeCodes = i18n.getSupportedLanguages();

		List<String> moduleIds = new ArrayList<>();
		for (Module installedModule : installedModules) {
			String moduleId = installedModule.getId();
			moduleIds.add(moduleId);
		}

		view.setLocaleCodes(localeCodes);
		view.setModuleIds(moduleIds);
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return true;
	}

	public String getLogoTarget() {
		SystemConfigurationsManager manager = modelLayerFactory.getSystemConfigurationsManager();
		String linkTarget = manager.getValue(ConstellioEIMConfigs.LOGO_LINK);
		if (StringUtils.isBlank(linkTarget)) {
			linkTarget = "http://www.constellio.com";
		}
		return linkTarget;
	}

	public void saveRequested(String setupLocaleCode, List<String> modules, String collectionTitle, String collectionCode,
			String adminPassword)
			throws ConstellioSetupPresenterException {
		if (modules.isEmpty()) {
			throw new ConstellioSetupPresenterException_MustSelectAtLeastOneModule();
		} else if (modules.size() == 1 && modules.contains("tasks")) {
			throw new ConstellioSetupPresenterException_TasksCannotBeTheOnlySelectedModule();
		}
		view.showMessage($("ConstellioSetupView.setupInProgress"));

		ConstellioFactories factories = view.getConstellioFactories();

		setSystemLanguage(setupLocaleCode);
		Record collectionRecord = factories.getAppLayerFactory().getCollectionsManager().createCollectionInCurrentVersion(
				collectionCode, asList(setupLocaleCode));
		Collection collection = new Collection(collectionRecord,
				modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collectionCode));
		collection.setTitle(collectionTitle);

		ConstellioModulesManager modulesManager = factories.getAppLayerFactory().getModulesManager();

		List<String> roles = new ArrayList<>();
		for (String moduleCode : modules) {
			Module module = modulesManager.getInstalledModule(moduleCode);
			modulesManager.installValidModuleAndGetInvalidOnes(module,
					factories.getModelLayerFactory().getCollectionsListManager());
			modulesManager.enableValidModuleAndGetInvalidOnes(collectionCode, module);
			roles.addAll(PluginUtil.getRolesForCreator(module));
			try {
				((InstallableModule) module).addDemoData(collectionCode, appLayerFactory);
			} catch (Throwable e) {
				LOGGER.error("Error when adding demo data of module " + module.getId() + " in collection " + collection, e);
			}
		}

		ModelLayerFactory modelLayerFactory = factories.getModelLayerFactory();

		UserServices userServices = modelLayerFactory.newUserServices();
		UserCredential adminCredential = new XmlUserCredential("admin", "System", "Admin", "admin@administration.com",
				new ArrayList<String>(), asList(collectionCode),
				UserCredentialStatus.ACTIVE).withSystemAdminPermission();
		userServices.addUpdateUserCredential(adminCredential);
		userServices.addUserToCollection(adminCredential, collectionCode);
		User user = userServices.getUserRecordInCollection("admin", collectionCode);
		if (StringUtils.isBlank(adminPassword)) {
			adminPassword = "password";
		}
		modelLayerFactory.getPasswordFileAuthenticationService().changePassword("admin", adminPassword);
		try {
			modelLayerFactory.newRecordServices().update(user.setUserRoles(roles).setCollectionAllAccess(true));
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}

		// TODO Vincent fix session context creation
		//		UserVO userVO = userToVOBuilder.build(user.getWrappedRecord(), VIEW_MODE.DISPLAY);
		//		SessionContext sessionContext = view.getSessionContext();
		//		sessionContext.setCurrentCollection(collectionCode);
		//		sessionContext.setCurrentLocale(new Locale(setupLocaleCode));
		//		sessionContext.setCurrentUser(userVO);

		view.updateUI();
	}

	public void setSystemLanguage(String languageCode) {
		modelLayerFactory.getConfiguration().setMainDataLanguage(languageCode);
	}

	public void loadSaveStateRequested(File saveStateFile)
			throws ConstellioSetupPresenterException {
		try {
			File tempFolder = createTempFolder();
			try {
				ZipService zipService = modelLayerFactory.getIOServicesFactory().newZipService();
				DataLayerConfiguration dataLayerConfiguration = modelLayerFactory.getDataLayerFactory()
						.getDataLayerConfiguration();
				IOServices ioServices = modelLayerFactory.getIOServicesFactory().newIOServices();
				File settingsFolder = dataLayerConfiguration.getSettingsFileSystemBaseFolder();
				File contentsFolder = dataLayerConfiguration.getContentDaoFileSystemFolder();
				BigVaultServer bigVaultServer = modelLayerFactory.getDataLayerFactory().getContentsVaultServer();
				DataLayerSystemExtensions dataLayerSystemExtensions = modelLayerFactory
						.getDataLayerFactory().getExtensions().getSystemWideExtensions();
				ConstellioFactories.clear();

				try {
					extractSaveState(zipService, saveStateFile, tempFolder);
					copyExtractedFiles(tempFolder, ioServices, settingsFolder, contentsFolder);
					List<File> tLogFiles = getTLogs(tempFolder);

					TransactionLogReadWriteServices readWriteServices = new TransactionLogReadWriteServices(ioServices,
							dataLayerConfiguration, dataLayerSystemExtensions);
					new TransactionLogReplayServices(readWriteServices, bigVaultServer, new DataLayerLogger())
							.replayTransactionLogs(tLogFiles);
				} catch (Throwable t) {
					revertState(ioServices, settingsFolder, contentsFolder);
					ConstellioFactories.start();
					throw new ConstellioSetupPresenterException_CannotLoadSaveState();
				}

				view.updateUI();

			} finally {
				modelLayerFactory.getIOServicesFactory().newIOServices().deleteQuietly(tempFolder);
			}
		} catch (Throwable t) {
			LOGGER.info("Error when trying to load system from a saved state", t);
			throw new ConstellioSetupPresenterException_CannotLoadSaveState();
		}
	}

	private List<File> getTLogs(File tempFolder) {
		File tlogsFolder = new File(new File(tempFolder, "content"), "tlogs");
		return new ArrayList<>(FileUtils.listFiles(tlogsFolder, new String[] { "tlog" }, false));
	}

	private void revertState(IOServices ioServices, File settingsFolder, File contentsFolder) {
		ioServices.deleteDirectoryWithoutExpectableIOException(settingsFolder);
		ioServices.deleteDirectoryWithoutExpectableIOException(contentsFolder);
		new File(settingsFolder.getPath()).mkdirs();
		new File(contentsFolder.getPath()).mkdirs();
	}

	private File createTempFolder() {
		return modelLayerFactory.getIOServicesFactory().newIOServices().newTemporaryFolder(TEMP_UNZIP_FOLDER);
	}

	private void copyExtractedFiles(File tempFolder, IOServices ioServices, File settingsFolder, File contentsFolder) {
		ioServices.deleteDirectoryWithoutExpectableIOException(settingsFolder);
		ioServices.deleteDirectoryWithoutExpectableIOException(contentsFolder);

		File settingsFolderInSaveState = new File(tempFolder, "settings");
		File contentsFolderInSaveState = new File(tempFolder, "content");

		ioServices.moveFolder(settingsFolderInSaveState, settingsFolder);
		ioServices.moveFolder(contentsFolderInSaveState, contentsFolder);
	}

	private void extractSaveState(ZipService zipService, File saveStateFile, File tempFolder)
			throws ZipServiceException {
		zipService.unzip(saveStateFile, tempFolder);
	}

	private void loadTransactionLog()
			throws AppManagementServiceException {
		appLayerFactory.newApplicationService().restart();
	}

}
