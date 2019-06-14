package com.constellio.app.ui.pages.globalGroup;

import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.viewGroups.AdminViewGroup;

public interface DisplayGlobalGroupView extends BaseView, AdminViewGroup {

	void refreshTable();

	DisplayGlobalGroupPresenter getPresenter();

	String getBreadCrumb();
}
