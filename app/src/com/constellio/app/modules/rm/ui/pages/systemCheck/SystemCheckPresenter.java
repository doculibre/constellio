package com.constellio.app.modules.rm.ui.pages.systemCheck;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.services.records.SystemCheckManager;
import com.constellio.app.services.records.SystemCheckReportBuilder;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.wrappers.User;

public class SystemCheckPresenter extends BasePresenter<SystemCheckView> {
	
	private boolean systemCheckStarted = false;

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
		systemCheckStarted = systemCheckManager.isSystemCheckResultsRunning();
		view.setSystemCheckRunning(systemCheckStarted);
		if (systemCheckManager.getLastSystemCheckResults() != null) {
			String reportContent = new SystemCheckReportBuilder(systemCheckManager).build();
			view.setReportContent(reportContent);
		}
	}
	
	void startSystemCheckButtonClicked() {
		SystemCheckManager systemCheckManager = getSystemCheckManager();
		systemCheckManager.startSystemCheck(false);
		view.setSystemCheckRunning(true);
		systemCheckStarted = true;
	}
	
	void startSystemCheckAndRepairButtonClicked() {
		SystemCheckManager systemCheckManager = getSystemCheckManager();
		systemCheckManager.startSystemCheck(true);
		view.setSystemCheckRunning(true);
		systemCheckStarted = true;
	}
	
	void viewRefreshed() {
		if (systemCheckStarted) {
			System.out.println("test");
			SystemCheckManager systemCheckManager = getSystemCheckManager();
			boolean systemCheckRunning = systemCheckManager.isSystemCheckResultsRunning();
			if (!systemCheckRunning) {
				String reportContent = new SystemCheckReportBuilder(systemCheckManager).build();
				view.setReportContent(reportContent);
				view.setSystemCheckRunning(false);
				systemCheckRunning = false;
			}
		}
	}
	
	void backButtonClicked() {
		view.navigate().to().adminModule();
	}

}
