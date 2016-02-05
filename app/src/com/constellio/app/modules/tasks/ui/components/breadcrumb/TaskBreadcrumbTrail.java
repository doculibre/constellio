package com.constellio.app.modules.tasks.ui.components.breadcrumb;

import com.constellio.app.modules.tasks.ui.components.breadcrumb.TaskBreadcrumbTrailPresenter.TaskBreadcrumbItem;
import com.constellio.app.ui.framework.components.breadcrumb.BaseBreadcrumbTrail;
import com.constellio.app.ui.framework.components.breadcrumb.BreadcrumbItem;
import com.constellio.app.ui.util.FileIconUtils;
import com.vaadin.server.Resource;
import com.vaadin.ui.Button;

public class TaskBreadcrumbTrail extends BaseBreadcrumbTrail {
	
	private TaskBreadcrumbTrailPresenter presenter;

	public TaskBreadcrumbTrail(String recordId) {
		this.presenter = new TaskBreadcrumbTrailPresenter(recordId, this);
	}

	@Override
	protected Button newButton(BreadcrumbItem item) {
		Button button = super.newButton(item);
		String recordId = ((TaskBreadcrumbItem) item).getTaskId();
		Resource icon = FileIconUtils.getIconForRecordId(recordId);
		button.setIcon(icon);
		return button;
	}

	@Override
	protected void itemClick(BreadcrumbItem item) {
		presenter.itemClicked(item);
	}

}
