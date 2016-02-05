package com.constellio.app.modules.rm.ui.pages.agent;

import com.constellio.app.modules.rm.ui.util.ConstellioAgentUtils;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.wrappers.User;

public class AgentSetupPresenter extends BasePresenter<AgentSetupView> {

	public AgentSetupPresenter(AgentSetupView view) {
		super(view);
	}

	public AgentSetupPresenter(AgentSetupView view,
			ConstellioFactories constellioFactories,
			SessionContext sessionContext) {
		super(view, constellioFactories, sessionContext);
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return true;
	}
	
	void viewEntered() {
		String agentVersion = ConstellioAgentUtils.getAgentVersion();
		String agentDownloadURL = ConstellioAgentUtils.getAgentDownloadURL();
		String agentInitURL = ConstellioAgentUtils.getAgentInitURL();
		view.setAgentVersion(agentVersion);
		view.setAgentDownloadURL(agentDownloadURL);
		view.setAgentInitURL(agentInitURL);
	}

}
