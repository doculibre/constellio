package com.constellio.app.modules.tasks.ui.pages;

import static com.constellio.app.ui.i18n.i18n.$;

import com.constellio.app.modules.tasks.ui.components.TaskTable;
import com.constellio.app.modules.tasks.ui.components.WorkflowTable;
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

	@Override
	public void reloadCurrentTab() {
		presenter.tabSelected(sheet.getSelectedTab().getId());
	}

	@Override
	protected String getTitle() {
		return $("TasksManagementView.viewTitle");
	}

	@Override
	public void displayTasks(RecordVODataProvider provider) {
		VerticalLayout layout = getEmptiedSelectedTab();
		layout.addComponent(new TaskTable(provider, presenter));
	}

	@Override
	public void displayWorkflows(RecordVODataProvider provider) {
		VerticalLayout layout = getEmptiedSelectedTab();
		layout.addComponent(new WorkflowTable(provider, presenter));
	}

	private VerticalLayout buildEmptyTab(String tabId) {
		VerticalLayout tab = new VerticalLayout();
		tab.setCaption(presenter.getTabCaption(tabId));
		tab.setId(tabId);
		tab.setSpacing(true);
		return tab;
	}

	private VerticalLayout getEmptiedSelectedTab() {
		VerticalLayout tab = (VerticalLayout) sheet.getSelectedTab();
		tab.removeAllComponents();
		return tab;
	}
}
