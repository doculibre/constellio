package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.security.roles.RolesManager;

import java.util.ArrayList;
import java.util.List;

public class RMMigrationTo8_3_2_2 implements MigrationScript {

	@Override
	public String getVersion() {
		return "8.3.2.2";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider provider, AppLayerFactory appLayerFactory)
			throws Exception {

		RolesManager rolesManager = appLayerFactory.getModelLayerFactory().getRolesManager();
		List<Role> roleList = rolesManager.getAllRoles(collection);

		for (Role role : roleList) {
			if (role.hasOperationPermission(RMMigrationTo8_3.USE_CART_OLD_PERMISSION)) {
				Role newRole = rolesManager.getRole(collection, role.getCode());

				List<String> permissions = new ArrayList<>(newRole.getOperationPermissions());
				permissions.add(RMPermissionsTo.USE_GROUP_CART);

				if (!role.hasOperationPermission(RMPermissionsTo.USE_MY_CART)) {
					permissions.add(RMPermissionsTo.USE_MY_CART);
				}

				permissions.remove(RMMigrationTo8_3.USE_CART_OLD_PERMISSION);
				rolesManager.updateRole(newRole.withPermissions(permissions));
			}
		}

	}
}

