package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.EmailToSend;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.security.roles.RolesManager;

import java.util.List;

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
		appLayerFactory.getSystemGlobalConfigsManager().setReindexingRequired(true);
		new CoreSchemaAlterationFor_8_2_3(collection, migrationResourcesProvider, appLayerFactory).migrate();

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

	class CoreSchemaAlterationFor_8_2_3 extends MetadataSchemasAlterationHelper {

		protected CoreSchemaAlterationFor_8_2_3(String collection,
											  MigrationResourcesProvider migrationResourcesProvider,
											  AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaBuilder emailToSendSchema = typesBuilder.getDefaultSchema(EmailToSend.SCHEMA_TYPE);
			emailToSendSchema.createUndeletable(EmailToSend.BODY).setType(MetadataValueType.TEXT);
			emailToSendSchema.createUndeletable(EmailToSend.LINKED_FILES).setType(MetadataValueType.CONTENT).setMultivalue(true);
		}
	}
}
