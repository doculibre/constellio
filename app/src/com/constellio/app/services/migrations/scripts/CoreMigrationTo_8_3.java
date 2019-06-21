package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.constants.RMRoles;
import com.constellio.app.modules.tasks.TasksPermissionsTo;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.migrations.CoreRoles;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.security.Role;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.security.roles.RolesManager;

import java.util.List;

import static com.constellio.model.entities.security.global.UserCredential.HAS_READ_LAST_ALERT;
import static java.util.Arrays.asList;

public class CoreMigrationTo_8_3 implements MigrationScript {

	@Override
	public String getVersion() {
		return "8.3";
	}


	@Override
	public void migrate(String collection, MigrationResourcesProvider provider,
						AppLayerFactory appLayerFactory)
			throws Exception {

		if (Collection.SYSTEM_COLLECTION.equals(collection)) {
			new CoreSchemaAlterationFor8_3(collection, provider, appLayerFactory).migrate();
		}

		updateRole(appLayerFactory, collection);
	}

	public void updateRole(AppLayerFactory factory, String collection) {
		RolesManager rolesManager = factory.getModelLayerFactory().getRolesManager();

		Role admRole = rolesManager.getRole(collection, CoreRoles.ADMINISTRATOR);
		rolesManager.updateRole(admRole.withNewPermissions(asList(CorePermissions.BATCH_PROCESS)));

		List<Role> allRoles = rolesManager.getAllRoles(collection);
		for (Role role : allRoles) {
			if (!role.getCode().equals(RMRoles.USER)) {
				rolesManager.updateRole(role.withNewPermissions(asList(CorePermissions.VIEW_LOGIN_NOTIFICATION_STATE_ALERT)));
			}
		}
	}


	private class CoreSchemaAlterationFor8_3 extends MetadataSchemasAlterationHelper {
		public CoreSchemaAlterationFor8_3(String collection, MigrationResourcesProvider migrationResourcesProvider,
										  AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder builder) {
			if (!builder.getDefaultSchema(UserCredential.SCHEMA_TYPE).hasMetadata(HAS_READ_LAST_ALERT)) {
				builder.getDefaultSchema(UserCredential.SCHEMA_TYPE)
						.createUndeletable(HAS_READ_LAST_ALERT).setType(MetadataValueType.BOOLEAN);
			}
		}
	}


}
