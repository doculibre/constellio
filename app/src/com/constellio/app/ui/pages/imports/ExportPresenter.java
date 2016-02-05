package com.constellio.app.ui.pages.imports;

import static com.constellio.app.ui.i18n.i18n.$;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import com.constellio.app.services.importExport.systemStateExport.SystemStateExportParams;
import com.constellio.app.services.importExport.systemStateExport.SystemStateExporter;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.User;

public class ExportPresenter extends BasePresenter<ExportView> {
	public static final String EXPORT_FOLDER_RESOURCE = "ExportPresenterFolder";

	private transient SystemStateExporter exporter;

	public ExportPresenter(ExportView view) {
		super(view);
	}

	public void backButtonPressed() {
		view.navigateTo().adminModule();
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return modelLayerFactory.newUserServices().has(user.getUsername())
				.globalPermissionInAnyCollection(CorePermissions.MANAGE_SYSTEM_DATA_IMPORTS);
	}

	public InputStream buildExportFile(boolean includeContents) {
		File folder = modelLayerFactory.getDataLayerFactory().getIOServicesFactory().newFileService()
				.newTemporaryFolder(EXPORT_FOLDER_RESOURCE);
		File file = new File(folder, "export.zip");
		SystemStateExportParams params = includeContents ?
				new SystemStateExportParams().setExportAllContent() : new SystemStateExportParams().setExportNoContent();
		exporter().exportSystemToFile(file, params);
		try {
			return new FileInputStream(file);
		} catch (FileNotFoundException e) {
			view.showErrorMessage($("ExportView.error"));
			return null;
		}
	}

	private SystemStateExporter exporter() {
		if (exporter == null) {
			exporter = new SystemStateExporter(appLayerFactory);
		}
		return exporter;
	}
}
