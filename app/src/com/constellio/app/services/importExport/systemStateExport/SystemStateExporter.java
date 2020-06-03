package com.constellio.app.services.importExport.systemStateExport;

import com.constellio.app.conf.AppLayerConfiguration;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.conf.DataLayerConfiguration;
import com.constellio.data.dao.services.bigVault.SearchResponseIterator;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.dao.services.transactionLog.SecondTransactionLogManager;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.io.services.zip.ZipService;
import com.constellio.data.io.services.zip.ZipServiceException;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.utils.SavestateFileWriter;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import org.apache.commons.io.FileUtils;
import org.apache.ignite.internal.util.lang.GridFunc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromEveryTypesOfEveryCollection;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.where;
import static com.constellio.model.services.search.query.logical.QueryExecutionMethod.USE_SOLR;
import static java.util.Arrays.asList;

public class SystemStateExporter {

	private static final Logger LOGGER = LoggerFactory.getLogger(SystemStateExporter.class);

	private static final String TEMP_FILE_RESOURCE = "SystemStateExporter-tempFile";
	private static final String TEMP_ZIP_FILE_RESOURCE = "SystemStateExporter-tempZipFile";
	public static final String TEMP_FOLDER_RESOURCE_NAME = "SystemStateExporter-tempFolder";

	RecordServices recordServices;

	DataLayerConfiguration dataLayerConfiguration;

	final AppLayerConfiguration appLayerConfiguration;

	ZipService zipService;

	IOServices ioServices;

	MetadataSchemasManager schemasManager;

	SecondTransactionLogManager secondTransactionLogManager;

	DataLayerFactory dataLayerFactory;
	ModelLayerFactory modelLayerFactory;
	AppLayerFactory appLayerFactory;

	public SystemStateExporter(AppLayerFactory appLayerFactory) {
		this.appLayerConfiguration = appLayerFactory.getAppLayerConfiguration();
		this.modelLayerFactory = appLayerFactory.getModelLayerFactory();
		this.dataLayerFactory = modelLayerFactory.getDataLayerFactory();
		this.dataLayerConfiguration = dataLayerFactory.getDataLayerConfiguration();
		this.zipService = dataLayerFactory.getIOServicesFactory().newZipService();
		this.ioServices = dataLayerFactory.getIOServicesFactory().newIOServices();
		this.secondTransactionLogManager = dataLayerFactory.getSecondTransactionLogManager();
		this.schemasManager = modelLayerFactory.getMetadataSchemasManager();
		this.recordServices = modelLayerFactory.newRecordServices();
		this.appLayerFactory = appLayerFactory;
	}

	public void exportSystemToFolder(File folder, SystemStateExportParams params) {
		secondTransactionLogManager.regroupAndMoveInVault();
		File tempFolderContentFolder = new File(folder, "content");
		File tempFolderSettingsFolder = new File(folder, "settings");

		copySettingsTo(tempFolderSettingsFolder);

		if (params.isExportAllContent()) {

			copyContentsTo(tempFolderContentFolder);
		} else {

			new PartialVaultExporter(tempFolderContentFolder, appLayerFactory)
					.export(params.getOnlyExportContentOfRecords());

			if (params.isUseWeeklyExport()) {
				File tempBaseSavestateZip = new File(folder, "weeklyExport.zip");
			}
			//copyTLogsTo(tempFolderContentFolder);
		}

	}

	public void exportSystemToFile(File file, SystemStateExportParams params) {
		secondTransactionLogManager.regroupAndMoveInVault();
		File tempFolder = ioServices.newTemporaryFolder(TEMP_FOLDER_RESOURCE_NAME);

		try {
			exportSystemToFolder(tempFolder, params);
			File tempFolderContentFolder = new File(tempFolder, "content");
			File tempFolderSettingsFolder = new File(tempFolder, "settings");

			List<File> list = asList(tempFolderContentFolder, tempFolderSettingsFolder);
			try {
				zipService.zip(file, list);
			} catch (ZipServiceException e) {
				throw new RuntimeException(e);
			}

		} finally {
			ioServices.deleteQuietly(tempFolder);
		}

	}

	//	private Set<String> findExportedHashes(SystemStateExportParams params) {
	//		Set<String> exportedHashes = new HashSet<>();
	//
	//		for (String recordId : params.getOnlyExportContentOfRecords()) {
	//
	//			try {
	//				Record record = recordServices.getDocumentById(recordId);
	//
	//				exportedHashes.addAll(recordHashes);
	//				if (recordHashes.isEmpty()) {
	//					throw new SystemStateExporterRuntimeException_RecordHasNoContent(recordId);
	//				}
	//
	//			} catch (NoSuchRecordWithId e) {
	//				throw new SystemStateExporterRuntimeException_InvalidRecordId(recordId);
	//			}
	//
	//		}
	//
	//		return exportedHashes;
	//	}

