package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.migrations.CoreRoles;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.security.roles.RolesManager;

import java.util.Arrays;

public class RMMigrationTo9_0_0_88 implements MigrationScript {

	@Override
	public String getVersion() {
		return "9.0.0.88";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory) throws Exception {

		RolesManager rolesManager = appLayerFactory.getModelLayerFactory().getRolesManager();
		Role adminRole = rolesManager.getRole(collection, CoreRoles.ADMINISTRATOR);

		rolesManager.updateRole(adminRole.withNewPermissions(Arrays.asList(RMPermissionsTo.EDIT_ALL_ANNOTATION)));
	}
}
