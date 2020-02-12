package com.constellio.app.modules.tasks.services.menu.behaviors;

import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.extensions.api.RMModuleExtensions;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.tasks.extensions.TaskManagementPresenterExtension;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.navigation.TaskViews;
import com.constellio.app.modules.tasks.services.TaskPresenterServices;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.app.modules.tasks.services.TasksSearchServices;
import com.constellio.app.modules.tasks.services.menu.behaviors.util.TaskUrlUtil;
import com.constellio.app.modules.tasks.ui.components.fields.list.ListAddRemoveCollaboratorsField;
import com.constellio.app.modules.tasks.ui.components.fields.list.ListAddRemoveCollaboratorsGroupsField;
import com.constellio.app.modules.tasks.ui.pages.tasks.TaskCompleteWindowButton;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.menu.behavior.MenuItemActionBehaviorParams;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.ConfirmDialogButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.clipboard.CopyToClipBoard;
import com.constellio.app.ui.framework.components.RMSelectionPanelReportPresenter;
import com.constellio.app.ui.framework.components.ReportTabButton;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.base.SchemaPresenterUtils;
import com.constellio.app.ui.util.MessageUtils;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.extensions.ModelLayerCollectionExtensions;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.logging.LoggingServices;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesRuntimeException;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.app.ui.util.UrlUtil.getConstellioUrl;
import static com.vaadin.ui.themes.ValoTheme.BUTTON_PRIMARY;
import static com.vaadin.ui.themes.ValoTheme.LABEL_BOLD;
import static java.util.Arrays.asList;

public class TaskMenuItemActionBehaviors {

	private ModelLayerCollectionExtensions extensions;
	private String collection;
	private AppLayerFactory appLayerFactory;
	private ModelLayerFactory modelLayerFactory;
	private RecordServices recordServices;
	private LoggingServices loggingServices;
	private TaskPresenterServices taskPresenterServices;
	private TasksSchemasRecordsServices tasksSchemas;
	private TasksSearchServices tasksSearchServices;

	public TaskMenuItemActionBehaviors(String collection, AppLayerFactory appLayerFactory) {
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;
		this.modelLayerFactory = appLayerFactory.getModelLayerFactory();
		this.recordServices = modelLayerFactory.newRecordServices();
		this.loggingServices = modelLayerFactory.newLoggingServices();
		this.extensions = modelLayerFactory.getExtensions().forCollection(collection);
		this.tasksSchemas = new TasksSchemasRecordsServices(collection, appLayerFactory);
		this.tasksSearchServices = new TasksSearchServices(tasksSchemas);
		this.taskPresenterServices = new TaskPresenterServices(tasksSchemas, recordServices, tasksSearchServices, loggingServices);

	}

	public void getConsultationLink(Task task, MenuItemActionBehaviorParams params) {
		String constellioURL = getConstellioUrl(modelLayerFactory);
		CopyToClipBoard.copyToClipBoard(constellioURL + TaskUrlUtil.getPathToConsultLinkForTask(task.getId()));
	}

	public void display(Task task, MenuItemActionBehaviorParams params) {
		params.getView().navigate().to(TaskViews.class).displayTask(task.getId());
	}


	public void edit(Task task, MenuItemActionBehaviorParams params) {
		params.getView().navigate().to().editTask(task.getId());
	}

	public void autoAssign(Task task, MenuItemActionBehaviorParams params) {
		taskPresenterServices.autoAssignTask(task.getWrappedRecord(), params.getUser());
	}

	public void complete(Task task, MenuItemActionBehaviorParams params) {
		new TaskCompleteWindowButton(task,
				$("DisplayTaskView.completeTask"), appLayerFactory, new TaskMenuItemPresenterImpl(appLayerFactory, params.getUser(), params.getView())) {
			@Override
			protected String getConfirmDialogMessage() {
				if (taskPresenterServices.isSubTaskPresentAndHaveCertainStatus(task.getId())) {
					return $("DisplayTaskView.subTaskPresentComplete");
				}

				return $("DisplayTaskView.completeTaskDialogMessage");
			}
		}.click();
	}

