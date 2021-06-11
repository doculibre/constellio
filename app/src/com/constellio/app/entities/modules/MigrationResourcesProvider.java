package com.constellio.app.entities.modules;

import com.constellio.app.entities.modules.MigrationResourcesProviderRuntimeException.MigrationResourcesProviderRuntimeException_NoBundle;
import com.constellio.app.entities.modules.locators.ModuleResourcesLocator;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.model.entities.Language;
import com.constellio.model.utils.i18n.Utf8ResourceBundles;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import static com.constellio.app.ui.i18n.i18n.$;

public class MigrationResourcesProvider {

	String module;
	MigrationScript script;
	Utf8ResourceBundles bundles;
	IOServices ioServices;
	File propertiesFolder;
	Language language;
	List<Language> collectionLanguages;
	ModuleResourcesLocator moduleResourcesLocator;

	public MigrationResourcesProvider(String module, Language language, List<Language> collectionLanguages,
									  MigrationScript script,
									  IOServices ioServices,
									  ModuleResourcesLocator moduleResourcesLocator) {
		this.module = module;
		this.script = script;
		this.ioServices = ioServices;
		this.language = language;
		this.collectionLanguages = collectionLanguages;
		this.propertiesFolder = moduleResourcesLocator.getModuleMigrationResourcesFolder(module, script);
		this.bundles = moduleResourcesLocator.getModuleMigrationI18nBundle(module, script);
		this.moduleResourcesLocator = moduleResourcesLocator;

	}

	public String getValuesOfAllLanguagesWithSeparator(String key, String separator) {
		StringBuilder sb = new StringBuilder();

		for (Language language : collectionLanguages) {

			if (sb.length() > 0) {
				sb.append(separator);
			}

			sb.append(getString(key, language.getLocale()));
		}

		return sb.toString();
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

	public Map<Language, String> getLanguagesString(String key) {
		ensureBundles();

		Map<Language, String> labels = new HashMap<>();

		for (Language language : collectionLanguages) {
			Locale locale = language.getLocale();
			ResourceBundle bundle = bundles.getBundle(locale);
			String label = key;
			if (bundle.containsKey(key)) {
				label = bundle.getString(key);
			}
			labels.put(language, label);
		}

		return labels;
	}

	private void ensureBundles() {
		if (bundles == null) {
			throw new MigrationResourcesProviderRuntimeException_NoBundle(this.moduleResourcesLocator
					.getI18nBundleFile(module, script ));
		}

	}

	public InputStream getStream(String key) {
		File file = new File(propertiesFolder, key);
		String streamName = "MigrationResourcesProvider-" + script.getClass().getName() + "-" + key;
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
