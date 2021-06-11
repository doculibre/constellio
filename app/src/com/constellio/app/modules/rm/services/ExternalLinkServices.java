package com.constellio.app.modules.rm.services;

import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.extensions.ExternalLinkServicesExtension;
import com.constellio.app.modules.rm.extensions.ExternalLinkServicesExtension.BeforeExternalLinkImportParams;
import com.constellio.app.modules.rm.extensions.ExternalLinkServicesExtension.ImportExternalLinkParams;
import com.constellio.app.modules.rm.extensions.ExternalLinkServicesExtension.SetupExternalLinkImportParams;
import com.constellio.app.modules.rm.extensions.api.RMModuleExtensions;
import com.constellio.app.services.factories.AppLayerFactory;

import java.util.List;

public class ExternalLinkServices {
	private RMModuleExtensions rmModuleExtensions;

	public ExternalLinkServices(String collection, AppLayerFactory appLayerFactory) {
		rmModuleExtensions = appLayerFactory.getExtensions().forCollection(collection).forModule(ConstellioRMModule.ID);
	}

	public void setupExternalLinkImport(String currentUsersUsername, String requester) {
		if (rmModuleExtensions != null) {
			for (ExternalLinkServicesExtension extension : rmModuleExtensions.getExternalLinkServicesExtensions()) {
				SetupExternalLinkImportParams params = new SetupExternalLinkImportParams(currentUsersUsername, requester);
				extension.setupExternalLinkImport(params);
			}
		}
	}

	public void beforeExternalLinkImport(List<String> willBeImportedExternalLinks) {
		if (rmModuleExtensions != null) {
			for (ExternalLinkServicesExtension extension : rmModuleExtensions.getExternalLinkServicesExtensions()) {
				BeforeExternalLinkImportParams params = new BeforeExternalLinkImportParams(willBeImportedExternalLinks);
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
