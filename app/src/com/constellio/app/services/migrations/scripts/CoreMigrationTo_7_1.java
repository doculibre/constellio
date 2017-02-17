package com.constellio.app.services.migrations.scripts;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import com.constellio.app.modules.reports.wrapper.Printable;
import com.constellio.app.modules.rm.constants.RMRoles;
import com.constellio.app.services.migrations.CoreRoles;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.factories.ModelLayerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

import java.util.ArrayList;
import java.util.List;

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
        }
    }
}
