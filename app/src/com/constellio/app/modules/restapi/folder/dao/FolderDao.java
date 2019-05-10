package com.constellio.app.modules.restapi.folder.dao;

import com.constellio.app.modules.restapi.core.exception.RecordCopyNotPermittedException;
import com.constellio.app.modules.restapi.folder.dto.FolderDto;
import com.constellio.app.modules.restapi.resource.dao.ResourceDao;
import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.extensions.api.RMModuleExtensions;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningService;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.type.FolderType;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchema;

public class FolderDao extends ResourceDao {

	public Record createFolder(User user, MetadataSchema folderSchema, FolderDto folderDto, String flush)
			throws Exception {
		Transaction transaction = buildTransaction(flush, user);

		Record folderRecord = recordServices.newRecordWithSchema(folderSchema);

		Record documentTypeRecord = getResourceTypeRecord(folderDto.getType(), folderSchema.getCollection());

		updateFolderMetadataValues(folderRecord, documentTypeRecord, folderSchema, folderDto, false);
		updateCustomMetadataValues(folderRecord, folderSchema, folderDto.getExtendedAttributes(), false);

		transaction.add(folderRecord);

		recordServices.execute(transaction);

		return folderRecord;
	}

	public Record copyFolder(User user, MetadataSchema folderSchema, String sourceFolderId, FolderDto folderDto,
							 String flush) throws Exception {
		Record sourceFolderRecord = recordServices.getDocumentById(sourceFolderId);
		String collection = sourceFolderRecord.getCollection();
		RMSchemasRecordsServices rmSchemas =
				new RMSchemasRecordsServices(sourceFolderRecord.getCollection(), appLayerFactory);
		Folder sourceFolder = rmSchemas.wrapFolder(sourceFolderRecord);

		RMModuleExtensions rmModuleExtensions = appLayerFactory.getExtensions().forCollection(collection)
				.forModule(ConstellioRMModule.ID);
		if (!rmModuleExtensions.isCopyActionPossibleOnFolder(sourceFolder, user)) {
			throw new RecordCopyNotPermittedException(sourceFolderId);
		}

		Transaction transaction = buildTransaction(flush, user);

		DecommissioningService decommissioningService = new DecommissioningService(collection, appLayerFactory);
		Folder copyFolder = decommissioningService.duplicateStructureAndDocuments(sourceFolder, user, false);
		updateFolderMetadataValues(copyFolder.getWrappedRecord(), null, folderSchema, folderDto, true);
		updateCustomMetadataValues(copyFolder.getWrappedRecord(), folderSchema, folderDto.getExtendedAttributes(), true);

		transaction.add(copyFolder);

		recordServices.execute(transaction);

		return copyFolder.getWrappedRecord();
	}

	private void updateFolderMetadataValues(Record folderRecord, Record folderTypeRecord, MetadataSchema schema,
											FolderDto folderDto, boolean partial) {
		updateDocumentMetadataValue(folderRecord, schema, Folder.PARENT_FOLDER, folderDto.getParentFolderId(), partial);
		updateDocumentMetadataValue(folderRecord, schema, Folder.CATEGORY_ENTERED, folderDto.getCategory(), partial);
		updateDocumentMetadataValue(folderRecord, schema, Folder.RETENTION_RULE_ENTERED, folderDto.getRetentionRule(), partial);
		updateDocumentMetadataValue(folderRecord, schema, Folder.ADMINISTRATIVE_UNIT_ENTERED, folderDto.getAdministrativeUnit(), partial);
		updateDocumentMetadataValue(folderRecord, schema, Folder.MAIN_COPY_RULE_ID_ENTERED, folderDto.getMainCopyRule(), partial);
		updateDocumentMetadataValue(folderRecord, schema, Folder.COPY_STATUS_ENTERED, folderDto.getCopyStatus(), partial);
		updateDocumentMetadataValue(folderRecord, schema, Folder.MEDIUM_TYPES, folderDto.getMediumTypes(), partial);
		updateDocumentMetadataValue(folderRecord, schema, Folder.CONTAINER, folderDto.getContainer(), partial);
		updateDocumentMetadataValue(folderRecord, schema, Folder.TITLE, folderDto.getTitle(), partial);
		updateDocumentMetadataValue(folderRecord, schema, Folder.DESCRIPTION, folderDto.getDescription(), partial);
		updateDocumentMetadataValue(folderRecord, schema, Folder.KEYWORDS, folderDto.getKeywords(), partial);
		updateDocumentMetadataValue(folderRecord, schema, Folder.OPENING_DATE, folderDto.getOpeningDate(), partial);
		updateDocumentMetadataValue(folderRecord, schema, Folder.ENTERED_CLOSING_DATE, folderDto.getClosingDate(), partial);
		updateDocumentMetadataValue(folderRecord, schema, Folder.MANUAL_EXPECTED_TRANSFER_DATE, folderDto.getActualTransferDate(), partial);
		updateDocumentMetadataValue(folderRecord, schema, Folder.MANUAL_EXPECTED_DEPOSIT_DATE, folderDto.getActualDepositDate(), partial);
		updateDocumentMetadataValue(folderRecord, schema, Folder.MANUAL_EXPECTED_DESTRUCTION_DATE, folderDto.getExpectedDestructionDate(), partial);

		String folderTypeId = folderTypeRecord != null ? folderTypeRecord.getId() : null;
		updateMetadataValue(folderRecord, schema, Folder.TYPE, folderTypeId);
	}

	@Override
	protected String getResourceSchemaType() {
		return Folder.SCHEMA_TYPE;
	}

	@Override
	protected String getResourceTypeSchemaType() {
		return FolderType.SCHEMA_TYPE;
	}
}
