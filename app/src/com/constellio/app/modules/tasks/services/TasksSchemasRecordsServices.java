package com.constellio.app.modules.tasks.services;

import com.constellio.app.modules.rm.wrappers.type.SchemaLinkingType;
import com.constellio.app.modules.tasks.TaskModule;
import com.constellio.app.modules.tasks.model.managers.TaskReminderEmailManager;
import com.constellio.app.modules.tasks.model.wrappers.BetaWorkflow;
import com.constellio.app.modules.tasks.model.wrappers.BetaWorkflowInstance;
import com.constellio.app.modules.tasks.model.wrappers.BetaWorkflowTask;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.model.wrappers.request.BorrowRequest;
import com.constellio.app.modules.tasks.model.wrappers.request.ExtensionRequest;
import com.constellio.app.modules.tasks.model.wrappers.request.ReactivationRequest;
import com.constellio.app.modules.tasks.model.wrappers.request.RequestTask;
import com.constellio.app.modules.tasks.model.wrappers.request.ReturnRequest;
import com.constellio.app.modules.tasks.model.wrappers.types.TaskStatus;
import com.constellio.app.modules.tasks.model.wrappers.types.TaskType;
import com.constellio.app.modules.tasks.ui.entities.TaskVO;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.modules.tasks.model.wrappers.TaskStatusType.FINISHED;
import static com.constellio.app.modules.tasks.model.wrappers.types.TaskStatus.CLOSED_CODE;
import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.where;
import static java.util.Arrays.asList;

public class TasksSchemasRecordsServices extends SchemasRecordsServices {

	protected final AppLayerFactory appLayerFactory;

	public TasksSchemasRecordsServices(String collection, AppLayerFactory appLayerFactory) {
		super(collection, appLayerFactory.getModelLayerFactory());
		this.appLayerFactory = appLayerFactory;
	}

	/** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** **/
	// Auto-generated methods by GenerateHelperClassAcceptTest -- start

	/**
	 * * ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** **
	 **/

	public TaskStatus wrapTaskStatus(Record record) {
		return record == null ? null : new TaskStatus(record, getTypes());
	}

	public List<TaskStatus> wrapTaskStatuss(List<Record> records) {
		List<TaskStatus> wrapped = new ArrayList<>();
		for (Record record : records) {
			wrapped.add(new TaskStatus(record, getTypes()));
		}

		return wrapped;
	}

	public List<TaskStatus> searchTaskStatuss(LogicalSearchQuery query) {
		return wrapTaskStatuss(appLayerFactory.getModelLayerFactory().newSearchServices().search(query));
	}

	public List<TaskStatus> searchTaskStatuss(LogicalSearchCondition condition) {
		MetadataSchemaType type = ddvTaskStatus.schemaType();
		LogicalSearchQuery query = new LogicalSearchQuery(from(type).whereAllConditions(asList(condition)));
		return wrapTaskStatuss(appLayerFactory.getModelLayerFactory().newSearchServices().search(query));
	}

	public List<String> cachedSearchTaskStatussIds(LogicalSearchCondition condition) {
		MetadataSchemaType type = ddvTaskStatus.schemaType();
		LogicalSearchQuery query = new LogicalSearchQuery(from(type).whereAllConditions(asList(condition)));
		return appLayerFactory.getModelLayerFactory().newSearchServices().cachedSearchRecordIds(query);
	}

	public List<TaskStatus> cachedSearchTaskStatuss(LogicalSearchCondition condition) {
		MetadataSchemaType type = ddvTaskStatus.schemaType();
		LogicalSearchQuery query = new LogicalSearchQuery(from(type).whereAllConditions(asList(condition)));
		return wrapTaskStatuss(appLayerFactory.getModelLayerFactory().newSearchServices().cachedSearch(query));
	}

	public TaskStatus getTaskStatus(String id) {
		return wrapTaskStatus(get(id));
	}

	public List<TaskStatus> getTaskStatuss(List<String> ids) {
		return wrapTaskStatuss(get(ids));
	}

	public TaskStatus getTaskStatusWithCode(String code) {
		return wrapTaskStatus(getByCode(ddvTaskStatus.schemaType(), code));
	}

	public TaskStatus getTaskStatusWithLegacyId(String legacyId) {
		return wrapTaskStatus(getByLegacyId(ddvTaskStatus.schemaType(), legacyId));
	}

	public TaskStatus newTaskStatus() {
		return wrapTaskStatus(create(ddvTaskStatus.schema()));
	}

