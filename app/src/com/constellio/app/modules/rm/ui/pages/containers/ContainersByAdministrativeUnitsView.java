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
