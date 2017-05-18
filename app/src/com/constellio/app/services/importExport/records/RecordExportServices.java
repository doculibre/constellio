package com.constellio.app.services.importExport.records;

import com.constellio.app.api.extensions.params.OnWriteRecordParams;
import com.constellio.app.modules.rm.wrappers.structures.Comment;
import com.constellio.app.modules.rm.wrappers.structures.CommentFactory;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.importExport.records.RecordExportServicesRuntimeException.ExportServicesRuntimeException_NoRecords;
import com.constellio.app.services.importExport.records.writers.ImportRecordOfSameCollectionWriter;
import com.constellio.app.services.importExport.records.writers.ModifiableImportRecord;
import com.constellio.app.services.schemas.bulkImport.RecordsImportServicesExecutor;
import com.constellio.app.services.schemas.bulkImport.data.ImportDataOptions;
import com.constellio.data.dao.services.bigVault.SearchResponseIterator;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.io.services.zip.ZipService;
import com.constellio.data.io.services.zip.ZipServiceException;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.*;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.entities.structures.*;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;

public class RecordExportServices {

	public static final String RECORDS_EXPORT_TEMP_FOLDER = "RecordsExportServices_recordsExportTempFolder";

	public static final String RECORDS_EXPORT_TEMP_DDV = "ddv";

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
			try {
				writeRecords(collection, writer, options);
			} finally {
				writer.close();
			}

