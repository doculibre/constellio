package com.constellio.app.modules.robots.ui.pages;

import com.constellio.app.entities.modules.ProgressInfo;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.viewGroups.AdminViewGroup;

public interface DeleteRobotRecordsView extends BaseView, AdminViewGroup {
	
	void setTitle(String title);
	
	void setProgressInfo(ProgressInfo progressInfo);

}
