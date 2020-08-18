package com.constellio.app.ui.pages.home;

import com.constellio.app.modules.rm.ui.pages.viewGroups.RecordsManagementViewGroup;
import com.constellio.app.ui.pages.base.BaseView;

public interface HomeView extends BaseView, RecordsManagementViewGroup {

	String TABS = "home.tabs";

	String getSelectedTabCode();

	void recordChanged(String recordId);

	void updateCaption(String recordId, String newCaption);

	BaseView getNestedView();
	
}
