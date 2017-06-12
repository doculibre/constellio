package com.constellio.app.ui.pages.management.configs;

import com.constellio.app.ui.framework.data.SystemConfigurationGroupdataProvider;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.viewGroups.AdminViewGroup;

public interface ConfigManagementView extends BaseView, AdminViewGroup {
	
	void setDataProvider(SystemConfigurationGroupdataProvider dataProvider);
	
}
