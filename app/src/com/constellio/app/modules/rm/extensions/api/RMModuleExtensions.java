package com.constellio.app.modules.rm.extensions.api;

import java.util.ArrayList;
import java.util.List;

import com.constellio.app.extensions.ModuleExtensions;
import com.constellio.app.modules.rm.extensions.api.DocumentExtension.DocumentExtensionAddMenuItemsParams;
import com.constellio.app.modules.rm.extensions.api.reports.RMReportBuilderFactories;
import com.constellio.app.services.factories.AppLayerFactory;

public class RMModuleExtensions implements ModuleExtensions {

	RMReportBuilderFactories rmReportBuilderFactories;

	DecommissioningListFolderTableExtension decommissioningListFolderTableExtension;

	public List<DocumentExtension> documentExtensions = new ArrayList<>();

	public RMModuleExtensions(AppLayerFactory appLayerFactory) {
		this.rmReportBuilderFactories = new RMReportBuilderFactories(appLayerFactory);
	}

	public RMReportBuilderFactories getReportBuilderFactories() {
		return rmReportBuilderFactories;
	}

	public DecommissioningListFolderTableExtension getDecommissioningListFolderTableExtension() {
		return decommissioningListFolderTableExtension;
	}

	public void setDecommissioningListFolderTableExtension(
			DecommissioningListFolderTableExtension decommissioningListFolderTableExtension) {
		this.decommissioningListFolderTableExtension = decommissioningListFolderTableExtension;
	}

	public void addMenuBarButtons(DocumentExtensionAddMenuItemsParams params) {
		for (DocumentExtension documentExtension : documentExtensions) {
			documentExtension.addMenuItems(params);
		}
	}

}
