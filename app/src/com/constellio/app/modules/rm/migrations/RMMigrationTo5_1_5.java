package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.model.enums.FolderStatus;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.type.DocumentType;
import com.constellio.app.modules.rm.wrappers.type.FolderType;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.builders.CommonMetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class RMMigrationTo5_1_5 implements MigrationScript {
	@Override
	public String getVersion() {
		return "5.1.5";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider provider, AppLayerFactory factory)
			throws Exception {
		new RMSchemaAlterationsFor5_1_5(collection, provider, factory).migrate();
		createNewRecords(collection, factory, provider);
	}

	private void createNewRecords(String collection, AppLayerFactory factory, MigrationResourcesProvider provider) {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, factory);
		Transaction transaction = new Transaction();
		createFolderTypeAndDocumentTypeFacets(transaction, rm, provider);
		try {
			factory.getModelLayerFactory().newRecordServices().execute(transaction);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
	}

	private void createFolderTypeAndDocumentTypeFacets(Transaction transaction, RMSchemasRecordsServices rm,
			MigrationResourcesProvider provider) {
		transaction.add(rm.newFacetField().setTitle(provider.getDefaultLanguageString("facets.folderType"))
				.setFieldDataStoreCode(rm.folder.folderType().getDataStoreCode()).setActive(false));
		transaction.add(rm.newFacetField().setTitle(provider.getDefaultLanguageString("facets.documentType"))
				.setFieldDataStoreCode(rm.documentDocumentType().getDataStoreCode()).setActive(false));
	}

	private class RMSchemaAlterationsFor5_1_5 extends MetadataSchemasAlterationHelper {

		protected RMSchemaAlterationsFor5_1_5(String collection, MigrationResourcesProvider provider, AppLayerFactory factory) {
			super(collection, provider, factory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaBuilder folderType = typesBuilder.getSchemaType(FolderType.SCHEMA_TYPE).getDefaultSchema();
			updateFolderSchema(typesBuilder.getSchemaType(Folder.SCHEMA_TYPE).getDefaultSchema(), folderType);

			MetadataSchemaBuilder documentType = typesBuilder.getSchemaType(DocumentType.SCHEMA_TYPE).getDefaultSchema();
			updateDocumentSchema(typesBuilder.getSchemaType(Document.SCHEMA_TYPE).getDefaultSchema(), documentType);
		}

		private void updateFolderSchema(MetadataSchemaBuilder folder, MetadataSchemaBuilder folderType) {
			folder.createSystemReserved(Folder.PERMISSION_STATUS).defineAsEnum(FolderStatus.class);
			folder.createSystemReserved(Folder.FOLDER_TYPE).setType(MetadataValueType.STRING).defineDataEntry()
					.asCopied(folder.getMetadata(Folder.TYPE), folderType.getMetadata(CommonMetadataBuilder.TITLE));
		}

		private void updateDocumentSchema(MetadataSchemaBuilder document, MetadataSchemaBuilder documentType) {
			document.createSystemReserved(Document.DOCUMENT_TYPE).setType(MetadataValueType.STRING).defineDataEntry()
					.asCopied(document.getMetadata(Document.TYPE), documentType.getMetadata(CommonMetadataBuilder.TITLE));
		}
	}
}
