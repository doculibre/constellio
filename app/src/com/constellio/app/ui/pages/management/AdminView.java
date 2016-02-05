package com.constellio.app.ui.pages.management;

import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.viewGroups.AdminViewGroup;

public interface AdminView extends BaseView, AdminViewGroup {
	String SYSTEM_SECTION = "admin.system";
	String COLLECTION_SECTION = "admin.collection";
}
