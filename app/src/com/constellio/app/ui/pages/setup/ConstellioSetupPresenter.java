package com.constellio.app.ui.pages.setup;

import com.constellio.app.entities.modules.InstallableModule;
import com.constellio.app.modules.rm.ui.builders.UserToVOBuilder;
import com.constellio.app.services.appManagement.AppManagementServiceException;
import com.constellio.app.services.collections.CollectionsManager;
import com.constellio.app.services.collections.CollectionsManagerRuntimeException.CollectionsManagerRuntimeException_InvalidCode;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.i18n.i18n;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.setup.ConstellioSetupPresenterException.ConstellioSetupPresenterException_AdminConfirmationPasswordNotEqualToAdminPassword;
import com.constellio.app.ui.pages.setup.ConstellioSetupPresenterException.ConstellioSetupPresenterException_CannotLoadSaveState;
import com.constellio.app.ui.pages.setup.ConstellioSetupPresenterException.ConstellioSetupPresenterException_CodeMustBeAlphanumeric;
import com.constellio.app.ui.pages.setup.ConstellioSetupPresenterException.ConstellioSetupPresenterException_MustSelectAtLeastOneModule;
import com.constellio.app.ui.pages.setup.ConstellioSetupPresenterException.ConstellioSetupPresenterException_TasksCannotBeTheOnlySelectedModule;
import com.constellio.data.conf.DataLayerConfiguration;
import com.constellio.data.conf.SecondTransactionLogType;
import com.constellio.data.dao.dto.sql.RecordTransactionSqlDTO;
import com.constellio.data.dao.services.DataLayerLogger;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServer;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.dao.services.sql.SqlRecordDaoType;
import com.constellio.data.dao.services.transactionLog.TransactionLogReadWriteServices;
import com.constellio.data.dao.services.transactionLog.TransactionLogSqlReadWriteServices;
import com.constellio.data.dao.services.transactionLog.replay.SqlTransactionLogReplayServices;
import com.constellio.data.dao.services.transactionLog.replay.TransactionLogReplayServices;
import com.constellio.data.extensions.DataLayerSystemExtensions;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.io.services.zip.ZipService;
import com.constellio.data.io.services.zip.ZipServiceException;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.data.utils.TenantUtils;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.entities.modules.Module;
import com.constellio.model.entities.modules.PluginUtil;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import com.constellio.model.services.collections.exceptions.NoMoreCollectionAvalibleException;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.extensions.ConstellioModulesManager;
import com.constellio.model.services.extensions.ConstellioModulesManagerException.ConstellioModulesManagerException_ModuleInstallationFailed;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.users.UserAddUpdateRequest;
import com.constellio.model.services.users.UserServices;
import com.vaadin.server.Page;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static com.constellio.app.ui.i18n.i18n.$;

public class ConstellioSetupPresenter extends BasePresenter<ConstellioSetupView> {

	private static final String TEMP_UNZIP_FOLDER = "ConstellioSetupPresenter-TempUnzipFolder";

	private static final Logger LOGGER = LogManager.getLogger(ConstellioSetupPresenter.class);

	private String setupLocaleCode;

	private boolean loadSaveState;

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

		List<String> localeCodes = factories.getAppLayerConfiguration().getSupportedLanguageCodes();

		List<String> moduleIds = new ArrayList<>();
		for (Module installedModule : installedModules) {
			String moduleId = installedModule.getId();
			moduleIds.add(moduleId);
		}

