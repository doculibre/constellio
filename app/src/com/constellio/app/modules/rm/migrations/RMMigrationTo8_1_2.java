package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.constants.RMRoles;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Email;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

import java.util.ArrayList;
import java.util.List;

public class RMMigrationTo8_1_2 extends MigrationHelper implements MigrationScript {
	@Override
	public String getVersion() {
		return "8.1.2";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {
		alterRoles(collection, appLayerFactory.getModelLayerFactory());
	}

	private void alterRoles(String collection, ModelLayerFactory modelLayerFactory) {
		Role RGDrole = modelLayerFactory.getRolesManager().getRole(collection, RMRoles.RGD);
		List<String> newPermissions = new ArrayList<>();
		newPermissions.add(RMPermissionsTo.RETURN_OTHER_USERS_FOLDERS);
		modelLayerFactory.getRolesManager().updateRole(RGDrole.withNewPermissions(newPermissions));
	}
}