	public void close(Task task, MenuItemActionBehaviorParams params) {
		ConfirmDialogButton closeTask = new ConfirmDialogButton($("DisplayTaskView.closeTask")) {
			@Override
			protected String getConfirmDialogMessage() {
				return $("DisplayTaskView.closeTaskDialogMessage");
			}

			@Override
			protected void confirmButtonClick(ConfirmDialog dialog) {
				taskPresenterServices.closeTask(task.getWrappedRecord(), params.getUser());
				params.getView().navigate().to(TaskViews.class).displayTask(task.getId());
			}
		};

		closeTask.click();
	}


	public void createSubTask(Task task, MenuItemActionBehaviorParams params) {
		params.getView().navigate().to(TaskViews.class).addTask(task.getId());
	}

	public void delete(Task task, MenuItemActionBehaviorParams params) {
		DeleteButton deleteTask = new DeleteButton($("DisplayTaskView.deleteTask")) {
			@Override
			protected String getConfirmDialogMessage() {
				if (taskPresenterServices.isSubTaskPresentAndHaveCertainStatus(task.getId())) {
					return $("DisplayTaskView.subTaskPresentWarning");
				} else {
					return super.getConfirmDialogMessage();
				}
			}

			@Override
			protected void confirmButtonClick(ConfirmDialog dialog) {
				try {
					taskPresenterServices.deleteTask(task.getWrappedRecord(), params.getUser());
				} catch (RecordServicesRuntimeException.RecordServicesRuntimeException_CannotLogicallyDeleteRecord e) {
					params.getView().showErrorMessage(MessageUtils.toMessage(e));
				}
				// TODO: Properly redirect
				params.getView().navigate().to(TaskViews.class).taskManagement();
			}

		};

		deleteTask.click();
	}

	public void generateReport(Task task, MenuItemActionBehaviorParams params) {
		RMSelectionPanelReportPresenter reportPresenter =
				new RMSelectionPanelReportPresenter(appLayerFactory, collection, params.getUser()) {
					@Override
					public String getSelectedSchemaType() {
						return Folder.SCHEMA_TYPE;
					}

					@Override
					public List<String> getSelectedRecordIds() {
						return asList(task.getId());
					}
				};

		ReportTabButton reportGeneratorButton = new ReportTabButton($("SearchView.metadataReportTitle"), $("SearchView.metadataReportTitle"), appLayerFactory,
				params.getView().getCollection(), false, false, reportPresenter, params.getView().getSessionContext()) {
			@Override
			public void buttonClick(ClickEvent event) {
				setRecordVoList(params.getRecordVO());
				super.buttonClick(event);
			}
		};

		reportGeneratorButton.click();
	}

	public void shareTask(Task task, MenuItemActionBehaviorParams params) {
		WindowButton shareButton = new WindowButton($("DisplayTaskView.share"), $("DisplayTaskView.shareWindowCaption")) {
			@Override
			protected Component buildWindowContent() {
				VerticalLayout mainLayout = new VerticalLayout();
				mainLayout.setSpacing(true);

				ListAddRemoveCollaboratorsField collaboratorsField = buildCollaboratorField(params, task);
				ListAddRemoveCollaboratorsGroupsField collaboratorGroupsField = buildCollaboratorGroupsField(params, task);

				BaseButton saveButton = new BaseButton($("save")) {
					@Override
					protected void buttonClick(ClickEvent event) {
						taskPresenterServices.modifyCollaborators(collaboratorsField.getValue(), collaboratorGroupsField.getValue(), params.getRecordVO(),
								new SchemaPresenterUtils(Task.DEFAULT_SCHEMA, params.getView().getConstellioFactories(), params.getView().getSessionContext()));
						getWindow().close();
					}
				};
				saveButton.addStyleName(BUTTON_PRIMARY);

				HorizontalLayout buttonLayout = new HorizontalLayout();
				buttonLayout.addComponent(saveButton);
				buttonLayout.setSpacing(true);
				buttonLayout.setHeight("40px");

				Label collaboratorsLabel = new Label($("TaskAssignationListCollaboratorsField.taskCollaborators"));
				collaboratorsLabel.setStyleName(LABEL_BOLD);
				Label collaboratorsGroupsLabel = new Label($("TaskAssignationListCollaboratorsField.taskCollaboratorsGroups"));
				collaboratorsGroupsLabel.setStyleName(LABEL_BOLD);

				mainLayout.addComponents(collaboratorsLabel, collaboratorsField, collaboratorsGroupsLabel, collaboratorGroupsField, buttonLayout);
				mainLayout.setComponentAlignment(buttonLayout, Alignment.MIDDLE_CENTER);
				getWindow().setHeight(collaboratorsField.getHeight() * 80 + "px");
				return mainLayout;
			}
		};
		shareButton.click();
	}

