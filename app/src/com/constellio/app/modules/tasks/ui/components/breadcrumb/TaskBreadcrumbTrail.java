/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
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
