package com.constellio.app.modules.rm.ui.pages.systemCheck;

import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.viewGroups.AdminViewGroup;

public interface SystemCheckView extends BaseView, AdminViewGroup {
	
	void setSystemCheckRunning(boolean running);

	void setReportContent(String reportContent);

}
