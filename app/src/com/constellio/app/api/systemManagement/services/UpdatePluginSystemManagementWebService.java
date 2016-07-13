package com.constellio.app.api.systemManagement.services;

import java.io.File;

import javax.servlet.http.HttpServletRequest;

import org.jdom2.Element;

import com.constellio.app.services.extensions.plugins.PluginActivationFailureCause;

public class UpdatePluginSystemManagementWebService extends AdminSystemManagementWebService {

	public static final String TEMP_PLUGIN_FILE = "UpdatePluginSystemManagementWebService-TempPluginFile";

	@Override
	protected void doService(HttpServletRequest req, Element responseDocumentRootElement) {
		String pluginUrl = getRequiredParameter(req, "pluginUrl");

		File tempFile = ioServices().newTemporaryFile(TEMP_PLUGIN_FILE);

		try {
			downloadTo(pluginUrl, tempFile);

			PluginActivationFailureCause failure = appLayerFactory().getPluginManager().prepareInstallablePlugin(tempFile);
			if (failure != null) {
				throw new AdminHttpServletRuntimeException("Plugin '" + pluginUrl + "' failure : " + failure.name());
			}
		} finally {
			ioServices().deleteQuietly(tempFile);
		}
		responseDocumentRootElement.setText("success");
	}

}
