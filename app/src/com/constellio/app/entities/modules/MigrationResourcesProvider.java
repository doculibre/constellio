package com.constellio.app.entities.modules;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import com.constellio.app.entities.modules.MigrationResourcesProviderRuntimeException.MigrationResourcesProviderRuntimeException_NoBundle;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.conf.FoldersLocatorMode;
import com.constellio.model.entities.Language;
import com.constellio.model.utils.i18n.Utf8ResourceBundles;

public class MigrationResourcesProvider {

	String module;
	String version;
	Utf8ResourceBundles bundles;
	IOServices ioServices;
	File moduleVersionFolder;
	Language language;

	static Map<String, File> moduleMigrationResourcesMap = new HashMap<>();

	public MigrationResourcesProvider(String module, Language language, String version, Locale defaultLocale,
			IOServices ioServices) {
		this.module = module;
		this.version = version;
		this.ioServices = ioServices;
		this.language = language;

		moduleVersionFolder = getModuleVersionFolder(module, version);
		String versionWithUnderscores = version.replace(".", "_");
		String bundleName = module + "_" + versionWithUnderscores;
		File properties = new File(moduleVersionFolder, bundleName + ".properties");
		if (properties.exists()) {
			URL[] urls;
			try {
				urls = new URL[] { moduleVersionFolder.toURI().toURL() };
			} catch (MalformedURLException e) {
				throw new RuntimeException(e);
			}
			bundles = new Utf8ResourceBundles(bundleName, urls);
		}
	}

	private static File getModuleVersionFolder(String module, String version) {

		String key = module + ":" + version;
		File value = moduleMigrationResourcesMap.get(key);

		if (value == null) {
			FoldersLocator foldersLocator = new FoldersLocator();
			String versionWithUnderscores = version.replace(".", "_");

			if (foldersLocator.getFoldersLocatorMode() == FoldersLocatorMode.PROJECT) {
				File constellioPlugins = foldersLocator.getPluginsRepository();
				if (constellioPlugins.exists() && constellioPlugins.listFiles() != null) {
					for (File subFolder : constellioPlugins.listFiles()) {
						if (subFolder.getName().startsWith("plugin")) {
							File resourcesFolder = new File(subFolder, "resources");
							if (resourcesFolder.exists()) {
								File migrationFolder = new File(resourcesFolder, module + File.separator +
										"i18n" + File.separator + "migrations" + File.separator + versionWithUnderscores);

								if (migrationFolder.exists()) {
									value = migrationFolder;
									break;
								}
							}
						}
					}
				}
			}

			if (value == null) {
				File migrations = new File(foldersLocator.getI18nFolder(), "migrations");
				File moduleFolder = new File(migrations, module);
				value = new File(moduleFolder, versionWithUnderscores);
			}
			moduleMigrationResourcesMap.put(key, value);
		}

		return value;

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
		File file = new File(moduleVersionFolder, key);
		String streamName = "MigrationResourcesProvider-" + module + "-" + version + "-" + key;
		return ioServices.newBufferedFileInputStreamWithoutExpectableFileNotFoundException(file, streamName);
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

}
