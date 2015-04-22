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
package com.constellio.app.modules.rm.ui.pages.home;

import java.io.Serializable;
import java.util.List;

import com.constellio.app.modules.rm.ui.pages.viewGroups.RecordsManagementViewGroup;
import com.constellio.app.ui.framework.data.DataProvider;
import com.constellio.app.ui.pages.base.BaseView;

public interface RecordsManagementView extends BaseView, RecordsManagementViewGroup {

	enum TabType {RECORD_LIST, RECORD_TREE}

	List<RecordsManagementViewTab> getTabs();

	void setTabs(List<RecordsManagementViewTab> tabs);

	void selectTab(RecordsManagementViewTab tab);

	public static class RecordsManagementViewTab implements Serializable {

		private final String tabName;

		private final TabType tabType;

		private final List<DataProvider> dataProviders;

		public RecordsManagementViewTab(String tabName, TabType tabType, List<DataProvider> dataProviders) {
			this.tabName = tabName;
			this.tabType = tabType;
			this.dataProviders = dataProviders;
		}

		public boolean isEnabled() {
			return !dataProviders.isEmpty();
		}

		public final String getTabName() {
			return tabName;
		}

		public final TabType getTabType() {
			return tabType;
		}

		public final DataProvider getDataProvider() {
			return dataProviders.get(0);
		}

		public final List<? extends DataProvider> getDataProviders() {
			return dataProviders;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((tabName == null) ? 0 : tabName.hashCode());
			result = prime * result + ((tabType == null) ? 0 : tabType.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			RecordsManagementViewTab other = (RecordsManagementViewTab) obj;
			if (tabName == null) {
				if (other.tabName != null)
					return false;
			} else if (!tabName.equals(other.tabName))
				return false;
			if (tabType != other.tabType)
				return false;
			return true;
		}

	}

}
