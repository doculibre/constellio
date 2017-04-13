package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.constants.RMRoles;
import com.constellio.app.modules.rm.model.validators.MediumTypeValidator;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.type.MediumType;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

import static java.util.Arrays.asList;

/**
 * Created by constellios on 2017-04-04.
 */
public class RMMigrationTo7_1_3_1 implements MigrationScript {

    public String getVersion() {
        return "7.1.3.1";
    }

    @Override
    public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory) throws Exception {
        new SchemaAlterationFor7_1_3_1(collection, migrationResourcesProvider, appLayerFactory).migrate();
        updateNewPermissions(appLayerFactory, collection);
    }

    public void migration()
    {

    }

    private void updateNewPermissions(AppLayerFactory appLayerFactory, String collection) {
        ModelLayerFactory modelLayerFactory = appLayerFactory.getModelLayerFactory();

        Role admRole = modelLayerFactory.getRolesManager().getRole(collection, RMRoles.RGD);
        modelLayerFactory.getRolesManager().updateRole(admRole.withNewPermissions(asList(CorePermissions.MANAGE_SEARCH_BOOST)));
    }

    class SchemaAlterationFor7_1_3_1 extends MetadataSchemasAlterationHelper {

        protected SchemaAlterationFor7_1_3_1(String collection, MigrationResourcesProvider migrationResourcesProvider,
                                           AppLayerFactory appLayerFactory) {
            super(collection, migrationResourcesProvider, appLayerFactory);
        }

        public String getVersion() {
            return "7.1.3.1";
        }

        @Override
        protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
            migrateLabel(typesBuilder);
            typesBuilder.getSchema(Folder.DEFAULT_SCHEMA).get(Folder.TITLE).setSortable(true);

        }

        private void migrateLabel(MetadataSchemaTypesBuilder typesBuilder) {
            typesBuilder.getSchema(Folder.DEFAULT_SCHEMA).get(Folder.EXPECTED_TRANSFER_DATE).addLabel(Language.French, "Date de transfert prévue");
            typesBuilder.getSchema(Folder.DEFAULT_SCHEMA).get(Folder.EXPECTED_DEPOSIT_DATE).addLabel(Language.French, "Date de versement prévue");
            typesBuilder.getSchema(Folder.DEFAULT_SCHEMA).get(Folder.EXPECTED_DESTRUCTION_DATE).addLabel(Language.French, "Date de destruction prévue");

        }
    }
}
