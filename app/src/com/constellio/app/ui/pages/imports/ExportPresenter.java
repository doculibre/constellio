package com.constellio.app.ui.pages.imports;

import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.services.importExport.records.RecordExportOptions;
import com.constellio.app.services.importExport.records.RecordExportServices;
import com.constellio.app.services.importExport.settings.SettingsExportOptions;
import com.constellio.app.services.importExport.settings.SettingsExportServices;
import com.constellio.app.services.importExport.settings.model.ImportedSettings;
import com.constellio.app.services.importExport.settings.utils.SettingsXMLFileWriter;
import com.constellio.app.services.importExport.systemStateExport.CompleteSystemStateExporter;
import com.constellio.app.services.importExport.systemStateExport.PartialSystemStateExportParams;
import com.constellio.app.services.importExport.systemStateExport.PartialSystemStateExporter;
import com.constellio.app.services.importExport.systemStateExport.SystemStateExportParams;
import com.constellio.app.services.importExport.systemStateExport.SystemStateExporter;
import com.constellio.app.ui.framework.buttons.DownloadLink;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.data.dao.dto.records.RecordId;
import com.constellio.data.dao.services.idGenerator.ZeroPaddedSequentialUniqueIdGenerator;
import com.constellio.data.io.services.zip.ZipService;
import com.constellio.data.utils.LazyIterator;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.ExportAudit;
import com.constellio.model.entities.records.wrappers.TemporaryRecord;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.RecordServicesRuntimeException;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.search.SearchServices;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.server.StreamResource;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.joda.time.LocalDateTime;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.records.MergingRecordIterator.merge;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;
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
		export(false);
	}

	void exportWithoutContentsXMLButtonClicked(boolean isSameCollection, String schemaTypeCode,
											   List<String> legacyIds) {
		RecordExportOptions options = new RecordExportOptions();
		options.setForSameSystem(isSameCollection);
		final Metadata legacyIdMetadata = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection)
				.getSchemaType(schemaTypeCode).getDefaultSchema().getMetadata(Schemas.LEGACY_ID.getLocalCode());

		final Iterator<String> idsIterator = legacyIds.iterator();
		final RecordServices recordServices = modelLayerFactory.newRecordServices();


		options.setRecordsToExportIterator(new LazyIterator<Record>() {
			@Override
			protected Record getNextOrNull() {
				if (idsIterator.hasNext()) {
					String id = idsIterator.next();
					return recordServices.getRecordByMetadata(legacyIdMetadata, id.trim());
				} else {
					return null;
				}
			}
		});
		exportToXML(options);


	}

	void exportWithoutContentsXMLButtonClicked(boolean isSameCollection, List<String> folderIds,
											   List<String> documentIds,
											   List<String> containerIds) {
		RecordExportOptions options = new RecordExportOptions();
		options.setForSameSystem(isSameCollection);


		List<String> ids = new ArrayList<>();
		ids.addAll(folderIds);
		ids.addAll(documentIds);
		ids.addAll(containerIds);

		//PATH_PARTS replacement
		List<Iterator<Record>> recordsIterator = new ArrayList<Iterator<Record>>();
		for (String id : ids) {
			recordsIterator.add(searchServices().recordsIterator(fromAllSchemasIn(collection).whereAnyCondition(
					where(Schemas.IDENTIFIER).isEqualTo(id),
					where(Schemas.PRINCIPAL_CONCEPTS_INT_IDS).isEqualTo(RecordId.id(id).intValue()),
					where(Schemas.SECONDARY_CONCEPTS_INT_IDS).isEqualTo(RecordId.id(id).intValue()))
			));
		}

		options.setRecordsToExportIterator(merge(recordsIterator));

		exportToXML(options);
	}

	void exportSchemasClicked() {
		SettingsExportOptions options = new SettingsExportOptions();
		options.setOnlyUSR(true);
		options.setExportingAsCurrentCollection(true);

		SettingsExportServices services = new SettingsExportServices(appLayerFactory);
		try {
			ImportedSettings settings = services.exportSettings(collection, options);
			SettingsXMLFileWriter writer = new SettingsXMLFileWriter();
			final org.jdom2.Document document = writer.writeSettings(settings);
			final XMLOutputter xmlOutput = new XMLOutputter();
			xmlOutput.setFormat(Format.getPrettyFormat());
			String filename = "exportedSchemas-" + new SimpleDateFormat("yyyyMMdd").format(new Date()) + ".xml";
			StreamResource.StreamSource streamSource = new StreamResource.StreamSource() {
				@Override
				public InputStream getStream() {
					return new ByteArrayInputStream(xmlOutput.outputString(document).getBytes(StandardCharsets.UTF_8));
				}
			};
			StreamResource resource = new StreamResource(streamSource, filename);
			resource.setMIMEType("application/xml");
			Resource downloadedResource = DownloadLink.wrapForDownload(resource);
			Page.getCurrent().open(downloadedResource, null, false);
		} catch (Exception e) {
			view.showErrorMessage($("ExportView.errorWhileExportingSchemas"));
			e.printStackTrace();
		}
	}

	void exportCompleteClicked() {
		try {
			String filename = "exportedComplete-" + new SimpleDateFormat("yyyyMMdd").format(new Date()) + ".zip";
			StreamResource.StreamSource streamSource = new StreamResource.StreamSource() {
				@Override
				public InputStream getStream() {
					try {
						CompleteSystemStateExporter exporter = new CompleteSystemStateExporter(appLayerFactory);
						return exporter.exportCompleteSaveState();
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			};
			StreamResource resource = new StreamResource(streamSource, filename);
			resource.setMIMEType("application/zip");
			Resource downloadedResource = DownloadLink.wrapForDownload(resource);
			Page.getCurrent().open(downloadedResource, null, false);
		} catch (Exception e) {
			LOGGER.error(e);
			view.showErrorMessage($("ExportView.errorWhileExportingComplete"));
		}
	}

	void exportAdministrativeUnitXMLButtonClicked(boolean isSameCollection, List<String> unitIds,
												  boolean includeAuthorizations) {
		RecordExportOptions options = new ExportPresenterServices(collection, appLayerFactory)
				.buildOptionsForExportingAdministrativeUnitsAndItsContent(isSameCollection, unitIds, includeAuthorizations);
		exportToXML(options);
	}


	public void exportToolsToXMLButtonClicked(boolean isSameCollection, boolean includeAuthorizations) {
		RecordExportOptions options = new ExportPresenterServices(collection, appLayerFactory)
				.buildOptionsForExportingTools(isSameCollection, includeAuthorizations, appCollectionExtentions);
		exportToXML(options);
	}




	private void exportToXML(RecordExportOptions options) {
		String filename = "exportedData-" + new SimpleDateFormat("yyyyMMdd").format(new Date()) + ".zip";

		ExportAudit newExportAudit = createNewExportAudit();
		File zip = null;
		try {
			RecordExportServices recordExportServices = new RecordExportServices(appLayerFactory);
			zip = recordExportServices.exportRecordsAndZip("SDK Stream", options);
			view.startDownload(filename, new FileInputStream(zip), "application/zip");
		} catch (Throwable t) {
			String error = "Error while generating savestate";
			LOGGER.error(error, t);
			view.showErrorMessage($("ExportView.error"));
		} finally {
			if (zip != null) {
				completeImportExportAudit(newExportAudit, zip);
			}
		}
	}

	private ExportAudit createNewExportAudit() {
		ExportAudit exportAudit = new SchemasRecordsServices(collection, modelLayerFactory).newExportAudit();
		exportAudit.setCreatedOn(LocalDateTime.now());
		exportAudit.setCreatedBy(getCurrentUser().getId());
		try {
			recordServices().add(exportAudit);
		} catch (RecordServicesException e) {
			e.printStackTrace();
		}
		return exportAudit;
	}

	private TemporaryRecord completeImportExportAudit(ExportAudit exportAudit, File file) {

		try {
			ContentManager contentManager = appLayerFactory.getModelLayerFactory().getContentManager();
			ContentVersionDataSummary contentVersionDataSummary = contentManager.upload(file);
			Content content = contentManager.createMajor(getCurrentUser(), file.getName(), contentVersionDataSummary);
			exportAudit.setEndDate(LocalDateTime.now());
			exportAudit.setContent(content);
			recordServices().update(exportAudit);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return exportAudit;
	}

	void exportWithContentsButtonClicked() {
		export(false);
	}

	private void export(boolean onlyTools) {

		String exportedIdsStr = view.getExportedIds();

		String filename = "systemstate-" + new SimpleDateFormat("yyyyMMdd").format(new Date()) + ".zip";
		File folder = modelLayerFactory.getDataLayerFactory().getIOServicesFactory().newFileService()
				.newTemporaryFolder(EXPORT_FOLDER_RESOURCE);
		File file = new File(folder, filename);
		ExportAudit newExportAudit = createNewExportAudit();

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
			} finally {
				completeImportExportAudit(newExportAudit, file);
			}

		} else {
			if (appLayerFactory.getSystemGlobalConfigsManager().hasLastReindexingFailed()) {
				view.showErrorMessage($("ExportView.lastReindexingFailed"));

			} else {

				try {

					ConstellioEIMConfigs constellioEIMConfigs = new ConstellioEIMConfigs(
							modelLayerFactory.getSystemConfigurationsManager());

					SystemStateExportParams params = new SystemStateExportParams();
					if (constellioEIMConfigs.isIncludeContentsInSavestate()) {
						params.setExportAllContent();
					} else {
						params.setExportNoContent();
					}
					if (StringUtils.isNotBlank(exportedIdsStr)) {
						params.setOnlyExportContentOfRecords(asList(StringUtils.split(exportedIdsStr, ",")));
					}
					exporter().exportSystemToFile(file, params);
					view.startDownload(filename, new FileInputStream(file), "application/zip");
					completeImportExportAudit(newExportAudit, file);
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

			File logFile = new File(modelLayerFactory.getFoldersLocator().getWrapperInstallationFolder(), logFilename);
			if (logFile.exists()) {
				logFiles.add(logFile);
			}

		}
		File logsFolder = new File(modelLayerFactory.getFoldersLocator().getWrapperInstallationFolder(), "logs");
		if (logsFolder.exists()) {
			File[] logsFolderFiles = logsFolder.listFiles();
			if (logsFolderFiles != null) {
				logFiles.add(logsFolder);
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
		export(true);
	}
}
