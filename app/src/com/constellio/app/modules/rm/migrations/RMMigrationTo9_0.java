package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.security.roles.RolesManager;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

public class RMMigrationTo9_0 implements MigrationScript {
	public static final String USE_CART_OLD_PERMISSION = "rm.useCart";

	@Override
	public String getVersion() {
		return "9.0";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory)
			throws Exception {
		RolesManager rolesManager = appLayerFactory.getModelLayerFactory().getRolesManager();


		List<Role> roleList1 = rolesManager.getAllRoles(collection);

		for(Role role : roleList1) {
			boolean oldPermission = role.hasOperationPermission(USE_CART_OLD_PERMISSION);
			if(role.hasOperationPermission(RMPermissionsTo.USE_MY_CART) || oldPermission) {
				rolesManager.updateRole(role.withNewPermissions(asList(RMPermissionsTo.USE_GROUP_CART)));

				if(oldPermission) {
					Role newRole = rolesManager.getRole(collection, role.getCode());
					List<String> permissions = new ArrayList<>(newRole.getOperationPermissions());

					if(!role.hasOperationPermission(RMPermissionsTo.USE_MY_CART)) {
						permissions.add(RMPermissionsTo.USE_MY_CART);
					}

					permissions.remove(USE_CART_OLD_PERMISSION);
					rolesManager.updateRole(newRole.withPermissions(permissions));
				}
			}
		}
	}
}
