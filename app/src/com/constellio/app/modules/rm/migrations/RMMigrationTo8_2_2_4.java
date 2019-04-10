package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class RMMigrationTo8_2_2_4 implements MigrationScript {

	@Override
	public String getVersion() {
		return "8.2.2.4";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider provider, AppLayerFactory appLayerFactory)
			throws Exception {
		new SchemaAlterationFor8_2_2_4(collection, provider, appLayerFactory).migrate();
		SchemasDisplayManager displayManager = appLayerFactory.getMetadataSchemasDisplayManager();
		displayManager.saveSchema(displayManager.getSchema(collection, ContainerRecord.DEFAULT_SCHEMA).withRemovedFormMetadatas(
				ContainerRecord.DEFAULT_SCHEMA + "_" + ContainerRecord.BORROWER,
				ContainerRecord.DEFAULT_SCHEMA + "_" + ContainerRecord.FILING_SPACE,
				ContainerRecord.DEFAULT_SCHEMA + "_" + ContainerRecord.BORROW_DATE,
				ContainerRecord.DEFAULT_SCHEMA + "_" + ContainerRecord.COMPLETION_DATE,
				ContainerRecord.DEFAULT_SCHEMA + "_" + ContainerRecord.PLANIFIED_RETURN_DATE,
				ContainerRecord.DEFAULT_SCHEMA + "_" + ContainerRecord.REAL_DEPOSIT_DATE,
				ContainerRecord.DEFAULT_SCHEMA + "_" + ContainerRecord.REAL_RETURN_DATE,
				ContainerRecord.DEFAULT_SCHEMA + "_" + ContainerRecord.REAL_TRANSFER_DATE
		));
	}

	private class SchemaAlterationFor8_2_2_4 extends MetadataSchemasAlterationHelper {
		public SchemaAlterationFor8_2_2_4(String collection, MigrationResourcesProvider migrationResourcesProvider,
									   AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder builder) {
			builder.getDefaultSchema(ContainerRecord.SCHEMA_TYPE).get(ContainerRecord.BORROWER).setEssential(false);
			builder.getDefaultSchema(ContainerRecord.SCHEMA_TYPE).get(ContainerRecord.FILING_SPACE).setEssential(false);
			builder.getDefaultSchema(ContainerRecord.SCHEMA_TYPE).get(ContainerRecord.BORROW_DATE).setEssential(false);
			builder.getDefaultSchema(ContainerRecord.SCHEMA_TYPE).get(ContainerRecord.COMPLETION_DATE).setEssential(false);
			builder.getDefaultSchema(ContainerRecord.SCHEMA_TYPE).get(ContainerRecord.PLANIFIED_RETURN_DATE).setEssential(false);
			builder.getDefaultSchema(ContainerRecord.SCHEMA_TYPE).get(ContainerRecord.REAL_DEPOSIT_DATE).setEssential(false);
			builder.getDefaultSchema(ContainerRecord.SCHEMA_TYPE).get(ContainerRecord.REAL_RETURN_DATE).setEssential(false);
			builder.getDefaultSchema(ContainerRecord.SCHEMA_TYPE).get(ContainerRecord.REAL_TRANSFER_DATE).setEssential(false);
			builder.getDefaultSchema(ContainerRecord.SCHEMA_TYPE).get(ContainerRecord.TEMPORARY_IDENTIFIER).setEssential(false);
		}
	}
}
