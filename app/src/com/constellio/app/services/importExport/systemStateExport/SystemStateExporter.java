package com.constellio.app.services.importExport.systemStateExport;

import com.constellio.app.conf.AppLayerConfiguration;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.conf.DataLayerConfiguration;
import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.services.bigVault.RecordDaoException.NoSuchRecordWithId;
import com.constellio.data.dao.services.bigVault.SearchResponseIterator;
import com.constellio.data.dao.services.contents.ContentDao;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.dao.services.records.RecordDao;
import com.constellio.data.dao.services.transactionLog.SecondTransactionLogManager;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.io.services.zip.ZipService;
import com.constellio.data.io.services.zip.ZipServiceException;
import com.constellio.data.utils.PropertyFileUtils;
import com.constellio.data.utils.TimeProvider;
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
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import static com.constellio.data.utils.PropertyFileUtils.loadKeyValues;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.allConditions;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.anyConditions;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromEveryTypesOfEveryCollection;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.where;
import static com.constellio.model.services.search.query.logical.QueryExecutionMethod.USE_SOLR;
import static java.util.Arrays.asList;

public class SystemStateExporter {

	private static final Logger LOGGER = LoggerFactory.getLogger(SystemStateExporter.class);

	public static final String EXPORT_TIMESTAMP = "timestamp";

	public static final String TEMP_FOLDER_RESOURCE_NAME = "SystemStateExporter-tempFolder";

