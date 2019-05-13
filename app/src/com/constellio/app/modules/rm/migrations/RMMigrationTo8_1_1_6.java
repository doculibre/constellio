package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.model.calculators.UserDocumentContentHashesCalculator;
import com.constellio.app.modules.rm.model.calculators.document.DocumentContentHashesCalculator;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.wrappers.UserDocument;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class RMMigrationTo8_1_1_6 extends MigrationHelper implements MigrationScript {

	@Override
	public String getVersion() {
		return "8.1.1.6";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory) throws Exception {
		new SchemaAlterationFor8_1_1_6(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	class SchemaAlterationFor8_1_1_6 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationFor8_1_1_6(String collection, MigrationResourcesProvider migrationResourcesProvider,
											 AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			typesBuilder.getDefaultSchema(Document.SCHEMA_TYPE).createUndeletable(Document.CONTENT_HASHES)
					.setType(MetadataValueType.STRING).setMultivalue(true).defineDataEntry().asCalculated(DocumentContentHashesCalculator.class);

			typesBuilder.getDefaultSchema(UserDocument.SCHEMA_TYPE).createUndeletable(UserDocument.CONTENT_HASHES)
					.setType(MetadataValueType.STRING).setMultivalue(true).defineDataEntry().asCalculated(UserDocumentContentHashesCalculator.class);
		}
	}
}
