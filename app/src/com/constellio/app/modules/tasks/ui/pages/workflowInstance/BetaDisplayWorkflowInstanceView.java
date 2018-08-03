package com.constellio.app.modules.tasks.ui.pages.workflowInstance;

import com.constellio.app.modules.tasks.ui.entities.BetaWorkflowInstanceVO;
import com.constellio.app.modules.tasks.ui.entities.BetaWorkflowTaskProgressionVO;
import com.constellio.app.modules.tasks.ui.pages.viewGroups.TasksViewGroup;
import com.constellio.app.ui.pages.base.BaseView;

import java.util.List;

public interface BetaDisplayWorkflowInstanceView extends BaseView, TasksViewGroup {

	void setWorkflowInstanceVO(BetaWorkflowInstanceVO workflowInstanceVO);

	void setWorkflowTaskProgressionVOs(List<BetaWorkflowTaskProgressionVO> workflowTaskVOs);

}
