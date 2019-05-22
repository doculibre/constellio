package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.constants.RMRoles;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.security.roles.RolesManager;

import java.util.List;

import static java.util.Arrays.asList;

/**
 * Created by Constelio on 2016-11-04.
 */
public class RMMigrationTo6_5_21 implements MigrationScript {

	@Override
	public String getVersion() {
		return "6.5.21";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider provider, AppLayerFactory appLayerFactory)
			throws Exception {

		addUseCartPermissionToAllRoles(collection, appLayerFactory);
	}

	private void addUseCartPermissionToAllRoles(String collection, AppLayerFactory appLayerFactory) {
		RolesManager rolesManager = appLayerFactory.getModelLayerFactory().getRolesManager();

		List<Role> roleList = rolesManager.getAllRoles(collection);
		for (Role role : roleList) {
			Role editedRole = role.withNewPermissions(asList(RMPermissionsTo.USE_MY_CART));
			if (editedRole.getCode().equals(RMRoles.RGD)) {
				editedRole = editedRole.withNewPermissions(asList(CorePermissions.USE_EXTERNAL_APIS_FOR_COLLECTION));
			}
			rolesManager.updateRole(editedRole);
		}
	}
}