	public TaskStatus newTaskStatusWithId(String id) {
		return wrapTaskStatus(create(ddvTaskStatus.schema(), id));
	}

	public final SchemaTypeShortcuts_ddvTaskStatus_default ddvTaskStatus
			= new SchemaTypeShortcuts_ddvTaskStatus_default("ddvTaskStatus_default");

	public TaskType getTaskTypeByCode(String code) {
		return wrapTaskType(getByCode(taskTypeSchemaType(), code));
	}

	public TaskType wrapTaskType(Record record) {
		return record == null ? null : new TaskType(record, getTypes());
	}

	public class SchemaTypeShortcuts_ddvTaskStatus_default extends SchemaTypeShortcuts {
		protected SchemaTypeShortcuts_ddvTaskStatus_default(String schemaCode) {
			super(schemaCode);
		}

		public Metadata statusType() {
			return metadata("statusType");
		}
	}

	public Task wrapTask(Record record) {
		return record == null ? null : new Task(record, getTypes());
	}

	public List<Task> wrapTasks(List<Record> records) {
		List<Task> wrapped = new ArrayList<>();
		for (Record record : records) {
			wrapped.add(new Task(record, getTypes()));
		}

		return wrapped;
	}

	public List<Task> searchTasks(LogicalSearchQuery query) {
		return wrapTasks(appLayerFactory.getModelLayerFactory().newSearchServices().search(query));
	}

	public List<Task> searchTasks(LogicalSearchCondition condition) {
		MetadataSchemaType type = userTask.schemaType();
		LogicalSearchQuery query = new LogicalSearchQuery(from(type).whereAllConditions(asList(condition)));
		return wrapTasks(appLayerFactory.getModelLayerFactory().newSearchServices().search(query));
	}

	public Task getTask(String id) {
		return wrapTask(get(id));
	}

	public List<Task> getTasks(List<String> ids) {
		return wrapTasks(get(ids));
	}

	public Task getTaskWithLegacyId(String legacyId) {
		return wrapTask(getByLegacyId(userTask.schemaType(), legacyId));
	}

	public Task newTask() {
		return wrapTask(create(userTask.schema()));
	}

	public Task newTaskWithId(String id) {
		return wrapTask(create(userTask.schema(), id));
	}

	public final SchemaTypeShortcuts_userTask_default userTask
			= new SchemaTypeShortcuts_userTask_default("userTask_default");

	public class SchemaTypeShortcuts_userTask_default extends SchemaTypeShortcuts {
		protected SchemaTypeShortcuts_userTask_default(String schemaCode) {
			super(schemaCode);
		}

		public Metadata assignedOn() {
			return metadata("assignedOn");
		}

		public Metadata linkedDocuments() {
			return metadata("linkedDocuments");
		}

		public Metadata assignee() {
			return metadata("assignee");
		}

		public Metadata assigneeGroupsCandidates() {
			return metadata("assigneeGroupsCandidates");
		}

		public Metadata assigneeUsersCandidates() {
			return metadata("assigneeUsersCandidates");
		}

		public Metadata assigner() {
			return metadata("assigner");
		}

		public Metadata comments() {
			return metadata("comments");
		}

		public Metadata contents() {
			return metadata("contents");
		}

		public Metadata decision() {
			return metadata("decision");
		}

		public Metadata description() {
			return metadata("description");
		}

		public Metadata dueDate() {
			return metadata("dueDate");
		}

		public Metadata endDate() {
			return metadata("endDate");
		}

		public Metadata isModel() {
			return metadata("isModel");
		}

		public Metadata modelTask() {
			return metadata("modelTask");
		}

		public Metadata nextReminderOn() {
			return metadata("nextReminderOn");
		}

		public Metadata parentTask() {
			return metadata("parentTask");
		}

		public Metadata parentTaskDueDate() {
			return metadata("parentTaskDueDate");
		}

		public Metadata progressPercentage() {
			return metadata("progressPercentage");
		}

		public Metadata reminders() {
			return metadata("reminders");
		}

		public Metadata startDate() {
			return metadata("startDate");
		}

		public Metadata status() {
			return metadata("status");
		}

		public Metadata reminderFrequency() {
			return metadata("reminderFrequency");
		}

		public Metadata statusType() {
			return metadata("statusType");
		}

		public Metadata taskFollowers() {
			return metadata("taskFollowers");
		}

		public Metadata taskFollowersIds() {
			return metadata("taskFollowersIds");
		}

		public Metadata type() {
			return metadata("type");
		}

