package com.constellio.app.services.recovery;

import static com.constellio.app.services.recovery.UpdateRecoveryImpossibleCause.TOO_SHORT_MEMORY;
import static com.constellio.app.services.recovery.UpdateRecoveryImpossibleCause.TOO_SHORT_SPACE;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.constellio.app.services.appManagement.WrapperConfService;
import com.constellio.app.services.systemProperties.SystemPropertiesServices;
import com.constellio.data.conf.DataLayerConfiguration;
import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.dao.services.recovery.TransactionLogRecoveryManager;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.services.configs.SystemConfigurationIOServices;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;

public class UpgradeAppRecoveryServiceImpl implements UpgradeAppRecoveryService {
	private static final String WORK_DIR_NAME = UpgradeAppRecoveryServiceImpl.class.getName() + "-settings";
	public static long REQUIRED_MEMORY_IN_MO = 200;
	public static double REQUIRED_SPACE_IN_GIG = 0.5;
	private final TransactionLogRecoveryManager transactionLogRecoveryManager;
	private final DataLayerFactory dataLayerFactory;
	private final SystemPropertiesServices systemPropertiesServices;
	private final IOServices ioServices;
	private final File oldSetting;
	private final ConfigManager configManager;
	private final UpgradeAppRecoveryConfigManager upgradeAppRecoveryConfigManager;
	private final SystemConfigurationIOServices systemConfigurationsServices;

	public UpgradeAppRecoveryServiceImpl(DataLayerFactory dataLayerFactory) {
		this.systemConfigurationsServices = new SystemConfigurationIOServices(dataLayerFactory);
		this.dataLayerFactory = dataLayerFactory;
		this.transactionLogRecoveryManager = dataLayerFactory.getTransactionLogRecoveryManager();
		this.ioServices = dataLayerFactory.getIOServicesFactory().newIOServices();
		this.oldSetting = ioServices.newTemporaryFolder(WORK_DIR_NAME);
		systemPropertiesServices = new SystemPropertiesServices(new FoldersLocator(), ioServices);
		this.configManager = dataLayerFactory.getConfigManager();
		this.upgradeAppRecoveryConfigManager = new UpgradeAppRecoveryConfigManager(
				dataLayerFactory.getConfigManager());
	}

	void prepareNextStartup(Throwable exception) {
		this.upgradeAppRecoveryConfigManager.onVersionMigratedWithException(exception);
		systemConfigurationsServices.setValue(ConstellioEIMConfigs.IN_UPDATE_PROCESS, false);
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
		systemConfigurationsServices.setValue(ConstellioEIMConfigs.IN_UPDATE_PROCESS, false);
	}

	@Override
	public boolean isInRollbackMode() {
		return transactionLogRecoveryManager.isInRollbackMode();
	}

	public void rollback(Throwable t) {
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
		DataLayerConfiguration configuration = dataLayerFactory.getDataLayerConfiguration();
		return this.systemPropertiesServices.getFileSizeInGig(configuration.getSecondTransactionLogBaseFolder());
	}

	@Override
	public void afterWarUpload(ConstellioVersionInfo currentInstalledVersion, ConstellioVersionInfo uploadedVersion) {
		this.upgradeAppRecoveryConfigManager.onVersionUploadedCorrectly(currentInstalledVersion, uploadedVersion);
		systemConfigurationsServices.setValue(ConstellioEIMConfigs.IN_UPDATE_PROCESS, true);
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
		String validVersionPath = this.upgradeAppRecoveryConfigManager.getLastValidVersionDirectoryPath();
		new WrapperConfService().updateWrapperConf(new File(validVersionPath));
	}

	void saveSettings() {
		File settingFolder = getSettingFolder();
		try {
			ioServices.copyDirectory(settingFolder, this.oldSetting);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void replaceSettingsByTheSavedOneButKeepRecoverySettings() {
		if (this.oldSetting.exists()) {
			Map<String, String> currentRecoveryProperties = this.upgradeAppRecoveryConfigManager.getAllProperties();
			ConfigManager confManager = dataLayerFactory.getConfigManager();
			confManager.copySettingsFrom(this.oldSetting);
			this.upgradeAppRecoveryConfigManager.replaceAllProperties(currentRecoveryProperties);
		}
	}

	File getSettingFolder() {
		return dataLayerFactory.getDataLayerConfiguration().getSettingsFileSystemBaseFolder();
	}

}
