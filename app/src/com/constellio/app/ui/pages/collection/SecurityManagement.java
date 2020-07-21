package com.constellio.app.ui.pages.collection;

import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.viewGroups.AdminViewGroup;
import com.vaadin.ui.VerticalLayout;

public interface SecurityManagement extends BaseView, AdminViewGroup {
	RecordVODataProvider getDataProvider();

	void setDataProvider(RecordVODataProvider dataProvider);

	VerticalLayout createTabLayout();

	void reloadContent();
}