		public Metadata betaNextTaskCreated() {
			return metadata("nextTaskCreated");
		}

		public Metadata betaNextTasks() {
			return metadata("nextTasks");
		}

		public Metadata betaNextTasksDecisions() {
			return metadata("nextTasksDecisions");
		}

		public Metadata betaWorkflow() {
			return metadata("workflow");
		}

		public Metadata betaWorkflowInstance() {
			return metadata("workflowInstance");
		}

		public Metadata betaWorkflowTaskSort() {
			return metadata("workflowTaskSort");
		}

		public Metadata readByUser() {
			return metadata("readByUser");
		}
	}

	public BetaWorkflowTask wrapBetaWorkflowTask(Record record) {
		return record == null ? null : new BetaWorkflowTask(record, getTypes());
	}

	public List<BetaWorkflowTask> wrapBetaWorkflowTasks(List<Record> records) {
		List<BetaWorkflowTask> wrapped = new ArrayList<>();
		for (Record record : records) {
			wrapped.add(new BetaWorkflowTask(record, getTypes()));
		}

		return wrapped;
	}

	public List<BetaWorkflowTask> searchBetaWorkflowTasks(LogicalSearchQuery query) {
		return wrapBetaWorkflowTasks(appLayerFactory.getModelLayerFactory().newSearchServices().search(query));
	}

	public List<BetaWorkflowTask> searchBetaWorkflowTasks(LogicalSearchCondition condition) {
		MetadataSchemaType type = userTask.schemaType();
		LogicalSearchQuery query = new LogicalSearchQuery(from(type).whereAllConditions(asList(condition)));
		return wrapBetaWorkflowTasks(appLayerFactory.getModelLayerFactory().newSearchServices().search(query));
	}

	public BetaWorkflowTask getBetaWorkflowTask(String id) {
		return wrapBetaWorkflowTask(get(id));
	}

	public List<BetaWorkflowTask> getBetaWorkflowTasks(List<String> ids) {
		return wrapBetaWorkflowTasks(get(ids));
	}

	public BetaWorkflowTask getBetaWorkflowTaskWithLegacyId(String legacyId) {
		return wrapBetaWorkflowTask(getByLegacyId(userTask.schemaType(), legacyId));
	}

	public BetaWorkflowTask newBetaWorkflowTask() {
		return wrapBetaWorkflowTask(create(userTask.schema()));
	}

	public BetaWorkflowTask newBetaWorkflowTaskWithId(String id) {
		return wrapBetaWorkflowTask(create(userTask.schema(), id));
	}

	public BetaWorkflow wrapBetaWorkflow(Record record) {
		return record == null ? null : new BetaWorkflow(record, getTypes());
	}

	public List<BetaWorkflow> wrapBetaWorkflows(List<Record> records) {
		List<BetaWorkflow> wrapped = new ArrayList<>();
		for (Record record : records) {
			wrapped.add(new BetaWorkflow(record, getTypes()));
		}

		return wrapped;
	}

	public List<BetaWorkflow> searchBetaWorkflows(LogicalSearchQuery query) {
		return wrapBetaWorkflows(appLayerFactory.getModelLayerFactory().newSearchServices().search(query));
	}

	public List<BetaWorkflow> searchBetaWorkflows(LogicalSearchCondition condition) {
		MetadataSchemaType type = betaWorkflow.schemaType();
		LogicalSearchQuery query = new LogicalSearchQuery(from(type).whereAllConditions(asList(condition)));
		return wrapBetaWorkflows(appLayerFactory.getModelLayerFactory().newSearchServices().search(query));
	}

	public BetaWorkflow getBetaWorkflow(String id) {
		return wrapBetaWorkflow(get(id));
	}

	public List<BetaWorkflow> getBetaWorkflows(List<String> ids) {
		return wrapBetaWorkflows(get(ids));
	}

	public BetaWorkflow getBetaWorkflowWithCode(String code) {
		return wrapBetaWorkflow(getByCode(betaWorkflow.schemaType(), code));
	}

	public BetaWorkflow getBetaWorkflowWithLegacyId(String legacyId) {
		return wrapBetaWorkflow(getByLegacyId(betaWorkflow.schemaType(), legacyId));
	}

	public BetaWorkflow newBetaWorkflow() {
		return wrapBetaWorkflow(create(betaWorkflow.schema()));
	}

	public BetaWorkflow newBetaWorkflowWithId(String id) {
		return wrapBetaWorkflow(create(betaWorkflow.schema(), id));
	}

