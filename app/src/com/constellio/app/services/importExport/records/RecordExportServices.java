package com.constellio.app.services.importExport.records;

import com.constellio.app.api.extensions.params.OnWriteRecordParams;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.RMObject;
import com.constellio.app.modules.rm.wrappers.structures.Comment;
import com.constellio.app.modules.rm.wrappers.structures.CommentFactory;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.importExport.records.RecordExportServicesRuntimeException.ExportServicesRuntimeException_NoRecords;
import com.constellio.app.services.importExport.records.writers.ImportRecordOfSameCollectionWriter;
import com.constellio.app.services.importExport.records.writers.ModifiableImportRecord;
import com.constellio.app.services.schemas.bulkImport.RecordsImportServicesExecutor;
import com.constellio.app.services.schemas.bulkImport.data.ImportDataOptions;
import com.constellio.data.conf.ContentDaoType;
import com.constellio.data.conf.DataLayerConfiguration;
import com.constellio.data.dao.services.contents.FileSystemContentDao;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.io.services.zip.ZipService;
import com.constellio.data.io.services.zip.ZipServiceException;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.ContentVersion;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.schemas.StructureFactory;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.entities.structures.EmailAddress;
import com.constellio.model.entities.structures.EmailAddressFactory;
import com.constellio.model.entities.structures.MapStringListStringStructure;
import com.constellio.model.entities.structures.MapStringListStringStructureFactory;
import com.constellio.model.entities.structures.MapStringStringStructure;
import com.constellio.model.entities.structures.MapStringStringStructureFactory;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;

public class RecordExportServices {

	public static final String RECORDS_EXPORT_TEMP_FOLDER = "RecordsExportServices_recordsExportTempFolder";

	AppLayerFactory appLayerFactory;
	ModelLayerFactory modelLayerFactory;
	ZipService zipService;
	IOServices ioServices;
	RecordServices recordServices;

	public RecordExportServices(AppLayerFactory appLayerFactory) {
		this.appLayerFactory = appLayerFactory;
		this.modelLayerFactory = appLayerFactory.getModelLayerFactory();
		this.zipService = modelLayerFactory.getIOServicesFactory().newZipService();
		this.ioServices = modelLayerFactory.getIOServicesFactory().newIOServices();
		this.recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
	}

