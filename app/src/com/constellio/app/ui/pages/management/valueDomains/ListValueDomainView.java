package com.constellio.app.ui.pages.management.valueDomains;

import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.viewGroups.AdminViewGroup;

public interface ListValueDomainView extends BaseView, AdminViewGroup {
	final String CUSTOM_TAB = "custom";
	final String SYSTEM_TAB = "system";

	void refreshTable();
}
