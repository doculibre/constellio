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
package com.constellio.app.modules.tasks.ui.pages;

import static com.constellio.app.ui.i18n.i18n.$;

import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;

public class TasksLogsViewImpl extends BaseViewImpl implements TasksLogsView {
	private TasksLogsPresenter presenter;

	public TasksLogsViewImpl() {
		presenter = new TasksLogsPresenter(this);
	}

	@Override
	protected String getTitle() {
		return $("TasksLogsView.viewTitle");
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		return new VerticalLayout();
	}
}
