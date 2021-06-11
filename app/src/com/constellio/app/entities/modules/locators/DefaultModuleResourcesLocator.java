package com.constellio.app.entities.modules.locators;

import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.model.utils.i18n.Utf8ResourceBundles;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class DefaultModuleResourcesLocator implements ModuleResourcesLocator {

	private File pluginsResourcesFolder;

	File modulesResources;

	private File i18nFolder;

	public DefaultModuleResourcesLocator(File pluginsResourcesFolder, File modulesResources, File i18nFolder) {
		this.pluginsResourcesFolder = pluginsResourcesFolder;
		this.modulesResources = modulesResources;
		this.i18nFolder = i18nFolder;
	}

	@Override
	public Utf8ResourceBundles getModuleI18nBundle(String module) {
		File folder = getModuleI18nFolder(module);
		if (folder == null) {
			return null;
		} else if (folder.getAbsolutePath().contains(File.separator + module + File.separator)) {
			return getResourceBundle(folder, module + "_i18n");
		} else {
			return getResourceBundle(folder, "i18n");
		}
	}

	@Override
	public Utf8ResourceBundles getModuleMigrationI18nBundle(String module, MigrationScript script) {
		String resources = script.getResourcesDirectoryName();
		File folder = getModuleMigrationResourcesFolder(module, script);

		if (folder == null) {
			return null;

		} else {
			return getResourceBundle(folder, script.getI18nBundleName(module));
		}

	}

	@Override
	public File getModuleMigrationResourcesFolder(String module, MigrationScript script) {
		String resources = script.getResourcesDirectoryName();

		File pluginResourcesFolder = getPluginResourcesFolder(module);
		if (pluginResourcesFolder != null) {
			File migrationFolder = new File(pluginResourcesFolder, "migrations" + File.separator + resources);
			return migrationFolder.exists() ? migrationFolder : null;
		}

		File moduleMigrations;
		if (module == null) {
			moduleMigrations = new File(i18nFolder, "migrations" + File.separator + "core");
		} else {
			moduleMigrations = new File(i18nFolder, "migrations" + File.separator + module);
		}

		return new File(moduleMigrations, resources);
	}

	@Override
	public File getModuleResourcesFolder(String module) {
		File pluginResourcesFolder = getPluginResourcesFolder(module);
		if (pluginResourcesFolder != null) {
			return pluginResourcesFolder;
		}

		if (module == null) {
			return nullIfInexistent(new File(modulesResources, "core"));
		} else {
			return nullIfInexistent(new File(modulesResources, module));
		}
	}

	@Override
	public File getModuleI18nFolder(String module) {
		if (module != null) {
			File pluginResourcesFolder = getPluginResourcesFolder(module);
			if (pluginResourcesFolder != null) {
				return nullIfInexistent(new File(pluginResourcesFolder, "i18n"));
			}

			return null;
		} else {
			return i18nFolder;
		}
	}

	@Override
	public File getModuleResource(String module, String resource) {
		return new File(getModuleResourcesFolder(module), removeBadSeparators(resource));
	}

	@Override
	public File getModuleMigrationResource(String module, MigrationScript script, String resource) {
		return new File(getModuleMigrationResourcesFolder(module, script), removeBadSeparators(resource));
	}

	private File getPluginResourcesFolder(String module) {
		return module == null ? null : nullIfInexistent(new File(pluginsResourcesFolder, module));
	}

	private File nullIfInexistent(File file) {
		return file.exists() ? file : null;
	}

	private String removeBadSeparators(String path) {
		if (File.separator.equals("/")) {
			return path.replace("\\", File.separator);
		} else {
			return path.replace("/", File.separator);
		}
	}

	public static Utf8ResourceBundles getResourceBundle(File folder, String bundleName) {
		File properties = new File(folder, bundleName + ".properties");
		if (properties.exists()) {
			URL[] urls;
			try {
				urls = new URL[]{folder.toURI().toURL()};
			} catch (MalformedURLException e) {
				throw new RuntimeException(e);
			}
			return new Utf8ResourceBundles(bundleName, urls);
		} else {
			return null;
		}
	}

	@Override
	public File getI18nBundleFile(String module, MigrationScript script) {

		File folder = getModuleMigrationResourcesFolder(module, script);

		String bundleName = script.getI18nBundleName(module);
		return new File(folder, bundleName + ".properties");


	}
}
