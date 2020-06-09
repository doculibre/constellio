package com.constellio.app.services.importExport.systemStateExport;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.importExport.systemStateExport.SystemStateExporterRuntimeException.SystemStateExporterRuntimeException_InvalidRecordId;
import com.constellio.data.dao.services.contents.FileSystemContentDao;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.records.RecordServicesRuntimeException;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;

public class PartialVaultExporter {

	File exportedPartialFolder;

	AppLayerFactory appLayerFactory;

	MetadataSchemasManager metadataSchemasManager;

	File contentDaoBaseFolder;
	FileSystemContentDao contentDao;

	public PartialVaultExporter(File exportedPartialFolder,
								AppLayerFactory appLayerFactory) {
		this.exportedPartialFolder = exportedPartialFolder;
		this.appLayerFactory = appLayerFactory;
		this.metadataSchemasManager = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager();
		this.contentDao = (FileSystemContentDao) appLayerFactory.getModelLayerFactory().getDataLayerFactory().getContentsDao();
		this.contentDaoBaseFolder = appLayerFactory.getModelLayerFactory().getDataLayerFactory()
				.getDataLayerConfiguration().getContentDaoFileSystemFolder();
	}

	public void export(List<String> recordIdsToInclude) {

		Set<String> hashes = new HashSet<>(appLayerFactory.getExtensions().getHashsToIncludeInSystemExport());

		if (recordIdsToInclude != null) {
			for (String id : recordIdsToInclude) {
				try {
					Record record = appLayerFactory.getModelLayerFactory().newRecordServices().getDocumentById(id);

					for (Metadata contentMetadata : metadataSchemasManager.getSchemaTypeOf(record).getAllMetadatas().onlyWithType(MetadataValueType.CONTENT)) {
						for (Content content : record.<Content>getValues(contentMetadata)) {
							hashes.addAll(content.getHashOfAllVersions());
						}
					}
				} catch (RecordServicesRuntimeException.NoSuchRecordWithId e) {
					throw new SystemStateExporterRuntimeException_InvalidRecordId(id);
				}
			}
		}

		for (String hash : hashes) {
			for (String filename : asList(hash, hash + "__parsed", hash + ".preview")) {
				contentDao.readonlyConsumeIfExists(filename, (f) -> {
					File destFile = new File(exportedPartialFolder, contentDao.getLocalRelativePath(filename));
					destFile.getParentFile().mkdirs();
					FileUtils.copyFile(f, destFile);
				});
			}
		}


	}
}
