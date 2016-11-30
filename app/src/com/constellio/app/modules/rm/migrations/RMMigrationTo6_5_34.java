package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.constants.RMRoles;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.security.roles.RolesManager;

import java.util.List;

import static com.constellio.model.entities.CorePermissions.USE_EXTERNAL_APIS_FOR_COLLECTION;
import static java.util.Arrays.asList;

public class RMMigrationTo6_5_34 implements MigrationScript {

	@Override
	public String getVersion() {
		return "6.5.34";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider provider, AppLayerFactory appLayerFactory)
			throws Exception {
		setupRoles(collection, appLayerFactory.getModelLayerFactory().getRolesManager(), provider);

	}

	private void setupRoles(String collection, RolesManager manager, MigrationResourcesProvider provider) {
		List<Role> roleList = manager.getAllRoles(collection);
		for(Role role: roleList) {
			manager.updateRole(role.withNewPermissions(asList(RMPermissionsTo.PUBLISH_AND_UNPUBLISH_DOCUMENTS)));
		}
	}

}
