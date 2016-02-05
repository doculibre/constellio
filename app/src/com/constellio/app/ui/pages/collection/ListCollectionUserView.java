package com.constellio.app.ui.pages.collection;

import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.viewGroups.AdminViewGroup;

// After rename CollectionSecurityManagementView
public interface ListCollectionUserView extends BaseView, AdminViewGroup {

	void refreshTable();
}
