package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.constants.RMRoles;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.security.roles.RolesManager;

import static com.constellio.app.services.migrations.CoreRoles.ADMINISTRATOR;
import static com.constellio.model.entities.CorePermissions.USE_EXTERNAL_APIS_FOR_COLLECTION;
import static java.util.Arrays.asList;

public class RMMigrationTo6_5_33 implements MigrationScript {

	@Override
	public String getVersion() {
		return "6.5.33";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider provider, AppLayerFactory appLayerFactory)
			throws Exception {

		new SchemaAlterationsFor6_5_33(collection, provider, appLayerFactory).migrate();
		SchemasDisplayManager schemaDisplayManager = appLayerFactory.getMetadataSchemasDisplayManager();
		SchemaTypesDisplayTransactionBuilder transaction = schemaDisplayManager.newTransactionBuilderFor(collection);
		transaction.in(ContainerRecord.SCHEMA_TYPE).addToDisplay(ContainerRecord.ADMINISTRATIVE_UNITS)
				.afterMetadata(ContainerRecord.ADMINISTRATIVE_UNITS);

		schemaDisplayManager.execute(transaction.build());
		setupRoles(collection, appLayerFactory.getModelLayerFactory().getRolesManager(), provider);

	}

	private void setupRoles(String collection, RolesManager manager, MigrationResourcesProvider provider) {
		manager.updateRole(
				manager.getRole(collection, RMRoles.RGD).withNewPermissions(asList(USE_EXTERNAL_APIS_FOR_COLLECTION)));
	}

	public static class SchemaAlterationsFor6_5_33 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationsFor6_5_33(String collection, MigrationResourcesProvider provider, AppLayerFactory factory) {
			super(collection, provider, factory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaTypeBuilder adminUnitSchemaType = types().getSchemaType(AdministrativeUnit.SCHEMA_TYPE);

			MetadataSchemaBuilder containerDefaultSchema = types().getSchema(ContainerRecord.DEFAULT_SCHEMA);
			if (!containerDefaultSchema.hasMetadata(ContainerRecord.ADMINISTRATIVE_UNITS)) {
				containerDefaultSchema.create(ContainerRecord.ADMINISTRATIVE_UNITS)
						.setMultivalue(true).defineReferencesTo(adminUnitSchemaType);
			}
		}
	}

}
