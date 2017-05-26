package com.constellio.app.ui.pages.imports;

import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.wrappers.*;
import com.constellio.app.services.importExport.records.RecordExportOptions;
import com.constellio.app.services.importExport.records.RecordExportServices;
import com.constellio.app.services.importExport.settings.SettingsExportOptions;
import com.constellio.app.services.importExport.settings.SettingsExportServices;
import com.constellio.app.services.importExport.settings.model.ImportedSettings;
import com.constellio.app.services.importExport.settings.utils.SettingsXMLFileWriter;
import com.constellio.app.services.importExport.systemStateExport.PartialSystemStateExportParams;
import com.constellio.app.services.importExport.systemStateExport.PartialSystemStateExporter;
import com.constellio.app.services.importExport.systemStateExport.SystemStateExportParams;
import com.constellio.app.services.importExport.systemStateExport.SystemStateExporter;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.data.dao.services.bigVault.SearchResponseIterator;
import com.constellio.data.dao.services.idGenerator.ZeroPaddedSequentialUniqueIdGenerator;
import com.constellio.data.io.services.zip.ZipService;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesRuntimeException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.sis.internal.jdk7.StandardCharsets;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.where;
import static java.util.Arrays.asList;

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

	void exportWithoutContentsXMLButtonClicked(boolean isSameCollection, List<String> folderIds, List<String> documentIds) {
		RecordExportOptions options = new RecordExportOptions();
		ArrayList<String> allIds = new ArrayList<>(folderIds);
		allIds.addAll(documentIds);
		options.setForSameSystem(isSameCollection);
		options.setExportedSchemaTypes(asList(Folder.SCHEMA_TYPE, Document.SCHEMA_TYPE));

		SearchResponseIterator<Record> recordsIterator = searchServices().recordsIterator(LogicalSearchQueryOperators.fromAllSchemasIn(collection).whereAnyCondition(
				where(Schemas.IDENTIFIER).isIn(allIds),
				where(Schemas.PATH_PARTS).isIn(folderIds))
		);
		exportToXML(options, recordsIterator);
	}

	void exportSchemasClicked() {
		SettingsExportOptions options = new SettingsExportOptions();
		options.setOnlyUSR(true);

		SettingsExportServices services = new SettingsExportServices(appLayerFactory);
		try {
			ImportedSettings settings = services.exportSettings(asList(collection), options);
			SettingsXMLFileWriter writer = new SettingsXMLFileWriter();
			org.jdom2.Document document = writer.writeSettings(settings);
			XMLOutputter xmlOutput = new XMLOutputter();
			xmlOutput.setFormat(Format.getPrettyFormat());
			String filename = "exportedSchema-" + new SimpleDateFormat("yyyyMMdd").format(new Date()) + ".xml";
			view.startDownload(filename, new ByteArrayInputStream(xmlOutput.outputString(document).getBytes(StandardCharsets.UTF_8)), "application/xml");
		} catch (Exception e) {
			view.showErrorMessage($("ExportView.errorWhileExportingSchemas"));
		}
	}

	void exportAdministrativeUnitXMLButtonClicked(boolean isSameCollection, String unitId) {
		RecordExportOptions options = new RecordExportOptions();
		options.setForSameSystem(isSameCollection);
		options.setExportedSchemaTypes(asList(Folder.SCHEMA_TYPE, Document.SCHEMA_TYPE, ContainerRecord.SCHEMA_TYPE, DecommissioningList.SCHEMA_TYPE));
		String path = (String)((List) recordServices().getDocumentById(unitId).get(Schemas.PATH)).get(0);
		SearchResponseIterator<Record> recordsIterator = searchServices().recordsIterator(LogicalSearchQueryOperators.fromAllSchemasIn(collection).where(Schemas.PATH).isStartingWithText(path));
		exportToXML(options, recordsIterator);
	}

	public void exportToolsToXMLButtonClicked(boolean isSameCollection) {
		RecordExportOptions options = new RecordExportOptions();
		options.setForSameSystem(isSameCollection);
		options.setExportedSchemaTypes(asList(AdministrativeUnit.SCHEMA_TYPE, Category.SCHEMA_TYPE, RetentionRule.SCHEMA_TYPE));
		options.setExportValueLists(true);
		exportToXML(options);
	}

	private void exportToXML(RecordExportOptions options, Iterator<Record> recordsToExport) {
		String filename = "exportedData-" + new SimpleDateFormat("yyyyMMdd").format(new Date()) + ".zip";

		if (appLayerFactory.getSystemGlobalConfigsManager().hasLastReindexingFailed()) {
			view.showErrorMessage($("ExportView.lastReindexingFailed"));

		} else {

			try {
				RecordExportServices recordExportServices = new RecordExportServices(appLayerFactory);
				File zip = recordExportServices.exportRecords(collection, "SDK Stream", options, recordsToExport);
				view.startDownload(filename, new FileInputStream(zip), "application/zip");
			} catch (Throwable t) {
				LOGGER.error("Error while generating savestate", t);
				view.showErrorMessage($("ExportView.error"));
			}
		}
	}

	private void exportToXML(RecordExportOptions options) {
		String filename = "exportedData-" + new SimpleDateFormat("yyyyMMdd").format(new Date()) + ".zip";

		if (appLayerFactory.getSystemGlobalConfigsManager().hasLastReindexingFailed()) {
			view.showErrorMessage($("ExportView.lastReindexingFailed"));

		} else {

			try {
				RecordExportServices recordExportServices = new RecordExportServices(appLayerFactory);
				File zip = recordExportServices.exportRecords(collection, "SDK Stream", options);
				view.startDownload(filename, new FileInputStream(zip), "application/zip");
			} catch (Throwable t) {
				LOGGER.error("Error while generating savestate", t);
				view.showErrorMessage($("ExportView.error"));
			}
		}
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
			if (appLayerFactory.getSystemGlobalConfigsManager().hasLastReindexingFailed()) {
				view.showErrorMessage($("ExportView.lastReindexingFailed"));

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

	public boolean hasCurrentCollectionRMModule() {
		return appLayerFactory.getModulesManager().isModuleEnabled(collection, new ConstellioRMModule());
	}

	public void exportToolsButtonClicked() {
		export(false, true);
	}
}
