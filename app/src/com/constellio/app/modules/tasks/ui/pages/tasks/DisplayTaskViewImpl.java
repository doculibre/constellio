package com.constellio.app.modules.tasks.ui.pages.tasks;

import com.constellio.app.modules.tasks.ui.components.TaskTable;
import com.constellio.app.modules.tasks.ui.components.breadcrumb.TaskBreadcrumbTrail;
import com.constellio.app.modules.tasks.ui.components.display.TaskDisplayFactory;
import com.constellio.app.modules.tasks.ui.components.fields.list.ListAddRemoveCollaboratorsField;
import com.constellio.app.modules.tasks.ui.components.fields.list.ListAddRemoveCollaboratorsGroupsField;
import com.constellio.app.modules.tasks.ui.entities.TaskVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.AddButton;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.ConfirmDialogButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.RecordDisplay;
import com.constellio.app.ui.framework.components.ReportTabButton;
import com.constellio.app.ui.framework.components.breadcrumb.BaseBreadcrumbTrail;
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
import com.vaadin.ui.themes.ValoTheme;
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
	private VerticalLayout recordDisplayLayout;

	public DisplayTaskViewImpl() {
		presenter = new DisplayTaskPresenter(this);
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		presenter.initTaskVO(event.getParameters());
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

		subTasks = new CustomComponent();
		subTasks.setId(SUB_TASKS_ID);

		tabSheet = new TabSheet();
		tabSheet.addStyleName(STYLE_NAME);
		tabSheet.addTab(recordDisplayLayout, $("DisplayTaskView.tabs.metadata"));
		tabSheet.addTab(subTasks, $("DisplayTaskView.tabs.subtasks", presenter.getSubTaskCount()));

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
	public Component getSelectedTab() {
		return tabSheet.getSelectedTab();
	}

	@Override
	protected void afterViewAssembled(ViewChangeEvent event) {
		presenter.viewAssembled();
	}

	@Override
	protected List<Button> buildActionMenuButtons(ViewChangeEvent event) {
		List<Button> actionMenuButtons = new ArrayList<>();

		if (!presenter.isLogicallyDeleted()) {

			if (!presenter.isClosedOrTerminated()) {

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
			}
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

			TaskCompleteWindowButton completeTask = new TaskCompleteWindowButton(presenter.getTask(),
					$("DisplayTaskView.completeTask"), this.getConstellioFactories().getAppLayerFactory(), presenter) {
				@Override
				protected String getConfirmDialogMessage() {
					if (presenter.isSubTaskPresentAndHaveCertainStatus(presenter.getTaskVO())) {
						return $("DisplayTaskView.subTaskPresentComplete");
					}

					return $("DisplayTaskView.completeTaskDialogMessage");
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
			if (!presenter.isClosedOrTerminated()) {
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
			}
			DeleteButton deleteTask = new DeleteButton($("DisplayTaskView.deleteTask")) {
				@Override
				protected String getConfirmDialogMessage() {
					if (presenter.isSubTaskPresentAndHaveCertainStatus(recordDisplay.getRecordVO())) {
						return $("DisplayTaskView.subTaskPresentWarning");
					} else {
						return super.getConfirmDialogMessage();
					}
				}

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

			ReportTabButton reportGeneratorButton = new ReportTabButton($("SearchView.metadataReportTitle"), $("SearchView.metadataReportTitle"), presenter.getApplayerFactory(),
					getCollection(), false, false, presenter.buildReportPresenter(), getSessionContext()) {
				@Override
				public void buttonClick(ClickEvent event) {
					setRecordVoList(getCurrentTask());
					super.buttonClick(event);
				}
			};
			actionMenuButtons.add(reportGeneratorButton);

			WindowButton shareButton = new WindowButton($("DisplayTaskView.share"), $("DisplayTaskView.shareWindowCaption")) {
				@Override
				protected Component buildWindowContent() {
					VerticalLayout mainLayout = new VerticalLayout();
					RecordVO recordVO = presenter.getTaskVO();
					ListAddRemoveCollaboratorsField collaboratorsField = new ListAddRemoveCollaboratorsField(recordVO, presenter.currentUserIsCollaborator(recordVO));
					ListAddRemoveCollaboratorsGroupsField collaboratorGroupsField = new ListAddRemoveCollaboratorsGroupsField(recordVO, presenter.currentUserIsCollaborator(recordVO));
					BaseButton saveButton = new BaseButton($("save")) {
						@Override
						protected void buttonClick(ClickEvent event) {
							presenter.addCollaborators(collaboratorsField.getValue(), collaboratorGroupsField.getValue(), (TaskVO) presenter.getTaskVO());
							getWindow().close();
						}
					};
					saveButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
					getWindow().setHeight(collaboratorsField.getHeight() * 80 + "px");
					mainLayout.addComponents(collaboratorsField, collaboratorGroupsField, saveButton);
					return mainLayout;
				}
			};
			actionMenuButtons.add(shareButton);
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
		tabSheet.setSelectedTab(recordDisplayLayout);
	}

	public void addFieldToRecordDisplay(Component newComponent, boolean readOnly) {
		if (newComponent != null) {
			recordDisplayLayout.addComponent(newComponent);
			newComponent.setReadOnly(readOnly);
		}
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

	public RecordVO getCurrentTask() {
		return presenter.getTaskVO();
	}
}
