package com.constellio.app.ui.pages.collection;

import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.viewGroups.AdminViewGroup;

public interface CollectionSecurityManagement extends BaseView, AdminViewGroup {


	RecordVODataProvider getGroupDataProvider();

	void setGroupDataProvider(RecordVODataProvider dataProvider);

	void selectGroupTab();

	void selectUserTab();

	RecordVODataProvider getUserDataProvider();

	void setUserDataProvider(RecordVODataProvider dataProvider);
}
