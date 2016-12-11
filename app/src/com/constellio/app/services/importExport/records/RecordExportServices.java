package com.constellio.app.services.importExport.records;

import static com.constellio.app.modules.rm.wrappers.AdministrativeUnit.CODE;
import static com.constellio.app.modules.rm.wrappers.AdministrativeUnit.PARENT;
import static com.constellio.model.entities.records.wrappers.RecordWrapper.TITLE;
import static java.util.Arrays.asList;

import java.io.File;

import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.type.DocumentType;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.importExport.ExportOptions;
import com.constellio.app.services.importExport.records.RecordExportServicesRuntimeException.ExportServicesRuntimeException_NoRecords;
import com.constellio.app.services.importExport.records.writers.ImportRecordOfSameCollectionWriter;
import com.constellio.app.services.importExport.records.writers.ImportRecordWriter;
import com.constellio.app.services.importExport.records.writers.ModifiableImportRecord;
import com.constellio.app.services.schemas.bulkImport.data.ImportDataOptions;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.io.services.zip.ZipService;
import com.constellio.data.io.services.zip.ZipServiceException;
import com.constellio.model.services.factories.ModelLayerFactory;

public class RecordExportServices {

	public static final String RECORDS_EXPORT_TEMP_FOLDER = "RecordsExportServices_recordsExportTempFolder";

	AppLayerFactory appLayerFactory;
	ModelLayerFactory modelLayerFactory;
	ZipService zipService;
	IOServices ioServices;

	public RecordExportServices(AppLayerFactory appLayerFactory) {
		this.appLayerFactory = appLayerFactory;
		this.modelLayerFactory = appLayerFactory.getModelLayerFactory();
		this.zipService = modelLayerFactory.getIOServicesFactory().newZipService();
		this.ioServices = modelLayerFactory.getIOServicesFactory().newIOServices();
	}

	public File exportRecords(String collection, String resourceKey, RecordExportOptions options) {

		File tempFolder = ioServices.newTemporaryFolder(RECORDS_EXPORT_TEMP_FOLDER);
		try {

			ImportRecordOfSameCollectionWriter writer = new ImportRecordOfSameCollectionWriter(tempFolder);
			writeRecords(writer, options);
			writer.close();
			File tempZipFile = ioServices.newTemporaryFile(resourceKey, "zip");
			if (tempFolder.listFiles() == null || tempFolder.listFiles().length == 0) {
				throw new ExportServicesRuntimeException_NoRecords();
			}
			zipService.zip(tempZipFile, asList(tempFolder.listFiles()));
			return tempZipFile;

		} catch (ZipServiceException e) {
			throw new RecordExportServicesRuntimeException.ExportServicesRuntimeException_FailedToZip(collection, e);
		} finally {
			ioServices.deleteQuietly(tempFolder);
		}

	}

	private void writeRecords(ImportRecordOfSameCollectionWriter writer, RecordExportOptions options) {
		//TODO Jonathan

		if (options.isExportValueLists()) {

			//Très import de définir cette ligne pour les domaines de valeurs et taxonomies (espaces virtuels)
			writer.setOptions(DocumentType.SCHEMA_TYPE,
					new ImportDataOptions().setMergeExistingRecordWithSameUniqueMetadata(true));

			//Un exemple de ce qu'il ne faut PAS faire : utiliser directements des classes d'un module dans un service du coeur de l'application
			writer.write(new ModifiableImportRecord("zeCollection", DocumentType.SCHEMA_TYPE, "777")
					.addField(CODE, DocumentType.EMAIL_DOCUMENT_TYPE).addField(TITLE, "Ze email"));
		}

		if (options.isExportTaxonomies()) {

			//Très import de définir cette ligne pour les domaines de valeurs et taxonomies (espaces virtuels)
			writer.setOptions(DocumentType.SCHEMA_TYPE,
					new ImportDataOptions().setMergeExistingRecordWithSameUniqueMetadata(true));

			//Un exemple de ce qu'il ne faut PAS faire : utiliser directements des classes d'un module dans un service du coeur de l'application
			writer.write(new ModifiableImportRecord("zeCollection", AdministrativeUnit.SCHEMA_TYPE, "42")
					.addField(CODE, "10").addField(TITLE, "Unité 10"));

			writer.write(new ModifiableImportRecord("zeCollection", AdministrativeUnit.SCHEMA_TYPE, "666")
					.addField(CODE, "10-A").addField(TITLE, "Unité 10-A").addField(PARENT, "42"));
		}

	}

}