	private ListAddRemoveCollaboratorsGroupsField buildCollaboratorGroupsField(MenuItemActionBehaviorParams params,
																			   Task task) {
		boolean userHasWriteAuthorization = params.getUser().hasWriteAccess().on(task);
		ListAddRemoveCollaboratorsGroupsField collaboratorsGroupField = new ListAddRemoveCollaboratorsGroupsField(params.getRecordVO());
		collaboratorsGroupField.writeButtonIsVisible(userHasWriteAuthorization);
		collaboratorsGroupField.setCurrentUserCanModifyDelete(taskPresenterServices.currentUserHasWriteAuthorisationWithoutBeingCollaborator(params.getRecordVO(), params.getUser().getId()));
		return collaboratorsGroupField;
	}

	private ListAddRemoveCollaboratorsField buildCollaboratorField(MenuItemActionBehaviorParams params, Task task) {
		boolean userHasWriteAuthorization = params.getUser().hasWriteAccess().on(task);
		ListAddRemoveCollaboratorsField collaboratorsField = new ListAddRemoveCollaboratorsField(params.getRecordVO());
		collaboratorsField.writeButtonIsVisible(userHasWriteAuthorization);
		collaboratorsField.setCurrentUserCanModifyDelete(taskPresenterServices.currentUserHasWriteAuthorisationWithoutBeingCollaborator(params.getRecordVO(), params.getUser().getId()));
		return collaboratorsField;
	}

	public interface TaskMenuItemPresenter {
		void afterCompletionActions();

		void beforeCompletionActions(Task task);

		void reloadTaskModified(String id);

		BaseView getView();
	}

	public static class TaskMenuItemPresenterImpl implements TaskMenuItemPresenter {

		private RMModuleExtensions rmModuleExtensions;
		private BaseView baseView;
		private User user;

		public TaskMenuItemPresenterImpl(AppLayerFactory appLayerFactory, User user, BaseView baseView) {
			this.rmModuleExtensions = appLayerFactory.getExtensions().forCollection(baseView.getCollection()).forModule(ConstellioRMModule.ID);
			this.baseView = baseView;
			this.user = user;
		}

		@Override
		public void afterCompletionActions() {
			if (rmModuleExtensions != null) {
				for (TaskManagementPresenterExtension extension : rmModuleExtensions.getTaskManagementPresenterExtensions()) {
					extension.afterCompletionActions(user);
				}
			}
		}

		@Override
		public void beforeCompletionActions(Task task) {
			if (rmModuleExtensions != null) {
				for (TaskManagementPresenterExtension extension : rmModuleExtensions.getTaskManagementPresenterExtensions()) {
					extension.beforeCompletionActions(task);
				}
			}
		}

		@Override
		public void reloadTaskModified(String id) {
			getView().navigate().to(TaskViews.class).displayTask(id);
		}

		@Override
		public BaseView getView() {
			return baseView;
		}
	}
}
