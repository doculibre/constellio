package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Printable;
import com.constellio.app.modules.rm.wrappers.PrintableReport;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

/**
 * Created by constellios on 2017-07-13.
 */
public class
RMMigrationTo7_4 extends MigrationHelper implements MigrationScript {
	@Override
	public String getVersion() {
		return "7.4";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {
		SchemaAlterationFor7_4 schemaAlterationFor7_4 = new RMMigrationTo7_4.SchemaAlterationFor7_4(collection,
				migrationResourcesProvider, appLayerFactory);
		schemaAlterationFor7_4.migrate();

		SchemasDisplayManager displayManager = appLayerFactory.getMetadataSchemasDisplayManager();
		SchemaTypesDisplayTransactionBuilder transaction = displayManager.newTransactionBuilderFor(collection);

		transaction
				.add(order(collection, appLayerFactory, "form", displayManager.getSchema(collection, PrintableReport.SCHEMA_NAME),
						PrintableReport.TITLE,
						PrintableReport.JASPERFILE,
						PrintableReport.RECORD_TYPE,
						PrintableReport.RECORD_SCHEMA
						).withNewTableMetadatas(PrintableReport.SCHEMA_NAME + "_" + PrintableReport.RECORD_SCHEMA)
				);
		displayManager.execute(transaction.build());
		//givenNewPermissionsToRGDandADMRoles(collection, appLayerFactory.getModelLayerFactory());
	}

	//    private void givenNewPermissionsToRGDandADMRoles(String collection, ModelLayerFactory modelLayerFactory) {
	//        Role adminRole = modelLayerFactory.getRolesManager().getRole(collection, CoreRoles.ADMINISTRATOR);
	//        List<String> newRgdPermissions = new ArrayList<>();
	//        newRgdPermissions.add(CorePermissions.MANAGE_PRINTABLE_REPORT);
	//        modelLayerFactory.getRolesManager().updateRole(adminRole.withNewPermissions(newRgdPermissions));
	//    }

	//		private void setupRoles(String collection, RolesManager manager, MigrationResourcesProvider provider) {
	//			manager.updateRole(
	//					manager.getRole(collection, RMRoles.MANAGER)
	//							.withNewPermissions(Collections.singletonList(CorePermissions.MANAGE_PRINTABLE_REPORT)));
	//		}

	class SchemaAlterationFor7_4 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationFor7_4(String collection, MigrationResourcesProvider migrationResourcesProvider,
										 AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaBuilder metadataSchemaBuilder = typesBuilder.getSchemaType(Printable.SCHEMA_TYPE)
																	  .createCustomSchema(PrintableReport.SCHEMA_TYPE);

			metadataSchemaBuilder.create(PrintableReport.RECORD_TYPE).setType(MetadataValueType.STRING).setUndeletable(true)
								 .setEssential(true);
			metadataSchemaBuilder.create(PrintableReport.RECORD_SCHEMA).setType(MetadataValueType.STRING).setUndeletable(true)
								 .setEssential(true);

			MetadataSchemaBuilder containerRecord = typesBuilder.getSchema(ContainerRecord.DEFAULT_SCHEMA);
			containerRecord.getMetadata(ContainerRecord.DECOMMISSIONING_TYPE).setDefaultRequirement(true);
			containerRecord.getMetadata(ContainerRecord.ADMINISTRATIVE_UNITS).setDefaultRequirement(true);

		}

	}
}
