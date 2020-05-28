package com.constellio.app.modules.tasks.ui.pages;

import com.constellio.app.modules.tasks.ui.components.TaskTable;
import com.constellio.app.modules.tasks.ui.components.TaskTable.TaskDetailsComponentFactory;
import com.constellio.app.ui.framework.buttons.AddButton;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.components.fields.BaseComboBox;
import com.constellio.app.ui.framework.components.layouts.I18NHorizontalLayout;
import com.constellio.app.ui.framework.components.tabs.IdTabSheet;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.entities.records.wrappers.User;
import com.vaadin.data.Property;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeListener;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tepi.filtertable.FilterGenerator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;
import static java.util.Arrays.asList;

public class TaskManagementViewImpl extends BaseViewImpl implements TaskManagementView {

	private static final Logger LOGGER = LoggerFactory.getLogger(TaskManagementViewImpl.class);

	private final TaskManagementPresenter presenter;

	//	private boolean workflowsTabsVisible;
	//	private boolean startWorkflowButtonVisible;

	private boolean tasksInSubTabSheet;

	private I18NHorizontalLayout actionButtonsLayout;

	private List<String> tasksTabs;
	// Key: primary tab, value: sub tabs
	private Map<String, List<String>> extraTabs = new HashMap<>();

	private List<? extends BaseButton> extraActionButtons = new ArrayList<>();

	private IdTabSheet primaryTabSheet;
	private IdTabSheet tasksTabSheet;

	private ComboBox timestamp;
	private String previousSelectedTab;
	private FilterGenerator filterGenerator;
	private TaskDetailsComponentFactory taskDetailsComponentFactory;
	private Label delegatedTasksAlert;

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
	protected boolean isFullWidthIfActionMenuAbsent() {
		return true;
	}

	@Override
	public void setTasksTabs(List<String> tasksTabs) {
		this.tasksTabs = tasksTabs;
	}

	public void setTasksInSubTabSheet(boolean tasksInSubTabSheet) {
		this.tasksInSubTabSheet = tasksInSubTabSheet;
	}

