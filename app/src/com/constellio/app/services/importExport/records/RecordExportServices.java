package com.constellio.app.services.importExport.records;

import com.constellio.app.api.extensions.params.OnWriteRecordParams;
import com.constellio.app.extensions.AppLayerCollectionExtensions;
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
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.ContentVersion;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Authorization;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.schemas.StructureFactory;
import com.constellio.model.entities.schemas.entries.CalculatedDataEntry;
import com.constellio.model.entities.security.SecurityModelAuthorization;
import com.constellio.model.entities.structures.EmailAddress;
import com.constellio.model.entities.structures.EmailAddressFactory;
import com.constellio.model.entities.structures.MapStringListStringStructure;
import com.constellio.model.entities.structures.MapStringListStringStructureFactory;
import com.constellio.model.entities.structures.MapStringStringStructure;
import com.constellio.model.entities.structures.MapStringStringStructureFactory;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesRuntimeException;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static com.constellio.model.entities.schemas.MetadataValueType.REFERENCE;
import static com.constellio.model.entities.schemas.MetadataValueType.STRUCTURE;
import static com.constellio.model.entities.schemas.entries.DataEntryType.CALCULATED;
import static com.constellio.model.entities.schemas.entries.DataEntryType.MANUAL;
import static java.util.Arrays.asList;

public class RecordExportServices {

	private static Logger LOGGER = LoggerFactory.getLogger(RecordExportServices.class);
	public static final String RECORDS_EXPORT_TEMP_FOLDER = "RecordsExportServices_recordsExportTempFolder";

	AppLayerFactory appLayerFactory;
	ModelLayerFactory modelLayerFactory;
	ZipService zipService;
	IOServices ioServices;
	RecordServices recordServices;
	MetadataSchemasManager metadataSchemasManager;

	public RecordExportServices(AppLayerFactory appLayerFactory) {
		this.appLayerFactory = appLayerFactory;
		this.modelLayerFactory = appLayerFactory.getModelLayerFactory();
		this.zipService = modelLayerFactory.getIOServicesFactory().newZipService();
		this.ioServices = modelLayerFactory.getIOServicesFactory().newIOServices();
		this.recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
		this.metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
	}

	public void exportRecordsAndZip(File zipFile, RecordExportOptions options) {

		File tempFolder = ioServices.newTemporaryFolder(RECORDS_EXPORT_TEMP_FOLDER);
		ValidationErrors errors = new ValidationErrors();

		try {

			//
			ImportRecordOfSameCollectionWriter writer = new ImportRecordOfSameCollectionWriter(tempFolder);
			StringBuilder contentPaths = new StringBuilder();
			try {
				writeRecords(writer, options, contentPaths, tempFolder);
			} finally {
				writer.close();
			}

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
			zipService.zip(zipFile, asList(tempFolder.listFiles()));


		} catch (ZipServiceException e) {
			throw new RecordExportServicesRuntimeException.ExportServicesRuntimeException_FailedToZip(e);
		}

	}


	public File exportRecordsAndZip(String resourceKey, RecordExportOptions options) {
		File tempZipFile = ioServices.newTemporaryFile(resourceKey, "zip");
		FileUtils.deleteQuietly(tempZipFile);
		exportRecordsAndZip(tempZipFile, options);
		return tempZipFile;
	}


	private void writeRecords(ImportRecordOfSameCollectionWriter writer,
							  RecordExportOptions options, StringBuilder contentPaths,
							  File tempFolder) {


		Set<String> receivedTypes = new HashSet<>();


		Iterator<Record> recordsToExportIterator = options.getRecordsToExportIterator();


		if (recordsToExportIterator == null) {
			throw new ExportServicesRuntimeException_NoRecords();
		}

		boolean atLestOneRecord = false;
		while (recordsToExportIterator.hasNext()) {

			Record record = recordsToExportIterator.next();

			String collection = record.getCollection();
			AppLayerCollectionExtensions collectionExtensions = appLayerFactory.getExtensions().forCollectionOf(record);
			MetadataSchemaType schemaType = metadataSchemasManager.getSchemaTypeOf(record);
			if (!collectionExtensions.isSchemaTypeExportable(schemaType, collection)) {
				continue;
			}
			atLestOneRecord = true;

			if (!receivedTypes.contains(record.getTypeCode())) {
				writer.setOptions(record.getTypeCode(), new ImportDataOptions()
						.setMergeExistingRecordWithSameUniqueMetadata(true)
						.setImportAsLegacyId(!options.isForSameSystem()));
				receivedTypes.add(record.getTypeCode());
			}

			MetadataSchema metadataSchema = metadataSchemasManager.getSchemaTypes(record.getCollection())
					.getSchema(record.getSchemaCode());

			String id = record.getId();
			if (!options.isForSameSystem() && record.get(Schemas.LEGACY_ID) != null) {
				id = record.get(Schemas.LEGACY_ID);
			}

			ModifiableImportRecord modifiableImportRecord = new ModifiableImportRecord(collection, record.getTypeCode(),
					id, metadataSchema.getLocalCode());

			writeRecord(record, modifiableImportRecord, options, contentPaths);
			if (options.isIncludeAuthorizations()) {
				writeRecordAuthorizations(record.getId(), collection, options, contentPaths, writer);
			}

			appLayerFactory.getExtensions().forCollection(collection)
					.onWriteRecord(new OnWriteRecordParams(record, modifiableImportRecord, options.isForSameSystem(), tempFolder));

			writer.write(modifiableImportRecord);
		}

		if (!atLestOneRecord) {
			throw new ExportServicesRuntimeException_NoRecords();
		}
	}

