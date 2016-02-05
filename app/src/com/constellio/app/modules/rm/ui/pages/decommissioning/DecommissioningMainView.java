package com.constellio.app.modules.rm.ui.pages.decommissioning;

import com.constellio.app.modules.rm.ui.pages.viewGroups.ArchivesManagementViewGroup;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.BaseView;

public interface DecommissioningMainView extends BaseView, ArchivesManagementViewGroup {
	
	void displayListCreation();

	void displayEditableTable(RecordVODataProvider dataProvider);

	void displayReadOnlyTable(RecordVODataProvider dataProvider);

	void reloadCurrentTab();
}
