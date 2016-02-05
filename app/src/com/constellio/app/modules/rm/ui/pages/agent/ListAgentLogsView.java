package com.constellio.app.modules.rm.ui.pages.agent;

import java.util.List;

import com.constellio.app.modules.rm.ui.entities.AgentLogVO;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.viewGroups.LogsViewGroup;

public interface ListAgentLogsView extends BaseView, LogsViewGroup {
	
	String getSelectedUserId();
	
	void setAgentLogs(List<AgentLogVO> agentLogVOs);

}
