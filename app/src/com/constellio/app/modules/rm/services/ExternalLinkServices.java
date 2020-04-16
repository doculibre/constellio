package com.constellio.app.modules.rm.services;

import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.extensions.ExternalLinkServicesExtension;
import com.constellio.app.modules.rm.extensions.ExternalLinkServicesExtension.BeforeExternalLinkImportParams;
import com.constellio.app.modules.rm.extensions.ExternalLinkServicesExtension.ImportExternalLinkParams;
import com.constellio.app.modules.rm.extensions.api.RMModuleExtensions;
import com.constellio.app.services.factories.AppLayerFactory;

public class ExternalLinkServices {
	private RMModuleExtensions rmModuleExtensions;

	public ExternalLinkServices(String collection, AppLayerFactory appLayerFactory) {
		rmModuleExtensions = appLayerFactory.getExtensions().forCollection(collection).forModule(ConstellioRMModule.ID);
	}

	public void beforeExternalLinkImport(String username) {
		if (rmModuleExtensions != null) {
			for (ExternalLinkServicesExtension extension : rmModuleExtensions.getExternalLinkServicesExtensions()) {
				BeforeExternalLinkImportParams params = new BeforeExternalLinkImportParams(username);
				extension.beforeExternalLinkImport(params);
			}
		}
	}

	public void importExternalLink(String externalLinkId, String folderId) throws Exception {
		if (rmModuleExtensions != null) {
			for (ExternalLinkServicesExtension extension : rmModuleExtensions.getExternalLinkServicesExtensions()) {
				ImportExternalLinkParams params = new ImportExternalLinkParams(externalLinkId, folderId);
				extension.importExternalLink(params);
			}
		}
	}
}
