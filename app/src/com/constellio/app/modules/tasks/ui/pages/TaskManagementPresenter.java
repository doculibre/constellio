package com.constellio.app.modules.tasks.ui.pages;

import com.constellio.app.api.extensions.params.UpdateComponentExtensionParams;
import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.extensions.api.RMModuleExtensions;
import com.constellio.app.modules.tasks.TasksPermissionsTo;
import com.constellio.app.modules.tasks.extensions.TaskManagementPresenterExtension;
import com.constellio.app.modules.tasks.model.wrappers.BetaWorkflow;
import com.constellio.app.modules.tasks.model.wrappers.BetaWorkflowInstance;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.navigation.TaskViews;
import com.constellio.app.modules.tasks.services.BetaWorkflowServices;
import com.constellio.app.modules.tasks.services.TaskPresenterServices;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.app.modules.tasks.services.TasksSearchServices;
import com.constellio.app.modules.tasks.ui.builders.TaskToVOBuilder;
import com.constellio.app.modules.tasks.ui.components.WorkflowTable.WorkflowPresenter;
import com.constellio.app.modules.tasks.ui.entities.TaskVO;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.buttons.report.ReportGeneratorButton;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.management.Report.PrintableReportListPossibleType;
import com.constellio.app.ui.util.MessageUtils;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.RecordServicesRuntimeException;
import com.constellio.model.services.records.RecordUtils;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.FunctionLogicalSearchQuerySort;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.search.query.logical.LogicalSearchQuerySort;
import com.vaadin.ui.Component;
import org.joda.time.LocalDate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.constellio.app.modules.tasks.model.wrappers.Task.ASSIGNEE;
import static com.constellio.app.modules.tasks.model.wrappers.Task.ASSIGNER;
import static com.constellio.app.modules.tasks.model.wrappers.Task.DEFAULT_SCHEMA;
import static com.constellio.app.modules.tasks.model.wrappers.Task.DUE_DATE;
import static com.constellio.app.modules.tasks.model.wrappers.Task.END_DATE;
import static com.constellio.app.modules.tasks.model.wrappers.Task.SCHEMA_TYPE;
import static com.constellio.app.modules.tasks.model.wrappers.Task.STARRED_BY_USERS;
import static com.constellio.app.modules.tasks.model.wrappers.Task.STATUS;
import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.entities.records.wrappers.RecordWrapper.TITLE;

public class TaskManagementPresenter extends AbstractTaskPresenter<TaskManagementView> implements WorkflowPresenter {

	private TasksSchemasRecordsServices tasksSchemasRecordsServices;
	private transient TasksSearchServices tasksSearchServices;
	private transient TaskPresenterServices taskPresenterServices;
	private transient BetaWorkflowServices workflowServices;
	private RecordVODataProvider provider;
	private transient SearchServices searchServices;

	private String selectedTab;

	private List<String> tasksTabs;

	private RMModuleExtensions rmModuleExtensions = appCollectionExtentions.forModule(ConstellioRMModule.ID);

	public TaskManagementPresenter(TaskManagementView view) {
		super(view, DEFAULT_SCHEMA);
		initTransientObjects();
		tasksSchemasRecordsServices = new TasksSchemasRecordsServices(collection, appLayerFactory);

		//		workflowsTabsVisible = /*areWorkflowsEnabled() &&*/ getCurrentUser().has(TasksPermissionsTo.MANAGE_WORKFLOWS).globally();
		//		startWorkflowButtonVisible = workflowsTabsVisible && hasPermissionToStartWorkflow();

		tasksTabs = new ArrayList<>();
		tasksTabs.add(TaskManagementView.TASKS_ASSIGNED_TO_CURRENT_USER);
		tasksTabs.add(TaskManagementView.TASKS_ASSIGNED_BY_CURRENT_USER);
		tasksTabs.add(TaskManagementView.TASKS_NOT_ASSIGNED);
		tasksTabs.add(TaskManagementView.TASKS_RECENTLY_COMPLETED);

		view.setTasksTabs(tasksTabs);
	}

	List<String> getTasksTabs() {
		return tasksTabs;
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return true;
	}

