package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.security.roles.RolesManager;

import java.util.ArrayList;
import java.util.List;
import com.constellio.app.modules.rm.constants.RMRoles;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.migrations.CoreRoles;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.factories.ModelLayerFactory;

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
		ModelLayerFactory modelLayerFactory = appLayerFactory.getModelLayerFactory();

		Role rgbRole = modelLayerFactory.getRolesManager().getRole(collection, RMRoles.RGD);
		Role admRole = modelLayerFactory.getRolesManager().getRole(collection, CoreRoles.ADMINISTRATOR);
		modelLayerFactory.getRolesManager().updateRole(rgbRole.withNewPermissions(asList(CorePermissions.BATCH_PROCESS,
				RMPermissionsTo.CONSULT_RETENTIONRULE, RMPermissionsTo.CONSULT_CLASSIFICATION_PLAN,
				RMPermissionsTo.CREATE_DECOMMISSIONING_LIST, RMPermissionsTo.CART_BATCH_DELETE)));

		modelLayerFactory.getRolesManager().updateRole(admRole.withNewPermissions(asList(
				RMPermissionsTo.CONSULT_RETENTIONRULE, RMPermissionsTo.CONSULT_CLASSIFICATION_PLAN,
				RMPermissionsTo.CREATE_DECOMMISSIONING_LIST, RMPermissionsTo.CART_BATCH_DELETE)));

		RolesManager rolesManager = appLayerFactory.getModelLayerFactory().getRolesManager();

		List<Role> roleList = rolesManager.getAllRoles(collection);

		for (Role role : roleList) {
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
