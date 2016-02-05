package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.CommonMetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class RMMigrationTo5_1_2_2 implements MigrationScript {
	@Override
	public String getVersion() {
		return "5.1.2.2";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory)
			throws Exception {
		new RMSchemaAlterationFor5_1_2_2(collection, migrationResourcesProvider, appLayerFactory).migrate();
		setupDisplayConfig(collection, appLayerFactory);
	}

	private void setupDisplayConfig(String collection, AppLayerFactory appLayerFactory) {
		SchemasDisplayManager manager = appLayerFactory.getMetadataSchemasDisplayManager();
		SchemaTypesDisplayTransactionBuilder transactionBuilder = manager.newTransactionBuilderFor(collection);

		transactionBuilder.in(Folder.SCHEMA_TYPE).removeFromDisplay(
				CommonMetadataBuilder.CREATED_BY, CommonMetadataBuilder.CREATED_ON,
				CommonMetadataBuilder.MODIFIED_BY, CommonMetadataBuilder.MODIFIED_ON);
		transactionBuilder.in(Document.SCHEMA_TYPE).removeFromDisplay(
				CommonMetadataBuilder.CREATED_BY, CommonMetadataBuilder.CREATED_ON,
				CommonMetadataBuilder.MODIFIED_BY, CommonMetadataBuilder.MODIFIED_ON);
		transactionBuilder.in(Folder.SCHEMA_TYPE)
				.addToDisplay(Folder.FORM_CREATED_BY, Folder.FORM_CREATED_ON, Folder.FORM_MODIFIED_BY, Folder.FORM_MODIFIED_ON)
				.beforeTheHugeCommentMetadata();
		transactionBuilder.in(Document.SCHEMA_TYPE)
				.addToDisplay(Document.FORM_CREATED_BY, Document.FORM_CREATED_ON,
						Document.FORM_MODIFIED_BY, Document.FORM_MODIFIED_ON).beforeTheHugeCommentMetadata();
		transactionBuilder.add(manager.getMetadata(collection, Folder.DEFAULT_SCHEMA, Folder.FORM_CREATED_BY)
				.withInputType(MetadataInputType.HIDDEN));
		transactionBuilder.add(manager.getMetadata(collection, Folder.DEFAULT_SCHEMA, Folder.FORM_CREATED_ON)
				.withInputType(MetadataInputType.HIDDEN));
		transactionBuilder.add(manager.getMetadata(collection, Document.DEFAULT_SCHEMA, Document.FORM_CREATED_BY)
				.withInputType(MetadataInputType.HIDDEN));
		transactionBuilder.add(manager.getMetadata(collection, Document.DEFAULT_SCHEMA, Document.FORM_CREATED_ON)
				.withInputType(MetadataInputType.HIDDEN));
		manager.execute(transactionBuilder.build());
	}

	private class RMSchemaAlterationFor5_1_2_2 extends MetadataSchemasAlterationHelper {
		public RMSchemaAlterationFor5_1_2_2(String collection, MigrationResourcesProvider migrationResourcesProvider,
				AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaTypeBuilder userSchemaType = typesBuilder.getSchemaType(User.SCHEMA_TYPE);

			MetadataSchemaBuilder documentSchema = typesBuilder.getSchema(Document.DEFAULT_SCHEMA);
			documentSchema.createSystemReserved(Folder.FORM_CREATED_BY).defineReferencesTo(userSchemaType);
			documentSchema.createSystemReserved(Folder.FORM_CREATED_ON).setType(MetadataValueType.DATE_TIME);
			documentSchema.createSystemReserved(Folder.FORM_MODIFIED_BY).defineReferencesTo(userSchemaType);
			documentSchema.createSystemReserved(Folder.FORM_MODIFIED_ON).setType(MetadataValueType.DATE_TIME);

			MetadataSchemaBuilder folderSchema = typesBuilder.getSchema(Folder.DEFAULT_SCHEMA);
			folderSchema.createSystemReserved(Folder.FORM_CREATED_BY).defineReferencesTo(userSchemaType);
			folderSchema.createSystemReserved(Folder.FORM_CREATED_ON).setType(MetadataValueType.DATE_TIME);
			folderSchema.createSystemReserved(Folder.FORM_MODIFIED_BY).defineReferencesTo(userSchemaType);
			folderSchema.createSystemReserved(Folder.FORM_MODIFIED_ON).setType(MetadataValueType.DATE_TIME);
		}
	}
}
