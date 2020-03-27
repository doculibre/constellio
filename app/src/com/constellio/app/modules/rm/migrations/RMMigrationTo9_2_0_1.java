package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.model.calculators.document.DocumentFilenameCalculator;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;

public class RMMigrationTo9_2_0_1 implements MigrationScript {

	@Override
	public String getVersion() {
		return "9.2.0.1";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider provider, AppLayerFactory appLayerFactory)
			throws Exception {
		appLayerFactory.getSystemGlobalConfigsManager().setReindexingRequired(true);
		new SchemaAlterationFor9_2_0_1(collection, provider, appLayerFactory).migrate();
	}

	private class SchemaAlterationFor9_2_0_1 extends MetadataSchemasAlterationHelper {
		public SchemaAlterationFor9_2_0_1(String collection, MigrationResourcesProvider migrationResourcesProvider,
										  AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder builder) {
			typesBuilder.getSchema(Document.DEFAULT_SCHEMA).createUndeletable(Document.FILENAME).setType(STRING)
					.defineDataEntry().asCalculated(new DocumentFilenameCalculator());
		}
	}
}
