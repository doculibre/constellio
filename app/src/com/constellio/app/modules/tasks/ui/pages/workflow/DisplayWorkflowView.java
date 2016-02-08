package com.constellio.app.modules.tasks.ui.pages.workflow;

import java.util.List;

import com.constellio.app.modules.tasks.ui.entities.WorkflowTaskVO;
import com.constellio.app.modules.tasks.ui.entities.WorkflowVO;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.viewGroups.AdminViewGroup;

public interface DisplayWorkflowView extends BaseView, AdminViewGroup {
	void setTaskTypeDataProvider(RecordVODataProvider taskTypeDataProvider);

	void setWorkflowVO(WorkflowVO workflowVO);

	void setWorkflowTaskVOs(List<WorkflowTaskVO> workflowTaskVOs);

	void openAddTaskWindow(WorkflowTaskVO workflowTaskVO, List<WorkflowTaskVO> availableTaskVOs);

	void openExistingTasksWindow(WorkflowTaskVO workflowTaskVO, List<WorkflowTaskVO> availableTaskVOs);

	void closeAddTaskWindow();

	void remove(WorkflowTaskVO workflowTaskVO);
}