	public void tabSelected(String tabId) {
		loadTab(tabId);
	}

	private void loadTab(String tabId) {
		if (selectedTab != null) {
			view.setTabBadge(selectedTab, "");
		}

		if (TaskManagementView.TASKS_TAB.equals(tabId)) {
			tabId = TaskManagementView.TASKS_ASSIGNED_TO_CURRENT_USER;
		}
		selectedTab = tabId;
		if (isTaskTab(tabId)) {
			provider = getTasks(tabId);
			view.setTabBadge(tabId, "" + provider.size());
			view.displayTasks(provider);
		} else {
			UpdateComponentExtensionParams params = new UpdateComponentExtensionParams((Component) view, view.getTabComponent(selectedTab));
			appCollectionExtentions.updateComponent(params);
		}
	}

	private List<String> getFinishedOrClosedStatuses() {
		return new RecordUtils().toWrappedRecordIdsList(tasksSchemasRecordsServices.getFinishedOrClosedStatuses());
	}

	public void addTaskButtonClicked() {
		view.navigate().to(TaskViews.class).addTask();
	}

	public String getTabCaption(String tabId) {
		return $("TasksManagementView.tab." + tabId);
	}

	private void refreshCurrentTab() {
		loadTab(selectedTab);
	}

	@Override
	public boolean isSubTaskPresentAndHaveCertainStatus(RecordVO recordVO) {

		Record record = recordVO.getRecord();

		List<Record> tasksSearchServices = searchServices.search(new LogicalSearchQuery(
				LogicalSearchQueryOperators.from(tasksSchemasRecordsServices.taskSchemaType())
						.where(tasksSchemasRecordsServices.userTask.parentTask()).isEqualTo(record.getId())));

		final String STAND_BY = "S";
		final String IN_PROGRESS = "I";

		boolean isSubTaskWithRequiredStatusFound = false;

		for (Record taskAsRecord : tasksSearchServices) {
			Task currentTask = tasksSchemasRecordsServices.wrapTask(taskAsRecord);
			if (!currentTask.isLogicallyDeletedStatus() && currentTask.getStatusType() != null
				&& (currentTask.getStatusType().getCode().equalsIgnoreCase(STAND_BY)
					|| currentTask.getStatusType().getCode().equalsIgnoreCase(IN_PROGRESS))) {
				isSubTaskWithRequiredStatusFound = true;
				break;
			}
		}
		return isSubTaskWithRequiredStatusFound;
	}

	@Override
	public void displayButtonClicked(RecordVO record) {
		view.navigate().to(TaskViews.class).displayTask(record.getId());
	}

	@Override
	public void editButtonClicked(RecordVO record) {
		view.navigate().to().editTask(record.getId());
	}

	@Override
	public void deleteButtonClicked(RecordVO record) {
		try {
			taskPresenterServices.deleteTask(record.getRecord(), getCurrentUser());
		} catch (RecordServicesRuntimeException.RecordServicesRuntimeException_CannotLogicallyDeleteRecord e) {
			view.showErrorMessage(MessageUtils.toMessage(e));
		}
		refreshCurrentTab();
	}

	@Override
	public void closeButtonClicked(RecordVO record) {
		taskPresenterServices.closeTask(record.getRecord(), getCurrentUser());
		refreshCurrentTab();
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
		taskPresenterServices.autoAssignTask(recordVO.getRecord(), getCurrentUser());
		refreshCurrentTab();
	}

	@Override
	public boolean isAutoAssignButtonEnabled(RecordVO recordVO) {
		return taskPresenterServices.isAutoAssignButtonEnabled(recordVO.getRecord(), getCurrentUser());
	}

	@Override
	public boolean isEditButtonEnabled(RecordVO recordVO) {
		Record record = recordVO.getRecord();
		Task task = tasksSchemasRecordsServices.wrapTask(record);
		String closed = task.getStatus();
		boolean isNotEditable = !getFinishedOrClosedStatuses().contains(closed);
		return isNotEditable && taskPresenterServices.isEditTaskButtonVisible(record, getCurrentUser());
	}