			File tempZipFile = ioServices.newTemporaryFile(resourceKey, "zip");
			if (tempFolder.listFiles() == null || tempFolder.listFiles().length == 0) {
				throw new ExportServicesRuntimeException_NoRecords();
			}
			zipService.zip(tempZipFile, asList(tempFolder.listFiles()));
			return tempZipFile;

		} catch (ZipServiceException e) {
			throw new RecordExportServicesRuntimeException.ExportServicesRuntimeException_FailedToZip(collection, e);
		}

	}

	private static boolean isSchemaCodePresent(List<String> schemaCodeList, String schemaCode) {
		boolean isSchemaCodePresent = false;

		for (String currentSchemaCode : schemaCodeList) {
			isSchemaCodePresent = schemaCode.equals(currentSchemaCode);
			if (isSchemaCodePresent) {
				break;
			}
		}

		return isSchemaCodePresent;
	}

	private void writeRecordSchema(String collection, ImportRecordOfSameCollectionWriter writer, RecordExportOptions options) {
		SearchServices searchServices = modelLayerFactory.newSearchServices();

		LogicalSearchQuery logicalSearchQuery = new LogicalSearchQuery();
		// From type options;

		MetadataSchemasManager metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
		MetadataSchemaTypes metadataSchemaTypes = metadataSchemasManager.getSchemaTypes(collection);

		List<String> schemaTypeList = new ArrayList<>();

		// Add exportedSchemaType.
		schemaTypeList.addAll(options.getExportedSchemaTypes());

		if (options.isExportValueLists()) {
			mergeExportValueLists(metadataSchemaTypes, schemaTypeList);
		}

		for (String schemaTypeCode : schemaTypeList) {
			writer.setOptions(schemaTypeCode,
					new ImportDataOptions().setMergeExistingRecordWithSameUniqueMetadata(true));
		}

		for (String exportedSchemaType : schemaTypeList) {

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
					else if(metadata.getType() == MetadataValueType.STRUCTURE) {
						StructureFactory structureFactory = metadata.getStructureFactory();
						if(structureFactory.getClass().equals(MapStringListStringStructureFactory.class)) {
                            manageMapStringListStringStructureFactory(record, metadata, modifiableImportRecord);
                        } else if (structureFactory.getClass().equals(MapStringStringStructureFactory.class)) {
                            manageMapStringStringStructureFactory(record, metadata, modifiableImportRecord);
                        } else if (structureFactory.getClass().equals(CommentFactory.class)) {
                            manageCommentFactory(record, metadata, modifiableImportRecord);
                        } else if(structureFactory.getClass().equals(EmailAddressFactory.class)) {
                            manageEmailAddressFactory(record, metadata, modifiableImportRecord);
                        }
					}
				}

				appLayerFactory.getExtensions().forCollection(collection)
						.onWriteRecord(new OnWriteRecordParams(record, modifiableImportRecord));

				writer.write(modifiableImportRecord);
			}
		}
	}

	private void manageMapStringListStringStructureFactory(Record record, Metadata metadata,ModifiableImportRecord modifiableImportRecord) {
		List<MapStringListStringStructure> mapStringListStructureList;
		MapStringListStringStructure mapStringListStructure;

		if(metadata.isMultivalue()) {
			mapStringListStructureList = record.getList(metadata);
			modifiableImportRecord.addField(metadata.getLocalCode(), mapStringListStructureList);
		}
		else {
			mapStringListStructure = record.get(metadata);
			if(mapStringListStructure == null)
			{
				return;
			}
			else {
				modifiableImportRecord.addField(metadata.getLocalCode(),mapStringListStructure);
			}
		}
	}

	private void manageMapStringStringStructureFactory(Record record, Metadata metadata, ModifiableImportRecord modifiableImportRecord) {
		List<MapStringStringStructure> mapStringStringStructureList;
		MapStringStringStructure mapStringStringStructure;

		if(metadata.isMultivalue()) {
			mapStringStringStructureList = record.getList(metadata);
			modifiableImportRecord.addField(metadata.getLocalCode(), mapStringStringStructureList);
		}
		else {
			mapStringStringStructure = record.get(metadata);
			if(mapStringStringStructure == null)
			{
				return;
			}
			else {
				modifiableImportRecord.addField(metadata.getLocalCode(), mapStringStringStructure);
			}
		}
	}

	private void manageCommentFactory(Record record, Metadata metadata,ModifiableImportRecord modifiableImportRecord) {
		List<Comment> commentList;
		Comment comment;
		List<HashMap<String, String>> commentHashMapList = new ArrayList<>();

		if(metadata.isMultivalue()) {
			commentList = record.getList(metadata);

			for(Comment currentComment : commentList)
			{
				commentHashMapList.add(getCommentHasHashMap(currentComment));
			}

			modifiableImportRecord.addField(metadata.getLocalCode(), commentHashMapList);
		}
		else {
			comment = record.get(metadata);
			if(comment == null)
			{
				return;
			}
			else {
				modifiableImportRecord.addField(metadata.getLocalCode(), getCommentHasHashMap(comment));
			}
		}
	}

	// For Comments

	private HashMap<String,String> getCommentHasHashMap(Comment comment)
	{
		HashMap<String,String> commentHasMap = new HashMap<String, String>();

		commentHasMap.put(RecordsImportServicesExecutor.COMMENT_MESSAGE, comment.getMessage());
		commentHasMap.put(RecordsImportServicesExecutor.COMMENT_USER_NAME, comment.getUsername());
		if(comment.getDateTime() != null)
		{
			commentHasMap.put(RecordsImportServicesExecutor.COMMENT_DATE_TIME, comment.getDateTime().toString("yyyy-MM-dd"));
		}

		return commentHasMap;
	}

	private void manageEmailAddressFactory(Record record, Metadata metadata,ModifiableImportRecord modifiableImportRecord) {
		List<EmailAddress> commentList;
		EmailAddress emailAddress;
		List<HashMap<String, String>> commentHashMapList = new ArrayList<>();

		if(metadata.isMultivalue()) {
			commentList = record.getList(metadata);

			for(EmailAddress currentEmailAddress : commentList)
			{
				commentHashMapList.add(getEmailAddressHashMap(currentEmailAddress));
			}

			modifiableImportRecord.addField(metadata.getLocalCode(), commentHashMapList);
		}
		else {
			emailAddress = record.get(metadata);
			if(emailAddress == null)
			{
				return;
			}
			else {
				modifiableImportRecord.addField(metadata.getLocalCode(), getEmailAddressHashMap(emailAddress));
			}
		}
	}



	private HashMap<String,String> getEmailAddressHashMap(EmailAddress emailAddress)
	{
		HashMap<String,String> commentHasMap = new HashMap<String, String>();

		commentHasMap.put(RecordsImportServicesExecutor.EMAIL_ADDRESS_EMAIL, emailAddress.getEmail());
		commentHasMap.put(RecordsImportServicesExecutor.EMAIL_ADDRESS_NAME, emailAddress.getName());

		return commentHasMap;
	}

	private void mergeExportValueLists(MetadataSchemaTypes metadataSchemaTypes, List<String> schemaTypeList) {
		// Code
		// Itération sur les type de schema.
		for (MetadataSchemaType metadata : metadataSchemaTypes.getSchemaTypes()) {
			if (metadata.getCode().toLowerCase().startsWith(RECORDS_EXPORT_TEMP_DDV)) {
				if (!isSchemaCodePresent(schemaTypeList, metadata.getCode())) {
					schemaTypeList.add(metadata.getCode());
				}
			}
		}
	}

	private void writeRecords(String collection, ImportRecordOfSameCollectionWriter writer, RecordExportOptions options) {
		writeRecordSchema(collection, writer, options);
	}

}
