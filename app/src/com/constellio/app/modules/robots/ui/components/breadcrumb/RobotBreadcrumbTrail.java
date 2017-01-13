package com.constellio.app.modules.robots.ui.components.breadcrumb;

import com.constellio.app.modules.robots.ui.components.breadcrumb.RobotBreadcrumbTrailPresenter.RobotBreadcrumbItem;
import com.constellio.app.ui.framework.components.breadcrumb.BreadcrumbItem;
import com.constellio.app.ui.framework.components.breadcrumb.TitleBreadcrumbTrail;
import com.constellio.app.ui.pages.base.BaseView;
import com.vaadin.ui.Button;

public class RobotBreadcrumbTrail extends TitleBreadcrumbTrail {
	
	private final RobotBreadcrumbTrailPresenter presenter;

	public RobotBreadcrumbTrail(String robotId, BaseView view) {
		super(view, null);
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
