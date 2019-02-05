package com.constellio.app.modules.tasks.ui.pages;

import com.constellio.app.entities.schemasDisplay.SchemaDisplayConfig;
import com.constellio.app.modules.tasks.ui.components.DemoFilterDecorator;
import com.constellio.app.modules.tasks.ui.components.DemoFilterGenerator;
import com.constellio.app.modules.tasks.ui.components.FilterTableAdapter;
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
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.constellio.app.ui.framework.buttons.WindowButton.WindowConfiguration.modalDialog;
import static com.constellio.app.ui.i18n.i18n.$;
import static java.util.Arrays.asList;

public class TaskManagementViewImpl extends BaseViewImpl implements TaskManagementView {
	private final TaskManagementPresenter presenter;
	private TabSheet sheet;
	private ComboBox timestamp;
	private String previousSelectedTab;

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

		previousSelectedTab = presenter.getPreviousSelectedTab();
		backToPreviousSelectedTab();

		return mainLayout;
	}

	public void backToPreviousSelectedTab() {
		if (previousSelectedTab != null) {
			Iterator<Component> iterator = sheet.iterator();
			while (iterator.hasNext()) {
				Component component = iterator.next();
				if (previousSelectedTab.equals(component.getId())) {
					sheet.setSelectedTab(component);
					presenter.tabSelected(previousSelectedTab);
					break;
				}
			}
		}
	}

	public String getPreviousSelectedTab() {
		return previousSelectedTab;
	}

	@Override
	public void reloadCurrentTab() {
		presenter.tabSelected(sheet.getSelectedTab().getId());
	}

	@Override
	public Component getSelectedTab() {
		return sheet.getSelectedTab();
	}

	@Override
	public void registerPreviousSelectedTab() {
		presenter.registerPreviousSelectedTab();
	}

	@Override
	public void displayTasks(RecordVODataProvider provider) {
		VerticalLayout layout = getEmptiedSelectedTab();
		TaskTable taskTable = new TaskTable(provider, presenter) {
			@Override
			protected TableColumnsManager newColumnsManager() {
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

		taskTable.setColumnExpandRatio("userTask_default_linkedWorkflowExecution", 0);
		FilterTableAdapter tableAdapter = new FilterTableAdapter(taskTable, new DemoFilterDecorator(), new DemoFilterGenerator());

		// cas uniquement pour l'exemple
		tableAdapter.setFilterFieldVisible("menuBar", false);
		tableAdapter.setFilterBarVisible(true);


		layout.addComponent(tableAdapter);
		//layout.addComponent(new BaseFilteringTable());
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
