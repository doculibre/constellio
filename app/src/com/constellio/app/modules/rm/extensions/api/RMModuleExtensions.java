package com.constellio.app.modules.rm.extensions.api;

import com.constellio.app.extensions.ModuleExtensions;
import com.constellio.app.modules.rm.extensions.api.DocumentExtension.DocumentExtensionAddMenuItemParams;
import com.constellio.app.modules.rm.extensions.api.reports.RMReportBuilderFactories;
import com.constellio.app.services.factories.AppLayerFactory;

import java.util.ArrayList;
import java.util.List;

public class RMModuleExtensions implements ModuleExtensions {

	private RMReportBuilderFactories rmReportBuilderFactories;
	private DecommissioningListFolderTableExtension decommissioningListFolderTableExtension;
	private List<DecommissioningListPresenterExtension> decommissioningListPresenterExtensions;
	private List<DocumentExtension> documentExtensions;

	public RMModuleExtensions(AppLayerFactory appLayerFactory) {
		rmReportBuilderFactories = new RMReportBuilderFactories(appLayerFactory);
		decommissioningListPresenterExtensions = new ArrayList<>();
		documentExtensions = new ArrayList<>();
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

	public List<DecommissioningListPresenterExtension> getDecommissioningListPresenterExtensions() {
		return decommissioningListPresenterExtensions;
	}

	public List<DocumentExtension> getDocumentExtensions() {
		return documentExtensions;
	}

	public void addMenuBarButtons(DocumentExtensionAddMenuItemParams params) {
		for (DocumentExtension documentExtension : documentExtensions) {
			documentExtension.addMenuItems(params);
		}
	}
	
}
