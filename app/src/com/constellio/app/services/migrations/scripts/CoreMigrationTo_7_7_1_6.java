package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.wrappers.ThesaurusConfig;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class CoreMigrationTo_7_7_1_6 implements MigrationScript {
	@Override
	public String getVersion() {
		return "7.7.1.6";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {
		new CoreSchemaAlterationFor_7_7_1_6(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	class CoreSchemaAlterationFor_7_7_1_6 extends MetadataSchemasAlterationHelper {

		protected CoreSchemaAlterationFor_7_7_1_6(String collection,
												  MigrationResourcesProvider migrationResourcesProvider,
												  AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaBuilder thesaurusConfig;
			if(typesBuilder.hasSchemaType(ThesaurusConfig.SCHEMA_TYPE)) {
				thesaurusConfig = typesBuilder.getDefaultSchema(ThesaurusConfig.SCHEMA_TYPE);
			} else {
				thesaurusConfig = typesBuilder.createNewSchemaTypeWithSecurity(ThesaurusConfig.SCHEMA_TYPE).getDefaultSchema();
			}

			if(!thesaurusConfig.hasMetadata(ThesaurusConfig.CONTENT)) {
				thesaurusConfig.createUndeletable(ThesaurusConfig.CONTENT).setSystemReserved(true).setType(MetadataValueType.CONTENT);
			}

			if(!thesaurusConfig.hasMetadata(ThesaurusConfig.DENINED_WORDS)) {
				thesaurusConfig.createUndeletable(ThesaurusConfig.DENINED_WORDS).setSystemReserved(true).setMultivalue(true).setType(MetadataValueType.STRING);
			}
		}
	}
}