		view.setLocaleCodes(localeCodes);
		view.setModuleIds(moduleIds);
	}

	public void restart() {
		try {
			if (hasUpdatePermission()) {
				appLayerFactory.newApplicationService().restart();
			} else {
				appLayerFactory.newApplicationService().restartTenant();
			}
		} catch (AppManagementServiceException e) {
			view.showErrorMessage($("UpdateManagerViewImpl.error.restart"));
		}

		Page.getCurrent().setLocation("/constellio/serviceMonitoring");
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return true;
	}

	private boolean hasUpdatePermission() {
		if (TenantUtils.isSupportingTenants()) {
			return Toggle.ENABLE_CLOUD_SYSADMIN_FEATURES.isEnabled();
		}
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

	boolean isLoadSaveState() {
		return loadSaveState;
	}

	public String getSetupLocaleCode() {
		return setupLocaleCode;
	}

	void languageButtonClicked(String localeCode) {
		loadSaveState = false;
		setupLocaleCode = localeCode;
		Locale setupLocale = new Locale(setupLocaleCode);
		i18n.setLocale(setupLocale);
		view.setLocale(setupLocale);
		view.reloadForm();
	}

	void loadSaveStateButtonClicked() {
		loadSaveState = true;
		view.reloadForm();
	}

	public void saveRequested(final List<String> languages, final List<String> modules, final String collectionTitle,
							  final String collectionCode,
							  final String adminPassword, String adminPasswordConfirmation, final boolean demoData)
			throws ConstellioSetupPresenterException {

		validUserEntry(modules, collectionCode, adminPassword, adminPasswordConfirmation);

		view.setSubmitButtonEnabled(false);

		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				CollectionsManager.NEW_SYSTEM_MAIN_DATA_LANGUAGE = setupLocaleCode;
				ConstellioFactories factories = view.getConstellioFactories();
				try {
					setSystemLanguage(setupLocaleCode);
					Record collectionRecord = factories.getAppLayerFactory().getCollectionsManager().createCollectionInCurrentVersion(
							collectionCode, languages);
					Collection collection = new Collection(collectionRecord,
							modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collectionCode));
					String effectiveCollectionTitle;
					if (StringUtils.isBlank(collectionTitle)) {
						effectiveCollectionTitle = collectionCode;
					} else {
						effectiveCollectionTitle = collectionTitle;
					}
					collection.setName(effectiveCollectionTitle).setTitle(effectiveCollectionTitle);
					try {
						recordServices().update(collection);
					} catch (RecordServicesException e) {
						throw new RuntimeException(e);
					}

					ConstellioModulesManager modulesManager = factories.getAppLayerFactory().getModulesManager();

					List<String> roles = new ArrayList<>();
					for (String moduleCode : modules) {
						Module module = modulesManager.getInstalledModule(moduleCode);

						modulesManager.installValidModuleAndGetInvalidOnes(module,
								factories.getModelLayerFactory().getCollectionsListManager());

						modulesManager.enableValidModuleAndGetInvalidOnes(collectionCode, module);

						roles.addAll(PluginUtil.getRolesForCreator(module));
						if (demoData) {
							try {
								((InstallableModule) module).addDemoData(collectionCode, appLayerFactory);
							} catch (Throwable e) {
								LOGGER.error("Error when adding demo data of module " + module.getId() + " in collection " + collection, e);
							}
						}
					}

					ModelLayerFactory modelLayerFactory = factories.getModelLayerFactory();

					UserServices userServices = modelLayerFactory.newUserServices();
					UserAddUpdateRequest adminRequest = userServices.addUpdate("admin")
							.setFirstName("System")
							.setLastName("Admin")
							.setEmail("admin@administration.com")
							.setServiceKey(null)
							.setSystemAdmin(false)
							.addCollection(collectionCode)
							.setStatusForAllCollections(UserCredentialStatus.ACTIVE)
							.setDomain(null)
							.setMsExchDelegateListBL(null)
							.setDn(null);


					userServices.execute(adminRequest);
					userServices.execute("admin", (req) -> req.addCollection(collectionCode));
					User user = userServices.getUserRecordInCollection("admin", collectionCode);
					String effectiveAdminPassword;
					if (StringUtils.isBlank(adminPassword)) {
						effectiveAdminPassword = "password";
					} else {
						effectiveAdminPassword = adminPassword;
					}
					modelLayerFactory.getPasswordFileAuthenticationService().changePassword("admin", effectiveAdminPassword);
					try {
						modelLayerFactory.newRecordServices().update(user.setUserRoles(roles).setCollectionAllAccess(true));
					} catch (RecordServicesException e) {
						throw new RuntimeException(e);
					}

					SessionContext sessionContext = view.getSessionContext();
					UserVO userVO = userToVOBuilder.build(user.getWrappedRecord(), VIEW_MODE.DISPLAY, sessionContext);
					sessionContext.setCurrentCollection(collectionCode);
					sessionContext.setCurrentLocale(new Locale(setupLocaleCode));
					sessionContext.setCurrentUser(userVO);

					view.updateUI();
				} catch (ConstellioModulesManagerException_ModuleInstallationFailed constellioModulesManagerException_moduleInstallationFailed) {
					throw new RuntimeException(constellioModulesManagerException_moduleInstallationFailed);

				} catch (NoMoreCollectionAvalibleException noMoreCollectionAvalible) {
					throw new ImpossibleRuntimeException("To many collection. Should not happen here");

				}
			}
		};
		view.runAsync(runnable);
	}

	public void validUserEntry(List<String> modules, String collectionCode, String adminPassword,
							   String adminPasswordConfirmation)
			throws ConstellioSetupPresenterException_CodeMustBeAlphanumeric, ConstellioSetupPresenterException_MustSelectAtLeastOneModule, ConstellioSetupPresenterException_TasksCannotBeTheOnlySelectedModule, ConstellioSetupPresenterException_AdminConfirmationPasswordNotEqualToAdminPassword {
		if (!isValidCode(collectionCode)) {
			throw new ConstellioSetupPresenterException_CodeMustBeAlphanumeric();
		} else if (modules.isEmpty()) {
			throw new ConstellioSetupPresenterException_MustSelectAtLeastOneModule();
		} else if (modules.size() == 1 && modules.contains("tasks")) {
			throw new ConstellioSetupPresenterException_TasksCannotBeTheOnlySelectedModule();
		} else if ((StringUtils.isNotBlank(adminPassword) || StringUtils.isNotBlank(adminPasswordConfirmation))
				   && !adminPassword.equals(adminPasswordConfirmation)) {
			throw new ConstellioSetupPresenterException_AdminConfirmationPasswordNotEqualToAdminPassword();
		}
		view.showMessage($("ConstellioSetupView.setupInProgress"));
	}

	private boolean isValidCode(String collectionCode) {
		try {
			appLayerFactory.getCollectionsManager().validateCode(collectionCode);
			return true;

		} catch (CollectionsManagerRuntimeException_InvalidCode e) {
			return false;
		}
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

					if (dataLayerConfiguration.getSecondTransactionLogMode() == SecondTransactionLogType.SQL_SERVER) {
						replaySqlLogs(dataLayerConfiguration, bigVaultServer, dataLayerSystemExtensions);
					} else {
						List<File> tLogFiles = getTLogs(tempFolder);
						TransactionLogReadWriteServices readWriteServices = new TransactionLogReadWriteServices(ioServices,
								dataLayerConfiguration, dataLayerSystemExtensions);
						new TransactionLogReplayServices(readWriteServices, bigVaultServer, new DataLayerLogger())
								.replayTransactionLogs(tLogFiles);
					}
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

	private void replaySqlLogs(DataLayerConfiguration dataLayerConfiguration, BigVaultServer bigVaultServer,
							   DataLayerSystemExtensions dataLayerSystemExtensions) throws java.sql.SQLException {
		DataLayerFactory dataLayerFactory = view.getConstellioFactories().getDataLayerFactory();
		for (long i = 0; i < dataLayerFactory.
				getSqlRecordDao().getRecordDao(SqlRecordDaoType.RECORDS).getTableCount(); i = i + 1000) {
			List<RecordTransactionSqlDTO> sqlRecords = dataLayerFactory.
					getSqlRecordDao().getRecordDao(SqlRecordDaoType.RECORDS).getAll(1000, true);
			TransactionLogSqlReadWriteServices readWriteServices = new TransactionLogSqlReadWriteServices(
					dataLayerConfiguration, dataLayerSystemExtensions);
			new SqlTransactionLogReplayServices(readWriteServices, bigVaultServer, new DataLayerLogger())
					.replayTransactionLogs(sqlRecords);
			dataLayerFactory.
					getSqlRecordDao().getRecordDao(SqlRecordDaoType.RECORDS).deleteAll(sqlRecords.stream().map(x -> x.getId()).collect(Collectors.toList()));
			LOGGER.info("Replayed 1000 sql records successfully.");
		}
	}

	private List<File> getTLogs(File tempFolder) {
		File tlogsFolder = new File(new File(tempFolder, "content"), "tlogs");
		return new ArrayList<>(FileUtils.listFiles(tlogsFolder, new String[]{"tlog"}, false));
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
		if (hasUpdatePermission()) {
			appLayerFactory.newApplicationService().restart();
		} else {
			appLayerFactory.newApplicationService().restartTenant();
		}
	}
}
