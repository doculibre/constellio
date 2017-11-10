package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.schemas.calculators.TokensCalculator4;

public class RMMigrationTo7_6_666 extends MigrationHelper implements MigrationScript {
	@Override
	public String getVersion() {
		return "7.6.3";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory)
			throws Exception {
		new SchemaAlterationFor7_6_666(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	class SchemaAlterationFor7_6_666 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationFor7_6_666(String collection, MigrationResourcesProvider migrationResourcesProvider,
				AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {

			if (typesBuilder.getSchema(Document.DEFAULT_SCHEMA).getMetadata(Schemas.NON_TAXONOMY_AUTHORIZATIONS.getLocalCode())
					.getType() == MetadataValueType.REFERENCE) {
				typesBuilder.getSchema(Document.DEFAULT_SCHEMA).getMetadata(Schemas.TOKENS.getLocalCode())
						.defineDataEntry().asCalculated(TokensCalculator4.class);

				typesBuilder.getSchema(Folder.DEFAULT_SCHEMA).getMetadata(Schemas.TOKENS.getLocalCode())
						.defineDataEntry().asCalculated(TokensCalculator4.class);
			}
		}
	}
}
