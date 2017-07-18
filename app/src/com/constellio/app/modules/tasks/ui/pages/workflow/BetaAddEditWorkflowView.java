package com.constellio.app.modules.tasks.ui.pages.workflow;

import com.constellio.app.modules.tasks.ui.entities.BetaWorkflowVO;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.viewGroups.AdminViewGroup;

public interface BetaAddEditWorkflowView extends BaseView, AdminViewGroup {
	
	void setWorkflowVO(BetaWorkflowVO workflowVO);
	
	void setAddView(boolean addView);

}
