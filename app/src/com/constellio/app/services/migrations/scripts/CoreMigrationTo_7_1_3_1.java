package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.migrations.CoreRoles;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.factories.ModelLayerFactory;

import static java.util.Arrays.asList;

public class CoreMigrationTo_7_1_3_1 implements MigrationScript {
    @Override
    public String getVersion() {
        return "7.1.3.1";
    }

    @Override
    public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
                        AppLayerFactory appLayerFactory) {
        updateNewPermissions(appLayerFactory, collection);
    }

    private void updateNewPermissions(AppLayerFactory appLayerFactory, String collection) {
        ModelLayerFactory modelLayerFactory = appLayerFactory.getModelLayerFactory();

        Role admRole = modelLayerFactory.getRolesManager().getRole(collection, CoreRoles.ADMINISTRATOR);
        modelLayerFactory.getRolesManager().updateRole(admRole.withNewPermissions(asList(CorePermissions.MANAGE_SEARCH_BOOST)));
    }
}