	private void writeRecordAuthorizations(String recordId, String collection, RecordExportOptions options,
										   StringBuilder contentPaths, ImportRecordOfSameCollectionWriter writer) {
		List<SecurityModelAuthorization> authorizationsList = recordServices.getSecurityModel(collection).getAuthorizationsOnTarget(recordId);

		SchemasRecordsServices schemas = new SchemasRecordsServices(collection, modelLayerFactory);
		for (SecurityModelAuthorization authorization : authorizationsList) {
			Record authorizationRecord = authorization.getDetails().get();
			ModifiableImportRecord modifiableImportRecord = new ModifiableImportRecord(collection, Authorization.SCHEMA_TYPE, authorization.getDetails().getId());
			writeRecord(authorizationRecord, modifiableImportRecord, options, contentPaths);

			List<String> principals = new ArrayList<>();

			for (String userId : authorization.getUserIds()) {
				User user = schemas.getUser(userId);
				if (user != null) {
					principals.add("user:" + user.getUsername());
				}
			}

			for (String groupId : authorization.getGroupIds()) {
				Group group = schemas.getGroup(groupId);
				if (group != null) {
					principals.add("group:" + group.getCode());
				}
			}

			if (!options.isForSameSystem) {
				String targetSchemaType = authorization.getDetails().getTargetSchemaType();
				Record record = recordServices.getDocumentById(authorization.getTargetRecordId().stringValue());
				if (metadataSchemasManager.getSchemaTypes(collection).getSchemaType(targetSchemaType).hasMetadataWithCode("code")) {

					modifiableImportRecord.with(Authorization.TARGET, "code:" + record.get(Schemas.CODE));
				} else if (record.get(Schemas.LEGACY_ID) != null) {
					modifiableImportRecord.with(Authorization.TARGET, record.get(Schemas.LEGACY_ID));

				}
			}

			modifiableImportRecord.with(Authorization.PRINCIPALS, principals);

			writer.write(modifiableImportRecord);

		}

	}

	private static List<String> preferedMetadatas = asList(Schemas.CODE.getLocalCode(), User.USERNAME);

	private String toReferenceImportInstruction(String referencedRecordId, MetadataSchemaType schemaType,
												boolean isForSameSystem) {

		if (isForSameSystem) {
			return "id:" + referencedRecordId;
		} else {

			MetadataSchema defaultSchema = schemaType.getDefaultSchema();
			try {
				Record referencedRecord = recordServices.getDocumentById(referencedRecordId);
				for (String preferedMetadataLocalCode : preferedMetadatas) {
					if (defaultSchema.hasMetadataWithCode(preferedMetadataLocalCode)) {
						Metadata preferedMetadata = defaultSchema.getMetadata(preferedMetadataLocalCode);
						String metadataValue = referencedRecord.get(preferedMetadata);
						if (metadataValue != null && preferedMetadata.isUniqueValue()) {
							return preferedMetadataLocalCode + ":" + metadataValue;
						}
					}
				}

				String legacyId = referencedRecord.get(Schemas.LEGACY_ID);
				if (legacyId != null) {
					return legacyId;
				}

			} catch (RecordServicesRuntimeException.NoSuchRecordWithId e) {
				LOGGER.warn("Record does not exist", e);
				return "id:" + referencedRecordId;
				//Strange situation where the record does not exist locally (probably a partial copy of an instance)
			}

			//We guess that the id of the record will the legacy id of the equivalent record in another collection
			return referencedRecordId;
		}

	}


