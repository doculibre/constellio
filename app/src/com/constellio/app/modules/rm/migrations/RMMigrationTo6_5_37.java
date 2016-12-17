package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.RMEmailTemplateConstants;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.services.factories.AppLayerFactory;

/**
 * Created by Constelio on 2016-11-25.
 */

import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.tasks.TasksEmailTemplates;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.managers.config.ConfigManagerException.OptimisticLockingConfiguration;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.emails.EmailTemplatesManager;
import com.constellio.model.services.security.roles.RolesManager;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static java.util.Arrays.asList;

public class RMMigrationTo6_5_37 extends MigrationHelper implements MigrationScript {

    private String collection;

    private MigrationResourcesProvider migrationResourcesProvider;

    private AppLayerFactory appLayerFactory;

    @Override
    public String getVersion() {
        return "6.5.37";
    }

    @Override
    public void migrate(String collection, MigrationResourcesProvider provider, AppLayerFactory appLayerFactory)
            throws Exception {
        setupRoles(collection, appLayerFactory.getModelLayerFactory().getRolesManager());
    }

    private void setupRoles(String collection, RolesManager manager) {
        List<Role> roleList = manager.getAllRoles(collection);
        for(Role role: roleList) {
            manager.updateRole(role.withNewPermissions(asList(RMPermissionsTo.PUBLISH_AND_UNPUBLISH_DOCUMENTS)));
        }
    }
}
