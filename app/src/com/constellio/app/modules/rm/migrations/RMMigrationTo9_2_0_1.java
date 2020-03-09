package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.model.enums.DecomListStatus;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.records.BulkRecordTransactionHandler;
import com.constellio.model.services.records.BulkRecordTransactionHandlerOptions;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class RMMigrationTo9_2_0_1 implements MigrationScript {

	private final static String DECOMMISSIONING_LIST_FOLDERS = "folders";
	private final static String DECOMMISSIONING_LIST_DOCUMENTS = "documents";
	private final static String DECOMMISSIONING_LIST_UNIFORM_COPY_RULE = "uniformCopyRule";
	private final static String DECOMMISSIONING_LIST_UNIFORM_COPY_TYPE = "uniformCopyType";
	private final static String DECOMMISSIONING_LIST_UNIFORM_CATEGORY = "uniformCategory";
	private final static String DECOMMISSIONING_LIST_UNIFORM_RULE = "uniformRule";
	private final static String DECOMMISSIONING_LIST_UNIFORM = "uniform";

	private RMSchemasRecordsServices rm;

	private Map<String, Folder> foldersToUpdate;
	private Map<String, Document> documentsToUpdate;

	@Override
	public String getVersion() {
		return "9.2.0.1";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory) throws Exception {
		new SchemaAlterationFor9_2_0_1(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	private class SchemaAlterationFor9_2_0_1 extends MetadataSchemasAlterationHelper {
		SchemaAlterationFor9_2_0_1(String collection, MigrationResourcesProvider migrationResourcesProvider,
								   AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			createMetadatas();
			migrateRecords();
			deleteMetadatas();
		}

		private void createMetadatas() {
			MetadataSchemaBuilder decomSchema = types().getDefaultSchema(DecommissioningList.SCHEMA_TYPE);
			MetadataSchemaBuilder documentSchema = types().getDefaultSchema(Document.SCHEMA_TYPE);
			MetadataSchemaBuilder folderSchema = types().getDefaultSchema(Folder.SCHEMA_TYPE);

			documentSchema.createUndeletable(Document.CURRENT_DECOMMISSIONING_LIST)
					.setType(MetadataValueType.REFERENCE)
					.defineReferencesTo(decomSchema)
					.setCacheIndex(true);

			documentSchema.createUndeletable(Document.PREVIOUS_DECOMMISSIONING_LISTS)
					.setType(MetadataValueType.REFERENCE)
					.defineReferencesTo(decomSchema)
					.setMultivalue(true)
					.setCacheIndex(true);

			MetadataBuilder currentDecomMetadata = folderSchema.createUndeletable(Folder.CURRENT_DECOMMISSIONING_LIST)
					.setType(MetadataValueType.REFERENCE)
					.defineReferencesTo(decomSchema)
					.setCacheIndex(true);

			MetadataBuilder previousDecomMetadata = folderSchema.createUndeletable(Folder.PREVIOUS_DECOMMISSIONING_LISTS)
					.setType(MetadataValueType.REFERENCE)
					.defineReferencesTo(decomSchema)
					.setMultivalue(true)
					.setCacheIndex(true);

			MetadataBuilder mediaTypeMetadata = folderSchema.getMetadata(Folder.MEDIA_TYPE);

			Map<MetadataBuilder, List<MetadataBuilder>> valueMetadatasByReferenceMetadata = new HashMap<>();
			valueMetadatasByReferenceMetadata.put(currentDecomMetadata, Collections.singletonList(mediaTypeMetadata));
			valueMetadatasByReferenceMetadata.put(previousDecomMetadata, Collections.singletonList(mediaTypeMetadata));

			decomSchema.getMetadata(DecommissioningList.FOLDERS_MEDIA_TYPES)
					.defineDataEntry().asUnion(valueMetadatasByReferenceMetadata);
		}

		private void deleteMetadatas() {
			MetadataSchemaBuilder decomSchema = types().getDefaultSchema(DecommissioningList.SCHEMA_TYPE);

			decomSchema.deleteMetadataWithoutValidation(DECOMMISSIONING_LIST_UNIFORM);
			decomSchema.deleteMetadataWithoutValidation(DECOMMISSIONING_LIST_UNIFORM_COPY_RULE);
			decomSchema.deleteMetadataWithoutValidation(DECOMMISSIONING_LIST_UNIFORM_COPY_TYPE);
			decomSchema.deleteMetadataWithoutValidation(DECOMMISSIONING_LIST_UNIFORM_CATEGORY);
			decomSchema.deleteMetadataWithoutValidation(DECOMMISSIONING_LIST_UNIFORM_RULE);

			decomSchema.deleteMetadataWithoutValidation(DECOMMISSIONING_LIST_DOCUMENTS);
			decomSchema.deleteMetadataWithoutValidation(DECOMMISSIONING_LIST_FOLDERS);
		}

		private void migrateRecords() {
			SearchServices searchServices = modelLayerFactory.newSearchServices();
			rm = new RMSchemasRecordsServices(collection, appLayerFactory);

			foldersToUpdate = new HashMap<>();
			documentsToUpdate = new HashMap<>();

			LogicalSearchQuery query = new LogicalSearchQuery(from(rm.decommissioningList.schemaType()).returnAll());
			List<Record> records = searchServices.search(query);
			for (Record record : records) {
				migrateRecord(record);
			}

			updateRecords();
		}

		private void migrateRecord(Record record) {
			DecommissioningList decomList = rm.wrapDecommissioningList(record);

			if (decomList.hasValue(DECOMMISSIONING_LIST_FOLDERS)) {
				List<Folder> folders = getFolders(decomList.get(DECOMMISSIONING_LIST_FOLDERS));
				for (Folder folder : folders) {
					if (decomList.getStatus() == DecomListStatus.PROCESSED) {
						folder.addPreviousDecommissioningList(decomList.getId());
					} else {
						folder.setCurrentDecommissioningList(decomList.getId());
					}
				}
			}

			if (decomList.hasValue(DECOMMISSIONING_LIST_DOCUMENTS)) {
				List<Document> documents = getDocuments(decomList.get(DECOMMISSIONING_LIST_DOCUMENTS));
				for (Document document : documents) {
					if (decomList.getStatus() == DecomListStatus.PROCESSED) {
						document.addPreviousDecommissioningList(decomList.getId());
					} else {
						document.setCurrentDecommissioningList(decomList.getId());
					}
				}
			}
		}

		private List<Folder> getFolders(List<String> folderIds) {
			List<Folder> folders = new ArrayList<>();
			for (String folderId : folderIds) {
				if (!foldersToUpdate.containsKey(folderId)) {
					foldersToUpdate.put(folderId, rm.getFolder(folderId));
				}
				folders.add(foldersToUpdate.get(folderId));
			}
			return folders;
		}

		private List<Document> getDocuments(List<String> documentIds) {
			List<Document> documents = new ArrayList<>();
			for (String documentId : documentIds) {
				if (!documentsToUpdate.containsKey(documentId)) {
					documentsToUpdate.put(documentId, rm.getDocument(documentId));
				}
				documents.add(documentsToUpdate.get(documentId));
			}
			return documents;
		}

		private void updateRecords() {
			BulkRecordTransactionHandlerOptions options =
					new BulkRecordTransactionHandlerOptions().withRecordsPerBatch(5000);
			BulkRecordTransactionHandler bulkTransactionHandler = new BulkRecordTransactionHandler(
					modelLayerFactory.newRecordServices(), "RMMigrationTo9_2_0_1", options);

			List<Record> folders =
					foldersToUpdate.values().stream().map(Folder::getWrappedRecord).collect(Collectors.toList());
			bulkTransactionHandler.append(folders);

			List<Record> documents =
					documentsToUpdate.values().stream().map(Document::getWrappedRecord).collect(Collectors.toList());
			bulkTransactionHandler.append(documents);

			bulkTransactionHandler.closeAndJoin();
		}
	}
}
