package com.constellio.app.modules.tasks.ui.pages.tasks;

import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.extensions.api.RMModuleExtensions;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.events.RMEventsSearchServices;
import com.constellio.app.modules.tasks.extensions.TaskManagementPresenterExtension;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.navigation.TaskViews;
import com.constellio.app.modules.tasks.services.TaskPresenterServices;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.app.modules.tasks.services.TasksSearchServices;
import com.constellio.app.modules.tasks.ui.builders.TaskToVOBuilder;
import com.constellio.app.modules.tasks.ui.entities.TaskVO;
import com.constellio.app.modules.tasks.ui.pages.AbstractTaskPresenter;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.EventToVOBuilder;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.buttons.report.ReportGeneratorButton;
import com.constellio.app.ui.framework.components.RMSelectionPanelReportPresenter;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.management.Report.PrintableReportListPossibleType;
import com.constellio.app.ui.util.MessageUtils;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.structures.MapStringStringStructure;
import com.constellio.model.services.logging.LoggingServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.RecordServicesRuntimeException;
import com.constellio.model.services.records.RecordUtils;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

import java.io.IOException;
import java.util.List;

import static com.constellio.app.modules.tasks.model.wrappers.Task.ASSIGNEE;
import static com.constellio.app.modules.tasks.model.wrappers.Task.DUE_DATE;
import static com.constellio.app.ui.entities.RecordVO.VIEW_MODE.FORM;
import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.entities.records.wrappers.RecordWrapper.TITLE;
import static java.util.Arrays.asList;

public class DisplayTaskPresenter extends AbstractTaskPresenter<DisplayTaskView> {

	private static final String DISPLAY_TASK_PRESENTER_PREVIOUS_TAB = "DisplayTaskPresenterPreviousTab";

	TaskVO taskVO;
	private RecordVODataProvider subTaskDataProvider;
	private RecordVODataProvider eventsDataProvider;
	transient TasksSearchServices tasksSearchServices;
	transient private TasksSchemasRecordsServices tasksSchemas;
	transient private TaskPresenterServices taskPresenterServices;
	transient Record currentRecord;
	transient private LoggingServices loggingServices;

	transient private RMModuleExtensions rmModuleExtensions;

	public DisplayTaskPresenter(DisplayTaskView view) {
		super(view, Task.DEFAULT_SCHEMA);
		initTransientObjects();
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return true;
	}

	@Override
	protected boolean hasRestrictedRecordAccess(String params, User user, Record restrictedRecord) {
		return user.hasReadAccess().on(restrictedRecord);
	}

	@Override
	protected List<String> getRestrictedRecordIds(String taskId) {
		return asList(taskId);
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		initTransientObjects();
	}

	private void initTransientObjects() {
		tasksSchemas = new TasksSchemasRecordsServices(collection, appLayerFactory);
		tasksSearchServices = new TasksSearchServices(tasksSchemas);
		loggingServices = modelLayerFactory.newLoggingServices();
		taskPresenterServices = new TaskPresenterServices(tasksSchemas, recordServices(), tasksSearchServices, loggingServices);
		rmModuleExtensions = appCollectionExtentions.forModule(ConstellioRMModule.ID);
	}

	public String getPreviousSelectedTab() {
		String attribute = ConstellioUI.getCurrentSessionContext().getAttribute(DISPLAY_TASK_PRESENTER_PREVIOUS_TAB);
		ConstellioUI.getCurrentSessionContext().setAttribute(DISPLAY_TASK_PRESENTER_PREVIOUS_TAB, null);

		return attribute;
	}

	public void registerPreviousSelectedTab() {
		ConstellioUI.getCurrentSessionContext().setAttribute(DISPLAY_TASK_PRESENTER_PREVIOUS_TAB, view.getSelectedTab().getId());
	}

	@Override
	public Task getTask(RecordVO recordVO) {
		String originalSchemaCode = schemaPresenterUtils.getSchemaCode();
		schemaPresenterUtils.setSchemaCode(recordVO.getSchemaCode());
		Task task = tasksSchemas.wrapTask(toRecord(recordVO));
		schemaPresenterUtils.setSchemaCode(originalSchemaCode);
		return task;
	}

