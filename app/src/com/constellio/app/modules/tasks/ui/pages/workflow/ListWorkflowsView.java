package com.constellio.app.modules.tasks.ui.pages.workflow;

import java.util.List;

import com.constellio.app.modules.tasks.ui.entities.WorkflowVO;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.viewGroups.AdminViewGroup;

public interface ListWorkflowsView extends BaseView, AdminViewGroup {

	void setWorkflowVOs(List<WorkflowVO> workflowVOs);

	void remove(WorkflowVO workflowVO);

}