	public final SchemaTypeShortcuts_workflow_default betaWorkflow
			= new SchemaTypeShortcuts_workflow_default("workflow_default");

	public class SchemaTypeShortcuts_workflow_default extends SchemaTypeShortcuts {
		protected SchemaTypeShortcuts_workflow_default(String schemaCode) {
			super(schemaCode);
		}

		public Metadata code() {
			return metadata("code");
		}
	}

	public BetaWorkflowInstance wrapBetaWorkflowInstance(Record record) {
		return record == null ? null : new BetaWorkflowInstance(record, getTypes());
	}

	public List<BetaWorkflowInstance> wrapBetaWorkflowInstances(List<Record> records) {
		List<BetaWorkflowInstance> wrapped = new ArrayList<>();
		for (Record record : records) {
			wrapped.add(new BetaWorkflowInstance(record, getTypes()));
		}

		return wrapped;
	}

	public List<BetaWorkflowInstance> searchBetaWorkflowInstances(LogicalSearchQuery query) {
		return wrapBetaWorkflowInstances(appLayerFactory.getModelLayerFactory().newSearchServices().search(query));
	}

	public List<BetaWorkflowInstance> searchBetaWorkflowInstances(LogicalSearchCondition condition) {
		MetadataSchemaType type = betaWorkflowInstance.schemaType();
		LogicalSearchQuery query = new LogicalSearchQuery(from(type).whereAllConditions(asList(condition)));
		return wrapBetaWorkflowInstances(appLayerFactory.getModelLayerFactory().newSearchServices().search(query));
	}

	public BetaWorkflowInstance getBetaWorkflowInstance(String id) {
		return wrapBetaWorkflowInstance(get(id));
	}

	public List<BetaWorkflowInstance> getBetaWorkflowInstances(List<String> ids) {
		return wrapBetaWorkflowInstances(get(ids));
	}

	public BetaWorkflowInstance getBetaWorkflowInstanceWithLegacyId(String legacyId) {
		return wrapBetaWorkflowInstance(getByLegacyId(betaWorkflowInstance.schemaType(), legacyId));
	}

	public BetaWorkflowInstance newBetaWorkflowInstance() {
		return wrapBetaWorkflowInstance(create(betaWorkflowInstance.schema()));
	}

	public BetaWorkflowInstance newBetaWorkflowInstanceWithId(String id) {
		return wrapBetaWorkflowInstance(create(betaWorkflowInstance.schema(), id));
	}

	public final SchemaTypeShortcuts_workflowInstance_default betaWorkflowInstance
			= new SchemaTypeShortcuts_workflowInstance_default("workflowInstance_default");

	public class SchemaTypeShortcuts_workflowInstance_default extends SchemaTypeShortcuts {
		protected SchemaTypeShortcuts_workflowInstance_default(String schemaCode) {
			super(schemaCode);
		}

		public Metadata extraFields() {
			return metadata("extraFields");
		}

		public Metadata startedBy() {
			return metadata("startedBy");
		}

		public Metadata startedOn() {
			return metadata("startedOn");
		}

		public Metadata status() {
			return metadata("status");
		}

		public Metadata workflow() {
			return metadata("workflow");
		}
	}

	/** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** **/
	// Auto-generated methods by GenerateHelperClassAcceptTest -- end

	/**
	 * * ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** **
	 **/

	public TaskReminderEmailManager getTaskReminderEmailManager() {
		return appLayerFactory.getRegisteredManager(getCollection(), TaskModule.ID, TaskReminderEmailManager.ID);
	}

	public MetadataSchema getLinkedSchema(MetadataSchemaType schemaType, SchemaLinkingType recordType) {
		if (recordType == null || recordType.getLinkedSchema() == null) {
			return schemaType.getDefaultSchema();
		} else {
			return schemaType.getSchema(recordType.getLinkedSchema());
		}
	}

	public String getSchemaCodeForTaskTypeRecordId(String taskTypeRecordId) {
		ModelLayerFactory modelLayerFactory = getModelLayerFactory();
		RecordServices recordServices = modelLayerFactory.newRecordServices();
		Record schemaRecord = recordServices.getDocumentById(taskTypeRecordId);
		TaskType taskType = new TaskType(schemaRecord, getTypes());
		String linkedSchemaCode = taskType.getLinkedSchema();
		return linkedSchemaCode;
	}

	public TaskType getTaskType(String id) {
		return new TaskType(get(id), getTypes());
	}

	public TaskType newTaskType() {
		return new TaskType(create(getTypes().getSchemaType(TaskType.SCHEMA_TYPE).getDefaultSchema()), getTypes());
	}

