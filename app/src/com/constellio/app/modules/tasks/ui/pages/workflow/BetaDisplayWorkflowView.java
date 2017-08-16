package com.constellio.app.modules.tasks.ui.pages.workflow;

import java.util.List;

import com.constellio.app.modules.tasks.ui.entities.BetaWorkflowTaskVO;
import com.constellio.app.modules.tasks.ui.entities.BetaWorkflowVO;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.viewGroups.AdminViewGroup;

public interface BetaDisplayWorkflowView extends BaseView, AdminViewGroup {
	void setTaskTypeDataProvider(RecordVODataProvider taskTypeDataProvider);

	void setWorkflowVO(BetaWorkflowVO workflowVO);

	void setWorkflowTaskVOs(List<BetaWorkflowTaskVO> workflowTaskVOs);

	void openAddTaskWindow(BetaWorkflowTaskVO workflowTaskVO, List<BetaWorkflowTaskVO> availableTaskVOs);

	void openExistingTasksWindow(BetaWorkflowTaskVO workflowTaskVO, List<BetaWorkflowTaskVO> availableTaskVOs);

	void closeAddTaskWindow();

	void remove(BetaWorkflowTaskVO workflowTaskVO);
}
