package com.constellio.app.modules.tasks.ui.pages;

import static com.constellio.app.ui.framework.buttons.WindowButton.WindowConfiguration.modalDialog;
import static com.constellio.app.ui.i18n.i18n.$;
import static java.util.Arrays.asList;

import java.util.List;

import com.constellio.app.api.extensions.params.UpdateComponentExtensionParams;
import com.constellio.app.modules.tasks.ui.components.TaskTable;
import com.constellio.app.modules.tasks.ui.components.WorkflowTable;
import com.constellio.app.ui.framework.buttons.AddButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.framework.items.RecordVOItem;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.data.Property;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.*;

public class TaskManagementViewImpl extends BaseViewImpl implements TaskManagementView {
	private final TaskManagementPresenter presenter;
	private TabSheet sheet;
	private ComboBox timestamp;

	enum Timestamp {
		ALL, TODAY, WEEK, MONTH
	}

	public TaskManagementViewImpl() {
		this.presenter = new TaskManagementPresenter(this);
	}

	@Override
	protected String getTitle() {
		return $("TasksManagementView.viewTitle");
	}

	@Override
	protected List<Button> buildActionMenuButtons(ViewChangeEvent event) {
		List<Button> buttons = super.buildActionMenuButtons(event);
		buttons.add(new AddButton($("TasksManagementView.add")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.addTaskButtonClicked();
			}
		});

		if (presenter.areWorkflowsEnabled() && presenter.hasPermissionToStartWorkflow()) {
			Button startWorkflowButton = new StartWorkflowButton();
			startWorkflowButton.setVisible(presenter.hasPermissionToStartWorkflow());
			buttons.add(startWorkflowButton);
		}
		return buttons;
	}

	@Override
	protected Component buildMainComponent(ViewChangeListener.ViewChangeEvent event) {
		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setSpacing(true);
		timestamp = new ComboBox(presenter.getDueDateCaption(), asList(Timestamp.ALL, Timestamp.TODAY, Timestamp.WEEK, Timestamp.MONTH));
		timestamp.setNullSelectionAllowed(false);
		timestamp.setValue(Timestamp.ALL);
		timestamp.setItemCaption(Timestamp.ALL, $("all"));
		timestamp.setItemCaption(Timestamp.TODAY, $("today"));
		timestamp.setItemCaption(Timestamp.WEEK, $("week"));
		timestamp.setItemCaption(Timestamp.MONTH, $("month"));
		timestamp.setWidth("20%");
		timestamp.addValueChangeListener(new Property.ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				reloadCurrentTab();
			}
		});
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

		mainLayout.addComponents(timestamp, sheet);
		return mainLayout;
	}

	@Override
	public void reloadCurrentTab() {
		presenter.tabSelected(sheet.getSelectedTab().getId());
	}

	public void resortTable() {
//		VerticalLayout tab = (VerticalLayout) sheet.getSelectedTab();
//		TaskTable table = (TaskTable) tab.getComponent(0);
//		table.resort();
		reloadCurrentTab();
	}

	@Override
	public Component getSelectedTab() {
		return sheet.getSelectedTab();
	}

	@Override
	public void displayTasks(RecordVODataProvider provider) {
		VerticalLayout layout = getEmptiedSelectedTab();
		layout.addComponent(new TaskTable(provider, presenter));
	}

//	@Override
//	public void displayTasks(RecordVODataProvider provider, Object[] propertyId, boolean[] ascending) {
//		VerticalLayout layout = getEmptiedSelectedTab();
//		layout.addComponent(new TaskTable(provider, presenter).sort(propertyId, ascending));
//	}

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

	public TabSheet getSheet() {
		return sheet;
	}

	private class StartWorkflowButton extends WindowButton {
		public StartWorkflowButton() {
			super($("TasksManagementView.startWorkflow"), $("TasksManagementView.startWorkflow"), modalDialog("75%", "75%"));
		}

		@Override
		protected Component buildWindowContent() {
			RecordVOTable table = new RecordVOTable(presenter.getWorkflows());
			table.setWidth("98%");
			table.addItemClickListener(new ItemClickListener() {
				@Override
				public void itemClick(ItemClickEvent event) {
					RecordVOItem item = (RecordVOItem) event.getItem();
					presenter.workflowStartRequested(item.getRecord());
					getWindow().close();
				}
			});
			return table;
		}
	}

	@Override
	public Timestamp getTimestamp() {
		return (Timestamp) timestamp.getValue();
	}
}
