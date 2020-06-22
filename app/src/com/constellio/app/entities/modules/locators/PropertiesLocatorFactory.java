package com.constellio.app.entities.modules.locators;

import com.constellio.data.conf.FoldersLocator;
import com.constellio.data.conf.FoldersLocatorMode;

public class PropertiesLocatorFactory {

	static ProjectModeModuleResourcesLocator projectModePropertiesLocator;

	public static ModuleResourcesLocator get() {
		FoldersLocator foldersLocator = new FoldersLocator();
		if (foldersLocator.getFoldersLocatorMode() == FoldersLocatorMode.PROJECT) {
			if (projectModePropertiesLocator == null) {
				projectModePropertiesLocator = new ProjectModeModuleResourcesLocator();
			}
			return projectModePropertiesLocator;

		} else {
			return new DefaultModuleResourcesLocator(foldersLocator.getPluginsResourcesFolder(),
					foldersLocator.getModulesResourcesFolder(), foldersLocator.getI18nFolder());
		}
	}
}
