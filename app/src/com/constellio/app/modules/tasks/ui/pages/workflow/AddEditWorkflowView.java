package com.constellio.app.modules.tasks.ui.pages.workflow;

import com.constellio.app.modules.tasks.ui.entities.WorkflowVO;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.viewGroups.AdminViewGroup;

public interface AddEditWorkflowView extends BaseView, AdminViewGroup {
	
	void setWorkflowVO(WorkflowVO workflowVO);
	
	void setAddView(boolean addView);

}
