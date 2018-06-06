package com.constellio.app.modules.rm.extensions.api;

import com.constellio.app.extensions.ModuleExtensions;
import com.constellio.app.modules.rm.extensions.api.reports.RMReportBuilderFactories;
import com.constellio.app.services.factories.AppLayerFactory;

import java.util.ArrayList;
import java.util.List;

public class RMModuleExtensions implements ModuleExtensions {

	RMReportBuilderFactories rmReportBuilderFactories;
	
	DecommissioningListFolderTableExtension decommissioningListFolderTableExtension;

	List<DecommissioningListPresenterExtension> decommissioningListPresenterExtensions;

	public RMModuleExtensions(AppLayerFactory appLayerFactory) {
		this.rmReportBuilderFactories = new RMReportBuilderFactories(appLayerFactory);
		this.decommissioningListPresenterExtensions = new ArrayList<>();
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

	public void addDecommissioningListPresenterExtension(DecommissioningListPresenterExtension decommissioningListPresenterExtension) {
		this.decommissioningListPresenterExtensions.add(decommissioningListPresenterExtension);
	}
	
}
