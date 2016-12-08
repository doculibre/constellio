package com.constellio.app.entities.modules;

import static com.constellio.app.ui.i18n.i18n.$;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.commons.lang3.StringUtils;

import com.constellio.app.entities.modules.MigrationResourcesProviderRuntimeException.MigrationResourcesProviderRuntimeException_NoBundle;
import com.constellio.app.entities.modules.locators.ModuleResourcesLocator;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.model.entities.Language;
import com.constellio.model.utils.i18n.Utf8ResourceBundles;

public class MigrationResourcesProvider {

	String module;
	String version;
	Utf8ResourceBundles bundles;
	IOServices ioServices;
	File propertiesFolder;
	Language language;
	List<Language> collectionLanguages;

	public MigrationResourcesProvider(String module, Language language, List<Language> collectionLanguages, String version,
			IOServices ioServices,
			ModuleResourcesLocator moduleResourcesLocator) {
		this.module = module;
		this.version = version;
		this.ioServices = ioServices;
		this.language = language;
		this.collectionLanguages = collectionLanguages;
		this.propertiesFolder = moduleResourcesLocator.getModuleMigrationResourcesFolder(module, version);
		this.bundles = moduleResourcesLocator.getModuleMigrationI18nBundle(module, version);

	}

	public String get(String key) {
		return getDefaultLanguageString(key);
	}

	public String getDefaultLanguageString(String key) {
		ensureBundles();

		Locale locale = language.getLocale();
		ResourceBundle bundle = bundles.getBundle(locale);

		return bundle.containsKey(key) ? bundle.getString(key) : key;
	}

	private void ensureBundles() {
		if (bundles == null) {
			throw new MigrationResourcesProviderRuntimeException_NoBundle(version, module);
		}

	}

	public InputStream getStream(String key) {
		File file = new File(propertiesFolder, key);
		String streamName = "MigrationResourcesProvider-" + module + "-" + version + "-" + key;
		return ioServices.newBufferedFileInputStreamWithoutExpectableFileNotFoundException(file, streamName);
	}

	public File getFile(String key) {
		return new File(propertiesFolder, key);
	}

	public String getString(String key, Locale locale) {
		ensureBundles();
		ResourceBundle bundle = bundles.getBundle(locale);

		return bundle.containsKey(key) ? bundle.getString(key) : key;
	}

	public boolean containsKey(String key) {
		ensureBundles();
		//TODO
		ResourceBundle bundle = bundles.getBundle(language.getLocale());

		return bundle.containsKey(key);
	}

	public boolean containsKey(String key, Locale locale) {
		ensureBundles();
		ResourceBundle bundle = bundles.getBundle(locale);

		return bundle.containsKey(key);
	}

	public Language getLanguage() {
		return language;
	}

	public Map<String, Map<Language, String>> getLanguageMap(List<String> keys) {
		Map<String, Map<Language, String>> languageMap = new HashMap<>();

		for (String key : keys) {
			Map<Language, String> values = new HashMap<>();
			String i18nKey = key;
			if (i18nKey.startsWith("default:")) {
				i18nKey = StringUtils.substringAfter(i18nKey, ":");
			}
			languageMap.put(key, values);
			for (Language collectionLanguage : collectionLanguages) {

				String value = getString(i18nKey, collectionLanguage.getLocale());
				if (i18nKey.equals(value)) {
					value = $(i18nKey, collectionLanguage.getLocale());
				}
				values.put(collectionLanguage, value);

			}
		}

		return languageMap;
	}
}
