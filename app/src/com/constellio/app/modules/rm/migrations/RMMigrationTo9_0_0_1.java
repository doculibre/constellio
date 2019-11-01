package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.model.calculators.folder.FolderAllowedDocumentTypeCalculator;
import com.constellio.app.modules.rm.model.calculators.folder.FolderAllowedFolderTypeCalculator;
import com.constellio.app.modules.rm.model.validators.DocumentValidator;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RMTask;
import com.constellio.app.modules.rm.wrappers.type.DocumentType;
import com.constellio.app.modules.rm.wrappers.type.FolderType;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

import java.util.HashMap;

public class RMMigrationTo9_0_0_1 extends MigrationHelper implements MigrationScript {

	@Override
	public String getVersion() {
		return "9.0.0.1";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {
		new SchemaAlterationFor9_0_0_1(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	class SchemaAlterationFor9_0_0_1 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationFor9_0_0_1(String collection, MigrationResourcesProvider migrationResourcesProvider,
											 AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaBuilder taskSchema = typesBuilder.getSchemaType(RMTask.SCHEMA_TYPE).getDefaultSchema();

			if (taskSchema.hasMetadata(RMTask.CREATED_AUTHORIZATIONS)) {
				taskSchema.deleteMetadataWithoutValidation(RMTask.CREATED_AUTHORIZATIONS);
			}

			taskSchema.createUndeletable(RMTask.CREATED_AUTHORIZATIONS).setSystemReserved(true)
					.setType(MetadataValueType.STRING).setMultivalue(true);

			MetadataSchemaBuilder folderSchema = typesBuilder.getSchemaType(Folder.SCHEMA_TYPE).getDefaultSchema();
			MetadataSchemaBuilder documentTypeSchema = typesBuilder.getSchemaType(DocumentType.SCHEMA_TYPE).getDefaultSchema();
			HashMap<Language, String> labels = new HashMap<>();
			labels.put(Language.French, "Types de document autorisés");
			labels.put(Language.English, "Allowed document types");
			folderSchema.createUndeletable(Folder.ALLOWED_DOCUMENT_TYPES)
					.setLabels(labels)
					.setType(MetadataValueType.REFERENCE)
					.defineReferencesTo(documentTypeSchema)
					.setMultivalue(true)
					.defineDataEntry().asCalculated(FolderAllowedDocumentTypeCalculator.class)
					.setSystemReserved(true);

			MetadataSchemaBuilder folderTypeSchema = typesBuilder.getSchemaType(FolderType.SCHEMA_TYPE).getDefaultSchema();
			labels = new HashMap<>();
			labels.put(Language.French, "Types de dossier autorisés");
			labels.put(Language.English, "Allowed folder types");
			folderSchema.createUndeletable(Folder.ALLOWED_FOLDER_TYPES)
					.setLabels(labels)
					.setType(MetadataValueType.REFERENCE)
					.defineReferencesTo(folderTypeSchema)
					.setMultivalue(true)
					.defineDataEntry().asCalculated(FolderAllowedFolderTypeCalculator.class)
					.setSystemReserved(true);

			MetadataSchemaBuilder documentSchema = typesBuilder.getSchemaType(Document.SCHEMA_TYPE).getDefaultSchema();
			documentSchema.defineValidators().add(DocumentValidator.class);
		}
	}
}
