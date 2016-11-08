package com.constellio.app.ui.pages.imports;

import static com.constellio.app.ui.i18n.i18n.$;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.constellio.app.services.importExport.systemStateExport.SystemStateExportParams;
import com.constellio.app.services.importExport.systemStateExport.SystemStateExporter;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.User;

public class ExportPresenter extends BasePresenter<ExportView> {
	
	private static final Logger LOGGER = Logger.getLogger(ExportPresenter.class); 
	
	public static final String EXPORT_FOLDER_RESOURCE = "ExportPresenterFolder";

	private transient SystemStateExporter exporter;

	public ExportPresenter(ExportView view) {
		super(view);
	}

	public void backButtonPressed() {
		view.navigate().to().adminModule();
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return modelLayerFactory.newUserServices().has(user.getUsername())
				.globalPermissionInAnyCollection(CorePermissions.MANAGE_SYSTEM_DATA_IMPORTS);
	}
	
	void exportWithoutContentsButtonClicked() {
		export(false);
	}
	
	void exportWithContentsButtonClicked() {
		export(true);
	}
	
	private void export(boolean includeContents) {
		try {
			String exportedIdsStr = view.getExportedIds();
			String filename = "systemstate-" + new SimpleDateFormat("yyyyMMdd").format(new Date()) + ".zip";
			File folder = modelLayerFactory.getDataLayerFactory().getIOServicesFactory().newFileService()
					.newTemporaryFolder(EXPORT_FOLDER_RESOURCE);
			File file = new File(folder, filename);
			SystemStateExportParams params = new SystemStateExportParams();
			if (includeContents) {
				params.setExportAllContent();
			} else {
				params.setExportNoContent();
			}
			if (StringUtils.isNotBlank(exportedIdsStr)) {
				params.setOnlyExportContentOfRecords(Arrays.asList(StringUtils.split(exportedIdsStr, ",")));
			}
			exporter().exportSystemToFile(file, params);
			view.startDownload(filename, new FileInputStream(file), "application/zip");
		} catch (Throwable t) {
			LOGGER.error("Error while generating savestate", t);
			view.showErrorMessage($("ExportView.error"));
		}	
	}

	private SystemStateExporter exporter() {
		if (exporter == null) {
			exporter = new SystemStateExporter(appLayerFactory);
		}
		return exporter;
	}
}
