package com.constellio.app.conf;

import java.io.File;

public interface AppLayerConfiguration {

	void validate();

	File getTempFolder();

	File getPluginsFolder();

	File getPluginsManagementOnStartupFile();

	File getSetupProperties();

}
