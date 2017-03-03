package com.constellio.app.modules.rm.migrations;

import static com.constellio.model.entities.schemas.MetadataValueType.BOOLEAN;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.model.calculators.folder.FolderConfidentialCalculator;
import com.constellio.app.modules.rm.model.calculators.folder.FolderEssentialCalculator;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class RMMigrationTo7_0_11 implements MigrationScript {

	@Override
	public String getVersion() {
		return "7.0.11";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider provider, AppLayerFactory appLayerFactory)
			throws Exception {

		new SchemaAlterationsFor7_0_11(collection, provider, appLayerFactory).migrate();

		SchemasDisplayManager schemaDisplayManager = appLayerFactory.getMetadataSchemasDisplayManager();
		SchemaTypesDisplayTransactionBuilder transaction = schemaDisplayManager.newTransactionBuilderFor(collection);
		transaction.in(Folder.SCHEMA_TYPE).addToDisplay(Folder.ESSENTIAL).afterMetadata(Folder.RETENTION_RULE);
		transaction.in(Folder.SCHEMA_TYPE).addToDisplay(Folder.CONFIDENTIAL).afterMetadata(Folder.RETENTION_RULE);

		schemaDisplayManager.execute(transaction.build());

	}

	public static class SchemaAlterationsFor7_0_11 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationsFor7_0_11(String collection, MigrationResourcesProvider provider, AppLayerFactory factory) {
			super(collection, provider, factory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaBuilder folderSchema = types().getSchema(Folder.DEFAULT_SCHEMA);

			folderSchema.create(Folder.ESSENTIAL).setType(BOOLEAN).defineDataEntry().asCalculated(
					FolderEssentialCalculator.class);
			folderSchema.create(Folder.CONFIDENTIAL).setType(BOOLEAN).defineDataEntry().asCalculated(
					FolderConfidentialCalculator.class);
		}

	}
}
