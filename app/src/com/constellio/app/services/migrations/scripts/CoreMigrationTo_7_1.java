package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.entities.schemasDisplay.SchemaDisplayConfig;
import com.constellio.app.modules.rm.wrappers.Printable;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.migrations.CoreRoles;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.Role;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.schemas.validators.JasperFilePrintableValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class CoreMigrationTo_7_1 implements MigrationScript {

	private static final Logger LOGGER = LoggerFactory.getLogger(CoreMigrationTo_7_1.class);

	@Override
	public String getVersion() {
		return "7.1";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {
		givenNewPermissionsToRGDandADMRoles(collection, appLayerFactory.getModelLayerFactory());
		new CoreSchemaAlterationFor7_1(collection, migrationResourcesProvider, appLayerFactory).migrate();

		appLayerFactory.getSystemGlobalConfigsManager().setReindexingRequired(true);

		SchemasDisplayManager manager = appLayerFactory.getMetadataSchemasDisplayManager();
		SchemaDisplayConfig schemaDisplayUserConfig = MigrationHelper.order(collection, appLayerFactory, "display",
				manager.getSchema(collection, User.DEFAULT_SCHEMA),
				User.USERNAME,
				User.FIRSTNAME,
				User.LASTNAME,
				Schemas.TITLE.getLocalCode(),
				User.EMAIL,
				User.ROLES,
				User.GROUPS,
				User.JOB_TITLE,
				User.PHONE,
				User.STATUS,
				Schemas.CREATED_ON.getLocalCode(),
				Schemas.MODIFIED_ON.getLocalCode(),
				User.ALL_ROLES);
		manager.saveSchema(schemaDisplayUserConfig);

	}

	private void givenNewPermissionsToRGDandADMRoles(String collection, ModelLayerFactory modelLayerFactory) {
		Role admRole = modelLayerFactory.getRolesManager().getRole(collection, CoreRoles.ADMINISTRATOR);
		List<String> newAdmPermissions = new ArrayList<>();
		newAdmPermissions.add(CorePermissions.MANAGE_LABELS);
		modelLayerFactory.getRolesManager().updateRole(admRole.withNewPermissions(newAdmPermissions));
	}

	private class CoreSchemaAlterationFor7_1 extends MetadataSchemasAlterationHelper {
		public CoreSchemaAlterationFor7_1(String collection, MigrationResourcesProvider migrationResourcesProvider,
										  AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaBuilder builder = typesBuilder.createNewSchemaTypeWithSecurity(Printable.SCHEMA_TYPE).getDefaultSchema();
			builder.create(Printable.JASPERFILE).setType(MetadataValueType.CONTENT).setUndeletable(true).setEssential(true)
					.defineDataEntry().asManual()
					.addValidator(JasperFilePrintableValidator.class);
			builder.create(Printable.ISDELETABLE).setType(MetadataValueType.BOOLEAN).setUndeletable(true).setDefaultValue(true)
					.defineDataEntry().asManual();
			builder.get(Printable.TITLE).setMultiLingual(true);

			MetadataSchemaBuilder UserBuilder = typesBuilder.getSchemaType(User.SCHEMA_TYPE).getDefaultSchema();
			UserBuilder.create(User.FAX).setEssential(false).setType(MetadataValueType.STRING).defineDataEntry().asManual();
			UserBuilder.create(User.ADDRESS).setEssential(false).setType(MetadataValueType.STRING).defineDataEntry().asManual();

			if (typesBuilder.getCollection().equals(Collection.SYSTEM_COLLECTION)) {
				MetadataSchemaBuilder UserCredentialBuilder = typesBuilder.getSchemaType(UserCredential.SCHEMA_TYPE)
						.getDefaultSchema();
				UserCredentialBuilder.create(UserCredential.ADDRESS).setEssential(false).setType(MetadataValueType.STRING)
						.defineDataEntry().asManual();
				UserCredentialBuilder.create(UserCredential.FAX).setEssential(false).setType(MetadataValueType.STRING)
						.defineDataEntry().asManual();
				UserCredentialBuilder.create(UserCredential.JOB_TITLE).setEssential(false).setType(MetadataValueType.STRING)
						.defineDataEntry().asManual();
				UserCredentialBuilder.create(UserCredential.PHONE).setEssential(false).setType(MetadataValueType.STRING)
						.defineDataEntry().asManual();
			}
		}
	}
}
