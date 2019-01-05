package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.constants.RMRoles;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.security.roles.RolesManager;

public class RMMigrationTo8_1_1 extends MigrationHelper implements MigrationScript {
	@Override
	public String getVersion() {
		return "8.1.1";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory) {
		setupRoles(collection, appLayerFactory.getModelLayerFactory(), migrationResourcesProvider);
	}


	private void setupRoles(String collection, ModelLayerFactory modelLayerFactory,
							MigrationResourcesProvider migrationResourcesProvider) {
		RolesManager rolesManager = modelLayerFactory.getRolesManager();
		Role userRole = rolesManager.getRole(collection, RMRoles.USER);
		userRole = userRole.withTitle(migrationResourcesProvider.getValuesOfAllLanguagesWithSeparator("init.roles.U", " / "));
		Role managerRole = rolesManager.getRole(collection, RMRoles.MANAGER);
		managerRole = managerRole.withTitle(migrationResourcesProvider.getValuesOfAllLanguagesWithSeparator("init.roles.M", " / "));
		Role rgdRole = rolesManager.getRole(collection, RMRoles.RGD);
		rgdRole = rgdRole.withTitle(migrationResourcesProvider.getValuesOfAllLanguagesWithSeparator("init.roles.RGD", " / "));

		rolesManager.updateRole(userRole);
		rolesManager.updateRole(managerRole);
		rolesManager.updateRole(rgdRole);
	}
}

