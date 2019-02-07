package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.constants.RMRoles;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.migrations.CoreRoles;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.factories.ModelLayerFactory;

import static java.util.Arrays.asList;

public class RMMigrationTo_9_0 implements MigrationScript {
	@Override
	public String getVersion() {
		return "9.0";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory)
			throws Exception {
		ModelLayerFactory modelLayerFactory = appLayerFactory.getModelLayerFactory();

		Role rgbRole = modelLayerFactory.getRolesManager().getRole(collection, RMRoles.RGD);
		Role admRole = modelLayerFactory.getRolesManager().getRole(collection, CoreRoles.ADMINISTRATOR);
		modelLayerFactory.getRolesManager().updateRole(rgbRole.withNewPermissions(asList(CorePermissions.BATCH_PROCESS,
				RMPermissionsTo.CONSULT_RETENTIONRULE, RMPermissionsTo.CONSULT_CLASSIFICATION_PLAN, RMPermissionsTo.CREATE_DECOMMISSIONING_LIST)));

		modelLayerFactory.getRolesManager().updateRole(admRole.withNewPermissions(asList(
				RMPermissionsTo.CONSULT_RETENTIONRULE, RMPermissionsTo.CONSULT_CLASSIFICATION_PLAN, RMPermissionsTo.CREATE_DECOMMISSIONING_LIST)));
	}
}