	@Override
	public boolean isReadByUser(RecordVO recordVO) {
		return taskPresenterServices.isReadByUser(recordVO.getRecord());
	}

	@Override
	public void setReadByUser(RecordVO recordVO, boolean readByUser) {
		try {
			Record record = getRecord(recordVO.getId());
			taskPresenterServices.setReadByUser(record, readByUser);
			recordVO.set(Task.READ_BY_USER, readByUser);
			refreshCurrentTab();
			view.getMainLayout().getMenu().refreshBadges();
		} catch (RecordServicesException e) {
			view.showErrorMessage(e.getMessage());
			e.printStackTrace();
		}
	}

	public String getPreviousSelectedTab() {
		String attribute = ConstellioUI.getCurrentSessionContext().getAttribute(TaskManagementView.TASK_MANAGEMENT_PRESENTER_PREVIOUS_TAB);
		ConstellioUI.getCurrentSessionContext().setAttribute(TaskManagementView.TASK_MANAGEMENT_PRESENTER_PREVIOUS_TAB, null);
		return attribute;
	}

	public void registerPreviousSelectedTab() {
		ConstellioUI.getCurrentSessionContext().setAttribute(TaskManagementView.TASK_MANAGEMENT_PRESENTER_PREVIOUS_TAB, selectedTab);
	}

	@Override
	public Task getTask(RecordVO recordVO) {
		String originalSchemaCode = schemaPresenterUtils.getSchemaCode();
		schemaPresenterUtils.setSchemaCode(recordVO.getSchemaCode());
		Task task = tasksSchemasRecordsServices.wrapTask(recordVO.getRecord());
		schemaPresenterUtils.setSchemaCode(originalSchemaCode);
		return task;

	}

	@Override
	public boolean isCompleteButtonEnabled(RecordVO recordVO) {
		return taskPresenterServices.isCompleteTaskButtonVisible(recordVO.getRecord(), getCurrentUser());
	}

	@Override
	public boolean isCloseButtonEnabled(RecordVO recordVO) {
		return taskPresenterServices.isCloseTaskButtonVisible(recordVO.getRecord(), getCurrentUser());
	}

	@Override
	public boolean isDeleteButtonEnabled(RecordVO recordVO) {
		return taskPresenterServices.isDeleteTaskButtonVisible(recordVO.getRecord(), getCurrentUser());
	}

	@Override
	public boolean isDeleteButtonVisible(RecordVO entity) {
		return taskPresenterServices.isDeleteTaskButtonVisible(entity.getRecord(), getCurrentUser());
	}

	@Override
	public void displayWorkflowInstanceRequested(RecordVO recordVO) {
		view.navigate().to(TaskViews.class).displayWorkflowInstance(recordVO.getId());
	}

	@Override
	public void cancelWorkflowInstanceRequested(RecordVO record) {
		BetaWorkflowInstance instance = new TasksSchemasRecordsServices(view.getCollection(), appLayerFactory)
				.getBetaWorkflowInstance(record.getId());
		workflowServices.cancel(instance);
		refreshCurrentTab();
	}

	@Override
	public void generateReportButtonClicked(RecordVO recordVO) {
		ReportGeneratorButton button = new ReportGeneratorButton($("ReportGeneratorButton.buttonText"),
				$("Générer un rapport de métadonnées"), view, appLayerFactory, collection, PrintableReportListPossibleType.TASK, recordVO);
		button.click();
	}

	public RecordVODataProvider getWorkflows() {
		MetadataSchemaVO schemaVO = new MetadataSchemaToVOBuilder().build(
				schema(BetaWorkflow.DEFAULT_SCHEMA), VIEW_MODE.TABLE, view.getSessionContext());

		return new RecordVODataProvider(schemaVO, new RecordToVOBuilder(), modelLayerFactory, view.getSessionContext()) {
			@Override
			public LogicalSearchQuery getQuery() {
				return workflowServices.getWorkflowsQuery();
			}
		};
	}

