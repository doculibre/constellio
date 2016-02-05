package com.constellio.app.modules.rm.ui.pages.management;

import com.constellio.app.modules.rm.ui.pages.viewGroups.ArchivesManagementViewGroup;
import com.constellio.app.ui.pages.base.BaseView;

public interface ArchiveManagementView extends BaseView, ArchivesManagementViewGroup {

	void setDecommissioningButtonVisible(boolean visible);

	void setNewContainerButtonVisible(boolean visible);

	void setContainersButtonVisible(boolean visible);

	void setPrintReportsButtonVisible(boolean visible);
}
