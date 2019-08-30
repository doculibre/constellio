package com.constellio.app.services.importExport.systemStateExport;

import com.constellio.app.conf.AppLayerConfiguration;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.conf.DataLayerConfiguration;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.dao.services.transactionLog.SecondTransactionLogManager;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.io.services.zip.ZipService;
import com.constellio.data.io.services.zip.ZipServiceException;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;

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

	DataLayerFactory dataLayerFactory;

	AppLayerFactory appLayerFactory;

	public SystemStateExporter(AppLayerFactory appLayerFactory) {
		this.appLayerConfiguration = appLayerFactory.getAppLayerConfiguration();
		ModelLayerFactory modelLayerFactory = appLayerFactory.getModelLayerFactory();
		dataLayerFactory = modelLayerFactory.getDataLayerFactory();
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
		File tempPluginsFolder = new File(folder, "plugins");

		copySettingsTo(tempFolderSettingsFolder);

		if (params.isExportAllContent()) {
			copyContentsTo(tempFolderContentFolder);
		} else {

			new PartialVaultExporter(tempFolderContentFolder, appLayerFactory)
					.export(params.getOnlyExportContentOfRecords());

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