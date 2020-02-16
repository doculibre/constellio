package com.constellio.app.services.importExport.systemStateExport;

import com.constellio.app.conf.AppLayerConfiguration;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.conf.DataLayerConfiguration;
import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.dto.records.TransactionDTO;
import com.constellio.data.dao.services.bigVault.BigVaultRecordDao;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServerTransaction;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.dao.services.transactionLog.SecondTransactionLogManager;
import com.constellio.data.dao.services.transactionLog.writer1.TransactionWriterV1;
import com.constellio.data.extensions.DataLayerSystemExtensions;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.io.services.zip.ZipService;
import com.constellio.data.io.services.zip.ZipServiceException;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.records.wrappers.UserDocument;
import com.constellio.model.entities.records.wrappers.UserFolder;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.FieldsPopulator;
import com.constellio.model.services.records.RecordImpl;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.search.SearchServices;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static com.constellio.data.dao.dto.records.RecordsFlushing.NOW;
import static com.constellio.model.services.search.query.logical.LogicalSearchQuery.query;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;

public class PartialSystemStateExporter {

	private static final Logger LOGGER = LoggerFactory.getLogger(PartialSystemStateExporter.class);

	public static final String TEMP_FOLDER_RESOURCE_NAME = "SystemStateExporter-tempFolder";

	RecordServices recordServices;

	DataLayerConfiguration dataLayerConfiguration;

	final AppLayerConfiguration appLayerConfiguration;

	ZipService zipService;

	IOServices ioServices;

	MetadataSchemasManager schemasManager;

	ModelLayerFactory modelLayerFactory;

	SecondTransactionLogManager secondTransactionLogManager;

	SearchServices searchServices;

	BigVaultRecordDao recordDao;

	DataLayerFactory dataLayerFactory;

	AppLayerFactory appLayerFactory;

	public PartialSystemStateExporter(AppLayerFactory appLayerFactory) {
		this.appLayerConfiguration = appLayerFactory.getAppLayerConfiguration();
		this.modelLayerFactory = appLayerFactory.getModelLayerFactory();
		this.dataLayerFactory = modelLayerFactory.getDataLayerFactory();
		this.dataLayerConfiguration = dataLayerFactory.getDataLayerConfiguration();
		this.zipService = dataLayerFactory.getIOServicesFactory().newZipService();
		this.ioServices = dataLayerFactory.getIOServicesFactory().newIOServices();
		this.secondTransactionLogManager = dataLayerFactory.getSecondTransactionLogManager();
		this.schemasManager = modelLayerFactory.getMetadataSchemasManager();
		this.recordServices = modelLayerFactory.newRecordServices();
		this.searchServices = modelLayerFactory.newSearchServices();
		recordDao = (BigVaultRecordDao) dataLayerFactory.newRecordDao();
		this.appLayerFactory = appLayerFactory;
	}

	private void exportSystemToFolder(File folder, PartialSystemStateExportParams params) {
		if (secondTransactionLogManager != null) {
			secondTransactionLogManager.regroupAndMove();
		}
		File tempFolderContentFolder = new File(folder, "content");
		final File tlogsFolder = new File(tempFolderContentFolder, "tlogs");
		tlogsFolder.mkdirs();
		generateSaveState(tlogsFolder, params);
		File tempFolderSettingsFolder = new File(folder, "settings");
		File tempPluginsFolder = new File(folder, "plugins");

		copySettingsTo(tempFolderSettingsFolder);

		copyPluginsJarFolderTo(tempPluginsFolder, params.isExportPluginJars());
	}

