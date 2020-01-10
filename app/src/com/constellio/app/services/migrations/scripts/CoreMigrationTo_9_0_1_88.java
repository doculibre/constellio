package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.migrations.CoreRoles;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.security.roles.RolesManager;

import java.util.Arrays;

public class CoreMigrationTo_9_0_1_88 extends MigrationHelper implements MigrationScript {

	@Override
	public String getVersion() {
		return "9.0.1.88";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory) throws Exception {

		RolesManager rolesManager = appLayerFactory.getModelLayerFactory().getRolesManager();
		Role adminRole = rolesManager.getRole(collection, CoreRoles.ADMINISTRATOR);

		rolesManager.updateRole(adminRole.withNewPermissions(Arrays.asList(CorePermissions.EDIT_ALL_ANNOTATION)));
	}

}