	public MetadataSchema defaultTaskSchema() {
		return getTypes().getSchema(Task.DEFAULT_SCHEMA);
	}

	public MetadataSchemaType taskTypeSchemaType() {
		return getTypes().getSchemaType(TaskType.SCHEMA_TYPE);
	}

	public MetadataSchemaType taskSchemaType() {
		return getTypes().getSchemaType(Task.SCHEMA_TYPE);
	}

	public MetadataSchema taskSchemaFor(TaskType type) {
		return type == null ? defaultTaskSchema() : getLinkedSchema(taskSchemaType(), type);
	}

	public MetadataSchema taskSchemaFor(String typeId) {
		return typeId == null ? defaultTaskSchema() : taskSchemaFor(getTaskType(typeId));
	}

	public Task newTaskWithType(TaskType type) {
		Record record = create(taskSchemaFor(type));
		return new Task(record, getTypes()).setType(type);
	}

	public Task newTaskWithType(String typeId) {
		Record record = create(taskSchemaFor(typeId));
		return new Task(record, getTypes()).setType(typeId);
	}

	public BetaWorkflowTask newWorkflowModelTask(BetaWorkflow workflow) {
		return wrapBetaWorkflowTask(create(userTask.schema())).setModel(true).setWorkflow(workflow);
	}

	public BetaWorkflowTask newWorkflowModelTaskWithType(BetaWorkflow workflow, String typeId) {
		BetaWorkflowTask task = wrapBetaWorkflowTask(create(taskSchemaFor(typeId))).setModel(true).setWorkflow(workflow);
		TaskType taskType = getTaskType(typeId);
		task.setType(taskType);
		return task;
	}

	public BetaWorkflowTask newWorkflowModelTaskWithId(String id, BetaWorkflow workflow) {
		return wrapBetaWorkflowTask(create(userTask.schema(), id)).setModel(true).setWorkflow(workflow);
	}

	public List<TaskStatus> getFinishedOrClosedStatuses() {
		List<TaskStatus> status = new ArrayList<>();
		status.addAll(cachedSearchTaskStatuss(where(ddvTaskStatus.statusType()).is(FINISHED)));
		status.add(getTaskStatusWithCode(CLOSED_CODE));
		return status;
	}

	public List<String> getFinishedOrClosedStatusesIds() {
		List<String> status = new ArrayList<>();
		status.addAll(cachedSearchTaskStatussIds(where(ddvTaskStatus.statusType()).is(FINISHED)));
		status.add(getTaskStatusWithCode(CLOSED_CODE).getId());
		return status;
	}


	public void setType(Task task, TaskType taskType) {
		setType(task.getWrappedRecord(), taskType == null ? null : taskType.getWrappedRecord());
	}

	//KEEP
	public Task newBorrowFolderRequestTask(String assignerId, List<String> assignees, String folderId, int numberOfDays,
										   String recordTitle) {
		Task task = newTaskWithType(getTaskTypeByCode(BorrowRequest.SCHEMA_NAME));
		return task.setTitle($("TaskSchemasRecordsServices.borrowFolderRequest", recordTitle))
				.setAssigneeUsersCandidates(assignees)
				.setAssigner(assignerId)
				.setAssignationDate(LocalDate.now())
				.setLinkedFolders(asList(folderId)).set(BorrowRequest.BORROW_DURATION, numberOfDays)
				.set(RequestTask.APPLICANT, assignerId);
	}

	//KEEP
	public Task newReturnFolderRequestTask(String assignerId, List<String> assignees, String folderId,
										   String recordTitle) {
		return newTaskWithType(getTaskTypeByCode(ReturnRequest.SCHEMA_NAME))
				.setTitle($("TaskSchemasRecordsServices.returnFolderRequest", recordTitle))
				.setAssigneeUsersCandidates(assignees)
				.setAssigner(assignerId)
				.setAssignationDate(LocalDate.now())
				.setLinkedFolders(asList(folderId)).set(RequestTask.APPLICANT, assignerId);
	}

	//KEEP
	public Task newReactivateFolderRequestTask(String assignerId, List<String> assignees, String folderId,
											   String recordTitle,
											   LocalDate localDate) {
		return newTaskWithType(getTaskTypeByCode(ReactivationRequest.SCHEMA_NAME))
				.setTitle($("TaskSchemasRecordsServices.reactivationFolderRequest", recordTitle))
				.setAssigneeUsersCandidates(assignees)
				.setAssigner(assignerId)
				.setAssignationDate(LocalDate.now())
				.setLinkedFolders(asList(folderId)).set(RequestTask.APPLICANT, assignerId)
				.set(ReactivationRequest.REACTIVATION_DATE, localDate);
	}

