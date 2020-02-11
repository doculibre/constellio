package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.records.wrappers.SavedSearch;
import com.constellio.model.entities.records.wrappers.SearchEvent;
import com.constellio.model.entities.records.wrappers.UserDocument;
import com.constellio.model.entities.records.wrappers.UserFolder;
import com.constellio.model.services.schemas.builders.CommonMetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.schemas.calculators.AttachedAncestorsCalculator2;

public class CoreMigrationTo_9_0_42_1 extends MigrationHelper implements MigrationScript {

	@Override
	public String getVersion() {
		return "9.0.42.1";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {
		new SchemaAlterationFor9_0_42_1(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	class SchemaAlterationFor9_0_42_1 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationFor9_0_42_1(String collection, MigrationResourcesProvider migrationResourcesProvider,
											  AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {

			typesBuilder.getDefaultSchema(UserDocument.SCHEMA_TYPE).get(UserDocument.USER).setCacheIndex(true);
			typesBuilder.getDefaultSchema(UserDocument.SCHEMA_TYPE).get(UserDocument.USER_FOLDER).setCacheIndex(true);
			typesBuilder.getDefaultSchema(UserFolder.SCHEMA_TYPE).get(UserFolder.USER).setCacheIndex(true);

			for (MetadataSchemaTypeBuilder typeBuilder : typesBuilder.getTypes()) {
				if (typeBuilder.getDefaultSchema().hasMetadata(CommonMetadataBuilder.ATTACHED_ANCESTORS)) {
					if (!typeBuilder.getCode().equals(Event.SCHEMA_TYPE)
					&& !typeBuilder.getCode().equals(SavedSearch.SCHEMA_TYPE)
						&& !typeBuilder.getCode().equals(SearchEvent.SCHEMA_TYPE)) {
						typeBuilder.getDefaultSchema().getMetadata(CommonMetadataBuilder.ATTACHED_ANCESTORS)
								.defineDataEntry().asCalculated(AttachedAncestorsCalculator2.class);
					} else {
						typeBuilder.getDefaultSchema().getMetadata(CommonMetadataBuilder.ATTACHED_ANCESTORS).defineDataEntry().asManual();
					}
				}
			}
		}
	}
}
