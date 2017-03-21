package com.constellio.app.services.migrations.scripts;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.reports.wrapper.Printable;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.migrations.CoreRoles;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.security.Role;
import com.constellio.model.entities.security.global.SolrUserCredential;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class CoreMigrationTo_7_1 implements MigrationScript {

	private static final Logger LOGGER = LoggerFactory.getLogger(CoreMigrationTo_7_1.class);

	@Override
	public String getVersion() {
		return "7.1";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory)
			throws Exception {
		givenNewPermissionsToRGDandADMRoles(collection, appLayerFactory.getModelLayerFactory());
		new CoreSchemaAlterationFor7_1(collection, migrationResourcesProvider, appLayerFactory).migrate();

		appLayerFactory.getSystemGlobalConfigsManager().setReindexingRequired(true);

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
            MetadataSchemaBuilder builder = typesBuilder.createNewSchemaType(Printable.SCHEMA_TYPE).getDefaultSchema();
            builder.create(Printable.JASPERFILE).setType(MetadataValueType.CONTENT).setUndeletable(true).setEssential(true).defineDataEntry().asManual();
            builder.create(Printable.ISDELETABLE).setType(MetadataValueType.BOOLEAN).setUndeletable(true).setDefaultValue(true).defineDataEntry().asManual();

            MetadataSchemaBuilder UserBuilder = typesBuilder.getSchemaType(User.SCHEMA_TYPE).getDefaultSchema();
            UserBuilder.create(User.FAX).setEssential(false).setType(MetadataValueType.STRING).defineDataEntry().asManual();
            UserBuilder.create(User.ADDRESS).setEssential(false).setType(MetadataValueType.STRING).defineDataEntry().asManual();

            if (typesBuilder.getCollection().equals(Collection.SYSTEM_COLLECTION)) {
                MetadataSchemaBuilder UserCredentialBuilder = typesBuilder.getSchemaType(SolrUserCredential.SCHEMA_TYPE).getDefaultSchema();
                UserCredentialBuilder.create(SolrUserCredential.ADDRESS).setEssential(false).setType(MetadataValueType.STRING).defineDataEntry().asManual();
                UserCredentialBuilder.create(SolrUserCredential.FAX).setEssential(false).setType(MetadataValueType.STRING).defineDataEntry().asManual();
                UserCredentialBuilder.create(SolrUserCredential.JOB_TITLE).setEssential(false).setType(MetadataValueType.STRING).defineDataEntry().asManual();
                UserCredentialBuilder.create(SolrUserCredential.PHONE).setEssential(false).setType(MetadataValueType.STRING).defineDataEntry().asManual();
            }
        }
    }
}
