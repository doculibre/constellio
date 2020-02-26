package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.model.calculators.folder.FolderAllowedDocumentTypeCalculator;
import com.constellio.app.modules.rm.model.calculators.folder.FolderAllowedFolderTypeCalculator;
import com.constellio.app.modules.rm.model.validators.DocumentValidator;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RMTask;
import com.constellio.app.modules.rm.wrappers.type.DocumentType;
import com.constellio.app.modules.rm.wrappers.type.FolderType;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

import static com.constellio.model.entities.schemas.RecordCacheType.SUMMARY_CACHED_WITH_VOLATILE;

public class RMMigrationTo9_0_0_42 implements MigrationScript {
	@Override
	public String getVersion() {
		return "9.0.0.42";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory) throws Exception {
		new SchemaAlterationFor9_0_0_42(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	private class SchemaAlterationFor9_0_0_42 extends MetadataSchemasAlterationHelper {
		SchemaAlterationFor9_0_0_42(String collection, MigrationResourcesProvider migrationResourcesProvider,
									AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaBuilder folderSchema = typesBuilder.getSchemaType(Folder.SCHEMA_TYPE).getDefaultSchema();

			if (folderSchema.hasMetadata(Folder.ALLOWED_DOCUMENT_TYPES)) {
				folderSchema.getMetadata(Folder.ALLOWED_DOCUMENT_TYPES).setSystemReserved(true);
				folderSchema.getMetadata(Folder.ALLOWED_FOLDER_TYPES).setSystemReserved(true);

			} else {
				MetadataSchemaBuilder documentTypeSchema = typesBuilder.getSchemaType(DocumentType.SCHEMA_TYPE).getDefaultSchema();
				folderSchema.createUndeletable(Folder.ALLOWED_DOCUMENT_TYPES)
						.setType(MetadataValueType.REFERENCE)
						.defineReferencesTo(documentTypeSchema)
						.setMultivalue(true)
						.defineDataEntry().asCalculated(FolderAllowedDocumentTypeCalculator.class)
						.setSystemReserved(true);

				MetadataSchemaBuilder folderTypeSchema = typesBuilder.getSchemaType(FolderType.SCHEMA_TYPE).getDefaultSchema();
				folderSchema.createUndeletable(Folder.ALLOWED_FOLDER_TYPES)
						.setType(MetadataValueType.REFERENCE)
						.defineReferencesTo(folderTypeSchema)
						.setMultivalue(true)
						.defineDataEntry().asCalculated(FolderAllowedFolderTypeCalculator.class)
						.setSystemReserved(true);

				MetadataSchemaBuilder documentSchema = typesBuilder.getSchemaType(Document.SCHEMA_TYPE).getDefaultSchema();
				documentSchema.defineValidators().add(DocumentValidator.class);
			}

			typesBuilder.getSchemaType(Folder.SCHEMA_TYPE).setRecordCacheType(SUMMARY_CACHED_WITH_VOLATILE);
			typesBuilder.getDefaultSchema(Folder.SCHEMA_TYPE).get(Folder.SUB_FOLDERS_TOKENS).setEssentialInSummary(true);
			typesBuilder.getDefaultSchema(Folder.SCHEMA_TYPE).get(Folder.DOCUMENTS_TOKENS).setEssentialInSummary(true);
			typesBuilder.getDefaultSchema(Folder.SCHEMA_TYPE).get(Schemas.TOKENS_OF_HIERARCHY.getLocalCode()).setEssentialInSummary(true);
			typesBuilder.getDefaultSchema(Folder.SCHEMA_TYPE).get(Folder.PARENT_FOLDER).setCacheIndex(true);
			typesBuilder.getDefaultSchema(Folder.SCHEMA_TYPE).get(Folder.CATEGORY_ENTERED).setCacheIndex(true);
			typesBuilder.getDefaultSchema(Folder.SCHEMA_TYPE).get(Folder.ADMINISTRATIVE_UNIT_ENTERED).setCacheIndex(true);
			typesBuilder.getDefaultSchema(Folder.SCHEMA_TYPE).get(Schemas.PATH_PARTS).setCacheIndex(true);

			typesBuilder.getSchemaType(Document.SCHEMA_TYPE).setRecordCacheType(SUMMARY_CACHED_WITH_VOLATILE);
			typesBuilder.getDefaultSchema(Document.SCHEMA_TYPE).get(Schemas.TOKENS_OF_HIERARCHY.getLocalCode()).setEssentialInSummary(true);
			typesBuilder.getDefaultSchema(Document.SCHEMA_TYPE).get(Document.FOLDER).setCacheIndex(true);
			typesBuilder.getDefaultSchema(Document.SCHEMA_TYPE).get(Document.FOLDER_CATEGORY).setCacheIndex(true);
			typesBuilder.getDefaultSchema(Document.SCHEMA_TYPE).get(Schemas.PATH_PARTS).setCacheIndex(true);

			typesBuilder.getDefaultSchema(DecommissioningList.SCHEMA_TYPE).get("folders").setCacheIndex(true);
			typesBuilder.getDefaultSchema(DecommissioningList.SCHEMA_TYPE).get("documents").setCacheIndex(true);

			typesBuilder.getDefaultSchema(Task.SCHEMA_TYPE).get(RMTask.LINKED_FOLDERS).setCacheIndex(true);
			typesBuilder.getDefaultSchema(Task.SCHEMA_TYPE).get(RMTask.LINKED_DOCUMENTS).setCacheIndex(true);
		}
	}
}