	private static final String PATH_TO_BASE_FILE = "shared/tlogBaseFile.zip";
	private static final String PATH_TO_BASE_FILE_INFO = "shared/tlog-infos.txt";

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
	RecordDao recordDao;
	ContentDao contentDao;

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
		this.recordDao = appLayerFactory.getModelLayerFactory().getDataLayerFactory().newRecordDao();
		this.contentDao = dataLayerFactory.getContentsDao();
	}

	/**
	 * Return infos related to last export, or an empty map if that action was never done
	 *
	 * @return
	 */
	private Map<String, String> readWeeklyExportInfos() {
		Map<String, String> params = new HashMap<>();
		contentDao.readonlyConsumeIfExists(PATH_TO_BASE_FILE_INFO, (f) -> params.putAll(loadKeyValues(f)));
		return params;
	}

	private void writeWeeklyExportInfos(Map<String, String> map) {
		contentDao.produceAtVaultLocation(PATH_TO_BASE_FILE_INFO, (f) -> {
			PropertyFileUtils.writeMap(f, map);
		});
	}

	private LocalDateTime getLastWeeklyExportBeginningTimeStamp() {
		return null;
		//		Map<String, String> infos = readWeeklyExportInfos();
		//		String lastExport = infos.get(EXPORT_TIMESTAMP);
		//		return lastExport == null ? null : LocalDateTime.parse(lastExport);
	}

	private void markHasLastWeeklyExport(LocalDateTime localDateTime) {
		Map<String, String> infos = readWeeklyExportInfos();
		infos.put(EXPORT_TIMESTAMP, localDateTime.toString());
		writeWeeklyExportInfos(infos);
	}

	public void exportSystemToFolder(File folder, SystemStateExportParams params) {
		secondTransactionLogManager.regroupAndMove();
		File tempFolderContentFolder = new File(folder, "content");
		File tempFolderSettingsFolder = new File(folder, "settings");

		copySettingsTo(tempFolderSettingsFolder);

		if (params.isExportAllContent()) {
			copyContentsExceptTLog(tempFolderContentFolder);

		} else {
			new PartialVaultExporter(tempFolderContentFolder, appLayerFactory)
					.export(params.getOnlyExportContentOfRecords());

		}

		copyTLogsToUsingWeeklyExport(tempFolderContentFolder, params.isUseWeeklyExport());
	}

	public void exportSystemToFile(File file, SystemStateExportParams params) {
		secondTransactionLogManager.regroupAndMove();
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

	private void copyContentsExceptTLog(File tempFolderContentsFolder) {
		final File contentsFolder = dataLayerConfiguration.getContentDaoFileSystemFolder();
		final File tlogsFolder = new File(contentsFolder, "tlogs");
		final File tlogsBckFolder = new File(contentsFolder, "tlogs_bck");
		try {
			FileUtils.copyDirectory(contentsFolder, tempFolderContentsFolder, new FileFilter() {
				@Override
				public boolean accept(File pathname) {

					return pathname.equals(contentsFolder)
						   || (
								   !pathname.getAbsolutePath().contains(tlogsFolder.getAbsolutePath())
								   && !pathname.getAbsolutePath().contains(tlogsBckFolder.getAbsolutePath()));

				}
			});
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void copyTLogsToUsingWeeklyExport(File tempFolderContentsFolder, boolean tryUseBaseFile) {

		LocalDateTime lastFullExportToUse = tryUseBaseFile ? getLastWeeklyExportBeginningTimeStamp() : null;

		final File contentsFolder = dataLayerConfiguration.getContentDaoFileSystemFolder();
		final File tlogsFolder = new File(contentsFolder, "tlogs");
		final File tempFolderContentsTlogsFolder = new File(tempFolderContentsFolder, "tlogs");
		tempFolderContentsTlogsFolder.mkdirs();

		boolean useBaseFile = lastFullExportToUse != null;

		File[] tlogsFiles = tlogsFolder.listFiles();
		if (tlogsFiles != null) {

			if (useBaseFile) {
				try {
					contentDao.readonlyConsume(PATH_TO_BASE_FILE, (f) -> {
						try {
							zipService.unzip(f, tempFolderContentsTlogsFolder);
						} catch (ZipServiceException e) {
							throw new RuntimeException(e);
						}
					});
				} catch (Throwable t) {
					LOGGER.warn("Base export file cannot be used", t);
					lastFullExportToUse = null;
				}

			} else {
				lastFullExportToUse = null;
			}

			for (File tlogFile : tlogsFiles) {
				LocalDateTime fileDateTime = parseTlogFilename(tlogFile.getName());
				if (lastFullExportToUse == null || !fileDateTime.isBefore(lastFullExportToUse)) {
					try {
						FileUtils.copyFile(tlogFile, new File(tempFolderContentsTlogsFolder, tlogFile.getName()));

					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
			}

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

	public void createSavestateBaseFileInVault(boolean includeParsedContent) {
		recordDao.flush();
		final org.joda.time.LocalDateTime startTime = TimeProvider.getLocalDateTime();
		File tempFolder = ioServices.newTemporaryFolder("SystemStateExporter.createSavestateBaseFileInVault.temp");
		String now = TimeProvider.getLocalDateTime().toString().replace(".", "-").replace(":", "-");
		File tempFile = new File(tempFolder, now + ".tlog");
		File zipFile = new File(tempFolder, now + ".zip");
		try {
			createSavestateBaseFile(tempFile, includeParsedContent);
			zipService.zip(zipFile, asList(tempFile));
			modelLayerFactory.getDataLayerFactory().getContentsDao().moveFileToVault(PATH_TO_BASE_FILE, zipFile);
			markHasLastWeeklyExport(startTime);

		} catch (ZipServiceException e) {
			throw new RuntimeException(e);

		} finally {
			ioServices.deleteQuietly(tempFile);
			ioServices.deleteQuietly(zipFile);
			ioServices.deleteDirectoryWithoutExpectableIOException(tempFolder);
		}
	}

	public void createSavestateBaseFile(File file, boolean includeParsedContent) {
		boolean writeZZRecords = modelLayerFactory.getSystemConfigs().isWriteZZRecordsInTlog();

		SavestateFileWriter savestateFileWriter = new SavestateFileWriter(modelLayerFactory, file, includeParsedContent);

		SearchServices searchServices = modelLayerFactory.newSearchServices();

		try {
			List<RecordDTO> dtos = new ArrayList<>();
			dtos.add(recordDao.get("the_private_key"));

			for (Map.Entry<String, Long> entry : dataLayerFactory.getSequencesManager().getSequences().entrySet()) {
				RecordDTO sequence = recordDao.get("seq_" + entry.getKey());
				dtos.add(sequence);
			}
			savestateFileWriter.writeDTOs(dtos);
		} catch (NoSuchRecordWithId noSuchRecordWithId) {
			throw new RuntimeException(noSuchRecordWithId);
		}

		BiConsumer<Integer, Integer> exporterByRange = (min, max) -> {
			int loadedRecordMemoryInBytes = 100_000_000;
			int batchSize = Math.max(1, loadedRecordMemoryInBytes / max / 2);
			LogicalSearchQuery query = new LogicalSearchQuery();
			query.setQueryExecutionMethod(USE_SOLR);

			LogicalSearchCondition condition = fromEveryTypesOfEveryCollection().where(Schemas.COLLECTION).isNotNull();

			if (min == 0) {
				condition = allConditions(
						condition,
						anyConditions(
								where(Schemas.ESTIMATED_SIZE).isLessOrEqualThan(max),
								where(Schemas.ESTIMATED_SIZE).isNull()
						));
			} else {
				condition = condition.andWhere(Schemas.ESTIMATED_SIZE).isLessOrEqualThan(max)
						.andWhere(Schemas.ESTIMATED_SIZE).isGreaterOrEqualThan(min);
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

			String taskName = "Exporting records with size between " + min + "-" + max + " bytes in batch of " + batchSize;
			int counter = 0;
			while (recordIterator.hasNext()) {
				LOGGER.info(taskName + " " + counter + " / " + recordIterator.getNumFound());
				List<Record> records = recordIterator.next();

				savestateFileWriter.write(records);
				counter += records.size();
			}
			if (counter > 0) {
				LOGGER.info(taskName + " " + counter + " / " + recordIterator.getNumFound());
			}
		};

		exporterByRange.accept(0, 1_000);
		exporterByRange.accept(1_000, 10_000);
		exporterByRange.accept(10_000, 100_000);
		exporterByRange.accept(100_000, 1_000_000);
		exporterByRange.accept(1_000_000, 10_000_000);
		exporterByRange.accept(10_000_000, 1_000_000_000);


		savestateFileWriter.close();
	}

	public static LocalDateTime parseTlogFilename(String tlogFilename) {
		String pattern = "yyyy-MM-dd-HH-mm-ss-SSS";
		String nameWithoutExt = tlogFilename.replace(".tlog", "").replace(".zip", "").replace("T", "-");


		return LocalDateTime.parse(nameWithoutExt, DateTimeFormat.forPattern(pattern));
	}

	public void regroupAndMoveInVault() {
		this.secondTransactionLogManager.regroupAndMove();

	}
}