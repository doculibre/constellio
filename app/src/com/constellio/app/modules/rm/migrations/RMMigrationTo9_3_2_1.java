package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

import static com.constellio.model.entities.schemas.MetadataValueType.REFERENCE;

public class RMMigrationTo9_3_2_1 implements MigrationScript {
	@Override
	public String getVersion() {
		return "9.3.2.1";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory) throws Exception {
		new SchemaAlterationFor9_3_2_1(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	private class SchemaAlterationFor9_3_2_1 extends MetadataSchemasAlterationHelper {
		SchemaAlterationFor9_3_2_1(String collection, MigrationResourcesProvider migrationResourcesProvider,
								   AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaBuilder userSchema = typesBuilder.getSchemaType(User.SCHEMA_TYPE).getDefaultSchema();
			MetadataSchemaBuilder decomListSchema =
					typesBuilder.getSchemaType(DecommissioningList.SCHEMA_TYPE).getDefaultSchema();

			decomListSchema.createUndeletable(DecommissioningList.REQUESTER)
					.setType(REFERENCE)
					.defineReferencesTo(userSchema);
		}
	}
}
