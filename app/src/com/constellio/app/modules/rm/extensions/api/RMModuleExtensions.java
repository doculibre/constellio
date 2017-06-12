package com.constellio.app.modules.rm.extensions.api;

import com.constellio.app.extensions.ModuleExtensions;
import com.constellio.app.modules.rm.extensions.api.reports.RMReportBuilderFactories;
import com.constellio.app.services.factories.AppLayerFactory;

public class RMModuleExtensions implements ModuleExtensions {

	RMReportBuilderFactories rmReportBuilderFactories;
	
	DecommissioningListFolderTableExtension decommissioningListFolderTableExtension;

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
	
}
