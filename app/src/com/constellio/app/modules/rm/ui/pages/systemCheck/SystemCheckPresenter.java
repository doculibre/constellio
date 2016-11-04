package com.constellio.app.modules.rm.ui.pages.systemCheck;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.services.records.SystemCheckManager;
import com.constellio.app.services.records.SystemCheckReportBuilder;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.wrappers.User;

public class SystemCheckPresenter extends BasePresenter<SystemCheckView> {
	
	private boolean buttonsDisabled = false;

	public SystemCheckPresenter(SystemCheckView view) {
		super(view);
	}

	public SystemCheckPresenter(SystemCheckView view, ConstellioFactories constellioFactories,
			SessionContext sessionContext) {
		super(view, constellioFactories, sessionContext);
	}
	
	@Override
	protected boolean hasPageAccess(String params, User user) {
		return userServices().getUser(user.getUsername()).isSystemAdmin();
	}
	
	SystemCheckManager getSystemCheckManager() {
		return appLayerFactory.getSystemCheckManager();
	}
	
	void viewAssembled() {
		SystemCheckManager systemCheckManager = getSystemCheckManager();
		buttonsDisabled = systemCheckManager.isSystemCheckResultsRunning();
		view.setSystemCheckRunning(buttonsDisabled);
		if (systemCheckManager.getLastSystemCheckResults() != null) {
			String reportContent = new SystemCheckReportBuilder(systemCheckManager).build();
			view.setReportContent(reportContent);
		}
	}
	
	void startSystemCheckButtonClicked() {
		SystemCheckManager systemCheckManager = getSystemCheckManager();
		systemCheckManager.startSystemCheck(false);
		view.setSystemCheckRunning(true);
		buttonsDisabled = true;
	}
	
	void startSystemCheckAndRepairButtonClicked() {
		SystemCheckManager systemCheckManager = getSystemCheckManager();
		systemCheckManager.startSystemCheck(true);
		view.setSystemCheckRunning(true);
		buttonsDisabled = true;
	}
	
	void viewRefreshed() {
		SystemCheckManager systemCheckManager = getSystemCheckManager();
		boolean systemCheckRunning = systemCheckManager.isSystemCheckResultsRunning();
		if (buttonsDisabled != systemCheckRunning) {
			String reportContent = new SystemCheckReportBuilder(systemCheckManager).build();
			view.setReportContent(reportContent);
			view.setSystemCheckRunning(false);
			buttonsDisabled = false;
		}
	}
	
	void backButtonClicked() {
		view.navigate().to().adminModule();
	}

}
