package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.security.roles.RolesManager;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

public class RMMigrationTo8_3 implements MigrationScript {
	public static final String USE_CART_OLD_PERMISSION = "rm.useCart";

	@Override
	public String getVersion() {
		return "8.3";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {
		RolesManager rolesManager = appLayerFactory.getModelLayerFactory().getRolesManager();
		List<Role> roleList = rolesManager.getAllRoles(collection);

		for (Role role : roleList) {
			boolean oldPermission = role.hasOperationPermission(USE_CART_OLD_PERMISSION);
			if (role.hasOperationPermission(RMPermissionsTo.USE_MY_CART) || oldPermission) {
				rolesManager.updateRole(role.withNewPermissions(asList(RMPermissionsTo.USE_GROUP_CART)));
				if (oldPermission) {
					Role newRole = rolesManager.getRole(collection, role.getCode());
					List<String> permissions = new ArrayList<>(newRole.getOperationPermissions());

					if (!role.hasOperationPermission(RMPermissionsTo.USE_MY_CART)) {
						permissions.add(RMPermissionsTo.USE_MY_CART);
					}

					permissions.remove(USE_CART_OLD_PERMISSION);
					rolesManager.updateRole(newRole.withPermissions(permissions));
				}
			}

			List<String> newPermissions = new ArrayList<>();
			newPermissions.add(RMPermissionsTo.CART_BATCH_DELETE);
			newPermissions.add(CorePermissions.MODIFY_RECORDS_USING_BATCH_PROCESS);
			if (role.hasOperationPermission(RMPermissionsTo.MANAGE_RETENTIONRULE)) {
				newPermissions.add(RMPermissionsTo.CONSULT_RETENTIONRULE);
			}
			if (role.hasOperationPermission(RMPermissionsTo.MANAGE_CLASSIFICATION_PLAN)) {
				newPermissions.add(RMPermissionsTo.CONSULT_CLASSIFICATION_PLAN);
			}
			if (role.hasOperationPermission(RMPermissionsTo.PROCESS_DECOMMISSIONING_LIST)) {
				newPermissions.add(RMPermissionsTo.CREATE_DECOMMISSIONING_LIST);
			}
			if (role.hasOperationPermission(RMPermissionsTo.USE_CART)) {
				newPermissions.add(RMPermissionsTo.USE_GROUP_CART);
			}
			role = role.withNewPermissions(newPermissions);
			rolesManager.updateRole(role);
		}

	}
}