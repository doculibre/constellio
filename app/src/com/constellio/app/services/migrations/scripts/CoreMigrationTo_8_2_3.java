package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.security.Role;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.security.roles.RolesManager;

import java.util.List;
import java.util.Set;

import static com.constellio.model.entities.schemas.MetadataValueType.BOOLEAN;
import static com.constellio.model.entities.schemas.MetadataValueType.INTEGER;
import static com.constellio.model.entities.schemas.MetadataValueType.NUMBER;
import static java.util.Arrays.asList;

public class CoreMigrationTo_8_2_3 implements MigrationScript {

	@Override
	public String getVersion() {
		return "8.2.3";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {
		updatePermissions(appLayerFactory, collection);
	}

	private void updatePermissions(AppLayerFactory appLayerFactory, String collection) {
		RolesManager roleManager = appLayerFactory.getModelLayerFactory().getRolesManager();
		List<Role> allRoles = roleManager.getAllRoles(collection);
		for (Role role : allRoles) {
			if (roleManager.hasPermission(collection, role.getCode(), CorePermissions.MANAGE_SYSTEM_UPDATES)) {
				roleManager.updateRole(role.withNewPermissions(asList(CorePermissions.VIEW_SYSTEM_STATE)));
			}
		}
	}
}