	@Override
	protected Component buildMainComponent(ViewChangeListener.ViewChangeEvent event) {
		addStyleName("task-management-view");

		actionButtonsLayout = new I18NHorizontalLayout();
		actionButtonsLayout.setSpacing(true);
		actionButtonsLayout.addStyleName("task-action-buttons");

		VerticalLayout mainLayout = new VerticalLayout();

		timestamp = new BaseComboBox(presenter.getDueDateCaption(), asList(Timestamp.ALL, Timestamp.TODAY, Timestamp.WEEK, Timestamp.MONTH));
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
				// FIXME
				presenter.tabSelected(primaryTabSheet.getSelectedTab().getId());
			}
		});

		primaryTabSheet = new IdTabSheet();
		primaryTabSheet.setSizeFull();


		if (tasksInSubTabSheet) {
			primaryTabSheet.addTab(buildEmptyTab(TASKS_TAB));
			tasksTabSheet = new IdTabSheet();
			tasksTabSheet.addStyleName("tabsheet-secondary");
			tasksTabSheet.addSelectedTabChangeListener(new TabSheet.SelectedTabChangeListener() {
				@Override
				public void selectedTabChange(TabSheet.SelectedTabChangeEvent event) {
					presenter.tabSelected(tasksTabSheet.getSelectedTab().getId());
				}
			});

			for (String tabId : tasksTabs) {
				tasksTabSheet.addTab(buildEmptyTab(tabId));
			}
		} else {
			for (String tabId : tasksTabs) {
				primaryTabSheet.addTab(buildEmptyTab(tabId));
			}
		}

		primaryTabSheet.addSelectedTabChangeListener(new TabSheet.SelectedTabChangeListener() {
			@Override
			public void selectedTabChange(TabSheet.SelectedTabChangeEvent event) {
				presenter.tabSelected(primaryTabSheet.getSelectedTab().getId());
			}
		});
		delegatedTasksAlert = presenter.getDelegatedTaskAlertState();
		delegatedTasksAlert.addStyleName(ValoTheme.LABEL_COLORED);
		delegatedTasksAlert.addStyleName(ValoTheme.LABEL_BOLD);

		mainLayout.addComponent(actionButtonsLayout);
		mainLayout.addComponent(delegatedTasksAlert);
		mainLayout.setComponentAlignment(actionButtonsLayout, Alignment.TOP_RIGHT);
		mainLayout.addComponents(primaryTabSheet);

		previousSelectedTab = presenter.getPreviousSelectedTab();
		if (previousSelectedTab != null) {
			backToPreviousSelectedTab();
		} else {
			presenter.tabSelected(primaryTabSheet.getSelectedTab().getId());
		}

		return mainLayout;
	}

	public void backToPreviousSelectedTab() {
		if (previousSelectedTab != null) {
			IdTabSheet tabSheet = getTabSheet(previousSelectedTab);
			if (tabSheet != null) {
				Component tabComponent = tabSheet.getTabComponent(previousSelectedTab);
				tabSheet.setSelectedTab(tabComponent);
				presenter.tabSelected(previousSelectedTab);
			}
		}
	}

	public String getPreviousSelectedTab() {
		return previousSelectedTab;
	}

	private IdTabSheet getTabSheet(String tabId) {
		IdTabSheet tabSheet;
		if (tabId.equals(TASKS_TAB) || extraTabs.containsKey(tabId)) {
			tabSheet = primaryTabSheet;
		} else if (presenter.isTaskTab(tabId)) {
			if (tasksInSubTabSheet) {
				tabSheet = tasksTabSheet;
			} else {
				tabSheet = primaryTabSheet;
			}
		} else {
			tabSheet = (IdTabSheet) primaryTabSheet.getTabComponent(tabId);
			if (tabSheet == null) {
				for (String extraTab : extraTabs.keySet()) {
					if (extraTabs.get(extraTab).contains(tabId)) {
						tabSheet = (IdTabSheet) primaryTabSheet.getTabComponent(extraTab);
						break;
					}
				}
			}
			if (tabSheet == null) {
				LOGGER.warn("No tab with id '" + tabId + "'. Extra tabs : " + extraTabs.keySet());
			}
		}


		return tabSheet;
	}

	@Override
	public Component getTabComponent(String tabId) {
		IdTabSheet tabSheet = getTabSheet(tabId);
		return tabSheet.getTabComponent(tabId);
	}

	@Override
	public void registerPreviousSelectedTab() {
		presenter.registerPreviousSelectedTab();
	}

	@Override
	public void displayTasks(RecordVODataProvider provider) {
		List<BaseButton> newActionButtons = new ArrayList<>();
		newActionButtons.add(new AddTaskButton());
		newActionButtons.addAll(extraActionButtons);
		setActionButtons(newActionButtons);

		TabSheet tabSheet;
		if (tasksInSubTabSheet) {
			VerticalLayout tasksTabLayout = getEmptiedSelectedTab(primaryTabSheet);
			tasksTabLayout.addComponent(tasksTabSheet);

			tabSheet = tasksTabSheet;
		} else {
			tabSheet = primaryTabSheet;
		}

		VerticalLayout layout = getEmptiedSelectedTab(tabSheet);
		TaskTable taskTable = new TaskTable(provider, presenter);
		taskTable.setFilterGenerator(filterGenerator);
		taskTable.setTaskDetailsComponentFactory(taskDetailsComponentFactory);

		//		FilterTableAdapter tableAdapter = new FilterTableAdapter(taskTable.getTable(), new DemoFilterDecorator(), new DemoFilterGenerator());
		//
		//		// cas uniquement pour l'exemple
		//		tableAdapter.setFilterFieldVisible("menuBar", false);
		//		tableAdapter.setFilterBarVisible(true);


		//		String starredByUserCode = Task.DEFAULT_SCHEMA + "_" + Task.STARRED_BY_USERS;
		//		String workflouwExecutionCode = Task.DEFAULT_SCHEMA + "_" + "linkedWorkflowExecution";
		//		String titleCode = Task.DEFAULT_SCHEMA + "_" + Task.TITLE;
		//		for(Object visibleColumn : taskTable.getVisibleColumns()){
		//			if (visibleColumn instanceof MetadataVO) {
		//				if (starredByUserCode.equals(((MetadataVO) visibleColumn).getCode()) || workflouwExecutionCode.equals(((MetadataVO) visibleColumn).getCode())) {
		//					tableAdapter.setColumnExpandRatio(visibleColumn, 1);
		//				}
		//				if(titleCode.equals(((MetadataVO)visibleColumn).getCode())){
		//					tableAdapter.setColumnExpandRatio(visibleColumn, 0);
		//				}
		//			}
		//		}

		layout.addComponent(taskTable);
		//layout.addComponent(new BaseFilteringTable());
	}

	//	@Override
	//	public void displayWorkflows(RecordVODataProvider provider) {
	//		addTaskButton.setVisible(false);
	//		startWorkflowButton.setVisible(startWorkflowButtonVisible);
	//
	//		VerticalLayout workflowsTabLayout = getEmptiedSelectedTab(primaryTabSheet);
	//		workflowsTabLayout.addComponent(workflowsTabSheet);
	//
	//		VerticalLayout layout = getEmptiedSelectedTab(workflowsTabSheet);
	//		layout.addComponent(new WorkflowTable(provider, presenter));
	//	}

	@Override
	public void reloadCurrentTab() {
		presenter.reloadCurrentTabRequested();
	}

	private VerticalLayout buildEmptyTab(String tabId) {
		VerticalLayout tab = new VerticalLayout();
		tab.setCaption(presenter.getTabCaption(tabId));
		tab.setId(tabId);
		tab.setSpacing(true);
		return tab;
	}

	private VerticalLayout getEmptiedSelectedTab(TabSheet tabSheet) {
		VerticalLayout tab = (VerticalLayout) tabSheet.getSelectedTab();
		tab.removeAllComponents();
		return tab;
	}

	public TabSheet getPrimaryTabSheet() {
		return primaryTabSheet;
	}

	public TabSheet getTasksTabSheet() {
		return tasksTabSheet;
	}

	@Override
	public Timestamp getTimestamp() {
		return (Timestamp) timestamp.getValue();
	}

	public void setFilterGenerator(FilterGenerator filterGenerator) {
		this.filterGenerator = filterGenerator;
	}

	public void setTaskDetailsComponentFactory(TaskDetailsComponentFactory taskDetailsComponentFactory) {
		this.taskDetailsComponentFactory = taskDetailsComponentFactory;
	}

	public User getCurrentUser() {
		return presenter.getCurrentUser();
	}

	public Component getExtraPrimaryTab(String tabId) {
		return primaryTabSheet.getTabComponent(tabId);
	}

	public void addExtraPrimaryTab(String tabId, Component component) {
		extraTabs.put(tabId, new ArrayList<String>());
		component.setId(tabId);
		primaryTabSheet.addComponent(component);
	}

	public IdTabSheet getExtraTabSheet(String tabId) {
		return (IdTabSheet) getExtraPrimaryTab(tabId);
	}

	public void addExtraTabSheet(String tabId, final TabSheet tabSheet) {
		addExtraPrimaryTab(tabId, tabSheet);

		tabSheet.addStyleName("tabsheet-secondary");
		tabSheet.addSelectedTabChangeListener(new TabSheet.SelectedTabChangeListener() {
			@Override
			public void selectedTabChange(TabSheet.SelectedTabChangeEvent event) {
				presenter.tabSelected(tabSheet.getSelectedTab().getId());
			}
		});
	}

	public Component getExtraSubTab(String tabId) {
		IdTabSheet tabSheet = getTabSheet(tabId);
		return tabSheet.getTabComponent(tabId);
	}

	public void addExtraSubTab(String tabId, Component component, String parentTabId) {
		List<String> extraTabSubTabs = extraTabs.get(parentTabId);
		if (extraTabSubTabs == null) {
			throw new RuntimeException("Parent tab not added : " + parentTabId);
		} else {
			extraTabSubTabs.add(tabId);
			IdTabSheet parentTabSheet = (IdTabSheet) primaryTabSheet.getTabComponent(parentTabId);
			component.setId(tabId);

			Collection<?> selectionTabChangeListeners = parentTabSheet.getListeners(TabSheet.SelectedTabChangeEvent.class);
			for (Object listener : selectionTabChangeListeners) {
				SelectedTabChangeListener selectedTabChangeListener = (TabSheet.SelectedTabChangeListener) listener;
				parentTabSheet.removeSelectedTabChangeListener(selectedTabChangeListener);
			}
			parentTabSheet.addTab(component);
			for (Object listener : selectionTabChangeListeners) {
				SelectedTabChangeListener selectedTabChangeListener = (TabSheet.SelectedTabChangeListener) listener;
				parentTabSheet.addSelectedTabChangeListener(selectedTabChangeListener);
			}
		}
	}

	public List<Button> getActionButtons() {
		List<Button> actionButtons = new ArrayList<>();
		for (Iterator<Component> it = actionButtonsLayout.iterator(); it.hasNext(); ) {
			actionButtons.add((Button) it.next());
		}
		return actionButtons;
	}

	public void setActionButtons(List<? extends BaseButton> actionButtons) {
		actionButtonsLayout.removeAllComponents();
		for (BaseButton actionButton : actionButtons) {
			if (actionButton.getIcon() != null && StringUtils.isNotBlank(actionButton.getCaption())) {
				actionButton.setCaptionVisibleOnMobile(false);
			}
			actionButton.addStyleName(ValoTheme.BUTTON_LINK);
			actionButtonsLayout.addComponent(actionButton);
		}
		actionButtonsLayout.setVisible(actionButtonsLayout.getComponentCount() > 0);
	}

	public List<? extends Button> getExtraActionButtons() {
		return extraActionButtons;
	}

	public void setExtraActionButtons(List<? extends BaseButton> extraActionButtons) {
		this.extraActionButtons = extraActionButtons;
	}

	@Override
	public void setTabBadge(String tabId, String badge) {
		IdTabSheet tabSheet = getTabSheet(tabId);
		tabSheet.setBadge(tabId, badge);
	}

	public class AddTaskButton extends AddButton {

		public AddTaskButton() {
			super($("TasksManagementView.add"));
			setIcon(FontAwesome.TASKS);
			addStyleName("add-task-button");
			addStyleName(ValoTheme.BUTTON_LINK);
		}

		@Override
		protected void buttonClick(ClickEvent event) {
			presenter.addTaskButtonClicked();
		}
	}
}
