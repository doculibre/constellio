package com.constellio.app.services.migrations.scripts;

import java.util.ArrayList;
import java.util.List;

import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.migrations.CoreRoles;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.security.roles.RolesManager;

public class CoreMigrationTo_5_1_2 implements MigrationScript {
	@Override
	public String getVersion() {
		return "5.1.2";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider provider, AppLayerFactory appLayerFactory)
			throws Exception {
		setupRoles(collection, appLayerFactory.getModelLayerFactory().getRolesManager(), provider);
	}

	private void setupRoles(String collection, RolesManager manager, MigrationResourcesProvider provider) {
		String label = provider.getDefaultLanguageString("roles.administrator");
		List<String> permissions = new ArrayList<>(CorePermissions.PERMISSIONS.getAll());
		manager.addRole(new Role(collection, CoreRoles.ADMINISTRATOR, label, permissions));
	}
}
