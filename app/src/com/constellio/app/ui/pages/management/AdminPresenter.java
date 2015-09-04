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
package com.constellio.app.ui.pages.management;

import java.util.List;

import com.constellio.app.entities.navigation.NavigationItem;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.entities.records.wrappers.User;

public class AdminPresenter extends BasePresenter<AdminView> {
	public AdminPresenter(AdminView view) {
		super(view);
	}

	public List<NavigationItem> getSystemItems() {
		return navigationConfig().getNavigation(AdminView.SYSTEM_SECTION);
	}

	public List<NavigationItem> getCollectionItems() {
		return navigationConfig().getNavigation(AdminView.COLLECTION_SECTION);
	}

	public boolean isSystemSectionVisible() {
		return isAnyItemEnabled(getSystemItems());
	}

	public boolean isCollectionSectionVisible() {
		return isAnyItemEnabled(getCollectionItems());
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return isSystemSectionVisible() || isCollectionSectionVisible();
	}

	private boolean isAnyItemEnabled(List<NavigationItem> items) {
		for (NavigationItem item : items) {
			if (getStateFor(item).isEnabled()) {
				return true;
			}
		}
		return false;
	}
}
