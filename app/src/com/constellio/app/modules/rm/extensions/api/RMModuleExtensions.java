package com.constellio.app.modules.rm.extensions.api;

import java.util.ArrayList;
import java.util.List;

import com.constellio.app.extensions.ModuleExtensions;
import com.constellio.app.modules.rm.extensions.api.DocumentExtension.DocumentExtensionAddMenuItemParams;
import com.constellio.app.modules.rm.extensions.api.DocumentExtension.DocumentExtensionAddMenuItemsParams;
import com.constellio.app.modules.rm.extensions.api.reports.RMReportBuilderFactories;
import com.constellio.app.services.factories.AppLayerFactory;

import java.util.ArrayList;
import java.util.List;

public class RMModuleExtensions implements ModuleExtensions {

	private RMReportBuilderFactories rmReportBuilderFactories;
	private List<DecommissioningBuilderPresenterExtension> decommissioningBuilderPresenterExtensions;
	private DecommissioningListFolderTableExtension decommissioningListFolderTableExtension;
	private List<DecommissioningListPresenterExtension> decommissioningListPresenterExtensions;
	private List<DocumentExtension> documentExtensions;
	private List<FolderExtension> folderExtensions;
	private List<AdvancedSearchPresenterExtension> advancedSearchPresenterExtensions;
	RMReportBuilderFactories rmReportBuilderFactories;

	DecommissioningListFolderTableExtension decommissioningListFolderTableExtension;

	public List<DocumentExtension> documentExtensions = new ArrayList<>();

	public RMModuleExtensions(AppLayerFactory appLayerFactory) {
		rmReportBuilderFactories = new RMReportBuilderFactories(appLayerFactory);
		decommissioningBuilderPresenterExtensions = new ArrayList<>();
		decommissioningListPresenterExtensions = new ArrayList<>();
		documentExtensions = new ArrayList<>();
		folderExtensions = new ArrayList<>();
		advancedSearchPresenterExtensions = new ArrayList<>();
	}

	public RMReportBuilderFactories getReportBuilderFactories() {
		return rmReportBuilderFactories;
	}

	public List<DecommissioningBuilderPresenterExtension> getDecommissioningBuilderPresenterExtensions() {
		return decommissioningBuilderPresenterExtensions;
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

    public List<FolderExtension> getFolderExtensions() {
		return folderExtensions;
    }

    public List<AdvancedSearchPresenterExtension> getAdvancedSearchPresenterExtensions() {
		return advancedSearchPresenterExtensions;
	}

	public void addMenuBarButtons(DocumentExtensionAddMenuItemsParams params) {
		for (DocumentExtension documentExtension : documentExtensions) {
			documentExtension.addMenuItems(params);
		}
	}

}
