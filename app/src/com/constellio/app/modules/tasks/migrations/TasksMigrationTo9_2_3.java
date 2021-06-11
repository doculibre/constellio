package com.constellio.app.modules.tasks.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

import static com.constellio.model.entities.schemas.RecordCacheType.SUMMARY_CACHED_WITH_VOLATILE;

public class TasksMigrationTo9_2_3 implements MigrationScript {
	@Override
	public String getVersion() {
		return "9.2.3";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory) throws Exception {
		new SchemaAlterationFor9_2_3(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	private class SchemaAlterationFor9_2_3 extends MetadataSchemasAlterationHelper {
		SchemaAlterationFor9_2_3(String collection, MigrationResourcesProvider migrationResourcesProvider,
								 AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			typesBuilder.getSchemaType(Task.SCHEMA_TYPE).setRecordCacheType(SUMMARY_CACHED_WITH_VOLATILE);
			typesBuilder.getSchema(Task.DEFAULT_SCHEMA).getMetadata(Schemas.MANUAL_TOKENS.getLocalCode()).setEssentialInSummary(true);
			typesBuilder.getSchema(Task.DEFAULT_SCHEMA).getMetadata(Schemas.TOKENS_OF_HIERARCHY.getLocalCode()).setEssentialInSummary(true);
		}
	}
}
