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
package com.constellio.app.modules.rm.ui.pages.management;

import java.util.ArrayList;
import java.util.List;

import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.wrappers.FilingSpace;
import com.constellio.app.modules.rm.wrappers.UniformSubdivision;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.users.CredentialUserPermissionChecker;

public class AdminRMModulePresenter extends BasePresenter<AdminRMModuleView> {

	public AdminRMModulePresenter(AdminRMModuleView view) {
		super(view);
	}

	public void configButtonClicked() {
		view.navigateTo().configManagement();
	}

	public void ldapConfigButtonClicked() {
		view.navigateTo().ldapConfigManagement();
	}

	public void taxonomiesButtonClicked() {
		view.navigateTo().listTaxonomies();
	}

	//	public void classificationFilePlanButtonClicked() {
	//		view.navigateTo().taxonomyManagement(RMTaxonomies.CLASSIFICATION_PLAN);
	//	}

	public void valueDomainButtonClicked() {
		view.navigateTo().listValueDomains();
	}

	public void filingSpacesButtonClicked() {
		view.navigateTo().listSchemaRecords(FilingSpace.DEFAULT_SCHEMA);
	}

	public void retentionCalendarButtonClicked() {
		view.navigateTo().listRetentionRules();
	}

	public void metadataSchemasButtonClicked() {
		view.navigateTo().listSchemaType();
	}

	public void uniformSubdivisionsButtonClicked() {
		view.navigateTo().listSchemaRecords(UniformSubdivision.DEFAULT_SCHEMA);
	}

	public void importFileButtonClicked() {
		view.navigateTo().importFile();
	}

	public void optionsButtonClicked() {
	}

	public void manageGroupsButtonClicked() {
		view.navigateTo().listGlobalGroups();
	}

	public void manageUsersButtonClicked() {
		view.navigateTo().collectionSecurity();
	}

	public void manageRolesButtonClicked() {
		view.navigateTo().permissionManagement();
	}

	public boolean isUniformSubdivisionsButtonVisible() {
		return true;
	}

	public void manageCollectionsButtonClicked() {
		view.navigateTo().manageCollections();
	}

	public void dataExtractorButtonClicked() {
	}

	public void bigDataButtonClicked() {
	}

	public void updateCenterButtonClicked() {
		view.navigateTo().updateManager();
	}

	public void manageUserCredentialsButtonClicked() {
		view.navigateTo().listUserCredentials();
	}

	public void connectorsButtonClicked() {

	}

	public void searchEngineButtonClicked() {

	}

	public void modulesButtonClicked() {

	}

	public void trashBinButtonClicked() {

	}

	public boolean isSystemSectionVisible() {
		User user = getCurrentUser();
		return modelLayerFactory.newUserServices().has(user.getUsername())
				.anyGlobalPermissionInAnyCollection(CorePermissions.SYSTEM_MANAGEMENT_PERMISSIONS);
	}

	public void onViewAssembled() {
		User user = getCurrentUser();
		view.setManageTaxonomiesVisible(user.hasAny(CorePermissions.MANAGE_TAXONOMIES, CorePermissions.MANAGE_SECURITY,
				RMPermissionsTo.MANAGE_STORAGE_SPACES, RMPermissionsTo.MANAGE_CLASSIFICATION_PLAN).globally());
		view.setManageUniformSubdivisionsVisible(user.has(RMPermissionsTo.MANAGE_UNIFORMSUBDIVISIONS).globally());
		view.setManageRetentionRuleVisible(user.has(RMPermissionsTo.MANAGE_RETENTIONRULE).globally());
		view.setManageValueListVisible(user.has(CorePermissions.MANAGE_VALUELIST).globally());
		view.setManageMetadataSchemasVisible(user.has(CorePermissions.MANAGE_METADATASCHEMAS).globally());
		view.setManageSecurityVisible(user.has(CorePermissions.MANAGE_SECURITY).globally());
		view.setManageFilingSpaceVisible(user.has(CorePermissions.MANAGE_SECURITY).globally());
		view.setManageRolesVisible(user.has(CorePermissions.MANAGE_SECURITY).globally());
		view.setManageMetadataExtractorVisible(user.has(CorePermissions.MANAGE_METADATAEXTRACTOR).globally());
		view.setManageConnectorsVisible(user.has(CorePermissions.MANAGE_CONNECTORS).globally());
		view.setManageSearchEngineVisible(user.has(CorePermissions.MANAGE_SEARCHENGINE).globally());
		view.setManageTrashVisible(user.has(CorePermissions.MANAGE_TRASH).globally());

		CredentialUserPermissionChecker userHas = modelLayerFactory.newUserServices().has(user.getUsername());
		view.setManageSystemConfiguration(userHas.globalPermissionInAnyCollection(CorePermissions.MANAGE_SYSTEM_CONFIGURATION));
		view.setManageSystemGroups(userHas.globalPermissionInAnyCollection(CorePermissions.MANAGE_SYSTEM_GROUPS));
		view.setManageSystemUsers(userHas.globalPermissionInAnyCollection(CorePermissions.MANAGE_SYSTEM_USERS));
		view.setManageSystemCollections(userHas.globalPermissionInAnyCollection(CorePermissions.MANAGE_SYSTEM_COLLECTIONS));
		view.setManageSystemModules(userHas.globalPermissionInAnyCollection(CorePermissions.MANAGE_SYSTEM_MODULES));
		view.setManageSystemDataImports(userHas.globalPermissionInAnyCollection(CorePermissions.MANAGE_SYSTEM_DATA_IMPORTS));
		view.setManageSystemServers(userHas.globalPermissionInAnyCollection(CorePermissions.MANAGE_SYSTEM_SERVERS));
		view.setManageSystemUpdates(userHas.globalPermissionInAnyCollection(CorePermissions.MANAGE_SYSTEM_UPDATES));
		view.setManageSystemConfiguration(userHas.globalPermissionInAnyCollection(CorePermissions.MANAGE_SYSTEM_CONFIGURATION));
		view.setManageLdapConfiguration(userHas.globalPermissionInAnyCollection(CorePermissions.MANAGE_LDAP));
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		List<String> permissions = new ArrayList<>();
		permissions.addAll(RMPermissionsTo.RM_COLLECTION_MANAGEMENT_PERMISSIONS);
		permissions.addAll(CorePermissions.COLLECTION_MANAGEMENT_PERMISSIONS);

		boolean collectionManagementAccess = user.hasAny(permissions).globally();
		boolean systemManagementAccess = userServices().has(user).anyGlobalPermissionInAnyCollection(
				CorePermissions.SYSTEM_MANAGEMENT_PERMISSIONS);

		return collectionManagementAccess || systemManagementAccess;
	}
}
