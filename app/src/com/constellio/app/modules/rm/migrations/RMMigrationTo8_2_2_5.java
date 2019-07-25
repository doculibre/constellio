package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.model.calculators.document.DocumentCheckedOutUserCalculator2;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

import static com.constellio.model.entities.schemas.MetadataValueType.REFERENCE;

public class RMMigrationTo8_2_2_5 implements MigrationScript {

	@Override
	public String getVersion() {
		return "8.2.2.5";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider provider, AppLayerFactory appLayerFactory)
			throws Exception {
		appLayerFactory.getSystemGlobalConfigsManager().setReindexingRequired(true);
		new SchemaAlterationFor8_2_2_5_1(collection, provider, appLayerFactory).migrate();
		new SchemaAlterationFor8_2_2_5_2(collection, provider, appLayerFactory).migrate();
	}

	private class SchemaAlterationFor8_2_2_5_1 extends MetadataSchemasAlterationHelper {
		public SchemaAlterationFor8_2_2_5_1(String collection, MigrationResourcesProvider migrationResourcesProvider,
											AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder builder) {
			typesBuilder.getSchema(Document.DEFAULT_SCHEMA).deleteMetadataWithoutValidation(Document.CONTENT_CHECKED_OUT_BY);
		}
	}

	private class SchemaAlterationFor8_2_2_5_2 extends MetadataSchemasAlterationHelper {
		public SchemaAlterationFor8_2_2_5_2(String collection, MigrationResourcesProvider migrationResourcesProvider,
											AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder builder) {
			typesBuilder.getSchema(Document.DEFAULT_SCHEMA).createUndeletable(Document.CONTENT_CHECKED_OUT_BY).setType(REFERENCE)
					.defineReferencesTo(typesBuilder.getSchemaType(User.SCHEMA_TYPE)).defineDataEntry().asCalculated(new DocumentCheckedOutUserCalculator2());
		}
	}
}
