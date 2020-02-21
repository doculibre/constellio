package com.constellio.app.services.guide;

import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.managers.config.PropertiesAlteration;
import com.constellio.data.dao.services.factories.DataLayerFactory;

import java.util.Locale;
import java.util.Map;

public class GuideManager implements StatefulService {
	//private final BaseView view;
	private Locale language;
	private DataLayerFactory dataLayerFactory;
	final static String DEFAULT_LANGUAGE = "fr";
	public final static String GUIDE_CONFIG_PATH = "../../../../resources_i18n/guide/";    //todo: temporairement
	private static String FILENAME_PREFIX = "guide";
	private static String FILENAME_EXTENSION = "properties";

	final static String TOKEN_DURATION = "tokenDuration";
	final static int TOKEN_DURATION_VALUE = 30;

	private final ConfigManager configManager;

	public GuideManager(DataLayerFactory dataLayerFactory) {
		this.dataLayerFactory = dataLayerFactory;
		this.configManager = dataLayerFactory.getConfigManager();
		//this.configManager.keepInCache(GUIDE_CONFIG_PATH);
	}

	@Override
	public void initialize() {
	}
		/*configManager.updateProperties(GUIDE_CONFIG_PATH, new PropertiesAlteration() {
			@Override
			public void alter(Map<String, String> properties) {
				properties.put(TOKEN_DURATION, Integer.toString(TOKEN_DURATION_VALUE));
			}
		});*/


	public void alterProperty(final String language, final String property, final String value) {
		String path = GuideManager.GUIDE_CONFIG_PATH + getPropertyFile(language);
		configManager.updateProperties(path, new PropertiesAlteration() {
			@Override
			public void alter(Map<String, String> properties) {
				if (value != null) {
					properties.put(property, formatExternalUrl(value));
				}
			}
		});
	}

	public String getPropertyValue(final String language, final String property) {
		String path = GuideManager.GUIDE_CONFIG_PATH + getPropertyFile(language);
		return configManager.getProperties(path).getProperties().get(property);
	}


	private String formatExternalUrl(String url) {
		String PROTOCOL = "http://";
		String urlRegex = "[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&//=]*)";    //copiée de stackoverflow... a vérifier
		if (url.matches(urlRegex) && !url.startsWith(PROTOCOL)) {
			url = PROTOCOL + url;
		}
		return url;
	}

	private String getPropertyFile(String language) {
		if (language.equals(DEFAULT_LANGUAGE)) {
			return FILENAME_PREFIX + "." + FILENAME_EXTENSION;
		}
		return FILENAME_PREFIX + "_" + language + "." + FILENAME_EXTENSION;
	}

	public Map<String, String> getAllUrls(final String language) {
		String path = GuideManager.GUIDE_CONFIG_PATH + getPropertyFile(language);
		return configManager.getProperties(language).getProperties();
	}

	@Override
	public void close() {

	}
}
