package com.constellio.app.modules.tasks.ui.pages.tasks;

import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.extensions.api.RMModuleExtensions;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.events.RMEventsSearchServices;
import com.constellio.app.modules.tasks.TasksPermissionsTo;
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
import com.constellio.app.ui.framework.components.ReportTabButton;
import com.constellio.app.ui.framework.components.SelectionPanelReportPresenter;
import com.constellio.app.ui.framework.components.fields.list.TaskCollaboratorItem;
import com.constellio.app.ui.framework.components.fields.list.TaskCollaboratorsGroupItem;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.util.MessageUtils;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.structures.MapStringStringStructure;
import com.constellio.model.frameworks.validation.OptimisticLockException;
import com.constellio.model.services.logging.LoggingServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.RecordServicesRuntimeException;
import com.constellio.model.services.records.RecordUtils;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static com.constellio.app.modules.tasks.model.wrappers.Task.ASSIGNEE;
import static com.constellio.app.modules.tasks.model.wrappers.Task.DUE_DATE;
import static com.constellio.app.ui.entities.RecordVO.VIEW_MODE.FORM;
import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.entities.records.wrappers.RecordWrapper.TITLE;
import static java.util.Arrays.asList;

public class DisplayTaskPresenter extends AbstractTaskPresenter<DisplayTaskView> {
	private static final Logger LOGGER = LoggerFactory.getLogger(DisplayTaskPresenter.class);
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

	private boolean nestedView = false;
	private boolean inWindow = false;

	public DisplayTaskPresenter(DisplayTaskView view) {
		super(view, Task.DEFAULT_SCHEMA);
		initTransientObjects();
	}

	public DisplayTaskPresenter(DisplayTaskView view, RecordVO recordVO, boolean nestedView, boolean inWindow) {
		super(view, Task.DEFAULT_SCHEMA);
		this.nestedView = nestedView;
		this.inWindow = inWindow;
		initTransientObjects();
		if (recordVO != null) {
			initTaskVO(recordVO.getId());
		}
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return true;
	}

	@Override
	protected boolean hasRestrictedRecordAccess(String params, User user, Record restrictedRecord) {
		boolean isModelTaskAndUserIsWorkflowManager = false;

		if (restrictedRecord.getSchemaCode().startsWith(Task.SCHEMA_TYPE + "_")) {
			Task task = tasksSchemas.wrapTask(restrictedRecord);
			isModelTaskAndUserIsWorkflowManager = getCurrentUser().has(TasksPermissionsTo.MANAGE_WORKFLOWS).globally()
												  && task.isModel();
		}
		return isModelTaskAndUserIsWorkflowManager || user.hasReadAccess().on(restrictedRecord);
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
		Task task = tasksSchemas.wrapTask(fromVOToRecord(recordVO));
		schemaPresenterUtils.setSchemaCode(originalSchemaCode);
		return task;
	}