	//	public void workflowStartRequested(RecordVO record) {
	//		BetaWorkflow workflow = new TasksSchemasRecordsServices(view.getCollection(), appLayerFactory)
	//				.getBetaWorkflow(record.getId());
	//		Map<String, List<String>> parameters = new HashMap<>();
	//		workflowServices.start(workflow, getCurrentUser(), parameters);
	//		refreshCurrentTab();
	//	}

	private RecordVODataProvider getTasks(String tabId) {
		MetadataSchemaVO schemaVO = new MetadataSchemaToVOBuilder()
				.build(defaultSchema(), VIEW_MODE.TABLE, getMetadataForTab(tabId), view.getSessionContext(), true);

		switch (tabId) {
			case TaskManagementView.TASKS_TAB:
			case TaskManagementView.TASKS_ASSIGNED_TO_CURRENT_USER:
				return new RecordVODataProvider(schemaVO, new TaskToVOBuilder(), modelLayerFactory, view.getSessionContext()) {
					@Override
					public LogicalSearchQuery getQuery() {
						LogicalSearchQuery query = tasksSearchServices.getTasksAssignedToUserQuery(getCurrentUser());
						addTimeStampToQuery(query);
						addStarredSortToQuery(query);
						return query;
					}

					@Override
					protected void clearSort(LogicalSearchQuery query) {
						super.clearSort(query);
						addStarredSortToQuery(query);
					}
				};
			case TaskManagementView.TASKS_ASSIGNED_BY_CURRENT_USER:
				return new RecordVODataProvider(schemaVO, new TaskToVOBuilder(), modelLayerFactory, view.getSessionContext()) {
					@Override
					public LogicalSearchQuery getQuery() {
						LogicalSearchQuery query = tasksSearchServices.getTasksAssignedByUserQuery(getCurrentUser());
						addTimeStampToQuery(query);
						addStarredSortToQuery(query);
						return query;
					}

					@Override
					protected void clearSort(LogicalSearchQuery query) {
						super.clearSort(query);
						addStarredSortToQuery(query);
					}
				};
			case TaskManagementView.TASKS_NOT_ASSIGNED:
				return new RecordVODataProvider(schemaVO, new TaskToVOBuilder(), modelLayerFactory, view.getSessionContext()) {
					@Override
					public LogicalSearchQuery getQuery() {
						LogicalSearchQuery query = tasksSearchServices.getUnassignedTasksQuery(getCurrentUser());
						addTimeStampToQuery(query);
						addStarredSortToQuery(query);
						return query;
					}

					@Override
					protected void clearSort(LogicalSearchQuery query) {
						super.clearSort(query);
						addStarredSortToQuery(query);
					}
				};
			case TaskManagementView.TASKS_RECENTLY_COMPLETED:
				return new RecordVODataProvider(schemaVO, new TaskToVOBuilder(), modelLayerFactory, view.getSessionContext()) {
					@Override
					public LogicalSearchQuery getQuery() {
						LogicalSearchQuery query = tasksSearchServices.getRecentlyCompletedTasks(getCurrentUser());
						addTimeStampToQuery(query);
						addStarredSortToQuery(query);
						return query;
					}

					@Override
					protected void clearSort(LogicalSearchQuery query) {
						super.clearSort(query);
						addStarredSortToQuery(query);
					}
				};
			default:
				throw new RuntimeException("BUG: Unknown tabId + " + tabId);
		}
	}

	private void addTimeStampToQuery(LogicalSearchQuery query) {
		TaskManagementViewImpl.Timestamp timestamp = view.getTimestamp();
		switch (timestamp) {
			case ALL:
				break;
			case TODAY:
				tasksSearchServices.addDateFilterToQuery(query, LocalDate.now());
				break;
			case WEEK:
				tasksSearchServices.addDateFilterToQuery(query, LocalDate.now().plusWeeks(1));
				break;
			case MONTH:
				tasksSearchServices.addDateFilterToQuery(query, LocalDate.now().plusMonths(1));
				break;
		}
	}

