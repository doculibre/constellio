package com.constellio.app.modules.rm.migrations;

import static com.constellio.app.ui.i18n.i18n.$;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.wrappers.type.DocumentType;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class RMMigrationTo5_1_0_4 implements MigrationScript {

	@Override
	public String getVersion() {
		return "5.1.0.4";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
			AppLayerFactory appLayerFactory)
			throws Exception {

		new SchemaAlterationFor5_1_0_4(collection, migrationResourcesProvider, appLayerFactory).migrate();
		updateFormAndDisplay(collection, appLayerFactory);
	}

	class SchemaAlterationFor5_1_0_4 extends MetadataSchemasAlterationHelper {
		MetadataSchemaTypes types;

		protected SchemaAlterationFor5_1_0_4(String collection, MigrationResourcesProvider migrationResourcesProvider,
				AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
			types = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection);
		}

		public String getVersion() {
			return "5.1.0.4";
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {

			MetadataSchemaTypeBuilder ddvDocumentTypeSchemaTypeBuilder = typesBuilder.getSchemaType(DocumentType.SCHEMA_TYPE);

			MetadataSchemaBuilder ddvDocumentDefaultSchemaBuilder = ddvDocumentTypeSchemaTypeBuilder.getDefaultSchema();
			ddvDocumentDefaultSchemaBuilder.createUndeletable(DocumentType.TEMPLATES)
					.setLabel($("DocumentType.templates"))
					.setType(MetadataValueType.CONTENT)
					.setMultivalue(true);
		}
	}

	private void updateFormAndDisplay(String collection, AppLayerFactory appLayerFactory) {
		SchemasDisplayManager manager = appLayerFactory.getMetadataSchemasDisplayManager();

		SchemaTypesDisplayTransactionBuilder transactionBuilder = manager.newTransactionBuilderFor(collection);

		transactionBuilder.in(DocumentType.SCHEMA_TYPE)
				.addToForm(DocumentType.TEMPLATES)
				.atTheEnd();

		manager.execute(transactionBuilder.build());
	}

}
