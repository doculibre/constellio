package com.constellio.app.modules.tasks.ui.pages.tasks;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.events.RMEventsSearchServices;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.navigation.TaskViews;
import com.constellio.app.modules.tasks.services.TaskPresenterServices;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.app.modules.tasks.services.TasksSearchServices;
import com.constellio.app.modules.tasks.ui.builders.TaskToVOBuilder;
import com.constellio.app.modules.tasks.ui.components.TaskTable.TaskPresenter;
import com.constellio.app.modules.tasks.ui.entities.TaskVO;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.EventToVOBuilder;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.logging.LoggingServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

import java.io.IOException;
import java.util.List;

import static com.constellio.app.modules.tasks.model.wrappers.Task.ASSIGNEE;
import static com.constellio.app.modules.tasks.model.wrappers.Task.DUE_DATE;
import static com.constellio.app.ui.entities.RecordVO.VIEW_MODE.FORM;
import static com.constellio.model.entities.records.wrappers.RecordWrapper.TITLE;
import static java.util.Arrays.asList;

public class DisplayTaskPresenter extends SingleSchemaBasePresenter<DisplayTaskView> implements TaskPresenter {
	TaskVO taskVO;
	private RecordVODataProvider subTaskDataProvider;
	private RecordVODataProvider eventsDataProvider;
	transient TasksSearchServices tasksSearchServices;
	transient private TasksSchemasRecordsServices tasksSchemas;
	transient private TaskPresenterServices taskPresenterServices;
	transient Record currentRecord;
	transient private LoggingServices loggingServices;

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
	}

	public RecordVO getTask() {
		return taskVO;
	}

	public void initTaskVO(String id) {
		Task task = tasksSchemas.getTask(id);
		taskVO = new TaskVO(new TaskToVOBuilder().build(task.getWrappedRecord(), FORM, view.getSessionContext()));
		initSubTaskDataProvider();
		eventsDataProvider = getEventsDataProvider();
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

	public void completeButtonClicked() {
		view.navigate().to().editTask(taskVO.getId(), true);
	}

	@Override
	public void completeButtonClicked(RecordVO entity) {
		view.navigate().to().editTask(entity.getId(), true);
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
		taskPresenterServices.deleteTask(toRecord(taskVO), getCurrentUser());
		// TODO: Properly redirect
		view.navigate().to(TaskViews.class).taskManagement();
	}

	@Override
	public void deleteButtonClicked(RecordVO entity) {
		taskPresenterServices.deleteTask(toRecord(entity), getCurrentUser());
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

	public String getTaskTitle() {
		return taskVO.getTitle();
	}

	public void selectInitialTabForUser() {
		view.selectMetadataTab();
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
		modelLayerFactory.getDataLayerFactory().newEventsDao().flush();
		view.setEvents(getEventsDataProvider());
	}
}
