package com.constellio.app.modules.tasks.ui.pages.workflowInstance;

import java.util.List;

import com.constellio.app.modules.tasks.ui.entities.WorkflowInstanceVO;
import com.constellio.app.modules.tasks.ui.entities.WorkflowTaskProgressionVO;
import com.constellio.app.modules.tasks.ui.pages.viewGroups.TasksViewGroup;
import com.constellio.app.ui.pages.base.BaseView;

public interface DisplayWorkflowInstanceView extends BaseView, TasksViewGroup {
	
	void setWorkflowInstanceVO(WorkflowInstanceVO workflowInstanceVO);
	
	void setWorkflowTaskProgressionVOs(List<WorkflowTaskProgressionVO> workflowTaskVOs);
	
}
