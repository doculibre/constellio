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

import com.constellio.app.modules.tasks.ui.components.TaskTable;
import com.constellio.app.ui.framework.buttons.AddButton;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;

public class TaskManagementViewImpl extends BaseViewImpl implements TaskManagementView {
	private final TaskManagementPresenter presenter;
	private TabSheet sheet;

	public TaskManagementViewImpl() {
		this.presenter = new TaskManagementPresenter(this);
	}

	@Override
	protected boolean isFullWidthIfActionMenuAbsent() {
		return true;
	}

	@Override
	protected Component buildMainComponent(ViewChangeListener.ViewChangeEvent event) {
		Button add = new AddButton($("TasksManagementView.add")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.addTaskButtonClicked();
			}
		};

		sheet = new TabSheet();
		sheet.setSizeFull();
		sheet.addSelectedTabChangeListener(new TabSheet.SelectedTabChangeListener() {
			@Override
			public void selectedTabChange(TabSheet.SelectedTabChangeEvent event) {
				reloadCurrentTab();
			}
		});

		for (String tabId : presenter.getTabs()) {
			sheet.addTab(buildEmptyTab(tabId));
		}

		VerticalLayout layout = new VerticalLayout(add, sheet);
		layout.setComponentAlignment(add, Alignment.TOP_RIGHT);
		layout.setSpacing(true);
		layout.setSizeFull();

		return layout;
	}

	public void reloadCurrentTab() {
		presenter.tabSelected(sheet.getSelectedTab().getId());
	}

	private VerticalLayout buildEmptyTab(String tabId) {
		VerticalLayout tab = new VerticalLayout();
		tab.setCaption(presenter.getTabCaption(tabId));
		tab.setId(tabId);
		tab.setSpacing(true);
		return tab;
	}

	@Override
	protected String getTitle() {
		return $("TasksManagementView.viewTitle");
	}

	private VerticalLayout getEmptiedSelectedTab() {
		VerticalLayout tab = (VerticalLayout) sheet.getSelectedTab();
		tab.removeAllComponents();
		return tab;
	}

	@Override
	public void displayTasks(RecordVODataProvider provider) {
		VerticalLayout layout = getEmptiedSelectedTab();
		layout.addComponent(new TaskTable(provider, presenter));
	}

	@Override
	public void refreshCurrentTabTasksPanel() {
		reloadCurrentTab();
	}
}