	public void afterCompletionActions() {
		if (rmModuleExtensions != null) {
			for (TaskManagementPresenterExtension extension : rmModuleExtensions.getTaskManagementPresenterExtensions()) {
				extension.afterCompletionActions(getCurrentUser());
			}
		}
	}

	public void beforeCompletionActions(Task task) {
		if (rmModuleExtensions != null) {
			for (TaskManagementPresenterExtension extension : rmModuleExtensions.getTaskManagementPresenterExtensions()) {
				extension.beforeCompletionActions(task);
			}
		}
	}

	public RecordVO getTaskVO() {
		return taskVO;
	}

	public void initTaskVO(String id) {
		Record task = getRecord(id);
		setSchemaCode(task.getSchemaCode());
		taskVO = new TaskVO(new TaskToVOBuilder().build(task, FORM, view.getSessionContext()));
		initSubTaskDataProvider();
		eventsDataProvider = getEventsDataProvider();
	}

	public boolean isSubTaskPresentAndHaveCertainStatus(RecordVO recordVO) {
		return taskPresenterServices.isSubTaskPresentAndHaveCertainStatus(recordVO.getId());
	}

	@Override
	public void displayButtonClicked(RecordVO entity) {
		view.navigate().to(TaskViews.class).displayTask(entity.getId());
	}

	public void editButtonClicked() {
		view.navigate().to().editTask(taskVO.getId());
	}

	@Override
	public void editButtonClicked(RecordVO entity) {
		view.navigate().to().editTask(entity.getId());
	}


	public void closeButtonClicked() {
		closeButtonClicked(taskVO);
	}

	@Override
	public void closeButtonClicked(RecordVO entity) {
		taskPresenterServices.closeTask(toRecord(entity), getCurrentUser());
		reloadCurrentTask();
	}

	@Override
	public void generateReportButtonClicked(RecordVO recordVO) {
		ReportGeneratorButton button = new ReportGeneratorButton($("ReportGeneratorButton.buttonText"),
				$("Générer un rapport de métadonnées"), view, appLayerFactory, collection, PrintableReportListPossibleType.TASK,
				recordVO);
		button.click();
	}

	@Override
	public boolean isTaskOverdue(TaskVO taskVO) {
		return taskPresenterServices.isTaskOverdue(taskVO);
	}

	@Override
	public boolean isFinished(TaskVO taskVO) {
		return taskPresenterServices.isFinished(taskVO);
	}

	@Override
	public void autoAssignButtonClicked(RecordVO recordVO) {
		taskPresenterServices.autoAssignTask(toRecord(recordVO), getCurrentUser());
		reloadCurrentTask();
	}

	@Override
	public boolean isAutoAssignButtonEnabled(RecordVO recordVO) {
		return taskPresenterServices.isAutoAssignButtonEnabled(toRecord(recordVO), getCurrentUser());
	}

	@Override
	public boolean isEditButtonEnabled(RecordVO recordVO) {
		return taskPresenterServices.isEditTaskButtonVisible(toRecord(recordVO), getCurrentUser());
	}

	@Override
	public boolean isReadByUser(RecordVO recordVO) {
		return taskPresenterServices.isReadByUser(toRecord(recordVO));
	}