	//KEEP
	public Task newBorrowFolderExtensionRequestTask(String assignerId, List<String> assignees, String folderId,
													String recordTitle, LocalDate value) {
		return newTaskWithType(getTaskTypeByCode(ExtensionRequest.SCHEMA_NAME))
				.setTitle($("TaskSchemasRecordsServices.borrowFolderExtensionRequest", recordTitle))
				.setAssigneeUsersCandidates(assignees)
				.setAssigner(assignerId)
				.setAssignationDate(LocalDate.now())
				.setLinkedFolders(asList(folderId)).set(RequestTask.APPLICANT, assignerId)
				.set(ExtensionRequest.EXTENSION_VALUE, value);
	}

	//KEEP
	public Task newBorrowContainerRequestTask(String assignerId, List<String> assignees, String containerId,
											  int numberOfDays,
											  String recordTitle) {
		return newTaskWithType(getTaskTypeByCode(BorrowRequest.SCHEMA_NAME))
				.setTitle($("TaskSchemasRecordsServices.borrowContainerRequest", recordTitle))
				.setAssigneeUsersCandidates(assignees)
				.setAssigner(assignerId)
				.setAssignationDate(LocalDate.now())
				.setLinkedContainers(asList(containerId)).set(BorrowRequest.BORROW_DURATION, numberOfDays)
				.set(RequestTask.APPLICANT, assignerId);
	}

	//KEEP
	public Task newReturnContainerRequestTask(String assignerId, List<String> assignees, String containerId,
											  String recordTitle) {
		return newTaskWithType(getTaskTypeByCode(ReturnRequest.SCHEMA_NAME))
				.setTitle($("TaskSchemasRecordsServices.returnContainerRequest", recordTitle))
				.setAssigneeUsersCandidates(assignees)
				.setAssigner(assignerId)
				.setAssignationDate(LocalDate.now())
				.setLinkedContainers(asList(containerId)).set(RequestTask.APPLICANT, assignerId);
	}

	//KEEP
	public Task newReactivationContainerRequestTask(String assignerId, List<String> assignees, String containerId,
													String recordTitle, LocalDate localDate) {
		return newTaskWithType(getTaskTypeByCode(ReactivationRequest.SCHEMA_NAME))
				.setTitle($("TaskSchemasRecordsServices.reactivationContainerRequest", recordTitle))
				.setAssigneeUsersCandidates(assignees)
				.setAssigner(assignerId)
				.setAssignationDate(LocalDate.now())
				.setLinkedContainers(asList(containerId)).set(RequestTask.APPLICANT, assignerId)
				.set(ReactivationRequest.REACTIVATION_DATE, localDate);
	}

	//KEEP
	public Task newBorrowContainerExtensionRequestTask(String assignerId, List<String> assignees, String containerId,
													   String recordTitle, LocalDate value) {
		return newTaskWithType(getTaskTypeByCode(ExtensionRequest.SCHEMA_NAME))
				.setTitle($("TaskSchemasRecordsServices.borrowContainerExtensionRequest", recordTitle))
				.setAssigneeUsersCandidates(assignees)
				.setAssigner(assignerId)
				.setAssignationDate(LocalDate.now())
				.setLinkedContainers(asList(containerId)).set(RequestTask.APPLICANT, assignerId)
				.set(ExtensionRequest.EXTENSION_VALUE, value);
	}

	//KEEP
	public boolean isRequestTask(Task task) {
		List<String> acceptedSchemas = new ArrayList<>(asList(BorrowRequest.FULL_SCHEMA_NAME, ReturnRequest.FULL_SCHEMA_NAME,
				ReactivationRequest.FULL_SCHEMA_NAME, ExtensionRequest.FULL_SCHEMA_NAME));
		return acceptedSchemas.contains(task.getSchemaCode());
	}

	public boolean isRequestTask(TaskVO task) {
		List<String> acceptedSchemas = new ArrayList<>(asList(BorrowRequest.FULL_SCHEMA_NAME, ReturnRequest.FULL_SCHEMA_NAME,
				ReactivationRequest.FULL_SCHEMA_NAME, ExtensionRequest.FULL_SCHEMA_NAME));
		return acceptedSchemas.contains(task.getSchema().getCode());
	}
}
