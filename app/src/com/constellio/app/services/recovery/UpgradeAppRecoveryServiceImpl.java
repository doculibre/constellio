package com.constellio.app.services.recovery;

import com.constellio.app.services.appManagement.AppManagementService;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.systemProperties.SystemPropertiesServices;
import com.constellio.data.conf.DataLayerConfiguration;
import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.services.recovery.TransactionLogRecoveryManager;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static com.constellio.app.services.recovery.UpdateRecoveryImpossibleCause.TOO_SHORT_MEMORY;
import static com.constellio.app.services.recovery.UpdateRecoveryImpossibleCause.TOO_SHORT_SPACE;

public class UpgradeAppRecoveryServiceImpl implements UpgradeAppRecoveryService {
	private final static Logger LOGGER = LoggerFactory.getLogger(UpgradeAppRecoveryServiceImpl.class);
	private static final String WORK_DIR_NAME = UpgradeAppRecoveryServiceImpl.class.getName() + "-settings";
	public static long REQUIRED_MEMORY_IN_MO = 200;
	public static double REQUIRED_SPACE_IN_GIG = 0.5;
	private final TransactionLogRecoveryManager transactionLogRecoveryManager;
	private final AppLayerFactory appLayerFactory;
	private final SystemPropertiesServices systemPropertiesServices;
	private final IOServices ioServices;
	private final File oldSetting;
	private final ConfigManager configManager;
	private final UpgradeAppRecoveryConfigManager upgradeAppRecoveryConfigManager;
	public static boolean HAS_UPLOADED_A_WAR_SINCE_REBOOTING = false;

	public UpgradeAppRecoveryServiceImpl(AppLayerFactory appLayerFactory, IOServices ioServices) {
		this.appLayerFactory = appLayerFactory;
		this.transactionLogRecoveryManager = appLayerFactory.getModelLayerFactory().getDataLayerFactory()
				.getTransactionLogRecoveryManager();
		this.ioServices = ioServices;
		this.oldSetting = ioServices.newTemporaryFolder(WORK_DIR_NAME);
		systemPropertiesServices = new SystemPropertiesServices(appLayerFactory.getModelLayerFactory().getFoldersLocator(),
				ioServices);
		this.configManager = appLayerFactory.getModelLayerFactory().getDataLayerFactory().getConfigManager();
		this.upgradeAppRecoveryConfigManager = new UpgradeAppRecoveryConfigManager(
				appLayerFactory.getModelLayerFactory().getDataLayerFactory().getConfigManager());
	}

	void prepareNextStartup(Throwable exception) {
		this.upgradeAppRecoveryConfigManager.onVersionMigratedWithException(exception);
		SystemConfigurationsManager systemConfigurationsManager = appLayerFactory.getModelLayerFactory()
				.getSystemConfigurationsManager();
		systemConfigurationsManager.setValue(ConstellioEIMConfigs.IN_UPDATE_PROCESS, false);
		HAS_UPLOADED_A_WAR_SINCE_REBOOTING = false;
		pointToPreviousValidVersion();
	}

	public void deletePreviousWarCausingFailure() {
		//FIXME delete all previous war uploaded and not installed :
		// by keeping the list of all uploaded versions (modify UpgradeAppRecoveryConfigManager#onVersionUploadedCorrectly)
		String versionCausingPb = this.upgradeAppRecoveryConfigManager.getLastVersionCausingExceptionDirectoryPath();
		if (StringUtils.isNotBlank(versionCausingPb)) {
			ioServices.deleteQuietly(new File(versionCausingPb));
		}
	}

	@Override
	public void startRollbackMode() {

		// Synchronized since batch process may be running
		synchronized (configManager) {
			transactionLogRecoveryManager.startRollbackMode();
			saveSettings();
		}
	}

