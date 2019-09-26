package com.constellio.app.modules.tasks.services.menu.behaviors;

import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.extensions.api.RMModuleExtensions;
import com.constellio.app.modules.rm.services.menu.behaviors.util.RMUrlUtil;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.tasks.extensions.TaskManagementPresenterExtension;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.navigation.TaskViews;
import com.constellio.app.modules.tasks.services.TaskPresenterServices;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.app.modules.tasks.services.TasksSearchServices;
import com.constellio.app.modules.tasks.ui.pages.tasks.TaskCompleteWindowButton;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.menu.behavior.MenuItemActionBehaviorParams;
import com.constellio.app.ui.framework.buttons.ConfirmDialogButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.components.RMSelectionPanelReportPresenter;
import com.constellio.app.ui.framework.components.ReportTabButton;
import com.constellio.app.ui.framework.window.ConsultLinkWindow;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.util.MessageUtils;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.extensions.ModelLayerCollectionExtensions;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.logging.LoggingServices;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesRuntimeException;
import com.vaadin.ui.UI;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.app.ui.util.UrlUtil.getConstellioUrl;
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
		ConsultLinkWindow consultLinkWindow = new ConsultLinkWindow(asList(constellioURL + RMUrlUtil.getPathToConsultLinkForFolder(task.getId())));
		UI.getCurrent().addWindow(consultLinkWindow);
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
