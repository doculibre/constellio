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
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.security.roles.RolesManager;

public class CollectionGroupRolesPresenter extends SingleSchemaBasePresenter<CollectionGroupRolesView> {
	String recordId;

	public CollectionGroupRolesPresenter(CollectionGroupRolesView view) {
		super(view, Group.DEFAULT_SCHEMA);
	}

	public void forRequestParams(String parameters) {
		recordId = parameters;
	}

	public RecordVO getGroup() {
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
		Group group = coreSchemas().getGroup(recordId);
		List<String> roles = new ArrayList<>(group.getRoles());
		roles.add(roleCode);
		group.setRoles(roles);
		addOrUpdate(group.getWrappedRecord());
		view.roleAdded(roleCode);
	}

	public void roleRemovalRequested(String roleCode) {
		Group group = coreSchemas().getGroup(recordId);
		List<String> roles = new ArrayList<>(group.getRoles());
		roles.remove(roleCode);
		group.setRoles(roles);
		addOrUpdate(group.getWrappedRecord());
		view.roleRemoved(roleCode);
	}

	public void backButtonClicked() {
		view.navigateTo().displayCollectionGroup(recordId);
	}

	private RolesManager roleManager() {
		return modelLayerFactory.getRolesManager();
	}
}
