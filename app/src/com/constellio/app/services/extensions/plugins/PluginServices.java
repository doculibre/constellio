package com.constellio.app.services.extensions.plugins;

import com.constellio.app.services.extensions.plugins.pluginInfo.ConstellioPluginInfo;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface PluginServices {

	ConstellioPluginInfo extractPluginInfo(File pluginJar)
			throws InvalidPluginJarException;

	PluginActivationFailureCause validatePlugin(ConstellioPluginInfo pluginInfo,
												ConstellioPluginInfo previousPluginInfo);

	void saveNewPlugin(File pluginsDirectory, File newPluginFile, String pluginCode)
			throws IOException;

	void replaceOldPluginVersionsByNewOnes(File pluginsDirectory, File oldVersionsDestinationDirectory)
			throws PluginsReplacementException;

	File getPluginJar(File pluginsDirectory, String pluginId);

	void extractPluginResources(File jar, String pluginId, File pluginsResources);

	static class PluginsReplacementException extends IOException {
		final List<String> pluginsWithReplacementExceptionIds;

		public PluginsReplacementException(List<String> pluginsWithReplacementExceptionIds) {
			this.pluginsWithReplacementExceptionIds = pluginsWithReplacementExceptionIds;
		}

		public List<String> getPluginsWithReplacementExceptionIds() {
			return pluginsWithReplacementExceptionIds;
		}
	}
}
