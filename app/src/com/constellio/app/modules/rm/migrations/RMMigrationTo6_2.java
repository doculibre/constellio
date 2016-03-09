package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.model.calculators.folder.FolderMainCopyRuleCalculator2;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class RMMigrationTo6_2 implements MigrationScript {
	@Override
	public String getVersion() {
		return "6.2";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory)
			throws Exception {
		new SchemaAlterationsFor6_2(collection, migrationResourcesProvider, appLayerFactory).migrate();
		setupDisplayConfigs(collection, appLayerFactory);
	}

	private void setupDisplayConfigs(String collection, AppLayerFactory factory) {
		SchemasDisplayManager schemaDisplayManager = factory.getMetadataSchemasDisplayManager();

		SchemaTypesDisplayTransactionBuilder transaction = schemaDisplayManager.newTransactionBuilderFor(collection);

		transaction.in(Document.SCHEMA_TYPE).addToForm(Document.MAIN_COPY_RULE_ID_ENTERED).afterMetadata(Document.TITLE);

		schemaDisplayManager.execute(transaction.build());
	}

	public static class SchemaAlterationsFor6_2 extends MetadataSchemasAlterationHelper {
		protected SchemaAlterationsFor6_2(String collection, MigrationResourcesProvider provider, AppLayerFactory factory) {
			super(collection, provider, factory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			updateDocumentSchema(typesBuilder.getSchemaType(Document.SCHEMA_TYPE).getDefaultSchema());
			updateFolderSchema(typesBuilder.getSchemaType(Folder.SCHEMA_TYPE).getDefaultSchema());
		}

		private void updateDocumentSchema(MetadataSchemaBuilder documentSchemaType) {
			documentSchemaType.createUndeletable(Document.MAIN_COPY_RULE_ID_ENTERED).setType(MetadataValueType.STRING);
		}

		private void updateFolderSchema(MetadataSchemaBuilder folderSchemaType) {
			folderSchemaType.createUndeletable(Folder.MAIN_COPY_RULE_ID_ENTERED).setType(MetadataValueType.STRING);
			folderSchemaType.get(Folder.MAIN_COPY_RULE).defineDataEntry().asCalculated(FolderMainCopyRuleCalculator2.class);
		}
	}
}
