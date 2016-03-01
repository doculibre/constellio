package com.constellio.app.services.recovery;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.managers.config.PropertiesAlteration;

//Not a StatefulService since statefulServices are stopped during recovery mode
public class UpgradeAppRecoveryConfigManager {
	public static final String UPDATE_RECOVERY_CONFIG_PATH = "/updateRecovery.properties";
	private final ConfigManager configManager;
	public static final String LAST_VALID_VERSION_PROPERTY = "lastValidVersion";
	public static final String LAST_VALID_VERSION_DIRECTORY_PATH_PROPERTY = "lastValidVersionDirectoryPath";
	public static final String LAST_VERSION_CAUSING_EXCEPTION_DIRECTORY_PATH_PROPERTY = "previousVersionCausingExceptionDirectoryPath";
	public static final String UPGRADE_EXCEPTION_PROPERTY = "upgradeException";
	public static final String LAST_UPLOADED_VERSION = "lastUploadedVersion";
	public static final String LAST_UPLOADED_VERSION_DIRECTORY_PATH = "lastUploadedVersionDirectoryPath";

	public UpgradeAppRecoveryConfigManager(ConfigManager configManager) {
		this.configManager = configManager;
		configManager.createPropertiesDocumentIfInexistent(UPDATE_RECOVERY_CONFIG_PATH, new PropertiesAlteration() {
			@Override
			public void alter(Map<String, String> properties) {
				//Default values
			}
		});
	}

	public void onVersionUploadedCorrectly(final ConstellioVersionInfo currentInstalledVersion, final ConstellioVersionInfo uploadedVersion) {
		configManager.updateProperties(UPDATE_RECOVERY_CONFIG_PATH, new PropertiesAlteration() {
			@Override
			public void alter(Map<String, String> properties) {
				properties.put(LAST_VALID_VERSION_PROPERTY, currentInstalledVersion.getVersion());
				properties.put(LAST_VALID_VERSION_DIRECTORY_PATH_PROPERTY, currentInstalledVersion.getVersionDirectoryPath());
				properties.put(LAST_UPLOADED_VERSION, uploadedVersion.getVersion());
				properties.put(LAST_UPLOADED_VERSION_DIRECTORY_PATH, uploadedVersion.getVersionDirectoryPath());
				properties.put(LAST_VERSION_CAUSING_EXCEPTION_DIRECTORY_PATH_PROPERTY, "");
				properties.put(UPGRADE_EXCEPTION_PROPERTY, "");
			}
		});
	}

	public void onVersionMigratedCorrectly() {
		configManager.updateProperties(UPDATE_RECOVERY_CONFIG_PATH, new PropertiesAlteration() {
			@Override
			public void alter(Map<String, String> properties) {
				properties.put(LAST_VALID_VERSION_PROPERTY, notNull(properties.get(LAST_UPLOADED_VERSION)));
				properties.put(LAST_VALID_VERSION_DIRECTORY_PATH_PROPERTY, notNull(properties.get(LAST_UPLOADED_VERSION_DIRECTORY_PATH)));
				properties.put(LAST_VERSION_CAUSING_EXCEPTION_DIRECTORY_PATH_PROPERTY, "");
				properties.put(UPGRADE_EXCEPTION_PROPERTY, "");
			}
		});
	}

	public void onVersionMigratedWithException(final Throwable t) {
		configManager.updateProperties(UPDATE_RECOVERY_CONFIG_PATH, new PropertiesAlteration() {
			@Override
			public void alter(Map<String, String> properties) {
				properties.put(LAST_VERSION_CAUSING_EXCEPTION_DIRECTORY_PATH_PROPERTY, notNull(properties.get(LAST_UPLOADED_VERSION_DIRECTORY_PATH)));
				if(t == null){
					properties.put(UPGRADE_EXCEPTION_PROPERTY, "");
				}else{
					properties.put(UPGRADE_EXCEPTION_PROPERTY, ExceptionUtils.getStackTrace(t));
				}
			}
		});
	}

	public String getLastValidVersion() {
		Map<String, String> configs = configManager.getProperties(UPDATE_RECOVERY_CONFIG_PATH).getProperties();
		return notNull(configs.get(LAST_VALID_VERSION_PROPERTY));
	}

	private String notNull(String s) {
		return (StringUtils.isBlank(s)) ? "" : s;
	}

	public String getLastValidVersionDirectoryPath() {
		Map<String, String> configs = configManager.getProperties(UPDATE_RECOVERY_CONFIG_PATH).getProperties();
		return notNull(configs.get(LAST_VALID_VERSION_DIRECTORY_PATH_PROPERTY));
	}

	public String getLastVersionCausingExceptionDirectoryPath() {
		Map<String, String> configs = configManager.getProperties(UPDATE_RECOVERY_CONFIG_PATH).getProperties();
		return notNull(configs.get(LAST_VERSION_CAUSING_EXCEPTION_DIRECTORY_PATH_PROPERTY));
	}

	public String getUpgradeException() {
		Map<String, String> configs = configManager.getProperties(UPDATE_RECOVERY_CONFIG_PATH).getProperties();
		return notNull(configs.get(UPGRADE_EXCEPTION_PROPERTY));
	}

	Map<String, String> getAllProperties() {
		return configManager.getProperties(UPDATE_RECOVERY_CONFIG_PATH).getProperties();
	}

	void replaceAllProperties(final Map<String, String> currentRecoveryProperties) {
		configManager.updateProperties(UPDATE_RECOVERY_CONFIG_PATH, new PropertiesAlteration() {
			@Override
			public void alter(Map<String, String> properties) {
				properties.clear();
				properties.putAll(currentRecoveryProperties);
			}
		});
	}
}
