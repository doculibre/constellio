/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.ui.pages.base;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.users.UserServices;

public class MainLayoutPresenter implements Serializable {

	private MainLayout mainLayout;

	public MainLayoutPresenter(MainLayout mainLayout) {
		this.mainLayout = mainLayout;
	}

	public void viewAssembled() {

	}

	public void adminModuleButtonClicked() {
		mainLayout.navigateTo().adminModule();
	}

	public void auditButtonClicked() {
		mainLayout.navigateTo().listEvents();
	}

	public void dashboardButtonClicked() {
	}

	public void recordsManagementButtonClicked() {
		mainLayout.navigateTo().home();
	}

	public void archivesManagementButtonClicked() {
		mainLayout.navigateTo().archivesManagement();
	}

	public void enterpriseSearchButtonClicked() {
	}

	public void digitalAssetManagementButtonClicked() {
	}

	public void caseManagementButtonClicked() {
	}

	public void userDocumentsButtonClicked() {
		mainLayout.navigateTo().listUserDocuments();
	}

	public boolean isRecordsManagementViewVisible() {
		return true;
	}

	public boolean isArchivesManagementViewVisible() {
		return getUser()
				.hasAny(RMPermissionsTo.MANAGE_REPORTS, RMPermissionsTo.MANAGE_DECOMMISSIONING, RMPermissionsTo.MANAGE_CONTAINERS,
						RMPermissionsTo.MANAGE_ROBOTS).globally();
	}

	public boolean isLogsViewVisible() {
		return getUser().has(CorePermissions.VIEW_EVENTS).globally();
	}

	public boolean isAdminModuleViewVisible() {
		User currentUser = getUser();
		List<String> permissions = new ArrayList<>();
		permissions.addAll(CorePermissions.COLLECTION_MANAGEMENT_PERMISSIONS);
		permissions.addAll(RMPermissionsTo.RM_COLLECTION_MANAGEMENT_PERMISSIONS);

		boolean canManageCollection = currentUser.hasAny(permissions).globally();

		UserServices userServices = mainLayout.getHeader().getConstellioFactories().getModelLayerFactory().newUserServices();
		boolean canManageSystem = userServices.has(currentUser.getUsername())
				.anyGlobalPermissionInAnyCollection(CorePermissions.SYSTEM_MANAGEMENT_PERMISSIONS);
		return canManageCollection || canManageSystem;
	}

	private User getUser() {
		String collection = ConstellioUI.getCurrentSessionContext().getCurrentCollection();
		UserVO userVO = ConstellioUI.getCurrentSessionContext().getCurrentUser();
		ModelLayerFactory modelLayerFactory = mainLayout.getHeader().getConstellioFactories().getModelLayerFactory();
		RMSchemasRecordsServices schemas = new RMSchemasRecordsServices(collection, modelLayerFactory);
		return schemas.getUser(userVO.getId());

	}

	public String getCurrentVersion() {
		String collection = ConstellioUI.getCurrentSessionContext().getCurrentCollection();
		AppLayerFactory appLayerFactory = mainLayout.getHeader().getConstellioFactories().getAppLayerFactory();
		String version = appLayerFactory.newMigrationServices().getCurrentVersion(collection);

		if (version != null) {
			return version + (isBeta() ? " beta" : "");
		} else {
			return isBeta() ? "beta" : "";
		}

	}

	public boolean isBeta() {
		return "t".equals(System.getProperty("b"));
	}

}
