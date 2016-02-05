package com.constellio.app.ui.pages.home;

import com.constellio.app.modules.rm.ui.pages.viewGroups.RecordsManagementViewGroup;
import com.constellio.app.ui.pages.base.BaseView;

public interface HomeView extends BaseView, RecordsManagementViewGroup {
	String ACTION_MENU = "home.actionMenu";
	String TABS = "home.tabs";
}