	//	private Set<String> getRecordHashes(Record record) {
	//		Set<String> exportedHashes = new HashSet<>();
	//		MetadataSchema schema = schemasManager.getSchemaTypes(record.getCollection()).getSchema(record.getSchemaCode());
	//		for (Metadata contentMetadata : schema.getMetadatas().onlyWithType(MetadataValueType.CONTENT)) {
	//			if (contentMetadata.isMultivalue()) {
	//				List<Content> contents = record.getList(contentMetadata);
	//				for (Content content : contents) {
	//					exportedHashes.addAll(content.getHashOfAllVersions());
	//				}
	//			} else {
	//				Content content = record.get(contentMetadata);
	//				if (content != null) {
	//					exportedHashes.addAll(content.getHashOfAllVersions());
	//				}
	//			}
	//		}
	//		return exportedHashes;
	//	}

	private void copyTLogsTo(File tempFolderContentsFolder, LocalDateTime filter) {
		final File contentsFolder = dataLayerConfiguration.getContentDaoFileSystemFolder();
		final File tlogsFolder = new File(contentsFolder, "tlogs");
		final File tlogsBckFolder = new File(contentsFolder, "tlogs_bck");

		File[] tlogsFiles = tlogsFolder.listFiles();
		if (tlogsFiles != null) {
			if (!Toggle.EXPORT_SAVESTATES_USING_WITH_FAILSAFE.isEnabled() && filter != null) {

			}
		}

		try {
			FileUtils.copyDirectory(contentsFolder, tempFolderContentsFolder, new FileFilter() {
				@Override
				public boolean accept(File pathname) {

					if (pathname.equals(contentsFolder) || pathname.getAbsolutePath().contains(tlogsFolder.getAbsolutePath())) {
						return !pathname.getAbsolutePath().contains(tlogsBckFolder.getAbsolutePath());
					}

					return false;
				}
			});
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void copyContentsTo(File tempFolderContentsFolder) {
		File contentsFolder = dataLayerConfiguration.getContentDaoFileSystemFolder();
		try {
			FileUtils.copyDirectory(contentsFolder, tempFolderContentsFolder);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		File tlogBackup = new File(tempFolderContentsFolder, "tlogs_bck");
		if (tlogBackup.exists()) {
			try {
				FileUtils.deleteDirectory(tlogBackup);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private void copySettingsTo(File tempFolderSettingsFolder) {
		dataLayerFactory.getConfigManager().exportTo(tempFolderSettingsFolder);
	}

	public void createSavestateBaseFileInVault() {
		File tempFile = ioServices.newTemporaryFile(TEMP_FILE_RESOURCE);
		File zippedFile = ioServices.newTemporaryFile(TEMP_FILE_RESOURCE);
		try {
			createSavestateBaseFile(tempFile);
			zipService.zip(zippedFile, GridFunc.asList(tempFile));
			modelLayerFactory.getDataLayerFactory().getContentsDao().moveFileToVault(zippedFile, "shared/baseTlog.zip");

		} catch (ZipServiceException e) {
			throw new RuntimeException(e);

		} finally {
			ioServices.deleteQuietly(tempFile);
			ioServices.deleteQuietly(zippedFile);
		}
	}

	public void createSavestateBaseFile(File file) {
		boolean writeZZRecords = modelLayerFactory.getSystemConfigs().isWriteZZRecordsInTlog();

		SavestateFileWriter savestateFileWriter = new SavestateFileWriter(modelLayerFactory, file);

		SearchServices searchServices = modelLayerFactory.newSearchServices();

		int loadedRecordMemoryInBytes = 100_000_000;
		int[] maxRecordSizeSteps = new int[]{1_000, 10_000, 100_000, 1_000_000, 10_000_000, 1_000_000_000};

		for (int maxRecordSize : maxRecordSizeSteps) {
			int batchSize = Math.max(1, loadedRecordMemoryInBytes / maxRecordSize / 2);
			LogicalSearchQuery query = new LogicalSearchQuery();
			query.setQueryExecutionMethod(USE_SOLR);

			LogicalSearchCondition condition =
					fromEveryTypesOfEveryCollection().where(Schemas.ESTIMATED_SIZE).isLessOrEqualThan(maxRecordSize);
			if (maxRecordSize == 1000) {
				condition = condition.orWhere(Schemas.ESTIMATED_SIZE).isNull();
			}

			if (writeZZRecords) {
				query.setCondition(condition);
			} else {

				query.setCondition(LogicalSearchQueryOperators.allConditions(
						condition,
						where(Schemas.IDENTIFIER).isNot(LogicalSearchQueryOperators.startingWithText("ZZ"))
				));
			}

			SearchResponseIterator<List<Record>> recordIterator = searchServices.recordsIterator(query, batchSize).inBatches();

			String taskName = "Exporting records with size smaller than " + maxRecordSize + " bytes  in batch of " + batchSize;
			int counter = 0;
			while (recordIterator.hasNext()) {
				LOGGER.info(taskName + " " + counter + " / " + recordIterator.getNumFound());
				List<Record> records = recordIterator.next();

				savestateFileWriter.write(records);
				counter++;
			}
			LOGGER.info(taskName + " " + counter + " / " + recordIterator.getNumFound());

		}
		savestateFileWriter.close();
	}
}