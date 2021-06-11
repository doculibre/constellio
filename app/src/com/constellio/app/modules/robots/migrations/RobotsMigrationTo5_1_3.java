package com.constellio.app.modules.robots.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.robots.model.wrappers.Robot;
import com.constellio.app.modules.robots.model.wrappers.RobotLog;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class RobotsMigrationTo5_1_3 implements MigrationScript {
	@Override
	public String getVersion() {
		return "5.1.3";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {
		new SchemasAlterationsFor5_1_3(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	static class SchemasAlterationsFor5_1_3 extends MetadataSchemasAlterationHelper {
		protected SchemasAlterationsFor5_1_3(String collection, MigrationResourcesProvider provider,
											 AppLayerFactory factory) {
			super(collection, provider, factory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder builder) {
			createRobotLogSchemaType(builder, builder.getSchemaType(Robot.SCHEMA_TYPE));
		}

		private void createRobotLogSchemaType(MetadataSchemaTypesBuilder types, MetadataSchemaTypeBuilder robot) {
			MetadataSchemaTypeBuilder robotLog = types.createNewSchemaTypeWithSecurity(RobotLog.SCHEMA_TYPE);
			MetadataSchemaBuilder schema = robotLog.getDefaultSchema();

			schema.createUndeletable(RobotLog.ROBOT).defineReferencesTo(robot).setDefaultRequirement(true).setEssential(true);
		}
	}
}
