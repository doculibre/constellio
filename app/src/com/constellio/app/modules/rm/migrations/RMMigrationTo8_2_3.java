package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.model.calculators.document.DocumentCaptionCalculator;
import com.constellio.app.modules.rm.model.calculators.folder.FolderCaptionCalculator;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.SIParchive;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.TemporaryRecord;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

import static java.util.Arrays.asList;

public class RMMigrationTo8_2_3 extends MigrationHelper implements MigrationScript {
	@Override
	public String getVersion() {
		return "8.2.3";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {
		migrateRoles(collection, appLayerFactory, new RolesAlteration() {

			@Override
			public Role alter(Role role) {
				if (role.hasOperationPermission(RMPermissionsTo.MANAGE_FOLDER_AUTHORIZATIONS)) {
					return role.withNewPermissions(asList(RMPermissionsTo.VIEW_FOLDER_AUTHORIZATIONS));
				}

				if (role.hasOperationPermission(RMPermissionsTo.MANAGE_DOCUMENT_AUTHORIZATIONS)) {
					return role.withNewPermissions(asList(RMPermissionsTo.VIEW_DOCUMENT_AUTHORIZATIONS));
				}

				return role;
			}
		});
	}
}
