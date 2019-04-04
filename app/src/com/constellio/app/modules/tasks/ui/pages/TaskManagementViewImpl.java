package com.constellio.app.modules.tasks.ui.pages;

import static com.constellio.app.ui.framework.buttons.WindowButton.WindowConfiguration.modalDialog;
import static com.constellio.app.ui.i18n.i18n.$;
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

import com.constellio.app.entities.schemasDisplay.SchemaDisplayConfig;
import com.constellio.app.modules.tasks.ui.components.TaskTable;
import com.constellio.app.modules.tasks.ui.components.WorkflowTable;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.framework.buttons.AddButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.fields.BaseComboBox;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.components.table.columns.RecordVOTableColumnsManager;
import com.constellio.app.ui.framework.components.table.columns.TableColumnsManager;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.framework.items.RecordVOItem;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.pages.base.SessionContext;
import com.vaadin.data.Property;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

public class TaskManagementViewImpl extends BaseViewImpl implements TaskManagementView {
	
	private final TaskManagementPresenter presenter;
	
	private boolean workflowsTabsVisible;
	private boolean startWorkflowButtonVisible;
	
	private List<String> primaryTabs;
	private List<String> tasksTabs;
	
	private TabSheet primaryTabSheet;
	private TabSheet tasksTabSheet;
	private TabSheet workflowsTabSheet;
	private ComboBox timestamp;
	private String previousSelectedTab;
	private Button addTaskButton;
	private Button startWorkflowButton;

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
	public void setPrimaryTabs(List<String> tabs) {
		this.primaryTabs = tabs;
	}
	
	@Override
	public void setTasksTabs(List<String> tasksTabs) {
		this.tasksTabs = tasksTabs;
	}
	
	public boolean isWorkflowsTabsVisible() {
		return workflowsTabsVisible;
	}

	@Override
	public void setWorkflowsTabsVisible(boolean visible) {
		this.workflowsTabsVisible = visible;
	}
	
	public boolean isStartWorkflowButtonVisible() {
		return startWorkflowButtonVisible;
	}

	@Override
	public void setStartWorkflowButtonVisible(boolean visible) {
		this.startWorkflowButtonVisible = visible;
	}

	@Override
	protected Component buildMainComponent(ViewChangeListener.ViewChangeEvent event) {
		addStyleName("task-management-view");
		
		addTaskButton = new AddButton($("TasksManagementView.add")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.addTaskButtonClicked();
			}
		};
		addTaskButton.addStyleName("add-task-button");
		addTaskButton.addStyleName(ValoTheme.BUTTON_LINK);
		
		if (startWorkflowButton == null) {
			startWorkflowButton = new StartWorkflowButton();
		}
		startWorkflowButton.setVisible(startWorkflowButtonVisible);
		startWorkflowButton.addStyleName("start-workflow-button");
		startWorkflowButton.addStyleName(ValoTheme.BUTTON_LINK);
		
		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setSpacing(true);
		
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
		
		primaryTabSheet = new TabSheet();
		primaryTabSheet.setSizeFull();
		primaryTabSheet.addSelectedTabChangeListener(new TabSheet.SelectedTabChangeListener() {
			@Override
			public void selectedTabChange(TabSheet.SelectedTabChangeEvent event) {
				presenter.tabSelected(primaryTabSheet.getSelectedTab().getId());
			}
		});
		for (String tabId : primaryTabs) {
			primaryTabSheet.addTab(buildEmptyTab(tabId));
		}
		
		if (workflowsTabsVisible) {
			tasksTabSheet = new TabSheet();
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
			
			workflowsTabSheet = new TabSheet();
			workflowsTabSheet.addStyleName("tabsheet-secondary");
			workflowsTabSheet.addSelectedTabChangeListener(new TabSheet.SelectedTabChangeListener() {
				@Override
				public void selectedTabChange(TabSheet.SelectedTabChangeEvent event) {
					presenter.tabSelected(workflowsTabSheet.getSelectedTab().getId());
				}
			});
		}
		
		mainLayout.addComponent(addTaskButton);
		mainLayout.setComponentAlignment(addTaskButton, Alignment.TOP_RIGHT);
		if (startWorkflowButtonVisible) {
			mainLayout.addComponent(startWorkflowButton);
			mainLayout.setComponentAlignment(startWorkflowButton, Alignment.TOP_RIGHT);
		}
		mainLayout.addComponents(/*timestamp,*/ primaryTabSheet);

		previousSelectedTab = presenter.getPreviousSelectedTab();
		backToPreviousSelectedTab();