	private List<String> getMetadataForTab(String tabId) {
		switch (tabId) {
			case TaskManagementView.TASKS_ASSIGNED_TO_CURRENT_USER:
				return Arrays.asList(Task.DEFAULT_SCHEMA + "_" + STARRED_BY_USERS, Task.DEFAULT_SCHEMA + "_" + TITLE, Task.DEFAULT_SCHEMA + "_" + ASSIGNER, Task.DEFAULT_SCHEMA + "_" + DUE_DATE, Task.DEFAULT_SCHEMA + "_" + STATUS);
			case TaskManagementView.TASKS_ASSIGNED_BY_CURRENT_USER:
				return Arrays.asList(Task.DEFAULT_SCHEMA + "_" + STARRED_BY_USERS, Task.DEFAULT_SCHEMA + "_" + TITLE, Task.DEFAULT_SCHEMA + "_" + ASSIGNEE, Task.DEFAULT_SCHEMA + "_" + DUE_DATE, Task.DEFAULT_SCHEMA + "_" + STATUS);
			case TaskManagementView.TASKS_NOT_ASSIGNED:
				return Arrays.asList(Task.DEFAULT_SCHEMA + "_" + STARRED_BY_USERS, Task.DEFAULT_SCHEMA + "_" + TITLE, Task.DEFAULT_SCHEMA + "_" + DUE_DATE, Task.DEFAULT_SCHEMA + "_" + STATUS);
			default:
				return Arrays.asList(Task.DEFAULT_SCHEMA + "_" + STARRED_BY_USERS, Task.DEFAULT_SCHEMA + "_" + TITLE, Task.DEFAULT_SCHEMA + "_" + ASSIGNER, Task.DEFAULT_SCHEMA + "_" + END_DATE);
		}
	}

	public boolean isTaskTab(String tabId) {
		switch (tabId) {
			case TaskManagementView.TASKS_TAB:
			case TaskManagementView.TASKS_ASSIGNED_TO_CURRENT_USER:
			case TaskManagementView.TASKS_ASSIGNED_BY_CURRENT_USER:
			case TaskManagementView.TASKS_NOT_ASSIGNED:
			case TaskManagementView.TASKS_RECENTLY_COMPLETED:
				return true;
			default:
				return false;
		}
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		initTransientObjects();
	}

	private void initTransientObjects() {
		TasksSchemasRecordsServices schemas = new TasksSchemasRecordsServices(collection, appLayerFactory);
		workflowServices = new BetaWorkflowServices(collection, appLayerFactory);
		tasksSearchServices = new TasksSearchServices(schemas);
		taskPresenterServices = new TaskPresenterServices(
				schemas, recordServices(), tasksSearchServices, modelLayerFactory.newLoggingServices());
		searchServices = modelLayerFactory.newSearchServices();
	}

	public boolean hasPermissionToStartWorkflow() {
		return getCurrentUser().has(TasksPermissionsTo.START_WORKFLOWS).globally();
	}

	public boolean isMetadataReportAllowed(RecordVO recordVO) {
		return true;
	}


	@Override
	public BaseView getView() {
		return view;
	}

	@Override
	public void reloadTaskModified(String id) {
		loadTab(selectedTab);
	}

	@Override
	public void afterCompletionActions() {
		if (rmModuleExtensions != null) {
			for (TaskManagementPresenterExtension extension : rmModuleExtensions.getTaskManagementPresenterExtensions()) {
				extension.afterCompletionActions(getCurrentUser());
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
		provider.fireDataRefreshEvent();
	}

	public String getDueDateCaption() {
		return modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection)
				.getDefaultSchema(SCHEMA_TYPE).getMetadata(DUE_DATE)
				.getLabel(Language.withLocale(view.getSessionContext().getCurrentLocale()));
	}

	private void addStarredSortToQuery(LogicalSearchQuery query) {
		Metadata metadata = types().getSchema(Task.DEFAULT_SCHEMA).getMetadata(Task.STARRED_BY_USERS);
		LogicalSearchQuerySort sortField = new FunctionLogicalSearchQuerySort(
				"termfreq(" + metadata.getDataStoreCode() + ",\'" + getCurrentUserId() + "\')", false);
		query.sortFirstOn(sortField);
	}

	public void reloadCurrentTabRequested() {
		refreshCurrentTab();
	}

	public User getCurrentUser() {
		return super.getCurrentUser();
	}

}