	private void writeRecord(Record record, ModifiableImportRecord modifiableImportRecord,
							 final RecordExportOptions options, StringBuilder contentPaths) {
		MetadataSchemaTypes metadataSchemaTypes = metadataSchemasManager.getSchemaTypes(record);


		for (Metadata metadata : metadataSchemaTypes.getSchemaOf(record).getMetadatas()) {

			if (isMetadataExported(metadata, record, metadataSchemaTypes)) {
				Object rawValue = record.get(metadata);
				AppLayerCollectionExtensions collectionExtensions = appLayerFactory.getExtensions().forCollectionOf(record);

				if (!options.isForSameSystem && metadata.isSameLocalCode(Schemas.LEGACY_ID)) {
					rawValue = record.getId();
				}

				if (rawValue != null) {

					if (metadata.getType() == STRUCTURE) {
						convertStructureMetadataValue(record, modifiableImportRecord, metadata);

					} else {
						convertMetadata(modifiableImportRecord, options, metadataSchemaTypes, metadata, rawValue);
					}
				}

				if (metadata.getType() == MetadataValueType.CONTENT) {
					for (Content content : record.<Content>getValues(metadata)) {
						writeContentPath(content, contentPaths);
					}
				}
			}
		}

	}

	private boolean isMetadataExported(Metadata metadata, Record record, MetadataSchemaTypes metadataSchemaTypes) {
		AppLayerCollectionExtensions collectionExtensions = appLayerFactory.getExtensions().forCollectionOf(record);
		if (collectionExtensions.isMetadataExportForced(metadata)) {
			return true;
		}
		List<String> allowedMetadatas = asList(Schemas.CREATED_ON.getLocalCode(), Schemas.CREATED_BY.getLocalCode(),
				Schemas.MODIFIED_ON.getLocalCode(), Schemas.MODIFIED_BY.getLocalCode(),
				Schemas.LOGICALLY_DELETED_STATUS.getLocalCode(), Schemas.LOGICALLY_DELETED_ON.getLocalCode(),
				ContainerRecord.FIRST_TRANSFER_REPORT_DATE, ContainerRecord.FIRST_DEPOSIT_REPORT_DATE, ContainerRecord.DOCUMENT_RESPONSIBLE,
				RMObject.FORM_CREATED_BY, RMObject.FORM_CREATED_ON, RMObject.FORM_MODIFIED_BY, RMObject.FORM_MODIFIED_ON);


		if (metadata.getDataEntry().getType() == MANUAL) {
			return !metadata.isSystemReserved() || allowedMetadatas.contains(metadata.getLocalCode());
		}

		if (metadata.getDataEntry().getType() == CALCULATED) {
			MetadataValueCalculator calculator = ((CalculatedDataEntry) metadata.getDataEntry()).getCalculator();
			return calculator.hasEvaluator()
				   && (!metadata.isSystemReserved() || allowedMetadatas.contains(metadata.getLocalCode()))
				   && !recordServices.isValueAutomaticallyFilled(metadata, record);
		}

		return false;

	}

	private void convertMetadata(ModifiableImportRecord modifiableImportRecord, final RecordExportOptions options,
								 MetadataSchemaTypes metadataSchemaTypes, Metadata metadata, Object rawValue) {


		if (metadata.getType() == REFERENCE) {
			final MetadataSchemaType schemaType = metadataSchemaTypes.getSchemaType(metadata.getReferencedSchemaTypeCode());

			if (!metadata.isMultivalue()) {
				String instr = toReferenceImportInstruction((String) rawValue, schemaType, options.isForSameSystem());
				modifiableImportRecord.addField(metadata.getLocalCode(), instr);
			} else {
				List<String> convertedValues = new ArrayList<>();

				for (Object item : (List) rawValue) {
					String instr = toReferenceImportInstruction((String) item, schemaType, options.isForSameSystem());
					convertedValues.add(instr);
				}

				modifiableImportRecord.addField(metadata.getLocalCode(), convertedValues);
			}

		} else {
			modifiableImportRecord.addField(metadata.getLocalCode(), rawValue);
		}
	}

	private void convertStructureMetadataValue(Record record, ModifiableImportRecord modifiableImportRecord,
											   Metadata metadata) {
		//TODO Refactor this
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

	private void manageCommentFactory(Record record, Metadata metadata, ModifiableImportRecord
			modifiableImportRecord) {
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
		if (comment.getCreationDateTime() != null) {
			commentHasMap.put(RecordsImportServicesExecutor.COMMENT_DATE_TIME, comment.getCreationDateTime().toString("yyyy-MM-dd"));
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
