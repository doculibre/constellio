package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.constants.RMRoles;
import com.constellio.app.modules.rm.model.validators.FolderValidator;
import com.constellio.app.modules.rm.wrappers.Cart;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.builders.CommonMetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class RMMigrationTo6_5 implements MigrationScript {
	@Override
	public String getVersion() {
		return "6.5";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider provider, AppLayerFactory factory)
			throws Exception {
		new SchemaAlterationsFor6_5(collection, provider, factory).migrate();

		givenNewPermissionsToRGDRole(collection,factory.getModelLayerFactory());

	}

	private void givenNewPermissionsToRGDRole(String collection, ModelLayerFactory modelLayerFactory) {
		Role rgdRole = modelLayerFactory.getRolesManager().getRole(collection, RMRoles.RGD);
		List<String> newRgdPermissions = new ArrayList<>();
		newRgdPermissions.addAll(RMPermissionsTo.PERMISSIONS.getAll());
		newRgdPermissions.addAll(CorePermissions.PERMISSIONS.getAll());
		modelLayerFactory.getRolesManager().updateRole(rgdRole.withPermissions(newRgdPermissions));
	}

	public static class SchemaAlterationsFor6_5 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationsFor6_5(String collection, MigrationResourcesProvider provider, AppLayerFactory factory) {
			super(collection, provider, factory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {

		}
	}
}
