package com.constellio.app.modules.rm.ui.pages.management;

import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningSecurityService;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.entities.records.wrappers.User;

public class ArchiveManagementPresenter extends BasePresenter<ArchiveManagementView> {
	public ArchiveManagementPresenter(ArchiveManagementView view) {
		super(view);
	}

	public void decommissioningButtonClicked() {
		view.navigate().to(RMViews.class).decommissioning();
	}

	public void newContainerButtonClicked() {
		view.navigate().to(RMViews.class).addContainer();
	}

	public void containersButtonClicked() {
		view.navigate().to(RMViews.class).containersByAdministrativeUnits();
	}

	public void reportsButtonClicked() {
		view.navigate().to(RMViews.class).reports();
	}

	public void onViewAssembled() {
		User user = getCurrentUser();
		view.setDecommissioningButtonVisible(hasAccessToDecommissioningPage());
		view.setNewContainerButtonVisible(user.has(RMPermissionsTo.MANAGE_CONTAINERS).globally());
		view.setContainersButtonVisible(user.has(RMPermissionsTo.MANAGE_CONTAINERS).globally());
		view.setPrintReportsButtonVisible(user.has(RMPermissionsTo.MANAGE_REPORTS).globally());
	}

	private boolean hasAccessToDecommissioningPage() {
		User user = getCurrentUser();
		return new DecommissioningSecurityService(user.getCollection(), modelLayerFactory)
				.hasAccessToDecommissioningMainPage(user);
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.hasAny(RMPermissionsTo.MANAGE_REPORTS, RMPermissionsTo.MANAGE_CONTAINERS).globally() ||
				hasAccessToDecommissioningPage();
	}
}
