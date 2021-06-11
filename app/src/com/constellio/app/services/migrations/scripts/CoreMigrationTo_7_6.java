package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.records.wrappers.Capsule;
import com.constellio.model.entities.records.wrappers.ExportAudit;
import com.constellio.model.entities.records.wrappers.ImportAudit;
import com.constellio.model.entities.records.wrappers.TemporaryRecord;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static java.util.Arrays.asList;

public class CoreMigrationTo_7_6 implements MigrationScript {
	@Override
	public String getVersion() {
		return "7.6";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {
		new CoreSchemaAlterationFor_7_6_deleteStep(collection, migrationResourcesProvider, appLayerFactory).migrate();
		new CoreSchemaAlterationFor_7_6(collection, migrationResourcesProvider, appLayerFactory).migrate();
		migrateDisplayConfigs(appLayerFactory, collection);
		editTableMetadataOrder(collection, appLayerFactory.getMetadataSchemasDisplayManager());
		appLayerFactory.getSystemGlobalConfigsManager().setReindexingRequired(true);
	}

	public void migrateDisplayConfigs(AppLayerFactory appLayerFactory, String collection) {
		SchemasDisplayManager manager = appLayerFactory.getMetadataSchemasDisplayManager();
		SchemaTypesDisplayTransactionBuilder transactionBuilder = manager.newTransactionBuilderFor(collection);

		transactionBuilder.add(manager.getMetadata(collection, Capsule.DEFAULT_SCHEMA + "_" + Capsule.HTML)
				.withInputType(MetadataInputType.RICHTEXT));
		manager.execute(transactionBuilder.build());
	}

	private void editTableMetadataOrder(String collection, SchemasDisplayManager manager) {
		manager.saveSchema(manager.getSchema(collection, ImportAudit.FULL_SCHEMA).withTableMetadataCodes(asList(
				TemporaryRecord.DEFAULT_SCHEMA + "_" + Schemas.CREATED_BY.getLocalCode(),
				TemporaryRecord.DEFAULT_SCHEMA + "_" + Schemas.CREATED_ON.getLocalCode(),
				TemporaryRecord.DEFAULT_SCHEMA + "_" + TemporaryRecord.DESTRUCTION_DATE,
				TemporaryRecord.DEFAULT_SCHEMA + "_" + TemporaryRecord.CONTENT)));

		manager.saveSchema(manager.getSchema(collection, ExportAudit.FULL_SCHEMA).withTableMetadataCodes(asList(
				TemporaryRecord.DEFAULT_SCHEMA + "_" + Schemas.CREATED_BY.getLocalCode(),
				TemporaryRecord.DEFAULT_SCHEMA + "_" + Schemas.CREATED_ON.getLocalCode(),
				TemporaryRecord.DEFAULT_SCHEMA + "_" + TemporaryRecord.DESTRUCTION_DATE,
				TemporaryRecord.DEFAULT_SCHEMA + "_" + TemporaryRecord.CONTENT)));
	}

	class CoreSchemaAlterationFor_7_6_deleteStep extends MetadataSchemasAlterationHelper {

		protected CoreSchemaAlterationFor_7_6_deleteStep(String collection,
														 MigrationResourcesProvider migrationResourcesProvider,
														 AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaBuilder importAuditSchema = typesBuilder.getSchema(ImportAudit.FULL_SCHEMA);
			importAuditSchema.deleteMetadataWithoutValidation(ImportAudit.ERRORS);
		}
	}

	class CoreSchemaAlterationFor_7_6 extends MetadataSchemasAlterationHelper {

		protected CoreSchemaAlterationFor_7_6(String collection, MigrationResourcesProvider migrationResourcesProvider,
											  AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaBuilder builder = typesBuilder.createNewSchemaTypeWithSecurity(Capsule.SCHEMA_TYPE).getDefaultSchema();
			builder.create(Capsule.CODE).setType(MetadataValueType.STRING);
			builder.create(Capsule.HTML).setType(MetadataValueType.TEXT);
			builder.create(Capsule.KEYWORDS).setType(STRING).setMultivalue(true);

			MetadataSchemaBuilder importAuditSchema = typesBuilder.getSchema(ImportAudit.FULL_SCHEMA);
			importAuditSchema.createUndeletable(ImportAudit.ERRORS).setType(MetadataValueType.TEXT).setSystemReserved(true);
		}
	}
}
