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
	}

	@Override
	public void initialize() {
		configManager.updateProperties(GUIDE_CONFIG_PATH, new PropertiesAlteration() {
			@Override
			public void alter(Map<String, String> properties) {
				properties.put(TOKEN_DURATION, Integer.toString(TOKEN_DURATION_VALUE));
			}
		});
	}

	public void alterProperty(final String language, final String property, final String value) {
		String path = GuideManager.GUIDE_CONFIG_PATH + getPropertyFile(language);
		String parsedUrl = value;
		configManager.updateProperties(path, new PropertiesAlteration() {
			@Override
			public void alter(Map<String, String> properties) {
				properties.put(property, value);
			}
		});
	}

	/*
	private String fixExternalUrl(String url) {
		String repairedUrl = url;
		String[] splitUrl = url.split(".");
		if (splitUrl.length == 0) {
			return url;
		}
		if(!url.startsWith("http://")){
			repairedUrl = "http://"+url;
		}
		return repairedUrl;
	}*/

	private String getPropertyFile(String language) {
		if (language.equals(DEFAULT_LANGUAGE)) {
			return FILENAME_PREFIX + "." + FILENAME_EXTENSION;
		}
		return FILENAME_PREFIX + "_" + language + "." + FILENAME_EXTENSION;
	}

	@Override
	public void close() {

	}

	public void setUrl() {

		//String guideUrl = view.getGuideUrl();

	}


}
