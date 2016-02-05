package com.constellio.app.modules.robots.ui.components.breadcrumb;

import com.constellio.app.modules.robots.ui.components.breadcrumb.RobotBreadcrumbTrailPresenter.RobotBreadcrumbItem;
import com.constellio.app.ui.framework.components.breadcrumb.BaseBreadcrumbTrail;
import com.constellio.app.ui.framework.components.breadcrumb.BreadcrumbItem;
import com.vaadin.ui.Button;

public class RobotBreadcrumbTrail extends BaseBreadcrumbTrail {
	private final RobotBreadcrumbTrailPresenter presenter;

	public RobotBreadcrumbTrail(String robotId) {
		presenter = new RobotBreadcrumbTrailPresenter(this, robotId).createItems();
	}

	@Override
	protected Button newButton(BreadcrumbItem item) {
		return super.newButton(item);
	}

	@Override
	protected void itemClick(BreadcrumbItem item) {
		presenter.itemClicked((RobotBreadcrumbItem) item);
	}
}
