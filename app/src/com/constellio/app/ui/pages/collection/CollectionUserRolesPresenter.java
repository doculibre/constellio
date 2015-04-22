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
package com.constellio.app.ui.pages.collection;

import java.util.ArrayList;
import java.util.List;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.entities.RoleVO;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.security.roles.RolesManager;

public class CollectionUserRolesPresenter extends SingleSchemaBasePresenter<CollectionUserRolesView> {
	String recordId;

	public CollectionUserRolesPresenter(CollectionUserRolesView view) {
		super(view, User.DEFAULT_SCHEMA);
	}

	public void forRequestParams(String parameters) {
		recordId = parameters;
	}

	public RecordVO getUser() {
		return presenterService().getRecordVO(recordId, VIEW_MODE.DISPLAY);
	}

	public String getRoleTitle(String roleCode) {
		return roleManager().getRole(view.getCollection(), roleCode).getTitle();
	}

	public List<RoleVO> getRoles() {
		List<RoleVO> result = new ArrayList<>();
		for (Role role : roleManager().getAllRoles(view.getCollection())) {
			result.add(new RoleVO(role.getCode(), role.getTitle(), role.getOperationPermissions()));
		}
		return result;
	}

	public void roleAdditionRequested(String roleCode) {
		User user = coreSchemas().getUser(recordId);
		List<String> roles = new ArrayList<>(user.getUserRoles());
		roles.add(roleCode);
		user.setUserRoles(roles);
		addOrUpdate(user.getWrappedRecord());
		view.roleAdded(roleCode);
	}

	public void roleRemovalRequested(String roleCode) {
		User user = coreSchemas().getUser(recordId);
		List<String> roles = new ArrayList<>(user.getUserRoles());
		roles.remove(roleCode);
		user.setUserRoles(roles);
		addOrUpdate(user.getWrappedRecord());
		view.roleRemoved(roleCode);
	}

	public void backButtonClicked() {
		view.navigateTo().displayCollectionUser(recordId);
	}

	private RolesManager roleManager() {
		return modelLayerFactory.getRolesManager();
	}
}
