package com.constellio.app.modules.rm.migrations;

import org.apache.poi.hslf.record.RecordContainer;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class RMMigrationTo6_5_32 implements MigrationScript {

	@Override
	public String getVersion() {
		return "6.5.32";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider provider, AppLayerFactory appLayerFactory)
			throws Exception {

		new SchemaAlterationsFor6_5_32(collection, provider, appLayerFactory).migrate();
		SchemasDisplayManager schemaDisplayManager = appLayerFactory.getMetadataSchemasDisplayManager();
		SchemaTypesDisplayTransactionBuilder transaction = schemaDisplayManager.newTransactionBuilderFor(collection);
		transaction.in(ContainerRecord.SCHEMA_TYPE).addToDisplay(ContainerRecord.ADMINISTRATIVE_UNITS)
				.afterMetadata(ContainerRecord.ADMINISTRATIVE_UNITS);

		schemaDisplayManager.execute(transaction.build());

	}

	public static class SchemaAlterationsFor6_5_32 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationsFor6_5_32(String collection, MigrationResourcesProvider provider, AppLayerFactory factory) {
			super(collection, provider, factory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaTypeBuilder adminUnitSchemaType = types().getSchemaType(AdministrativeUnit.SCHEMA_TYPE);

			MetadataSchemaBuilder containerDefaultSchema = types().getSchema(ContainerRecord.DEFAULT_SCHEMA);
			containerDefaultSchema.create(ContainerRecord.ADMINISTRATIVE_UNITS)
					.setMultivalue(true).defineReferencesTo(adminUnitSchemaType);
		}
	}

}
