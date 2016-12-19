package com.constellio.app.modules.rm.migrations;

import static com.constellio.data.utils.LangUtils.withoutDuplicates;
import static java.util.Arrays.asList;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.constants.RMRoles;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.migrations.CoreRoles;
import com.constellio.app.services.migrations.CoreRoles;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.security.Role;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.security.roles.RolesManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RMMigrationTo6_5_50 implements MigrationScript {
    @Override
    public String getVersion() {
        return "6.5.50";
    }

    @Override
    public void migrate(String collection, MigrationResourcesProvider provider, AppLayerFactory appLayerFactory)
            throws Exception {

        givenNewPermissionsToRGDandADMRoles(collection, appLayerFactory.getModelLayerFactory());
        givenNewPermissionsToAllRoles(collection, appLayerFactory.getModelLayerFactory());
    }

    private void givenNewPermissionsToRGDandADMRoles(String collection, ModelLayerFactory modelLayerFactory) {
        Role rgdRole = modelLayerFactory.getRolesManager().getRole(collection, RMRoles.RGD);
        List<String> newRgdPermissions = new ArrayList<>();
        newRgdPermissions.add(RMPermissionsTo.DELETE_BORROWED_DOCUMENT);
        modelLayerFactory.getRolesManager().updateRole(rgdRole.withNewPermissions(newRgdPermissions));

        Role admRole = modelLayerFactory.getRolesManager().getRole(collection, CoreRoles.ADMINISTRATOR);
        List<String> newAdmPermissions = new ArrayList<>();
        newAdmPermissions.add(RMPermissionsTo.DELETE_BORROWED_DOCUMENT);
        modelLayerFactory.getRolesManager().updateRole(admRole.withNewPermissions(newAdmPermissions));
    }

    private void givenNewPermissionsToAllRoles(String collection, ModelLayerFactory modelLayerFactory) {
        List<Role> roleList = modelLayerFactory.getRolesManager().getAllRoles(collection);
        RolesManager manager = modelLayerFactory.getRolesManager();
        for(Role role: roleList) {
            manager.updateRole(role.withNewPermissions(asList(RMPermissionsTo.DELETE_PUBLISHED_DOCUMENT)));
        }
    }
}