	public File exportRecords(String resourceKey, RecordExportOptions options) {

		File tempFolder = ioServices.newTemporaryFolder(RECORDS_EXPORT_TEMP_FOLDER);
		ValidationErrors errors = new ValidationErrors();

		try {

			//
			ImportRecordOfSameCollectionWriter writer = new ImportRecordOfSameCollectionWriter(tempFolder);
			StringBuilder contentPaths = new StringBuilder();
			try {
				writeRecords(writer, options, contentPaths);
			} finally {
				writer.close();
			}

			File tempZipFile = ioServices.newTemporaryFile(resourceKey, "zip");

			String contentPathString = contentPaths.toString();
			if (!contentPathString.isEmpty()) {
				File file = new File(tempFolder, "contentPaths.txt");
				try {
					ioServices.appendFileContent(file, contentPathString);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if (tempFolder.listFiles() == null || tempFolder.listFiles().length == 0) {
				throw new ExportServicesRuntimeException_NoRecords();
			}
			zipService.zip(tempZipFile, asList(tempFolder.listFiles()));

			return tempZipFile;

		} catch (ZipServiceException e) {
			throw new RecordExportServicesRuntimeException.ExportServicesRuntimeException_FailedToZip(e);
		}

	}

	private void writeRecords(ImportRecordOfSameCollectionWriter writer,
							  RecordExportOptions options, StringBuilder contentPaths) {

		MetadataSchemasManager metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();

		Set<String> receivedTypes = new HashSet<>();


		Iterator<Record> recordsToExportIterator = options.getRecordsToExportIterator();


		if (recordsToExportIterator == null) {
			throw new ExportServicesRuntimeException_NoRecords();
		}

		boolean atLestOneRecord = false;
		while (recordsToExportIterator.hasNext()) {

			Record record = recordsToExportIterator.next();
			String collection = record.getCollection();
			atLestOneRecord = true;

			if (!receivedTypes.contains(record.getTypeCode())) {
				writer.setOptions(record.getTypeCode(), new ImportDataOptions()
						.setMergeExistingRecordWithSameUniqueMetadata(true)
						.setImportAsLegacyId(!options.isForSameSystem()));
				receivedTypes.add(record.getTypeCode());
			}

			MetadataSchema metadataSchema = metadataSchemasManager.getSchemaTypes(record.getCollection())
					.getSchema(record.getSchemaCode());

			ModifiableImportRecord modifiableImportRecord = new ModifiableImportRecord(collection, record.getTypeCode(),
					record.getId(), metadataSchema.getLocalCode());

			writeRecord(collection, record, modifiableImportRecord, options, contentPaths);

			appLayerFactory.getExtensions().forCollection(collection)
					.onWriteRecord(new OnWriteRecordParams(record, modifiableImportRecord));

			writer.write(modifiableImportRecord);
		}

		if (!atLestOneRecord) {
			throw new ExportServicesRuntimeException_NoRecords();
		}
	}

	private void writeRecord(String collection, Record record, ModifiableImportRecord modifiableImportRecord,
							 RecordExportOptions options, StringBuilder contentPaths) {
		MetadataSchemasManager metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
		MetadataSchemaTypes metadataSchemaTypes = metadataSchemasManager.getSchemaTypes(collection);
		MetadataSchema metadataSchema = metadataSchemaTypes.getSchema(record.getSchemaCode());
		RecordServices recordServices = modelLayerFactory.newRecordServices();
		List<String> allowedMetadatas = asList(Schemas.CREATED_ON.getLocalCode(), Schemas.CREATED_BY.getLocalCode(),
				Schemas.MODIFIED_ON.getLocalCode(), Schemas.MODIFIED_BY.getLocalCode(),
				Schemas.LOGICALLY_DELETED_STATUS.getLocalCode(), Schemas.LOGICALLY_DELETED_ON.getLocalCode(),
				ContainerRecord.FIRST_TRANSFER_REPORT_DATE, ContainerRecord.FIRST_DEPOSIT_REPORT_DATE, ContainerRecord.DOCUMENT_RESPONSIBLE,
				RMObject.FORM_CREATED_BY, RMObject.FORM_CREATED_ON, RMObject.FORM_MODIFIED_BY, RMObject.FORM_MODIFIED_ON);
		for (Metadata metadata : metadataSchema.getMetadatas()) {
			if ((!metadata.isSystemReserved() && metadata.getDataEntry().getType() == DataEntryType.MANUAL && metadata.getType() != MetadataValueType.STRUCTURE)
				|| (allowedMetadatas.contains(metadata.getLocalCode()))) {
				Object object = record.get(metadata);
				String referencePrefix = "id:";
				if (metadata.getType() == MetadataValueType.REFERENCE) {
					MetadataSchema defaultSchema = metadataSchemaTypes.getDefaultSchema(metadata.getReferencedSchemaType());
					referencePrefix = options.isForSameSystem ? "id:" : "";
					referencePrefix = defaultSchema.hasMetadataWithCode("code") && defaultSchema.getMetadata("code").isUniqueValue() ? "code:" : referencePrefix;
					referencePrefix = defaultSchema.hasMetadataWithCode("username") && defaultSchema.getMetadata("username").isUniqueValue() ? "username:" : referencePrefix;
				}
				if (object != null && metadata.getType() == MetadataValueType.REFERENCE && !metadata.isMultivalue()) {
					Object referencedObject = object;
					if ("code:".equals(referencePrefix) || "username:".equals(referencePrefix)) {
						Record referencedRecord = recordServices.getDocumentById((String) referencedObject);
						referencedObject = referencedRecord.get(metadataSchemaTypes.getSchema(referencedRecord.getSchemaCode()).get(referencePrefix.replace(":", "")));
					}
					modifiableImportRecord.addField(metadata.getLocalCode(), referencePrefix + referencedObject);
				} else if (object != null && metadata.getType() == MetadataValueType.REFERENCE && metadata.isMultivalue()) {
					if (object instanceof List) {
						List<String> idList = new ArrayList<>((List) object);
						for (int i = 0; i < idList.size(); i++) {
							Object referencedObject = idList.get(i);
							if ("code:".equals(referencePrefix) || "username:".equals(referencePrefix)) {
								Record referencedRecord = recordServices.getDocumentById((String) referencedObject);
								referencedObject = referencedRecord.get(metadataSchemaTypes.getSchema(referencedRecord.getSchemaCode()).get(referencePrefix.replace(":", "")));
							}
							idList.set(i, referencePrefix + referencedObject);
						}
						modifiableImportRecord.addField(metadata.getLocalCode(), idList);
					}
				} else if (object != null) {
					modifiableImportRecord.addField(metadata.getLocalCode(), object);
				}
			} else if (metadata.getType() == MetadataValueType.STRUCTURE) {
				StructureFactory structureFactory = metadata.getStructureFactory();
				if (structureFactory.getClass().equals(MapStringListStringStructureFactory.class)) {
					manageMapStringListStringStructureFactory(record, metadata, modifiableImportRecord);
				} else if (structureFactory.getClass().equals(MapStringStringStructureFactory.class)) {
					manageMapStringStringStructureFactory(record, metadata, modifiableImportRecord);
				} else if (structureFactory.getClass().equals(CommentFactory.class)) {
					manageCommentFactory(record, metadata, modifiableImportRecord);
				} else if (structureFactory.getClass().equals(EmailAddressFactory.class)) {
					manageEmailAddressFactory(record, metadata, modifiableImportRecord);
				}
			}
			if (metadata.getType() == MetadataValueType.CONTENT) {
				DataLayerFactory dataLayerFactory = modelLayerFactory.getDataLayerFactory();
				DataLayerConfiguration conf = dataLayerFactory.getDataLayerConfiguration();

				Object contentMetadataValue = record.get(metadata);
				if (contentMetadataValue instanceof Content) {
					Content content = (Content) contentMetadataValue;
					writeContentPath(content, contentPaths);
				} else if (contentMetadataValue instanceof java.util.Collection) {
					Iterator iterator = ((Collection) contentMetadataValue).iterator();
					while (iterator.hasNext()) {
						writeContentPath((Content) iterator.next(), contentPaths);
					}
				}
			}
		}
	}

	private void writeContentPath(Content content, StringBuilder contentPaths) {
		DataLayerFactory dataLayerFactory = modelLayerFactory.getDataLayerFactory();
		DataLayerConfiguration conf = dataLayerFactory.getDataLayerConfiguration();
		if (content != null) {
			List<ContentVersion> versions = content.getVersions();
			for (ContentVersion version : versions) {
				String systemFilePath = "";
				if (conf.getContentDaoType() == ContentDaoType.FILESYSTEM) {
					systemFilePath = ((FileSystemContentDao) dataLayerFactory.getContentsDao()).getFileOf(version.getHash()).getAbsolutePath();
				} else {
					systemFilePath = "Unsupported";
				}
				contentPaths.append(systemFilePath);
				//				contentPaths.append("*");
				contentPaths.append("\n");
			}
		}
	}


	private void manageMapStringListStringStructureFactory(Record record, Metadata metadata,
														   ModifiableImportRecord modifiableImportRecord) {
		List<MapStringListStringStructure> mapStringListStructureList;
		MapStringListStringStructure mapStringListStructure;

		if (metadata.isMultivalue()) {
			mapStringListStructureList = record.getList(metadata);
			modifiableImportRecord.addField(metadata.getLocalCode(), mapStringListStructureList);
		} else {
			mapStringListStructure = record.get(metadata);
			if (mapStringListStructure == null) {
				return;
			} else {
				modifiableImportRecord.addField(metadata.getLocalCode(), mapStringListStructure);
			}
		}
	}


	private void manageMapStringStringStructureFactory(Record record, Metadata metadata,
													   ModifiableImportRecord modifiableImportRecord) {
		List<MapStringStringStructure> mapStringStringStructureList;
		MapStringStringStructure mapStringStringStructure;

		if (metadata.isMultivalue()) {
			mapStringStringStructureList = record.getList(metadata);

			modifiableImportRecord.addField(metadata.getLocalCode(), mapStringStringStructureList);
		} else {
			mapStringStringStructure = record.get(metadata);
			if (mapStringStringStructure == null) {
				return;
			} else {
				modifiableImportRecord.addField(metadata.getLocalCode(), mapStringStringStructure);
			}
		}
	}

	private void manageCommentFactory(Record record, Metadata metadata, ModifiableImportRecord modifiableImportRecord) {
		List<Comment> commentList;
		Comment comment;
		List<HashMap<String, String>> commentHashMapList = new ArrayList<>();

		if (metadata.isMultivalue()) {
			commentList = record.getList(metadata);

			for (Comment currentComment : commentList) {
				commentHashMapList.add(getCommentHasHashMap(currentComment));
			}

			modifiableImportRecord.addField(metadata.getLocalCode(), commentHashMapList);
		} else {
			comment = record.get(metadata);
			if (comment == null) {
				return;
			} else {
				modifiableImportRecord.addField(metadata.getLocalCode(), getCommentHasHashMap(comment));
			}
		}
	}

	// For Comments

	private HashMap<String, String> getCommentHasHashMap(Comment comment) {
		HashMap<String, String> commentHasMap = new HashMap<String, String>();

		commentHasMap.put(RecordsImportServicesExecutor.COMMENT_MESSAGE, comment.getMessage());
		commentHasMap.put(RecordsImportServicesExecutor.COMMENT_USER_NAME, comment.getUsername());
		if (comment.getDateTime() != null) {
			commentHasMap.put(RecordsImportServicesExecutor.COMMENT_DATE_TIME, comment.getDateTime().toString("yyyy-MM-dd"));
		}

		return commentHasMap;
	}

	private void manageEmailAddressFactory(Record record, Metadata metadata,
										   ModifiableImportRecord modifiableImportRecord) {
		List<EmailAddress> commentList;
		EmailAddress emailAddress;
		List<HashMap<String, String>> commentHashMapList = new ArrayList<>();

		if (metadata.isMultivalue()) {
			commentList = record.getList(metadata);

			for (EmailAddress currentEmailAddress : commentList) {
				commentHashMapList.add(getEmailAddressHashMap(currentEmailAddress));
			}

			modifiableImportRecord.addField(metadata.getLocalCode(), commentHashMapList);
		} else {
			emailAddress = record.get(metadata);
			if (emailAddress == null) {
				return;
			} else {
				modifiableImportRecord.addField(metadata.getLocalCode(), getEmailAddressHashMap(emailAddress));
			}
		}
	}


	private HashMap<String, String> getEmailAddressHashMap(EmailAddress emailAddress) {
		HashMap<String, String> commentHasMap = new HashMap<String, String>();

		commentHasMap.put(RecordsImportServicesExecutor.EMAIL_ADDRESS_EMAIL, emailAddress.getEmail());
		commentHasMap.put(RecordsImportServicesExecutor.EMAIL_ADDRESS_NAME, emailAddress.getName());

		return commentHasMap;
	}


}
