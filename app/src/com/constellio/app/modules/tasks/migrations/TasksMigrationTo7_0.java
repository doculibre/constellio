package com.constellio.app.modules.tasks.migrations;

import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.tasks.TasksPermissionsTo;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.migrations.CoreRoles;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.security.roles.RolesManager;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

public class TasksMigrationTo7_0 implements MigrationScript {
    @Override
    public String getVersion() {
        return "7.0";
    }

    @Override
    public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory) throws Exception {
        updateRole(appLayerFactory, collection);
    }

    public void updateRole(AppLayerFactory factory, String collection) {
        RolesManager rolesManager = factory.getModelLayerFactory().getRolesManager();
        Role administrator = rolesManager.getRole(collection, CoreRoles.ADMINISTRATOR);
        rolesManager.updateRole(administrator.withNewPermissions(asList(TasksPermissionsTo.START_WORKFLOWS)));
    }
}
