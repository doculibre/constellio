package com.constellio.app.modules.tasks.ui.pages.tasks;

import com.constellio.app.modules.tasks.services.menu.TaskMenuItemServices.TaskItemActionType;
import com.constellio.app.modules.tasks.ui.components.TaskTable;
import com.constellio.app.modules.tasks.ui.components.breadcrumb.TaskBreadcrumbTrail;
import com.constellio.app.modules.tasks.ui.components.display.TaskDisplayFactory;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.RecordDisplay;
import com.constellio.app.ui.framework.components.breadcrumb.BaseBreadcrumbTrail;
import com.constellio.app.ui.framework.components.buttons.RecordVOActionButtonFactory;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.components.table.columns.EventVOTableColumnsManager;
import com.constellio.app.ui.framework.components.table.columns.TableColumnsManager;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.constellio.app.ui.i18n.i18n.$;

public class DisplayTaskViewImpl extends BaseViewImpl implements DisplayTaskView {
	private final DisplayTaskPresenter presenter;
	private Component subTasks;
	private Component eventsComponent;
	private TabSheet tabSheet;
	private RecordDisplay recordDisplay;
	private VerticalLayout recordDisplayLayout;
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
		recordDisplay = new RecordDisplay(currentTask, new TaskDisplayFactory());
		recordDisplayLayout.addComponent(recordDisplay);
		recordDisplayLayout.setId(RECORD_DISPLAY_LAYOUT_ID);



		tabSheet = new TabSheet();
		tabSheet.addTab(recordDisplayLayout, $("DisplayTaskView.tabs.metadata"));

		if(!presenter.isTaskModel()) {
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
	protected boolean isActionMenuBar() {
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
	protected List<Button> buildActionMenuButtons(ViewChangeEvent event) {
		if(!presenter.isTaskModel()) {
			return new RecordVOActionButtonFactory(
					presenter.getTaskVO(),
					Arrays.asList(TaskItemActionType.TASK_CONSULT.name(), TaskItemActionType.TASK_EDIT.name())).build();
		} else {
			return new ArrayList<>();
		}
	}

	@Override
	public void setSubTasks(RecordVODataProvider dataProvider) {
		Component table = new TaskTable(dataProvider, presenter);
		tabSheet.replaceComponent(subTasks, table);
		subTasks = table;
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
	protected List<Button> getQuickActionMenuButtons() {
		List<TaskItemActionType> taskItemsToExclude = new ArrayList(Arrays.asList(TaskItemActionType.values()));
		taskItemsToExclude.remove(TaskItemActionType.TASK_EDIT);

		return new RecordVOActionButtonFactory(presenter.getTaskVO(),
				taskItemsToExclude.stream().map((item) -> item.name()).collect(Collectors.toList())).build();
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
}