	@Override
	public void setReadByUser(RecordVO recordVO, boolean readByUser) {
		try {
			taskPresenterServices.setReadByUser(getCurrentRecord(), readByUser);
			reloadCurrentTask();
			view.getMainLayout().getMenu().refreshBadges();
		} catch (RecordServicesException e) {
			view.showErrorMessage(e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	public boolean isCompleteButtonEnabled(RecordVO recordVO) {
		return taskPresenterServices.isCompleteTaskButtonVisible(toRecord(recordVO), getCurrentUser());
	}

	@Override
	public boolean isCloseButtonEnabled(RecordVO recordVO) {
		return taskPresenterServices.isCloseTaskButtonVisible(toRecord(recordVO), getCurrentUser());
	}

	@Override
	public boolean isDeleteButtonEnabled(RecordVO recordVO) {
		return taskPresenterServices.isDeleteTaskButtonVisible(toRecord(recordVO), getCurrentUser());
	}

	public void deleteButtonClicked() {
		try {
			taskPresenterServices.deleteTask(toRecord(taskVO), getCurrentUser());
		} catch (RecordServicesRuntimeException.RecordServicesRuntimeException_CannotLogicallyDeleteRecord e) {
			view.showErrorMessage(MessageUtils.toMessage(e));
		}
		// TODO: Properly redirect
		view.navigate().to(TaskViews.class).taskManagement();
	}

	@Override
	public void deleteButtonClicked(RecordVO entity) {
		try {
			taskPresenterServices.deleteTask(toRecord(entity), getCurrentUser());
		} catch (RecordServicesRuntimeException.RecordServicesRuntimeException_CannotLogicallyDeleteRecord e) {
			view.showErrorMessage(MessageUtils.toMessage(e));
		}
		reloadCurrentTask();
	}

	void initSubTaskDataProvider() {
		MetadataSchemaVO schemaVO = new MetadataSchemaToVOBuilder()
				.build(defaultSchema(), VIEW_MODE.TABLE, asList(TITLE, ASSIGNEE, DUE_DATE), view.getSessionContext());
		final String taskId = taskVO.getId();
		subTaskDataProvider = new RecordVODataProvider(schemaVO, new TaskToVOBuilder(), modelLayerFactory,
				view.getSessionContext()) {
			@Override
			protected LogicalSearchQuery getQuery() {
				return tasksSearchServices.getDirectSubTasks(taskId, getCurrentUser());
			}
		};
	}

	private void reloadCurrentTask() {
		//TODO proper refresh
		view.navigate().to(TaskViews.class).displayTask(taskVO.getId());
	}

	private boolean hasCurrentUserWriteAccessOnCurrentTask() {
		if (taskVO == null) {
			return false;
		} else {
			return getCurrentUser().hasWriteAccess().on(getCurrentRecord());
		}
	}

	public boolean isEditCurrentTaskButtonVisible() {
		return hasCurrentUserWriteAccessOnCurrentTask();
	}

	public Record getCurrentRecord() {
		if (currentRecord == null) {
			currentRecord = recordServices().getDocumentById(taskVO.getId());
		}
		return currentRecord;
	}

	public Task getTask() {
		return tasksSchemas.wrapTask(getCurrentRecord());
	}

	public boolean isCompleteCurrentTaskButtonVisible() {
		return isCompleteTaskButtonVisible(taskVO);
	}

	public boolean isCloseCurrentTaskButtonVisible() {
		return isCloseTaskButtonVisible(taskVO);
	}

	public void createSubTaskButtonClicked() {
		view.navigate().to(TaskViews.class).addTask(taskVO.getId());
	}

	public boolean isCreateCurrentTaskSubTaskButtonVisible() {
		return hasCurrentUserWriteAccessOnCurrentTask();
	}

	public boolean isDeleteCurrentTaskButtonVisible() {
		return isDeleteButtonVisible(taskVO);
	}

	public boolean isCompleteTaskButtonVisible(RecordVO entity) {
		return taskPresenterServices.isCompleteTaskButtonVisible(toRecord(entity), getCurrentUser());
	}

	public boolean isCloseTaskButtonVisible(RecordVO entity) {
		return taskPresenterServices.isCloseTaskButtonVisible(toRecord(entity), getCurrentUser());
	}

	@Override
	public boolean isDeleteButtonVisible(RecordVO entity) {
		return taskPresenterServices.isDeleteTaskButtonVisible(toRecord(entity), getCurrentUser());
	}

	@Override
	public boolean isMetadataReportAllowed(RecordVO recordVO) {
		return true;
	}


	public static boolean containsExpressionLanguage(Object decisions) {
		if (decisions != null && decisions instanceof MapStringStringStructure) {
			MapStringStringStructure decisionsStruct = (MapStringStringStructure) decisions;
			for (String key : decisionsStruct.keySet()) {
				if (Task.isExpressionLanguage(key)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public BaseView getView() {
		return view;
	}

	@Override
	public void reloadTaskModified(Task task) {
		reloadCurrentTask();
	}

	@Override
	public String getCurrentUserId() {
		return getCurrentUser().getId();
	}

	@Override
	public void updateTaskStarred(boolean isStarred, String taskId) {
		TasksSchemasRecordsServices taskSchemas = new TasksSchemasRecordsServices(collection, appLayerFactory);
		Task task = taskSchemas.getTask(taskId);
		if (isStarred) {
			task.addStarredBy(getCurrentUser().getId());
		} else {
			task.removeStarredBy(getCurrentUser().getId());
		}
		try {
			recordServices().update(task);
		} catch (RecordServicesException e) {
			e.printStackTrace();
		}
	}

	public String getTaskTitle() {
		return taskVO.getTitle();
	}

	public void selectInitialTabForUser() {
		String previousSelectedTab = getPreviousSelectedTab();
		if (previousSelectedTab != null && DisplayTaskView.SUB_TASKS_ID.equals(previousSelectedTab)) {
			view.selectTasksTab();
		} else {
			view.selectMetadataTab();
		}
	}

	public void viewAssembled() {
		view.setSubTasks(subTaskDataProvider);
		view.setEvents(eventsDataProvider);
	}

	public void backButtonClicked() {
		view.navigate().to().previousView();
	}

	public void autoAssignButtonClicked() {
		autoAssignButtonClicked(taskVO);
	}

	public boolean isAutoAssignButtonEnabled() {
		return isAutoAssignButtonEnabled(taskVO);
	}

	public Object getSubTaskCount() {
		return subTaskDataProvider.size();
	}

	public RecordVODataProvider getEventsDataProvider() {
		final RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		final MetadataSchemaVO eventSchemaVO = new MetadataSchemaToVOBuilder()
				.build(rm.eventSchema(), VIEW_MODE.TABLE, view.getSessionContext());
		return new RecordVODataProvider(eventSchemaVO, new EventToVOBuilder(), modelLayerFactory, view.getSessionContext()) {
			@Override
			protected LogicalSearchQuery getQuery() {
				RMEventsSearchServices rmEventsSearchServices = new RMEventsSearchServices(modelLayerFactory, collection);
				return rmEventsSearchServices.newFindEventByRecordIDQuery(getCurrentUser(), taskVO.getId());
			}
		};
	}

	protected boolean hasCurrentUserPermissionToViewEvents() {
		return getCurrentUser().has(CorePermissions.VIEW_EVENTS).on(tasksSchemas.getTask(taskVO.getId()));
	}

	public void refreshEvents() {
		//modelLayerFactory.getDataLayerFactory().newEventsDao().flush();
		view.setEvents(getEventsDataProvider());
	}

	public boolean isLogicallyDeleted() {
		RecordVO task = getTaskVO();
		return Boolean.TRUE
				.equals(task.getMetadataValue(task.getMetadata(Schemas.LOGICALLY_DELETED_STATUS.getLocalCode())).getValue());
	}

	private List<String> getFinishedOrClosedStatuses() {
		return new RecordUtils().toWrappedRecordIdsList(tasksSchemas.getFinishedOrClosedStatuses());
	}

	public boolean isClosedOrTerminated() {
		Record record = toRecord(taskVO);
		Task task = tasksSchemas.wrapTask(record);
		String closed = task.getStatus();
		boolean isClosedOrTerminated = getFinishedOrClosedStatuses().contains(closed);

		return isClosedOrTerminated;
	}

	public RMSelectionPanelReportPresenter buildReportPresenter() {
		return new RMSelectionPanelReportPresenter(appLayerFactory, collection, getCurrentUser()) {
			@Override
			public String getSelectedSchemaType() {
				return Task.SCHEMA_TYPE;
			}

			@Override
			public List<String> getSelectedRecordIds() {
				return asList(taskVO.getId());
			}
		};
	}

	public AppLayerFactory getApplayerFactory() {
		return appLayerFactory;
	}

}
