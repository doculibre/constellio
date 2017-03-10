package com.constellio.app.modules.rm.migrations;

import static com.constellio.model.entities.schemas.MetadataValueType.BOOLEAN;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.model.calculators.FolderClosingDateCalculator2;
import com.constellio.app.modules.rm.model.calculators.document.DocumentConfidentialCalculator;
import com.constellio.app.modules.rm.model.calculators.document.DocumentEssentialCalculator;
import com.constellio.app.modules.rm.model.calculators.folder.FolderConfidentialCalculator;
import com.constellio.app.modules.rm.model.calculators.folder.FolderEssentialCalculator;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class RMMigrationTo7_0_10_5 implements MigrationScript {

	@Override
	public String getVersion() {
		return "7.0.10.5";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider provider, AppLayerFactory appLayerFactory)
			throws Exception {

		new SchemaAlterationsFor7_0_10_5(collection, provider, appLayerFactory).migrate();

		SchemasDisplayManager schemaDisplayManager = appLayerFactory.getMetadataSchemasDisplayManager();
		SchemaTypesDisplayTransactionBuilder transaction = schemaDisplayManager.newTransactionBuilderFor(collection);
		transaction.in(Folder.SCHEMA_TYPE).addToDisplay(Folder.ESSENTIAL).afterMetadata(Folder.RETENTION_RULE);
		transaction.in(Folder.SCHEMA_TYPE).addToDisplay(Folder.CONFIDENTIAL).afterMetadata(Folder.RETENTION_RULE);

		schemaDisplayManager.execute(transaction.build());

	}

	public static class SchemaAlterationsFor7_0_10_5 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationsFor7_0_10_5(String collection, MigrationResourcesProvider provider, AppLayerFactory factory) {
			super(collection, provider, factory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaBuilder folderSchema = types().getSchema(Folder.DEFAULT_SCHEMA);
			MetadataSchemaBuilder documentSchema = types().getSchema(Document.DEFAULT_SCHEMA);
			MetadataBuilder folderEssential = getOrCreateBoolean(folderSchema, Folder.ESSENTIAL);
			MetadataBuilder folderConfidential = getOrCreateBoolean(folderSchema, Folder.CONFIDENTIAL);
			MetadataBuilder documentEssential = getOrCreateBoolean(documentSchema, Document.ESSENTIAL);
			MetadataBuilder documentConfidential = getOrCreateBoolean(documentSchema, Document.CONFIDENTIAL);
			folderEssential.defineDataEntry().asCalculated(FolderEssentialCalculator.class);
			folderConfidential.defineDataEntry().asCalculated(FolderConfidentialCalculator.class);
			documentEssential.defineDataEntry().asCalculated(DocumentEssentialCalculator.class);
			documentConfidential.defineDataEntry().asCalculated(DocumentConfidentialCalculator.class);

			folderSchema.get(Folder.CLOSING_DATE).defineDataEntry().asCalculated(FolderClosingDateCalculator2.class);
		}

		private MetadataBuilder getOrCreateBoolean(MetadataSchemaBuilder schema, String localCode) {
			if (schema.hasMetadata(localCode)) {
				return schema.get(localCode);
			} else {
				return schema.create(localCode).setType(BOOLEAN);
			}
		}

	}
}
