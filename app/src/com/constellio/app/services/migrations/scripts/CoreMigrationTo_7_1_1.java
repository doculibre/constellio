package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.migrations.CoreRoles;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.factories.ModelLayerFactory;

import java.util.ArrayList;
import java.util.List;

public class CoreMigrationTo_7_1_1 implements MigrationScript {
    @Override
    public String getVersion() {
        return "7.1.1";
    }

    @Override
    public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory) throws Exception {
        alterRole(collection, appLayerFactory.getModelLayerFactory());
    }

    public void alterRole(String collection, ModelLayerFactory modelLayerFactory) {
        Role admRole = modelLayerFactory.getRolesManager().getRole(collection, CoreRoles.ADMINISTRATOR);
        List<String> newAdmPermissions = new ArrayList<>();
        newAdmPermissions.add(CorePermissions.VIEW_SYSTEM_BATCH_PROCESSES);
        modelLayerFactory.getRolesManager().updateRole(admRole.withNewPermissions(newAdmPermissions));
    }
}