	@Override
	public void addCollaborators(List<TaskCollaboratorItem> taskCollaboratorItems,
								 List<TaskCollaboratorsGroupItem> taskCollaboratorsGroupItems, RecordVO taskVO) {
		taskPresenterServices.modifyCollaborators(taskCollaboratorItems, taskCollaboratorsGroupItems, taskVO, schemaPresenterUtils);
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

	@Override
	public boolean currentUserHasWriteAuthorisationWithoutBeingCollaborator(RecordVO recordVO) {
		return taskPresenterServices.currentUserHasWriteAuthorisationWithoutBeingCollaborator(recordVO, getCurrentUserId());
	}

	@Override
	public boolean currentUserHasWriteAuthorization(RecordVO taskVO) {
		return getCurrentUser().hasWriteAccess().on(taskVO.getRecord());
	}

	public RecordVO getTaskVO() {
		return taskVO;
	}

	public void initTaskVO(String id) {
		Record task = getRecord(id);
		setSchemaCode(task.getSchemaCode());
		taskVO = new TaskToVOBuilder().build(task, FORM, view.getSessionContext());
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
		taskPresenterServices.closeTask(fromVOToRecord(entity), getCurrentUser());
		reloadCurrentTask();
	}

	@Override
	public void generateReportButtonClicked(RecordVO recordVO) {
		SelectionPanelReportPresenter selectionPanelReportPresenter = new SelectionPanelReportPresenter(appLayerFactory, collection, getCurrentUser()) {
			@Override
			public String getSelectedSchemaType() {
				return Task.SCHEMA_TYPE;
			}

			@Override
			public List<String> getSelectedRecordIds() {
				return Collections.singletonList(recordVO.getId());
			}
		};

		ReportTabButton reportGeneratorButton = new ReportTabButton($("SearchView.metadataReportTitle"),
				$("SearchView.metadataReportTitle"), appLayerFactory, collection,
				selectionPanelReportPresenter, view.getSessionContext()) {

		};
		reportGeneratorButton.setRecordVoList(recordVO);
		reportGeneratorButton.click();
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
		taskPresenterServices.autoAssignTask(fromVOToRecord(recordVO), getCurrentUser());
		reloadCurrentTask();
	}

	@Override
	public boolean isAutoAssignButtonEnabled(RecordVO recordVO) {
		return taskPresenterServices.isAutoAssignButtonEnabled(fromVOToRecord(recordVO), getCurrentUser());
	}

	@Override
	public boolean isEditButtonEnabled(RecordVO recordVO) {
		return taskPresenterServices.isEditTaskButtonVisible(fromVOToRecord(recordVO), getCurrentUser());
	}

	@Override
	public boolean isReadByUser(RecordVO recordVO) {
		return taskPresenterServices.isReadByUser(fromVOToRecord(recordVO));
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
		return taskPresenterServices.isCompleteTaskButtonVisible(fromVOToRecord(recordVO), getCurrentUser());
	}

	@Override
	public boolean isCloseButtonEnabled(RecordVO recordVO) {
		return taskPresenterServices.isCloseTaskButtonVisible(fromVOToRecord(recordVO), getCurrentUser());
	}

	@Override
	public boolean isDeleteButtonEnabled(RecordVO recordVO) {
		return taskPresenterServices.isDeleteTaskButtonVisible(fromVOToRecord(recordVO), getCurrentUser());
	}

	public void deleteButtonClicked() {
		try {
			taskPresenterServices.deleteTask(fromVOToRecord(taskVO), getCurrentUser());
		} catch (RecordServicesRuntimeException.RecordServicesRuntimeException_CannotLogicallyDeleteRecord e) {
			view.showErrorMessage(MessageUtils.toMessage(e));
		}
		// TODO: Properly redirect
		view.navigate().to(TaskViews.class).taskManagement();
	}

	@Override
	public void deleteButtonClicked(RecordVO entity) {
		try {
			taskPresenterServices.deleteTask(fromVOToRecord(entity), getCurrentUser());
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
			public LogicalSearchQuery getQuery() {
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
		return taskPresenterServices.isCompleteTaskButtonVisible(fromVOToRecord(entity), getCurrentUser());
	}

	public boolean isCloseTaskButtonVisible(RecordVO entity) {
		return taskPresenterServices.isCloseTaskButtonVisible(fromVOToRecord(entity), getCurrentUser());
	}

	@Override
	public boolean isDeleteButtonVisible(RecordVO entity) {
		return taskPresenterServices.isDeleteTaskButtonVisible(fromVOToRecord(entity), getCurrentUser());
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
	public void reloadTaskModified(String id) {
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
		if (!isTaskModel() && previousSelectedTab != null && DisplayTaskView.SUB_TASKS_ID.equals(previousSelectedTab)) {
			view.selectTasksTab();
		} else {
			view.selectMetadataTab();
		}
	}

	public boolean isTaskModel() {
		return Boolean.TRUE.equals(this.taskVO.isTaskModel());
	}

	public void viewAssembled() {
		if (!isTaskModel()) {
			view.setSubTasks(subTaskDataProvider);
		}
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
			public LogicalSearchQuery getQuery() {
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
		Record record = fromVOToRecord(taskVO);
		Task task = tasksSchemas.wrapTask(record);
		String closed = task.getStatus();
		boolean isClosedOrTerminated = getFinishedOrClosedStatuses().contains(closed);

		return isClosedOrTerminated;
	}

	public SelectionPanelReportPresenter buildReportPresenter() {
		return new SelectionPanelReportPresenter(appLayerFactory, collection, getCurrentUser()) {
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

	private Record fromVOToRecord(RecordVO recordVO) {
		try {
			return toRecord(recordVO);
		} catch (OptimisticLockException e) {
			LOGGER.error(e.getMessage());
			view.showErrorMessage(e.getMessage());
		}
		return null;
	}
}
