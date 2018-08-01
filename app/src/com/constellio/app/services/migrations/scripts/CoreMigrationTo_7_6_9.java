package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.calculators.UserDocumentTokensCalculator;
import com.constellio.model.entities.records.wrappers.UserDocument;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoreMigrationTo_7_6_9 implements MigrationScript {

	private static final Logger LOGGER = LoggerFactory.getLogger(CoreMigrationTo_7_6_9.class);

	@Override
	public String getVersion() {
		return "7.6.9";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {

		new CoreSchemaAlterationFor_7_6_9(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	class CoreSchemaAlterationFor_7_6_9 extends MetadataSchemasAlterationHelper {

		protected CoreSchemaAlterationFor_7_6_9(String collection,
												MigrationResourcesProvider migrationResourcesProvider,
												AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaBuilder userDocumentSchema = typesBuilder.getSchema(UserDocument.DEFAULT_SCHEMA);
			userDocumentSchema.get(Schemas.TOKENS).defineDataEntry().asCalculated(UserDocumentTokensCalculator.class);

		}
	}
}
