package com.constellio.app.services.importExport.records;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.modules.rm.wrappers.type.DocumentType;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.importExport.records.RecordExportServicesRuntimeException.ExportServicesRuntimeException_NoRecords;
import com.constellio.app.services.importExport.records.writers.ImportRecordOfSameCollectionWriter;
import com.constellio.app.services.importExport.records.writers.ModifiableImportRecord;
import com.constellio.app.services.schemas.bulkImport.data.ImportDataOptions;
import com.constellio.data.dao.services.bigVault.SearchResponseIterator;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.io.services.zip.ZipService;
import com.constellio.data.io.services.zip.ZipServiceException;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

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
			writeRecords(collection, writer, options);
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

	private void writeRecords(String collection, ImportRecordOfSameCollectionWriter writer, RecordExportOptions options) {
		//TODO Jonathan

		writer.setOptions(DocumentType.SCHEMA_TYPE,
				new ImportDataOptions().setMergeExistingRecordWithSameUniqueMetadata(true));

		SearchServices searchServices = modelLayerFactory.newSearchServices();

		LogicalSearchQuery logicalSearchQuery = new LogicalSearchQuery();

		// From type options;

		MetadataSchemasManager metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
		MetadataSchemaTypes metadataSchemaTypes = metadataSchemasManager.getSchemaTypes(collection);

		for (String exportedSchemaType : options.getExportedSchemaTypes()) {

			logicalSearchQuery.setCondition(from(metadataSchemaTypes.getSchemaType(exportedSchemaType)).returnAll());
			SearchResponseIterator<Record> recordSearchResponseIterator = searchServices.recordsIterator(logicalSearchQuery);

			while (recordSearchResponseIterator.hasNext()) {
				Record record = recordSearchResponseIterator.next();

				MetadataSchema metadataSchema = metadataSchemaTypes.getSchema(record.getSchemaCode());

				ModifiableImportRecord modifiableImportRecord = new ModifiableImportRecord(collection, exportedSchemaType,
						record.getId());

				for (Metadata metadata : metadataSchema.getMetadatas()) {
					if (!metadata.isSystemReserved()
							&& metadata.getDataEntry().getType() == DataEntryType.MANUAL
							&& metadata.getType() != MetadataValueType.STRUCTURE) {
						Object object = record.get(metadata);

						if (object != null) {
							modifiableImportRecord.addField(metadata.getLocalCode(), object);
						}
					}
				}

				if (RetentionRule.SCHEMA_TYPE.equals(exportedSchemaType)) {

					RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
					RetentionRule retentionRule = rm.wrapRetentionRule(record);

					List<Map<String, String>> importedCopyRetentionRules = new ArrayList<>();
					for (CopyRetentionRule copyRetentionRule : retentionRule.getCopyRetentionRules()) {
						//copyRetentionRule.etc

						List<String> mediumTypesCodes = new ArrayList<>();
						for (String mediumTypeId : copyRetentionRule.getMediumTypeIds()) {
							mediumTypesCodes.add(rm.getMediumType(mediumTypeId).getCode());
						}

					}
					modifiableImportRecord.addField(RetentionRule.COPY_RETENTION_RULES, importedCopyRetentionRules);

					//TODO
				}

				writer.write(modifiableImportRecord);
			}
		}

/*
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
*/
	}

}
