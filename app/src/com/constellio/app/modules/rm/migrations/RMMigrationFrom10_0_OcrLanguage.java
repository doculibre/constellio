package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class RMMigrationFrom10_0_OcrLanguage implements MigrationScript {

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory) throws Exception {
		new SchemaAlteration(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	private class SchemaAlteration extends MetadataSchemasAlterationHelper {
		SchemaAlteration(String collection, MigrationResourcesProvider migrationResourcesProvider,
								  AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaBuilder documentSchema = typesBuilder.getDefaultSchema(Document.SCHEMA_TYPE);
			documentSchema.createUndeletable(Document.OCR_LANGUAGE).setType(MetadataValueType.STRING).setSystemReserved(true);
		}
	}
}
