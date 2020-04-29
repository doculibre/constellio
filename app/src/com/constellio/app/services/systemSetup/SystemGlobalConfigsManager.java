package com.constellio.app.services.systemSetup;

import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.managers.config.PropertiesAlteration;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.dao.services.idGenerator.UUIDV1Generator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class SystemGlobalConfigsManager implements StatefulService {

	private static final Logger LOGGER = LoggerFactory.getLogger(SystemGlobalConfigsManager.class);

	public final static String SYSTEM_GLOBAL_PROPERTIES = "/globalProperties";
	final static String REINDEXING_REQUIRED = "reindexingRequired";
	final static String LAST_REINDEXING_FAILED = "lastReindexingFailed";
	final static String EXPECTED_LOCAL_CACHE_VERSION = "expectedLocalCacheVersion";
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

	public boolean isReindexingRequired() {
		return "true".equals(getGlobalProperties().get(REINDEXING_REQUIRED));
	}

	public void setReindexingRequired(boolean value) {
		//if (value) {
		//Throwable t = new Throwable();
		//LOGGER.info("System was marked for reindexing", t);

		//}
		setProperty(REINDEXING_REQUIRED, value ? "true" : "false");
	}

	public boolean hasLastReindexingFailed() {
		return "true".equals(getGlobalProperties().get(LAST_REINDEXING_FAILED));
	}

	public void setLastReindexingFailed(boolean value) {
		setProperty(LAST_REINDEXING_FAILED, value ? "true" : "false");
	}

	public void markLocalCachesAsRequiringRebuild() {
		//TODO Use an eventbus to enable the flag on all instances
		setProperty(EXPECTED_LOCAL_CACHE_VERSION, UUIDV1Generator.newRandomId());
	}

	public String getExpectedLocalCacheVersion() {
		return getGlobalProperties().get(EXPECTED_LOCAL_CACHE_VERSION);
	}

	private Map<String, String> getGlobalProperties() {
		return configManager.getProperties(SYSTEM_GLOBAL_PROPERTIES).getProperties();
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
