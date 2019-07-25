package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.security.roles.RolesManager;

import java.util.List;

import static java.util.Arrays.asList;

public class CoreMigrationTo_9_0 implements MigrationScript {

	@Override
	public String getVersion() {
		return "9.0";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory)
			throws Exception {
		ModelLayerFactory modelLayerFactory = appLayerFactory.getModelLayerFactory();


		RolesManager rolesManager = modelLayerFactory.getRolesManager();
		List<Role> roleList = rolesManager.getAllRoles(collection);

		for(Role role : roleList){
			rolesManager.updateRole(role.withNewPermissions(asList(CorePermissions.MODIFY_RECORDS_USING_BATCH_PROCESS)));
		}

		modelLayerFactory.getCollectionsListManager().giveCollectionIdToCollectionThatDontHaveOne();
	}
}
