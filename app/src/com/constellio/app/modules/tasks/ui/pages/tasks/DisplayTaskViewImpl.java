package com.constellio.app.modules.tasks.ui.pages.tasks;

import com.constellio.app.modules.tasks.ui.components.TaskTable;
import com.constellio.app.modules.tasks.ui.components.breadcrumb.TaskBreadcrumbTrail;
import com.constellio.app.modules.tasks.ui.components.display.TaskDisplayFactory;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.AddButton;
import com.constellio.app.ui.framework.buttons.ConfirmDialogButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.components.RecordDisplay;
import com.constellio.app.ui.framework.components.breadcrumb.BaseBreadcrumbTrail;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class DisplayTaskViewImpl extends BaseViewImpl implements DisplayTaskView {
	public static final String STYLE_NAME = "display-folder";
	private final DisplayTaskPresenter presenter;
	private Component subTasks;
	private Component eventsComponent;
	private TabSheet tabSheet;
	private RecordDisplay recordDisplay;

	public DisplayTaskViewImpl() {
		presenter = new DisplayTaskPresenter(this);
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		presenter.initTaskVO(event.getParameters());
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		RecordVO currentTask = presenter.getTask();
		VerticalLayout verticalLayout = new VerticalLayout();

		recordDisplay = new RecordDisplay(currentTask, new TaskDisplayFactory());
		subTasks = new CustomComponent();

		tabSheet = new TabSheet();
		tabSheet.addStyleName(STYLE_NAME);
		tabSheet.addTab(recordDisplay, $("DisplayTaskView.tabs.metadata"));
		tabSheet.addTab(subTasks, $("DisplayTaskView.tabs.subtasks", presenter.getSubTaskCount()));

		eventsComponent = new CustomComponent();
		tabSheet.addTab(eventsComponent, $("DisplayTaskView.tabs.logs"));
		tabSheet.getTab(eventsComponent).setEnabled(true);

		verticalLayout.addComponent(tabSheet);
		presenter.selectInitialTabForUser();
		return verticalLayout;
	}

	@Override
	protected void afterViewAssembled(ViewChangeEvent event) {
		presenter.viewAssembled();
	}

	@Override
	protected List<Button> buildActionMenuButtons(ViewChangeEvent event) {
		List<Button> actionMenuButtons = new ArrayList<>();

		EditButton editCurrentTask = new EditButton($("DisplayTaskView.modifyTask")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.editButtonClicked();
			}

			@Override
			public boolean isVisible() {
				return super.isVisible() && presenter.isEditCurrentTaskButtonVisible();
			}
		};
		actionMenuButtons.add(editCurrentTask);

		ConfirmDialogButton autoAssignTask = new ConfirmDialogButton($("DisplayTaskView.autoAssignTask")) {
			@Override
			protected String getConfirmDialogMessage() {
				return $("DisplayTaskView.autoAssignTaskDialogMessage");
			}

			@Override
			protected void confirmButtonClick(ConfirmDialog dialog) {
				presenter.autoAssignButtonClicked();
			}

			@Override
			public boolean isVisible() {
				return super.isVisible() && presenter.isAutoAssignButtonEnabled();
			}
		};
		actionMenuButtons.add(autoAssignTask);

		ConfirmDialogButton completeTask = new ConfirmDialogButton($("DisplayTaskView.completeTask")) {
			@Override
			protected String getConfirmDialogMessage() {
				return $("DisplayTaskView.completeTaskDialogMessage");
			}

			@Override
			protected void confirmButtonClick(ConfirmDialog dialog) {
				presenter.completeButtonClicked();
			}

			@Override
			public boolean isVisible() {
				return super.isVisible() && presenter.isCompleteCurrentTaskButtonVisible();
			}
		};
		actionMenuButtons.add(completeTask);

		ConfirmDialogButton closeTask = new ConfirmDialogButton($("DisplayTaskView.closeTask")) {
			@Override
			protected String getConfirmDialogMessage() {
				return $("DisplayTaskView.closeTaskDialogMessage");
			}

			@Override
			protected void confirmButtonClick(ConfirmDialog dialog) {
				presenter.closeButtonClicked();
			}

			@Override
			public boolean isVisible() {
				return super.isVisible() && presenter.isCloseCurrentTaskButtonVisible();
			}
		};
		actionMenuButtons.add(closeTask);

		AddButton createSubTask = new AddButton($("DisplayTaskView.createSubTask"), false) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.createSubTaskButtonClicked();
			}

			@Override
			public boolean isVisible() {
				return super.isVisible() && presenter.isCreateCurrentTaskSubTaskButtonVisible();
			}
		};
		actionMenuButtons.add(createSubTask);

		DeleteButton deleteTask = new DeleteButton($("DisplayTaskView.deleteTask")) {
			@Override
			protected void confirmButtonClick(ConfirmDialog dialog) {
				presenter.deleteButtonClicked();
			}

			@Override
			public boolean isVisible() {
				return super.isVisible() && presenter.isDeleteCurrentTaskButtonVisible();
			}
		};
		actionMenuButtons.add(deleteTask);

		return actionMenuButtons;
	}

	@Override
	public void setSubTasks(RecordVODataProvider dataProvider) {
		Component table = new TaskTable(dataProvider, presenter);
		tabSheet.replaceComponent(subTasks, table);
		subTasks = table;
	}

	@Override
	public void setEvents(RecordVODataProvider dataProvider) {
		Table table = new RecordVOTable($("DisplayTaskView.tabs.logs"), new RecordVOLazyContainer(dataProvider));
		table.setSizeFull();
		tabSheet.replaceComponent(eventsComponent, table);
		eventsComponent = table;
	}

	@Override
	public void selectMetadataTab() {
		tabSheet.setSelectedTab(recordDisplay);
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
		RecordVO currentTask = presenter.getTask();
		String recordId = currentTask.getId();
		return new TaskBreadcrumbTrail(recordId);
	}

	@Override
	protected ClickListener getBackButtonClickListener() {
		return new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.backButtonClicked();
			}
		};
	}

	@Override
	public void refreshSubTasksTable() {
		// TODO: Reimplement
	}
}
