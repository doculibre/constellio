package com.constellio.app.modules.tasks.ui.pages.tasks;

import com.constellio.app.modules.tasks.ui.components.TaskTable;
import com.constellio.app.modules.tasks.ui.components.breadcrumb.TaskBreadcrumbTrail;
import com.constellio.app.modules.tasks.ui.components.display.TaskDisplayFactory;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.AddButton;
import com.constellio.app.ui.framework.buttons.ConfirmDialogButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.buttons.report.ReportGeneratorButton;
import com.constellio.app.ui.framework.components.RecordDisplay;
import com.constellio.app.ui.framework.components.breadcrumb.BaseBreadcrumbTrail;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.components.table.columns.EventVOTableColumnsManager;
import com.constellio.app.ui.framework.components.table.columns.TableColumnsManager;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.pages.management.Report.PrintableReportListPossibleType;
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
		if(presenter.hasCurrentUserPermissionToViewEvents()) {
			tabSheet.getTab(eventsComponent).setEnabled(true);
		}
		else {
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
	protected void afterViewAssembled(ViewChangeEvent event) {
		presenter.viewAssembled();
	}

	@Override
	protected List<Button> buildActionMenuButtons(ViewChangeEvent event) {
		List<Button> actionMenuButtons = new ArrayList<>();

		if(!presenter.isLogicallyDeleted()) {
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
			protected String getConfirmDialogOKCaption() {
				return $("DisplayTaskView.quickComplete");
			}

			@Override
			protected String getConfirmDialogCancelCaption() {
				return $("cancel");
			}

			@Override
			protected String getConfirmDialogNotOkCaption() {
				return $("DisplayTaskView.slowComplete");
			}

			@Override
			protected void confirmButtonClick(ConfirmDialog dialog) {
				if(dialog.isConfirmed()) {
					presenter.completeQuicklyButtonClicked(presenter.getTask());
				} else if(dialog.isCanceled()) {

				} else {presenter.completeButtonClicked(presenter.getTask());
				}
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

				@Override
				public boolean isEnabled() {
					return super.isEnabled() && presenter.isCloseCurrentTaskButtonVisible();
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

			ReportGeneratorButton reportGeneratorButton = new ReportGeneratorButton($("ReportGeneratorButton.buttonText"), $("ReportGeneratorButton.windowText"), this, getConstellioFactories().getAppLayerFactory(), getCollection(), PrintableReportListPossibleType.TASK,  presenter.getTask());
			actionMenuButtons.add(reportGeneratorButton);
		}

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
		return new TaskBreadcrumbTrail(recordId, this);
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
