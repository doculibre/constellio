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

import java.util.Arrays;
import java.util.List;

import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.User;

public class ArchiveManagementPresenter extends BasePresenter<ArchiveManagementView> {
	public ArchiveManagementPresenter(ArchiveManagementView view) {
		super(view);
	}

	public void decommissioningButtonClicked() {
		view.navigateTo().decommissioning();
	}

	public void containersButtonClicked() {
		view.navigateTo().containersByAdministrativeUnits();
	}

	public void robotsButtonClicked() {
		// TODO: Instantiate a robot killer, or something...
	}

	public void onViewAssembled() {
		User user = getCurrentUser();
		view.setPrintReportsButtonVisible(user.has(RMPermissionsTo.MANAGE_REPORTS).globally());
		view.setDecommissioningButtonVisible(user.has(RMPermissionsTo.MANAGE_DECOMMISSIONING).globally());
		view.setRobotsButtonVisible(user.has(RMPermissionsTo.MANAGE_ROBOTS).globally());
		view.setContainersButtonVisible(user.has(RMPermissionsTo.MANAGE_CONTAINERS).globally());
	}

	public void reportsButtonClicked() {
		view.navigateTo().reports();
	}

	
	
	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.hasAny(RMPermissionsTo.MANAGE_REPORTS,
				RMPermissionsTo.MANAGE_DECOMMISSIONING,
				RMPermissionsTo.MANAGE_ROBOTS,
				RMPermissionsTo.MANAGE_CONTAINERS).globally();
	}

}
