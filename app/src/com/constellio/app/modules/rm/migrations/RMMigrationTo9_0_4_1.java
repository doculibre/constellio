package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.model.calculators.document.DocumentCurrentContentSizeCalculator;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class RMMigrationTo9_0_4_1 implements MigrationScript {
	@Override
	public String getVersion() {
		return "9.0.4.1";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory) throws Exception {
		new SchemaAlterationFor9_0_4_1(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	private static class SchemaAlterationFor9_0_4_1 extends MetadataSchemasAlterationHelper {
		SchemaAlterationFor9_0_4_1(String collection, MigrationResourcesProvider migrationResourcesProvider,
								   AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaBuilder document = typesBuilder.getDefaultSchema(Document.SCHEMA_TYPE);
			if (!document.hasMetadata(Document.CURRENT_CONTENT_SIZE)) {
				document.createUndeletable(Document.CURRENT_CONTENT_SIZE).setType(MetadataValueType.NUMBER)
						.defineDataEntry().asCalculated(DocumentCurrentContentSizeCalculator.class);
			}
		}
	}
}
