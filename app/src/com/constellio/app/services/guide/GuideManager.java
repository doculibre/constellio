package com.constellio.app.services.guide;

import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.services.factories.DataLayerFactory;

import java.util.Locale;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;

public class GuideManager implements StatefulService {

	public final static String GUIDE_CONFIG_PATH = "/guideConfig.properties";
	private final ConfigManager configManager;

	public GuideManager(DataLayerFactory dataLayerFactory) {
		this.configManager = dataLayerFactory.getConfigManager();
		initialize();
	}

	@Override
	public void initialize() {
		configManager.createPropertiesDocumentIfInexistent(GUIDE_CONFIG_PATH, ConfigManager.EMPTY_PROPERTY_ALTERATION);
	}

	public void alterProperty(final String language, final String property, final String newValue) {
		String formattedProperty = getPropertyNameForLanguage(property, language);
		if (newValue.equals(getDefaultValue(property, language)) || newValue.isEmpty()) {
			removeProperty(formattedProperty);
		} else {
			addOrUpdateProperty(formattedProperty, formatExternalUrl(newValue));
		}
	}

	private void removeProperty(String property) {
		configManager.updateProperties(GUIDE_CONFIG_PATH, properties -> properties.remove(property));
	}

	private void addOrUpdateProperty(String property, String newValue) {
		configManager.updateProperties(GUIDE_CONFIG_PATH, properties -> {
			if (newValue != null) {
				properties.put(property, newValue);
			}
		});
	}

	private String getDefaultValue(String property, String language) {
		return $(property, new Locale(language));
	}

	public String getPropertyValue(final String language, final String property) {
		String field = getPropertyNameForLanguage(property, language);
		return configManager.getProperties(GUIDE_CONFIG_PATH).getProperties().get(field);
	}


	private String getPropertyNameForLanguage(String property, String language) {
		return property + "_" + language;
	}

	private String formatExternalUrl(String url) {
		String PROTOCOL = "http://";
		String urlRegex = "[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&//=]*)";    //copiée de stackoverflow... a vérifier
		if (url.matches(urlRegex) && !url.startsWith(PROTOCOL)) {
			url = PROTOCOL + url;
		}
		return url;
	}


	public Map<String, String> getAllUrls(final String language) {
		return configManager.getProperties(GUIDE_CONFIG_PATH).getProperties();
	}

	@Override
	public void close() {

	}
}
