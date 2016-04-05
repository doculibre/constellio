package com.constellio.app.entities.modules.locators;

import java.io.File;

import com.constellio.model.utils.i18n.Utf8ResourceBundles;

public interface ModuleResourcesLocator {

	Utf8ResourceBundles getModuleI18nBundle(String module);

	Utf8ResourceBundles getModuleMigrationI18nBundle(String module, String version);

	File getModuleMigrationResourcesFolder(String module, String version);

	File getModuleResourcesFolder(String module);

	File getModuleI18nFolder(String module);

	File getModuleResource(String module, String resource);

	File getModuleMigrationResource(String module, String version, String resource);
}
