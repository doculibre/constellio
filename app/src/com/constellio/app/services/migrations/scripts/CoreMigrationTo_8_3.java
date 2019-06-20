package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.migrations.CoreRoles;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.security.Role;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

import static com.constellio.model.entities.security.global.UserCredential.HAS_AGREED_TO_PRIVACY_POLICY;
import static com.constellio.model.entities.security.global.UserCredential.HAS_SEEN_MESSAGE;
import static java.util.Arrays.asList;

public class CoreMigrationTo_8_3 implements MigrationScript {

	@Override
	public String getVersion() {
		return "8.3";
	}





	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory)
			throws Exception {
		ModelLayerFactory modelLayerFactory = appLayerFactory.getModelLayerFactory();

		Role admRole = modelLayerFactory.getRolesManager().getRole(collection, CoreRoles.ADMINISTRATOR);
		modelLayerFactory.getRolesManager().updateRole(admRole.withNewPermissions(asList(CorePermissions.BATCH_PROCESS)));
	}


	private class CoreSchemaAlterationFor8_1 extends MetadataSchemasAlterationHelper {
		public CoreSchemaAlterationFor8_1(String collection, MigrationResourcesProvider migrationResourcesProvider,
										  AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder builder) {

				builder.getDefaultSchema(UserCredential.SCHEMA_TYPE)
						.createUndeletable(HAS_SEEN_MESSAGE).setType(MetadataValueType.BOOLEAN);

		}
	}























}
