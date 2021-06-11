package com.constellio.app.entities.modules.locators;

import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.model.utils.i18n.Utf8ResourceBundles;

import java.io.File;

public interface ModuleResourcesLocator {

	Utf8ResourceBundles getModuleI18nBundle(String module);

	Utf8ResourceBundles getModuleMigrationI18nBundle(String module, MigrationScript script);

	File getModuleMigrationResourcesFolder(String module, MigrationScript script);

	File getModuleResourcesFolder(String module);

	File getModuleI18nFolder(String module);

	File getModuleResource(String module, String resource);

	File getModuleMigrationResource(String module, MigrationScript script, String resource);

	File getI18nBundleFile(String module, MigrationScript script);
}
