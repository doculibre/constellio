package com.constellio.app.services.systemSetup;

import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.managers.config.PropertiesAlteration;
import com.constellio.data.dao.services.factories.DataLayerFactory;

import java.util.Map;

public class SystemGlobalConfigsManager implements StatefulService {

	public final static String SYSTEM_GLOBAL_PROPERTIES = "/globalProperties";
	final static String MARKED_FOR_REINDEXING = "markedForReindexing";
	final static String MARKED_FOR_CACHE_REBUILD = "markedForCacheRebuild";
	final static String REINDEXING_REQUIRED = "reindexingRequired";
	final static String CACHE_REBUILD_REQUIRED = "cacheRebuildRequired";
	final static String RESTART_REQUIRED = "restartRequired";
	final static String LAST_REINDEXING_FAILED = "lastReindexingFailed";
	final static String FAIL_SAFE_MODE_ENABLED = "failSafeModeEnabled";
	final static String MAIN_DATA_LANGUAGE = "mainLanguage";
	final static String TOKEN_DURATION = "tokenDuration";
	final static String NOTIFICATION_MINUTES = "notificationMinutes";
	final static int TOKEN_DURATION_VALUE = 30;
	final static int NOTIFICATION_MINUTES_VALUE = 60;

	private final ConfigManager configManager;

	public SystemGlobalConfigsManager(DataLayerFactory dataLayerFactory) {
		this.configManager = dataLayerFactory.getConfigManager();
		this.configManager.keepInCache(SYSTEM_GLOBAL_PROPERTIES);
	}

	@Override
	public void initialize() {
		configManager.createPropertiesDocumentIfInexistent(SYSTEM_GLOBAL_PROPERTIES, ConfigManager.EMPTY_PROPERTY_ALTERATION);

		configManager.updateProperties(SYSTEM_GLOBAL_PROPERTIES, new PropertiesAlteration() {
			@Override
			public void alter(Map<String, String> properties) {
				properties.put(TOKEN_DURATION, Integer.toString(TOKEN_DURATION_VALUE));
				properties.put(NOTIFICATION_MINUTES, Integer.toString(NOTIFICATION_MINUTES_VALUE));
			}
		});
	}

	public String getMainDataLanguage() {
		return getGlobalProperties().get(MAIN_DATA_LANGUAGE);
	}

	public int getTokenDuration() {
		return Integer.parseInt(getGlobalProperties().get(TOKEN_DURATION));
	}

	public int getDelayBeforeSendingNotificationEmailsInMinutes() {
		return Integer.parseInt(getGlobalProperties().get(NOTIFICATION_MINUTES));
	}

	public boolean isMarkedForReindexing() {
		return "true".equals(getGlobalProperties().get(MARKED_FOR_REINDEXING));
	}

	public void setMarkedForReindexing(boolean value) {
		setProperty(MARKED_FOR_REINDEXING, value ? "true" : "false");
	}

	public boolean isReindexingRequired() {
		return "true".equals(getGlobalProperties().get(REINDEXING_REQUIRED));
	}

	public void setReindexingRequired(boolean value) {
		setProperty(REINDEXING_REQUIRED, value ? "true" : "false");
	}

	public boolean isCacheRebuildRequired() {
		return "true".equals(getGlobalProperties().get(CACHE_REBUILD_REQUIRED));
	}

	public void setCacheRebuildRequired(boolean value) {
		setProperty(CACHE_REBUILD_REQUIRED, value ? "true" : "false");
	}

	public boolean isMarkedForCacheRebuild() {
		return "true".equals(getGlobalProperties().get(MARKED_FOR_CACHE_REBUILD));
	}

	public void setMarkedForCacheRebuild(boolean value) {
		setProperty(MARKED_FOR_CACHE_REBUILD, value ? "true" : "false");
	}

	public boolean isFailSafeModeEnabled() {
		return "true".equals(getGlobalProperties().get(FAIL_SAFE_MODE_ENABLED));
	}

	public void setFailSafeModeEnabled(boolean value) {
		setProperty(FAIL_SAFE_MODE_ENABLED, value ? "true" : "false");
	}


	public boolean isRestartRequired() {
		return "true".equals(getGlobalProperties().get(RESTART_REQUIRED));
	}

	public void setRestartRequired(boolean value) {
		setProperty(RESTART_REQUIRED, value ? "true" : "false");
	}

	public boolean hasLastReindexingFailed() {
		return "true".equals(getGlobalProperties().get(LAST_REINDEXING_FAILED));
	}

	public void setLastReindexingFailed(boolean value) {
		setProperty(LAST_REINDEXING_FAILED, value ? "true" : "false");
	}

	private Map<String, String> getGlobalProperties() {
		Map<String, String> p = configManager.getProperties(SYSTEM_GLOBAL_PROPERTIES).getProperties();
		return p;
	}

	public void setProperty(final String key, final String value) {
		configManager.updateProperties(SystemGlobalConfigsManager.SYSTEM_GLOBAL_PROPERTIES, new PropertiesAlteration() {
			@Override
			public void alter(Map<String, String> properties) {
				properties.put(key, value);
			}
		});
	}

	@Override
	public void close() {

	}

}
