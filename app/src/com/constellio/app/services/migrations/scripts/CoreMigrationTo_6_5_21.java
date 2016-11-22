package com.constellio.app.services.migrations.scripts;

import static com.constellio.app.services.migrations.CoreRoles.ADMINISTRATOR;
import static com.constellio.model.entities.CorePermissions.USE_EXTERNAL_APIS_FOR_COLLECTION;
import static java.util.Arrays.asList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.services.security.roles.RolesManager;

public class CoreMigrationTo_6_5_21 implements MigrationScript {
	private final static Logger LOGGER = LoggerFactory.getLogger(CoreMigrationTo_6_5_21.class);

	@Override
	public String getVersion() {
		return "6.5.21";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider provider, AppLayerFactory appLayerFactory)
			throws Exception {
		setupRoles(collection, appLayerFactory.getModelLayerFactory().getRolesManager(), provider);
	}

	private void setupRoles(String collection, RolesManager manager, MigrationResourcesProvider provider) {
		manager.updateRole(
				manager.getRole(collection, ADMINISTRATOR).withNewPermissions(asList(USE_EXTERNAL_APIS_FOR_COLLECTION)));
	}

}
