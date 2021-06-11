package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class CoreMigrationTo_9_2_20 extends MigrationHelper implements MigrationScript {

	@Override
	public String getVersion() {
		return "9.2.20";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {
		new SchemaAlterationFor_9_2_20(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	class SchemaAlterationFor_9_2_20 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationFor_9_2_20(String collection, MigrationResourcesProvider migrationResourcesProvider,
											 AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaBuilder userSchema = typesBuilder.getSchemaType(User.SCHEMA_TYPE).getDefaultSchema();
			userSchema.getMetadata(User.TITLE).setSearchable(true);
			userSchema.getMetadata(User.USERNAME).setSearchable(true);
			userSchema.getMetadata(User.EMAIL).setSearchable(true);

			MetadataSchemaBuilder groupSchema = typesBuilder.getSchemaType(Group.SCHEMA_TYPE).getDefaultSchema();
			groupSchema.getMetadata(Group.TITLE).setSearchable(true);
		}
	}
}