	@Override
	public void stopRollbackMode() {
		deleteSavedSettings();
		transactionLogRecoveryManager.stopRollbackMode();
		upgradeAppRecoveryConfigManager.onVersionMigratedCorrectly();
		SystemConfigurationsManager systemConfigurationsManager = appLayerFactory.getModelLayerFactory()
				.getSystemConfigurationsManager();
		systemConfigurationsManager.setValue(ConstellioEIMConfigs.IN_UPDATE_PROCESS, false);
		HAS_UPLOADED_A_WAR_SINCE_REBOOTING = false;
	}

	@Override
	public boolean isInRollbackMode() {
		return transactionLogRecoveryManager.isInRollbackMode();
	}

	public void rollback(Throwable t) {
		closeAppAndModelLayers();
		replaceSettingsByTheSavedOneButKeepRecoverySettings();
		transactionLogRecoveryManager.rollback(t);
		prepareNextStartup(t);
		deleteSavedSettings();
	}

	@Override
	public UpdateRecoveryImpossibleCause isUpdateWithRecoveryPossible() {
		if (this.systemPropertiesServices
				.isFreeSpaceInTempFolderLowerThan(getTransactionLogFileSizeInGig() + REQUIRED_SPACE_IN_GIG)) {
			return TOO_SHORT_SPACE;
		}
		if (this.systemPropertiesServices.isAvailableMemoryLowerThan(REQUIRED_MEMORY_IN_MO)) {
			return TOO_SHORT_MEMORY;
		}
		return null;
	}

	double getTransactionLogFileSizeInGig() {
		DataLayerConfiguration configuration = appLayerFactory.getModelLayerFactory()
				.getDataLayerFactory().getDataLayerConfiguration();
		return this.systemPropertiesServices.getFileSizeInGig(configuration.getSecondTransactionLogBaseFolder());
	}

	@Override
	public void afterWarUpload(ConstellioVersionInfo currentInstalledVersion, ConstellioVersionInfo uploadedVersion) {
		this.upgradeAppRecoveryConfigManager.onVersionUploadedCorrectly(currentInstalledVersion, uploadedVersion);
		SystemConfigurationsManager systemConfigurationsManager = appLayerFactory.getModelLayerFactory()
				.getSystemConfigurationsManager();
		systemConfigurationsManager.setValue(ConstellioEIMConfigs.IN_UPDATE_PROCESS, true);
		HAS_UPLOADED_A_WAR_SINCE_REBOOTING = true;
	}

	@Override
	public String getLastUpgradeExceptionMessage() {
		return this.upgradeAppRecoveryConfigManager.getUpgradeException();
	}

	public void close() {
		this.transactionLogRecoveryManager.close();
	}

	private void deleteSavedSettings() {
		ioServices.deleteQuietly(this.oldSetting);
	}

	private void pointToPreviousValidVersion() {
		String version = this.upgradeAppRecoveryConfigManager.getLastValidVersion();
		String validVersionPath = this.upgradeAppRecoveryConfigManager.getLastValidVersionDirectoryPath();
		AppManagementService appService = appLayerFactory.newApplicationService();
		appService.pointToVersionDuringApplicationStartup(new ConstellioVersionInfo(version, validVersionPath));
	}

	void saveSettings() {
		File settingFolder = getSettingFolder();
		try {
			ioServices.copyDirectory(settingFolder, this.oldSetting);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void closeAppAndModelLayers() {
		appLayerFactory.close(false);
		appLayerFactory.getModelLayerFactory().close(false);
	}

	private void replaceSettingsByTheSavedOneButKeepRecoverySettings() {
		if (this.oldSetting.exists()) {
			Map<String, String> currentRecoveryProperties = this.upgradeAppRecoveryConfigManager.getAllProperties();
			ConfigManager confManager = appLayerFactory.getModelLayerFactory()
					.getDataLayerFactory().getConfigManager();
			confManager.copySettingsFrom(this.oldSetting);
			this.upgradeAppRecoveryConfigManager.replaceAllProperties(currentRecoveryProperties);
		}
	}

	File getSettingFolder() {
		return appLayerFactory.getModelLayerFactory().getDataLayerFactory().getDataLayerConfiguration()
				.getSettingsFileSystemBaseFolder();
	}

}
