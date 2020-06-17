package com.constellio.app.entities.modules.locators;

import com.constellio.data.conf.FoldersLocator;
import com.constellio.model.utils.i18n.Utf8ResourceBundles;

import java.io.File;

import static com.constellio.app.entities.modules.locators.DefaultModuleResourcesLocator.getResourceBundle;

public class ProjectModeModuleResourcesLocator implements ModuleResourcesLocator {

	private FoldersLocator foldersLocator = new FoldersLocator();

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
	public Utf8ResourceBundles getModuleMigrationI18nBundle(String module, String version) {
		String versionWithUnderscores = version.replace(".", "_");
		File folder = getModuleMigrationResourcesFolder(module, version);

		if (folder == null) {
			return null;

		} else if (module == null) {
			return getResourceBundle(folder, "core_" + versionWithUnderscores);

		} else {
			return getResourceBundle(folder, module + "_" + versionWithUnderscores);
		}
	}

	@Override
	public File getModuleMigrationResourcesFolder(String module, String version) {
		String versionWithUnderscores = version.replace(".", "_");
		File pluginResourcesFolder = getPluginResourcesFolder(module);
		if (pluginResourcesFolder != null) {
			File migrationFolder = new File(pluginResourcesFolder, "migrations" + File.separator + versionWithUnderscores);
			return migrationFolder.exists() ? migrationFolder : null;
		}

		FoldersLocator foldersLocator = new FoldersLocator();

		File moduleMigrations;
		if (module == null) {
			moduleMigrations = new File(foldersLocator.getI18nFolder(), "migrations" + File.separator + "core");
		} else {
			moduleMigrations = new File(foldersLocator.getI18nFolder(), "migrations" + File.separator + module);
		}

		return new File(moduleMigrations, versionWithUnderscores);
	}

	@Override
	public File getModuleResourcesFolder(String module) {
		if (module == null) {
			return foldersLocator.getResourcesFolder();

		} else {
			File pluginFolder = getPluginResourcesFolder(module);
			return nullIfInexistent(pluginFolder != null ? pluginFolder : foldersLocator.getModuleResourcesFolder(module));
		}
	}

	private File nullIfInexistent(File file) {
		return file.exists() ? file : null;
	}

	@Override
	public File getModuleI18nFolder(String module) {
		File pluginResourcesFolder = getPluginResourcesFolder(module);
		if (pluginResourcesFolder != null) {
			return nullIfInexistent(new File(pluginResourcesFolder, "i18n"));
		}

		return module == null ? foldersLocator.getI18nFolder() : null;
	}

	@Override
	public File getModuleResource(String module, String resource) {
		return new File(getModuleResourcesFolder(module), removeBadSeparators(resource));
	}

	@Override
	public File getModuleMigrationResource(String module, String version, String resource) {
		return new File(getModuleMigrationResourcesFolder(module, version), removeBadSeparators(resource));
	}

	private File getPluginResourcesFolder(String module) {
		File constellioPlugins = foldersLocator.getPluginsRepository();
		if (constellioPlugins.exists() && constellioPlugins.listFiles() != null) {
			for (File subFolder : constellioPlugins.listFiles()) {
				if (subFolder.getName().startsWith("plugin")) {
					File resourcesFolder = new File(subFolder, "resources" + File.separator + module);
					if (resourcesFolder.exists()) {
						return resourcesFolder;
					}
				}
			}
		}
		return null;
	}

	private String removeBadSeparators(String path) {
		if (File.separator.equals("/")) {
			return path.replace("\\", File.separator);
		} else {
			return path.replace("/", File.separator);
		}
	}

}
