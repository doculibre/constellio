package com.constellio.app.conf;

import java.io.File;
import java.util.List;

import com.constellio.model.entities.Language;

public interface AppLayerConfiguration {

	void validate();

	File getTempFolder();

	File getPluginsFolder();

	File getPluginsManagementOnStartupFile();

	File getSetupProperties();

	boolean isFastMigrationsEnabled();

	List<Language> getSupportedLanguages();

	List<String> getSupportedLanguageCodes();

}