		return mainLayout;
	}
	
	public void setStartWorkflowButton(Button button) {
		this.startWorkflowButton = button;
	}

	public void backToPreviousSelectedTab() {
		if (previousSelectedTab != null) {
			TabSheet tabSheet = getTabSheet(previousSelectedTab);
			Component tabComponent = getTabComponent(tabSheet, previousSelectedTab);
			tabSheet.setSelectedTab(tabComponent);
			presenter.tabSelected(previousSelectedTab);
		}
	}

	public String getPreviousSelectedTab() {
		return previousSelectedTab;
	}
	
	private TabSheet getTabSheet(String tabId) {
		TabSheet tabSheet;
		if (presenter.isWorkflowTab(tabId)) {
			tabSheet = tasksTabSheet;
		} else {
			tabSheet = primaryTabSheet;
		}
		return tabSheet;
	}
	
	private Component getTabComponent(TabSheet tabSheet, String tabId) {
		Component tabComponent = null;
		Iterator<Component> iterator = tabSheet.iterator();
		while (iterator.hasNext()) {
			Component component = iterator.next();
			if (tabId.equals(component.getId())) {
				tabComponent = component;
				break;
			}
		}	
		return tabComponent;
	}
	
	@Override
	public Component getTabComponent(String tabId) {
		TabSheet tabSheet = getTabSheet(tabId);
		return getTabComponent(tabSheet, tabId);
	}

	@Override
	public void registerPreviousSelectedTab() {
		presenter.registerPreviousSelectedTab();
	}

	@Override
	public void displayTasks(RecordVODataProvider provider) {
		addTaskButton.setVisible(true);
		startWorkflowButton.setVisible(false);
		
		TabSheet tabSheet;
		if (workflowsTabsVisible) {
			VerticalLayout tasksTabLayout = getEmptiedSelectedTab(primaryTabSheet);
			tasksTabLayout.addComponent(tasksTabSheet);
			
			tabSheet = tasksTabSheet;
		} else {
			tabSheet = primaryTabSheet;
		}
			
		VerticalLayout layout = getEmptiedSelectedTab(tabSheet);
		TaskTable taskTable = new TaskTable(provider, presenter) {
			@Override
			protected TableColumnsManager newColumnsManager() {
				if (true) return super.newColumnsManager();
				return new RecordVOTableColumnsManager() {
					@Override
					protected List<String> getDefaultVisibleColumnIds(Table table) {
						List<String> defaultVisibleColumnIds;
						RecordVOTable recordVOTable = (RecordVOTable) table;
						List<MetadataSchemaVO> schemaVOs = recordVOTable.getSchemas();
						if (!schemaVOs.isEmpty()) {
							defaultVisibleColumnIds = new ArrayList<>();
							SessionContext sessionContext = ConstellioUI.getCurrentSessionContext();
							String collection = sessionContext.getCurrentCollection();
							AppLayerFactory appLayerFactory = constellioFactories.getAppLayerFactory();
							SchemasDisplayManager schemasDisplayManager = appLayerFactory.getMetadataSchemasDisplayManager();

							for (MetadataSchemaVO schemaVO : schemaVOs) {
								if (CollectionUtils.isNotEmpty(schemaVO.getTableMetadataCodes())) {
									defaultVisibleColumnIds.addAll(schemaVO.getTableMetadataCodes());
								} else {
									String schemaCode = schemaVO.getCode();
									SchemaDisplayConfig schemaDisplayConfig = schemasDisplayManager.getSchema(collection, schemaCode);
									defaultVisibleColumnIds.addAll(schemaDisplayConfig.getTableMetadataCodes());
								}
							}

							Object[] tableVisibleColumns = table.getVisibleColumns();
							for (Object tableVisibleColumn : tableVisibleColumns) {
								if (!(tableVisibleColumn instanceof MetadataVO)) {
									String columnId = toColumnId(tableVisibleColumn);
									defaultVisibleColumnIds.add(columnId);
								}
							}
						} else {
							defaultVisibleColumnIds = super.getDefaultVisibleColumnIds(table);
						}
						return defaultVisibleColumnIds;
					}
				};
			}
		};

//		FilterTableAdapter tableAdapter = new FilterTableAdapter(taskTable.getTable(), new DemoFilterDecorator(), new DemoFilterGenerator());
//
//		// cas uniquement pour l'exemple
//		tableAdapter.setFilterFieldVisible("menuBar", false);
//		tableAdapter.setFilterBarVisible(true);


		layout.addComponent(taskTable);
		//layout.addComponent(new BaseFilteringTable());
	}

	@Override
	public void displayWorkflows(RecordVODataProvider provider) {
		addTaskButton.setVisible(false);
		startWorkflowButton.setVisible(startWorkflowButtonVisible);
		
		VerticalLayout workflowsTabLayout = getEmptiedSelectedTab(primaryTabSheet);
		workflowsTabLayout.addComponent(workflowsTabSheet);
		
		VerticalLayout layout = getEmptiedSelectedTab(workflowsTabSheet);
		layout.addComponent(new WorkflowTable(provider, presenter));
	}

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
	
	public TabSheet getWorkflowsTabSheet() {
		return workflowsTabSheet;
	}

	private class StartWorkflowButton extends WindowButton {
		public StartWorkflowButton() {
			super($("TasksManagementView.startWorkflowBeta"), $("TasksManagementView.startWorkflow"), modalDialog("75%", "75%"));
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
