package com.constellio.app.modules.tasks.ui.components.breadcrumb;

import com.constellio.app.modules.tasks.ui.components.breadcrumb.TaskBreadcrumbTrailPresenter.TaskBreadcrumbItem;
import com.constellio.app.ui.framework.components.breadcrumb.BreadcrumbItem;
import com.constellio.app.ui.framework.components.breadcrumb.TitleBreadcrumbTrail;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.util.FileIconUtils;
import com.vaadin.server.Resource;
import com.vaadin.ui.Button;

public class TaskBreadcrumbTrail extends TitleBreadcrumbTrail {
	
	private TaskBreadcrumbTrailPresenter presenter;

	public TaskBreadcrumbTrail(String recordId, BaseView view) {
		super(view, null);
		this.presenter = new TaskBreadcrumbTrailPresenter(recordId, this);
	}

	@Override
	protected Button newButton(BreadcrumbItem item) {
		Button button = super.newButton(item);
		if (item instanceof TaskBreadcrumbItem) {
			String recordId = ((TaskBreadcrumbItem) item).getTaskId();
			Resource icon = FileIconUtils.getIconForRecordId(recordId);
			button.setIcon(icon);
		}
		return button;
	}

	@Override
	protected void itemClick(BreadcrumbItem item) {
		if (!presenter.itemClicked(item)) {
			super.itemClick(item);
		}
	}

}