	private void generateSaveState(File tlogsFolder, PartialSystemStateExportParams params) {

		List<String> filteredSchemaTypes = asList(Folder.SCHEMA_TYPE, Document.SCHEMA_TYPE, Task.SCHEMA_TYPE,
				ContainerRecord.SCHEMA_TYPE, UserDocument.SCHEMA_TYPE, UserFolder.SCHEMA_TYPE, Event.SCHEMA_TYPE);
		List<FieldsPopulator> populators = new ArrayList<>();

		SavestateFileWriter writer = null;
		try {
			writer = new SavestateFileWriter(new File(tlogsFolder, "records.tlog"), filteredSchemaTypes, populators);

			for (String collection : modelLayerFactory.getCollectionsListManager().getCollections()) {
				MetadataSchemaTypes types = schemasManager.getSchemaTypes(collection);
				for (String typeCode : types.getSchemaTypesSortedByDependency()) {
					MetadataSchemaType type = types.getSchemaType(typeCode);
					if (!filteredSchemaTypes.contains(typeCode)) {
						Iterator<List<Record>> it = searchServices.recordsBatchIterator(5000, query(from(type).returnAll()));

						while (it.hasNext()) {
							writer.write(it.next());
						}
					}

				}

			}

			Set<String> ids = new HashSet<>();

			if (params.getIds() != null) {
				List<Record> records = new ArrayList<>();
				for (String id : params.getIds()) {
					String currentId = id;
					while (currentId != null) {
						Record record = recordServices.getDocumentById(currentId);
						String schemaType = new SchemaUtils().getSchemaTypeCode(record.getSchemaCode());

						if (!ids.contains(currentId) && filteredSchemaTypes.contains(schemaType)) {
							records.add(record);
							ids.add(currentId);
							currentId = record.getParentId();
						} else {
							currentId = null;
						}

					}

				}
				writer.write(records);
			}

		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}

	private class SavestateFileWriter {

		TransactionWriterV1 transactionWriter = new TransactionWriterV1(false, new DataLayerSystemExtensions());
		BufferedWriter writer;
		List<String> filteredSchemaTypes;
		List<FieldsPopulator> populators = new ArrayList<>();

		public SavestateFileWriter(File file, List<String> filteredSchemaTypes, List<FieldsPopulator> populators) {
			this.writer = writer;
			this.filteredSchemaTypes = filteredSchemaTypes;
			this.populators = populators;

			try {
				writer = new BufferedWriter(new FileWriter(file));

			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		public void write(List<Record> records) {

			List<RecordDTO> recordDTOs = new ArrayList<>();

			for (Record record : records) {
				MetadataSchema schema = schemasManager.getSchemaTypes(record.getCollection()).getSchema(record.getSchemaCode());
				recordDTOs.add(((RecordImpl) record).toDocumentDTO(schema, populators));
			}

			BigVaultServerTransaction bigVaultServerTransaction = recordDao.prepare(new TransactionDTO(NOW())
					.withFullRewrite(true).withNewRecords(recordDTOs));

			try {
				writer.append(transactionWriter.toLogEntry(bigVaultServerTransaction));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

		}

		public void close() {
			IOUtils.closeQuietly(writer);
		}
	}

	public void exportSystemToFile(File file, PartialSystemStateExportParams params) {
		if (secondTransactionLogManager != null) {
			secondTransactionLogManager.regroupAndMove();
		}
		File tempFolder = ioServices.newTemporaryFolder(TEMP_FOLDER_RESOURCE_NAME);

		try {
			exportSystemToFolder(tempFolder, params);
			File tempFolderContentFolder = new File(tempFolder, "content");

			new PartialVaultExporter(tempFolderContentFolder, appLayerFactory)
					.export(params.getIds());


			File tempFolderSettingsFolder = new File(tempFolder, "settings");

			List<File> list;
			if (params.isExportPluginJars()) {
				File tempPluginsFolder = new File(tempFolder, "plugins");
				if (tempPluginsFolder.exists()) {
					list = asList(tempFolderContentFolder, tempFolderSettingsFolder, tempPluginsFolder);
				} else {
					list = asList(tempFolderContentFolder, tempFolderSettingsFolder);
				}
			} else {
				list = asList(tempFolderContentFolder, tempFolderSettingsFolder);
			}
			try {
				zipService.zip(file, list);
			} catch (ZipServiceException e) {
				throw new RuntimeException(e);
			}

		} finally {
			ioServices.deleteQuietly(tempFolder);
		}

	}

	private void copySettingsTo(File tempFolderSettingsFolder) {

		dataLayerFactory.getConfigManager().exportTo(tempFolderSettingsFolder);
	}

	private void copyPluginsJarFolderTo(File tempPluginsFolder, boolean exportJars) {
		File pluginsFolder = appLayerConfiguration.getPluginsFolder();
		if (exportJars && pluginsFolder.exists()) {
			try {
				FileUtils.copyDirectory(pluginsFolder, tempPluginsFolder);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

}