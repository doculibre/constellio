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
package com.constellio.app.modules.rm.ui.pages.containers;

import java.io.Serializable;
import java.util.List;

import com.constellio.app.modules.rm.ui.pages.viewGroups.ArchivesManagementViewGroup;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.BaseView;

public interface ContainersByAdministrativeUnitsView extends BaseView, ArchivesManagementViewGroup {

	List<ContainersViewTab> getTabs();

	void setTabs(List<ContainersViewTab> tabs);

	void selectTab(ContainersViewTab tab);

	public static class ContainersViewTab implements Serializable {

		private final String tabName;

		private final RecordVODataProvider dataProvider;

		public ContainersViewTab(String tabName, RecordVODataProvider dataProvider) {
			this.tabName = tabName;
			this.dataProvider = dataProvider;
		}

		public final String getTabName() {
			return tabName;
		}

		public final RecordVODataProvider getDataProvider() {
			return dataProvider;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((tabName == null) ? 0 : tabName.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			ContainersViewTab other = (ContainersViewTab) obj;
			if (tabName == null) {
				if (other.tabName != null) {
					return false;
				}
			} else if (!tabName.equals(other.tabName)) {
				return false;
			}
			return true;
		}

	}

}
