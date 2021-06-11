package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.services.menu.ContainerMenuItemServices.ContainerRecordMenuItemActionType;
import com.constellio.app.modules.rm.services.menu.RMRecordsMenuItemServices.RMRecordsMenuItemActionType;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.services.actionDisplayManager.MenuDisplayItem;
import com.constellio.app.services.actionDisplayManager.MenuPositionActionOptions;
import com.constellio.app.services.actionDisplayManager.MenusDisplayTransaction;
import com.constellio.app.services.actionDisplayManager.MenusDisplayTransaction.Action;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.security.roles.RolesManager;
import com.vaadin.server.FontAwesome;

import java.util.Arrays;

import static com.constellio.app.services.menu.MenuItemServices.BATCH_ACTIONS_FAKE_SCHEMA_TYPE;

public class RMMigrationFrom10_0_ContainerContentSuppression implements MigrationScript {
	@Override
	public String getVersion() {
		return "10.0";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory) throws Exception {

		RolesManager rolesManager = appLayerFactory.getModelLayerFactory().getRolesManager();
		Role rgd = rolesManager.getRole(collection, "RGD");

		rolesManager.updateRole(rgd.withNewPermissions(Arrays.asList(RMPermissionsTo.CONTAINER_CONTENT_SUPPRESSION)));
		Role adm = rolesManager.getRole(collection, "ADM");

		rolesManager.updateRole(adm.withNewPermissions(Arrays.asList(RMPermissionsTo.CONTAINER_CONTENT_SUPPRESSION)));


		MenusDisplayTransaction transaction = new MenusDisplayTransaction();
		transaction.addElement(Action.ADD_UPDATE, ContainerRecord.SCHEMA_TYPE,
				new MenuDisplayItem(ContainerRecordMenuItemActionType.CONTAINER_DELETE_CONTENT.name(),
						FontAwesome.ERASER.name(), "ContainerMenuItemServices.deleteContent", true, null, true), MenuPositionActionOptions.displayActionAtEnd());

		transaction.addElement(Action.ADD_UPDATE, BATCH_ACTIONS_FAKE_SCHEMA_TYPE,
				new MenuDisplayItem(RMRecordsMenuItemActionType.RMRECORDS_DELETE_CONTAINERS_CONTENT.name(),
						FontAwesome.ERASER.name(), "ContainerMenuItemServices.deleteContent", true, null, true), MenuPositionActionOptions.displayActionAtEnd());


		appLayerFactory.getMenusDisplayManager().execute(collection, transaction);
	}

}
