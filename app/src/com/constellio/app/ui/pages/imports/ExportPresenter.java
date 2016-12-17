package com.constellio.app.ui.pages.imports;

import static com.constellio.app.ui.i18n.i18n.$;
import static java.util.Arrays.asList;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.constellio.app.services.importExport.systemStateExport.PartialSystemStateExportParams;
import com.constellio.app.services.importExport.systemStateExport.PartialSystemStateExporter;
import com.constellio.app.services.importExport.systemStateExport.SystemStateExportParams;
import com.constellio.app.services.importExport.systemStateExport.SystemStateExporter;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.data.dao.services.idGenerator.ZeroPaddedSequentialUniqueIdGenerator;
import com.constellio.data.io.services.zip.ZipService;
import com.constellio.data.io.services.zip.ZipServiceException;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesRuntimeException;
import com.constellio.model.services.search.SearchServices;

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
		export(false, false);
	}

	void exportWithContentsButtonClicked() {
		export(true, false);
	}

	private void export(boolean includeContents, boolean onlyTools) {

		String exportedIdsStr = view.getExportedIds();

		String filename = "systemstate-" + new SimpleDateFormat("yyyyMMdd").format(new Date()) + ".zip";
		File folder = modelLayerFactory.getDataLayerFactory().getIOServicesFactory().newFileService()
				.newTemporaryFolder(EXPORT_FOLDER_RESOURCE);
		File file = new File(folder, filename);

		if (!exportedIdsStr.isEmpty() || onlyTools) {
			List<String> ids = new ArrayList<>();
			PartialSystemStateExportParams params = new PartialSystemStateExportParams();
			if (!onlyTools) {
				ids.addAll(asList(exportedIdsStr.split(",")));

				List<String> verifiedIds = new ArrayList<>();

				RecordServices recordServices = modelLayerFactory.newRecordServices();
				SearchServices searchServices = modelLayerFactory.newSearchServices();

				for (String id : ids) {
					try {
						recordServices.getDocumentById(id);

					} catch (RecordServicesRuntimeException.NoSuchRecordWithId e) {
						id = ZeroPaddedSequentialUniqueIdGenerator.zeroPaddedNumber(Long.valueOf(id));
					}
					verifiedIds.add(id);
				}

				params.setIds(verifiedIds);
			}

			partialExporter().exportSystemToFile(file, params);
			try {
				view.startDownload(filename, new FileInputStream(file), "application/zip");
			} catch (Throwable t) {
				LOGGER.error("Error while generating savestate", t);
				view.showErrorMessage($("ExportView.error"));
			}

		} else {

			try {

				SystemStateExportParams params = new SystemStateExportParams();
				if (includeContents) {
					params.setExportAllContent();
				} else {
					params.setExportNoContent();
				}
				if (StringUtils.isNotBlank(exportedIdsStr)) {
					params.setOnlyExportContentOfRecords(asList(StringUtils.split(exportedIdsStr, ",")));
				}
				exporter().exportSystemToFile(file, params);
				view.startDownload(filename, new FileInputStream(file), "application/zip");
			} catch (Throwable t) {
				LOGGER.error("Error while generating savestate", t);
				view.showErrorMessage($("ExportView.error"));
			}
		}

	}

	private SystemStateExporter exporter() {
		if (exporter == null) {
			exporter = new SystemStateExporter(appLayerFactory);
		}
		return exporter;
	}

	private PartialSystemStateExporter partialExporter() {
		return new PartialSystemStateExporter(appLayerFactory);
	}

	public void exportLogs() {
		ZipService zipService = modelLayerFactory.getIOServicesFactory().newZipService();

		String filename = "logs-" + new SimpleDateFormat("yyyyMMdd").format(new Date()) + ".zip";
		File folder = modelLayerFactory.getDataLayerFactory().getIOServicesFactory().newFileService()
				.newTemporaryFolder(EXPORT_FOLDER_RESOURCE);
		File zipFile = new File(folder, filename);

		List<File> logFiles = new ArrayList<>();

		for (String logFilename : asList("wrapper.log", "constellio.log", "constellio.log.1", "constellio.log.2",
				"constellio.log.3", "constellio.log.4", "constellio.log.5")) {

			File logFile = new File("/opt/constellio/" + logFilename);
			if (logFile.exists()) {
				logFiles.add(logFile);
			}
		}

		if (logFiles.isEmpty()) {
			view.showErrorMessage($("ExportView.noLogs"));
		} else {

			try {
				zipService.zip(zipFile, logFiles);
				view.startDownload(filename, new FileInputStream(zipFile), "application/zip");
			} catch (Throwable t) {
				LOGGER.error("Error while generating zip of logss", t);
				view.showErrorMessage($("ExportView.error"));
			}
		}

	}

	public void exportToolsButtonClicked() {
		export(false, true);
	}
}
