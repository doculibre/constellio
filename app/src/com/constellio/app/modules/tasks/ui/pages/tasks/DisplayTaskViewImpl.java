package com.constellio.app.modules.tasks.ui.pages.tasks;

import com.constellio.app.modules.restapi.core.util.ListUtils;
import com.constellio.app.modules.tasks.TaskModule;
import com.constellio.app.modules.tasks.extensions.api.TaskModuleExtensions;
import com.constellio.app.modules.tasks.services.menu.TaskMenuItemServices.TaskItemActionType;
import com.constellio.app.modules.tasks.ui.components.ExpandableTaskTable;
import com.constellio.app.modules.tasks.ui.components.ExpandableTaskTable.TaskDetailsComponentFactory;
import com.constellio.app.modules.tasks.ui.components.FilterTableAdapter;
import com.constellio.app.modules.tasks.ui.components.LegacyTaskTable;
import com.constellio.app.modules.tasks.ui.components.breadcrumb.TaskBreadcrumbTrail;
import com.constellio.app.modules.tasks.ui.components.display.TaskDisplayFactory;
import com.constellio.app.services.menu.MenuItemAction;
import com.constellio.app.services.menu.MenuItemFactory.MenuItemRecordProvider;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.RecordDisplay;
import com.constellio.app.ui.framework.components.breadcrumb.BaseBreadcrumbTrail;
import com.constellio.app.ui.framework.components.buttons.RecordVOActionButtonFactory;
import com.constellio.app.ui.framework.components.menuBar.ActionMenuDisplay;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.components.table.TaskTableFilterDecorator;
import com.constellio.app.ui.framework.components.table.TaskTableFilterGenerator;
import com.constellio.app.ui.framework.components.table.columns.EventVOTableColumnsManager;
import com.constellio.app.ui.framework.components.table.columns.TableColumnsManager;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.data.utils.dev.Toggle;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import org.tepi.filtertable.FilterGenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.constellio.app.ui.i18n.i18n.$;

public class DisplayTaskViewImpl extends BaseViewImpl implements DisplayTaskView {
	private final DisplayTaskPresenter presenter;
	private Component subTasks;
	private Component eventsComponent;
	private TabSheet tabSheet;
	private RecordDisplay recordDisplay;
	private VerticalLayout recordDisplayLayout;
	private FilterGenerator filterGenerator;
	private TaskDetailsComponentFactory taskDetailsComponentFactory;
	private boolean nestedView = false;
	private boolean inWindow = false;

	public DisplayTaskViewImpl() {
		presenter = new DisplayTaskPresenter(this);
	}

	public DisplayTaskViewImpl(RecordVO recordVO, boolean nestedView, boolean inWindow) {
		this.nestedView = nestedView;
		this.inWindow = inWindow;
		presenter = new DisplayTaskPresenter(this, recordVO, nestedView, inWindow);
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		if (event != null) {
			presenter.initTaskVO(event.getParameters());
		}
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		RecordVO currentTask = presenter.getTaskVO();
		VerticalLayout verticalLayout = new VerticalLayout();

		recordDisplayLayout = new VerticalLayout();
		recordDisplayLayout.setSpacing(true);
		TaskModuleExtensions taskModuleExtensions = this.getConstellioFactories().getAppLayerFactory().getExtensions().forCollection(this.getCollection())
				.forModule(TaskModule.ID);
		recordDisplay = new RecordDisplay(currentTask, new TaskDisplayFactory(taskModuleExtensions));
		recordDisplayLayout.addComponent(recordDisplay);
		recordDisplayLayout.setId(RECORD_DISPLAY_LAYOUT_ID);


		tabSheet = new TabSheet();
		tabSheet.addTab(recordDisplayLayout, $("DisplayTaskView.tabs.metadata"));

		if (!presenter.isTaskModel()) {
			subTasks = new CustomComponent();
			subTasks.setId(SUB_TASKS_ID);
			tabSheet.addTab(subTasks, $("DisplayTaskView.tabs.subtasks", presenter.getSubTaskCount()));
		}

		eventsComponent = new CustomComponent();
		tabSheet.addTab(eventsComponent, $("DisplayTaskView.tabs.logs"));
		if (presenter.hasCurrentUserPermissionToViewEvents()) {
			tabSheet.getTab(eventsComponent).setEnabled(true);
		} else {
			tabSheet.getTab(eventsComponent).setEnabled(false);
		}

		tabSheet.addSelectedTabChangeListener(new TabSheet.SelectedTabChangeListener() {
			@Override
			public void selectedTabChange(TabSheet.SelectedTabChangeEvent event) {
				if (event.getTabSheet().getSelectedTab() == eventsComponent) {
					presenter.refreshEvents();
				}
			}
		});

		verticalLayout.addComponent(tabSheet);
		presenter.selectInitialTabForUser();


		return verticalLayout;
	}

	@Override
	protected boolean isFullWidthIfActionMenuAbsent() {
		return true;
	}

	@Override
	protected String getActionMenuBarCaption() {
		return null;
	}

	@Override
	protected boolean isBreadcrumbsVisible() {
		return !nestedView && !inWindow;
	}

	@Override
	public Component getSelectedTab() {
		return tabSheet.getSelectedTab();
	}

