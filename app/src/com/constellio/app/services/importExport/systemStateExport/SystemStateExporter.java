package com.constellio.app.services.importExport.systemStateExport;

import static java.util.Arrays.asList;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.conf.AppLayerConfiguration;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.importExport.systemStateExport.SystemStateExporterRuntimeException.SystemStateExporterRuntimeException_InvalidRecordId;
import com.constellio.app.services.importExport.systemStateExport.SystemStateExporterRuntimeException.SystemStateExporterRuntimeException_RecordHasNoContent;
import com.constellio.data.conf.DataLayerConfiguration;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.dao.services.transactionLog.SecondTransactionLogManager;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.io.services.zip.ZipService;
import com.constellio.data.io.services.zip.ZipServiceException;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesRuntimeException.NoSuchRecordWithId;
import com.constellio.model.services.schemas.MetadataSchemasManager;

public class SystemStateExporter {

	private static final Logger LOGGER = LoggerFactory.getLogger(SystemStateExporter.class);

	public static final String TEMP_FOLDER_RESOURCE_NAME = "SystemStateExporter-tempFolder";

	RecordServices recordServices;

	DataLayerConfiguration dataLayerConfiguration;

	final AppLayerConfiguration appLayerConfiguration;

	ZipService zipService;

	IOServices ioServices;

	MetadataSchemasManager schemasManager;

	SecondTransactionLogManager secondTransactionLogManager;

	public SystemStateExporter(AppLayerFactory appLayerFactory) {
		this.appLayerConfiguration = appLayerFactory.getAppLayerConfiguration();
		ModelLayerFactory modelLayerFactory = appLayerFactory.getModelLayerFactory();
		DataLayerFactory dataLayerFactory = modelLayerFactory.getDataLayerFactory();
		this.dataLayerConfiguration = dataLayerFactory.getDataLayerConfiguration();
		this.zipService = dataLayerFactory.getIOServicesFactory().newZipService();
		this.ioServices = dataLayerFactory.getIOServicesFactory().newIOServices();
		this.secondTransactionLogManager = dataLayerFactory.getSecondTransactionLogManager();
		this.schemasManager = modelLayerFactory.getMetadataSchemasManager();
		this.recordServices = modelLayerFactory.newRecordServices();
	}

	public void exportSystemToFolder(File folder, SystemStateExportParams params) {
		secondTransactionLogManager.regroupAndMoveInVault();
		File tempFolderContentFolder = new File(folder, "content");
		File tempFolderSettingsFolder = new File(folder, "settings");
		File tempPluginsFolder = new File(folder, "plugins");

		copySettingsTo(tempFolderSettingsFolder);

		if (params.isExportAllContent()) {
			copyContentsTo(tempFolderContentFolder);
		} else {
			Set<String> exportedHashes = findExportedHashes(params);
			copyContentsTo(tempFolderContentFolder, exportedHashes);
		}

		copyPluginsJarFolderTo(tempPluginsFolder, params.isExportPluginJars());
	}

	public void exportSystemToFile(File file, SystemStateExportParams params) {
		secondTransactionLogManager.regroupAndMoveInVault();
		File tempFolder = ioServices.newTemporaryFolder(TEMP_FOLDER_RESOURCE_NAME);

		try {
			exportSystemToFolder(tempFolder, params);
			File tempFolderContentFolder = new File(tempFolder, "content");
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

	private Set<String> findExportedHashes(SystemStateExportParams params) {
		Set<String> exportedHashes = new HashSet<>();

		for (String recordId : params.getOnlyExportContentOfRecords()) {

			try {
				Record record = recordServices.getDocumentById(recordId);
				Set<String> recordHashes = getRecordHashes(record);
				exportedHashes.addAll(recordHashes);
				if (recordHashes.isEmpty()) {
					throw new SystemStateExporterRuntimeException_RecordHasNoContent(recordId);
				}

			} catch (NoSuchRecordWithId e) {
				throw new SystemStateExporterRuntimeException_InvalidRecordId(recordId);
			}

		}

		return exportedHashes;
	}

	private Set<String> getRecordHashes(Record record) {
		Set<String> exportedHashes = new HashSet<>();
		MetadataSchema schema = schemasManager.getSchemaTypes(record.getCollection()).getSchema(record.getSchemaCode());
		for (Metadata contentMetadata : schema.getMetadatas().onlyWithType(MetadataValueType.CONTENT)) {
			if (contentMetadata.isMultivalue()) {
				List<Content> contents = record.getList(contentMetadata);
				for (Content content : contents) {
					exportedHashes.addAll(content.getHashOfAllVersions());
				}
			} else {
				Content content = record.get(contentMetadata);
				if (content != null) {
					exportedHashes.addAll(content.getHashOfAllVersions());
				}
			}
		}
		return exportedHashes;
	}

	private void copyContentsTo(File tempFolderContentsFolder, final Set<String> exportedHashes) {
		final File contentsFolder = dataLayerConfiguration.getContentDaoFileSystemFolder();
		final File tlogsFolder = new File(contentsFolder, "tlogs");
		final File tlogsBckFolder = new File(contentsFolder, "tlogs_bck");
		try {
			FileUtils.copyDirectory(contentsFolder, tempFolderContentsFolder, new FileFilter() {
				@Override
				public boolean accept(File pathname) {

					if (pathname.equals(contentsFolder) || pathname.getAbsolutePath().contains(tlogsFolder.getAbsolutePath())) {
						return !pathname.getAbsolutePath().contains(tlogsBckFolder.getAbsolutePath());
					}

					String name;
					if (pathname.getName().contains("_") && pathname.getName().split("_").length > 0) {
						name = pathname.getName().split("_")[0];
					} else {
						name = pathname.getName();
					}

					for (String hash : exportedHashes) {
						if (hash.contains(name)) {
							return true;
						}
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
		File settingsFolder = dataLayerConfiguration.getSettingsFileSystemBaseFolder();
		try {
			FileUtils.copyDirectory(settingsFolder, tempFolderSettingsFolder);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
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