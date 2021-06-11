package com.constellio.app.modules.complementary.esRmRobots.migrations;

import com.constellio.app.entities.modules.ComboMigrationScript;
import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.builders.CommonMetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.security.roles.RolesManager;

import java.util.ArrayList;
import java.util.List;

public class ESRMRobotsMigrationCombo implements ComboMigrationScript {
	@Override
	public List<MigrationScript> getVersions() {
		List<MigrationScript> migrations = new ArrayList<>();

		migrations.add(new ESRMRobotsMigrationTo5_1_2());
		migrations.add(new ESRMRobotsMigrationTo5_1_5());
		migrations.add(new ESRMRobotsMigrationTo5_1_6());
		migrations.add(new ESRMRobotsMigrationTo5_1_7());
		migrations.add(new ESRMRobotsMigrationTo5_1_9());
		migrations.add(new ESRMRobotsMigrationTo6_0());
		migrations.add(new ESRMRobotsMigrationTo6_1());
		migrations.add(new ESRMRobotsMigrationTo6_2_2_1());
		migrations.add(new ESRMRobotsMigrationTo7_0_1());
		migrations.add(new ESRMRobotsMigrationTo7_1());
		migrations.add(new ESRMRobotsMigrationTo7_3_1());
		migrations.add(new ESRMRobotsMigrationTo7_5());
		migrations.add(new ESRMRobotsMigrationTo8_1_1());
		migrations.add(new ESRMRobotsMigrationTo8_1_1_1());
		return migrations;
	}

	@Override
	public String getVersion() {
		return "combo";
	}

	GeneratedESRMRobotsMigrationCombo generatedComboMigration;

	MigrationResourcesProvider migrationResourcesProvider;

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {
		this.migrationResourcesProvider = migrationResourcesProvider;
		ModelLayerFactory modelLayerFactory = appLayerFactory.getModelLayerFactory();
		generatedComboMigration = new GeneratedESRMRobotsMigrationCombo(collection, appLayerFactory,
				migrationResourcesProvider);

		new SchemaAlteration(collection, migrationResourcesProvider, appLayerFactory).migrate();
		generatedComboMigration.applyGeneratedRoles();
		generatedComboMigration.applySchemasDisplay(appLayerFactory.getMetadataSchemasDisplayManager());
		applySchemasDisplay2(collection, migrationResourcesProvider, appLayerFactory.getMetadataSchemasDisplayManager());

		RecordServices recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
		MetadataSchemaTypes types = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection);

		recordServices.execute(createRecordTransaction(collection, migrationResourcesProvider, appLayerFactory, types));

		RolesManager rolesManager = modelLayerFactory.getRolesManager();
		//		rolesManager.updateRole(
		//				rolesManager.getRole(collection, RMRoles.RGD).withNewPermissions(asList(RobotsPermissionsTo.MANAGE_ROBOTS)));
	}

	private void applySchemasDisplay2(String collection, MigrationResourcesProvider migrationResourcesProvider,
									  SchemasDisplayManager manager) {
		SchemaTypesDisplayTransactionBuilder transaction = manager.newTransactionBuilderFor(collection);

		//transaction.add(manager.getType(collection, ConnectorHttpDocument.SCHEMA_TYPE).withMetadataGroup(groups));
		transaction.add(manager.getSchema(collection, "robotLog_default").withNewTableMetadatas("robotLog_default_count"));
		transaction.add(manager.getSchema(collection, "containerRecord_default")
				.withRemovedFormMetadatas("containerRecord_default_fillRatioEntered"));

		manager.execute(transaction.build());
	}

	private Transaction createRecordTransaction(String collection,
												MigrationResourcesProvider migrationResourcesProvider,
												AppLayerFactory appLayerFactory, MetadataSchemaTypes types) {
		Transaction transaction = new Transaction();

		ESSchemasRecordsServices es = new ESSchemasRecordsServices(collection, appLayerFactory);

		return transaction;
	}

	class SchemaAlteration extends MetadataSchemasAlterationHelper {

		protected SchemaAlteration(String collection,
								   MigrationResourcesProvider migrationResourcesProvider,
								   AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			generatedComboMigration.applyGeneratedSchemaAlteration(typesBuilder);
			new CommonMetadataBuilder().addCommonMetadataToAllExistingSchemas(typesBuilder);
		}

	}

}