	@Override
	protected void afterViewAssembled(ViewChangeEvent event) {
		presenter.viewAssembled();
	}

	@Override
	public void setSubTasks(RecordVODataProvider dataProvider) {
		Component newSubTaskTable = null;
		if (Toggle.SHOW_LEGACY_TASK_TABLE.isEnabled()) {
			LegacyTaskTable unfilteredTable = new LegacyTaskTable(dataProvider, presenter);
			unfilteredTable.setTaskDetailsComponentFactory(taskDetailsComponentFactory);

			FilterTableAdapter tableAdapter;
			if (filterGenerator == null) {
				tableAdapter = new FilterTableAdapter(unfilteredTable, new TaskTableFilterDecorator(), new TaskTableFilterGenerator());
			} else {
				tableAdapter = new FilterTableAdapter(unfilteredTable, new TaskTableFilterDecorator(), filterGenerator);
			}

			// cas uniquement pour l'exemple
			tableAdapter.setFilterFieldVisible("menuBar", false);
			tableAdapter.setFilterBarVisible(true);

			newSubTaskTable = tableAdapter;
		} else {
			ExpandableTaskTable expandableTaskTable = new ExpandableTaskTable(dataProvider, presenter);

			newSubTaskTable = expandableTaskTable;
		}

		tabSheet.replaceComponent(subTasks, newSubTaskTable);
		subTasks = newSubTaskTable;
	}

	@Override
	public void setEvents(RecordVODataProvider dataProvider) {
		RecordVOTable table = new RecordVOTable($("DisplayTaskView.tabs.logs"), new RecordVOLazyContainer(dataProvider)) {
			@Override
			protected TableColumnsManager newColumnsManager() {
				return new EventVOTableColumnsManager();
			}
		};
		table.setSizeFull();
		tabSheet.replaceComponent(eventsComponent, table);
		eventsComponent = table;
	}

	@Override
	public void selectMetadataTab() {
		tabSheet.setSelectedTab(recordDisplayLayout);
	}

	public void addFieldToRecordDisplay(Component newComponent, boolean readOnly) {
		if (newComponent != null) {
			recordDisplayLayout.addComponent(newComponent);
			newComponent.setReadOnly(readOnly);
		}
	}

	@Override
	protected List<MenuItemAction> buildMenuItemActions(ViewChangeEvent event) {
		List<List<MenuItemAction>> menuItemSources = new ArrayList<>();
		menuItemSources.add(super.buildMenuItemActions(event));

		if (!presenter.isTaskModel()) {
			List<TaskItemActionType> taskItemsToExclude = new ArrayList<>();
			if (!nestedView) {
				taskItemsToExclude.add(TaskItemActionType.TASK_CONSULT);
			}

			menuItemSources.add(buildRecordVOActionButtonFactory(taskItemsToExclude).buildMenuItemActions());
		}

		return ListUtils.flatMapFilteringNull(menuItemSources);
	}

	@Override
	protected ActionMenuDisplay buildActionMenuDisplay(ActionMenuDisplay defaultActionMenuDisplay) {
		return new ActionMenuDisplay(defaultActionMenuDisplay) {
			@Override
			public Supplier<String> getSchemaTypeCodeSupplier() {
				return presenter.getTaskVO().getSchema()::getTypeCode;
			}

			@Override
			public Supplier<MenuItemRecordProvider> getMenuItemRecordProviderSupplier() {
				return buildRecordVOActionButtonFactory()::buildMenuItemRecordProvider;
			}
		};
	}


	private RecordVOActionButtonFactory buildRecordVOActionButtonFactory() {
		return buildRecordVOActionButtonFactory(Collections.emptyList());
	}

	private RecordVOActionButtonFactory buildRecordVOActionButtonFactory(List<TaskItemActionType> taskItemsToExclude) {
		return new RecordVOActionButtonFactory(presenter.getTaskVO(),
				taskItemsToExclude.stream().map((item) -> item.name()).collect(Collectors.toList()));
	}

	@Override
	public void selectTasksTab() {
		tabSheet.setSelectedTab(subTasks);
	}

	protected String getTitle() {
		//		return $("DisplayTaskView.viewTitle", presenter.getTaskTitle());
		return null;
	}

	@Override
	protected BaseBreadcrumbTrail buildBreadcrumbTrail() {
		RecordVO currentTask = presenter.getTaskVO();
		String recordId = currentTask.getId();
		return new TaskBreadcrumbTrail(recordId, this);
	}

	@Override
	protected ClickListener getBackButtonClickListener() {
		if (nestedView || inWindow) {
			return null;
		} else {
			return new ClickListener() {
				@Override
				public void buttonClick(ClickEvent event) {
					presenter.backButtonClicked();
				}
			};
		}
	}

	@Override
	public void refreshSubTasksTable() {
		// TODO: Reimplement
	}

	public RecordVO getCurrentTask() {
		return presenter.getTaskVO();
	}

	public void setFilterGenerator(FilterGenerator filterGenerator) {
		this.filterGenerator = filterGenerator;
	}

	public void setTaskDetailsComponentFactory(TaskDetailsComponentFactory taskDetailsComponentFactory) {
		this.taskDetailsComponentFactory = taskDetailsComponentFactory;
	}
}
