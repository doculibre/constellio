package com.constellio.app.modules.rm.ui.pages.agent;

import com.constellio.app.modules.rm.ui.pages.viewGroups.AgentViewGroup;
import com.constellio.app.ui.pages.base.BaseView;

public interface AgentSetupView extends BaseView, AgentViewGroup {

	void setAgentVersion(String agentVersion);
	
	void setAgentDownloadURL(String agentDownloadURL);
	
	void setAgentInitURL(String agentInitURL);
 
}
