package com.constellio.app.modules.robots.migrations;

import java.util.ArrayList;
import java.util.List;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.robots.model.wrappers.Robot;
import com.constellio.app.modules.robots.model.wrappers.RobotLog;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class RobotsMigrationTo6_3 implements MigrationScript {
	@Override
	public String getVersion() {
		return "6.3";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider provider, AppLayerFactory factory)
			throws Exception {
		new SchemaAlterationFor6_3(collection, provider, factory).migrate();
		updateFormAndDisplayConfigs(collection, factory);
	}

	private void updateFormAndDisplayConfigs(String collection, AppLayerFactory factory) {
		SchemasDisplayManager manager = factory.getMetadataSchemasDisplayManager();

		SchemaTypesDisplayTransactionBuilder transactionBuilder = manager.newTransactionBuilderFor(collection)
				.in(Robot.SCHEMA_TYPE).addToForm(Robot.AUTO_EXECUTE).atTheEnd();

		List<String> metas = new ArrayList<>();
		metas.addAll(manager.getSchema(collection, RobotLog.DEFAULT_SCHEMA).getTableMetadataCodes());
		metas.add(RobotLog.DEFAULT_SCHEMA + "_" + RobotLog.COUNT);

		transactionBuilder.add(manager.getSchema(collection, RobotLog.DEFAULT_SCHEMA).withTableMetadataCodes(metas));

		transactionBuilder.add(manager.getSchema(collection, "robotLog_default").withNewTableMetadatas("robotLog_default_count"));

		manager.execute(transactionBuilder.build());
	}

	static class SchemaAlterationFor6_3 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationFor6_3(String collection, MigrationResourcesProvider provider, AppLayerFactory factory) {
			super(collection, provider, factory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder builder) {
			updateRobotSchema(builder, builder.getSchemaType(Robot.SCHEMA_TYPE));
		}

		private void updateRobotSchema(MetadataSchemaTypesBuilder types, MetadataSchemaTypeBuilder robot) {
			MetadataSchemaBuilder robotSchema = robot.getDefaultSchema();

			robotSchema.createUndeletable(Robot.AUTO_EXECUTE).setType(MetadataValueType.BOOLEAN)
					.setDefaultRequirement(true).setDefaultValue(false);

			MetadataSchemaTypeBuilder robotLogSchemaType = types.getOrCreateNewSchemaType(RobotLog.SCHEMA_TYPE);
			MetadataSchemaBuilder robotLogSchema = robotLogSchemaType.getDefaultSchema();

			robotLogSchema.createUndeletable(RobotLog.COUNT).setType(MetadataValueType.NUMBER);
		}
	}
}
