package com.constellio.app.modules.tasks.services;

import com.constellio.app.modules.rm.wrappers.type.SchemaLinkingType;
import com.constellio.app.modules.tasks.TaskModule;
import com.constellio.app.modules.tasks.model.managers.TaskReminderEmailManager;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.model.wrappers.Workflow;
import com.constellio.app.modules.tasks.model.wrappers.WorkflowInstance;
import com.constellio.app.modules.tasks.model.wrappers.request.BorrowRequest;
import com.constellio.app.modules.tasks.model.wrappers.request.ExtensionRequest;
import com.constellio.app.modules.tasks.model.wrappers.request.ReactivationRequest;
import com.constellio.app.modules.tasks.model.wrappers.request.ReturnRequest;
import com.constellio.app.modules.tasks.model.wrappers.types.TaskStatus;
import com.constellio.app.modules.tasks.model.wrappers.types.TaskType;
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
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.util.*;

import static com.constellio.app.modules.tasks.model.wrappers.TaskStatusType.FINISHED;
import static com.constellio.app.modules.tasks.model.wrappers.types.TaskStatus.CLOSED_CODE;
import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.where;
import static java.util.Arrays.asList;

public class TasksSchemasRecordsServices extends SchemasRecordsServices {

	private final AppLayerFactory appLayerFactory;

	public TasksSchemasRecordsServices(String collection, AppLayerFactory appLayerFactory) {
		super(collection, appLayerFactory.getModelLayerFactory());
		this.appLayerFactory = appLayerFactory;
	}

	/** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** **/
	// Auto-generated methods by GenerateHelperClassAcceptTest -- start

	/** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** **/

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

		public Metadata nextTaskCreated() {
			return metadata("nextTaskCreated");
		}

		public Metadata nextTasks() {
			return metadata("nextTasks");
		}

		public Metadata nextTasksDecisions() {
			return metadata("nextTasksDecisions");
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

		public Metadata workflow() {
			return metadata("workflow");
		}

		public Metadata workflowInstance() {
			return metadata("workflowInstance");
		}

		public Metadata workflowTaskSort() {
			return metadata("workflowTaskSort");
		}
	}

	public Workflow wrapWorkflow(Record record) {
		return record == null ? null : new Workflow(record, getTypes());
	}

	public List<Workflow> wrapWorkflows(List<Record> records) {
		List<Workflow> wrapped = new ArrayList<>();
		for (Record record : records) {
			wrapped.add(new Workflow(record, getTypes()));
		}

		return wrapped;
	}

	public List<Workflow> searchWorkflows(LogicalSearchQuery query) {
		return wrapWorkflows(appLayerFactory.getModelLayerFactory().newSearchServices().search(query));
	}

	public List<Workflow> searchWorkflows(LogicalSearchCondition condition) {
		MetadataSchemaType type = workflow.schemaType();
		LogicalSearchQuery query = new LogicalSearchQuery(from(type).whereAllConditions(asList(condition)));
		return wrapWorkflows(appLayerFactory.getModelLayerFactory().newSearchServices().search(query));
	}

	public Workflow getWorkflow(String id) {
		return wrapWorkflow(get(id));
	}

	public List<Workflow> getWorkflows(List<String> ids) {
		return wrapWorkflows(get(ids));
	}

	public Workflow getWorkflowWithCode(String code) {
		return wrapWorkflow(getByCode(workflow.schemaType(), code));
	}

	public Workflow getWorkflowWithLegacyId(String legacyId) {
		return wrapWorkflow(getByLegacyId(workflow.schemaType(), legacyId));
	}

	public Workflow newWorkflow() {
		return wrapWorkflow(create(workflow.schema()));
	}

	public Workflow newWorkflowWithId(String id) {
		return wrapWorkflow(create(workflow.schema(), id));
	}

	public final SchemaTypeShortcuts_workflow_default workflow
			= new SchemaTypeShortcuts_workflow_default("workflow_default");

	public class SchemaTypeShortcuts_workflow_default extends SchemaTypeShortcuts {
		protected SchemaTypeShortcuts_workflow_default(String schemaCode) {
			super(schemaCode);
		}

		public Metadata code() {
			return metadata("code");
		}
	}

	public WorkflowInstance wrapWorkflowInstance(Record record) {
		return record == null ? null : new WorkflowInstance(record, getTypes());
	}

	public List<WorkflowInstance> wrapWorkflowInstances(List<Record> records) {
		List<WorkflowInstance> wrapped = new ArrayList<>();
		for (Record record : records) {
			wrapped.add(new WorkflowInstance(record, getTypes()));
		}

		return wrapped;
	}

	public List<WorkflowInstance> searchWorkflowInstances(LogicalSearchQuery query) {
		return wrapWorkflowInstances(appLayerFactory.getModelLayerFactory().newSearchServices().search(query));
	}

	public List<WorkflowInstance> searchWorkflowInstances(LogicalSearchCondition condition) {
		MetadataSchemaType type = workflowInstance.schemaType();
		LogicalSearchQuery query = new LogicalSearchQuery(from(type).whereAllConditions(asList(condition)));
		return wrapWorkflowInstances(appLayerFactory.getModelLayerFactory().newSearchServices().search(query));
	}

	public WorkflowInstance getWorkflowInstance(String id) {
		return wrapWorkflowInstance(get(id));
	}

	public List<WorkflowInstance> getWorkflowInstances(List<String> ids) {
		return wrapWorkflowInstances(get(ids));
	}

	public WorkflowInstance getWorkflowInstanceWithLegacyId(String legacyId) {
		return wrapWorkflowInstance(getByLegacyId(workflowInstance.schemaType(), legacyId));
	}

	public WorkflowInstance newWorkflowInstance() {
		return wrapWorkflowInstance(create(workflowInstance.schema()));
	}

	public WorkflowInstance newWorkflowInstanceWithId(String id) {
		return wrapWorkflowInstance(create(workflowInstance.schema(), id));
	}

	public final SchemaTypeShortcuts_workflowInstance_default workflowInstance
			= new SchemaTypeShortcuts_workflowInstance_default("workflowInstance_default");

	public class SchemaTypeShortcuts_workflowInstance_default extends SchemaTypeShortcuts {
		protected SchemaTypeShortcuts_workflowInstance_default(String schemaCode) {
			super(schemaCode);
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

	/** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** **/

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

	public Task newWorkflowModelTask(Workflow workflow) {
		return wrapTask(create(userTask.schema())).setModel(true).setWorkflow(workflow);
	}

	public Task newWorkflowModelTaskWithType(Workflow workflow, String typeId) {
		Task task = wrapTask(create(taskSchemaFor(typeId))).setModel(true).setWorkflow(workflow);
		TaskType taskType = getTaskType(typeId);
		task.setType(taskType);
		return task;
	}

	public Task newWorkflowModelTaskWithId(String id, Workflow workflow) {
		return wrapTask(create(userTask.schema(), id)).setModel(true).setWorkflow(workflow);
	}

	public List<TaskStatus> getFinishedOrClosedStatuses() {
		List<TaskStatus> status = new ArrayList<>();
		status.addAll(searchTaskStatuss(where(ddvTaskStatus.statusType()).is(FINISHED)));
		status.add(getTaskStatusWithCode(CLOSED_CODE));
		return status;
	}

	public void setType(Task task, TaskType taskType) {
		setType(task.getWrappedRecord(), taskType == null ? null : taskType.getWrappedRecord());
	}

	//KEEP
	public Task newBorrowFolderRequestTask(String assignerId, String folderId){
		Map<String, String> stringStringMap = new HashMap<>();
		stringStringMap.put($("yes"), "NO_VALUE");
		stringStringMap.put($("no"), "NO_VALUE");
		return newTaskWithType(getTaskTypeByCode(BorrowRequest.SCHEMA_NAME))
					.setTitle($("borrowRequest")).setAssignee(assignerId).setAssigner(assignerId)
					.setAssignedOn(LocalDate.now()).setLinkedFolders(asList(folderId))
					.setNextTasksDecisions(stringStringMap);
	}

	//KEEP
	public Task newReturnFolderRequestTask(String assignerId, String folderId){
		Map<String, String> stringStringMap = new HashMap<>();
		stringStringMap.put($("yes"), "NO_VALUE");
		stringStringMap.put($("no"), "NO_VALUE");
		return newTaskWithType(getTaskTypeByCode(ReactivationRequest.SCHEMA_NAME))
					.setTitle($("returnRequest")).setAssignee(assignerId).setAssigner(assignerId)
					.setAssignedOn(LocalDate.now()).setLinkedFolders(asList(folderId))
					.setNextTasksDecisions(stringStringMap);
	}

	//KEEP
	public Task newReactivateFolderRequestTask(String assignerId, String folderId){
		Map<String, String> stringStringMap = new HashMap<>();
		stringStringMap.put($("yes"), "NO_VALUE");
		stringStringMap.put($("no"), "NO_VALUE");
		return newTaskWithType(getTaskTypeByCode(ReactivationRequest.SCHEMA_NAME))
					.setTitle($("reactivationRequest")).setAssignee(assignerId).setAssigner(assignerId)
					.setAssignedOn(LocalDate.now()).setLinkedFolders(asList(folderId))
					.setNextTasksDecisions(stringStringMap);
	}

	//KEEP
	public Task newBorrowFolderExtensionRequestTask(String assignerId, String folderId, LocalDate value) {
		Map<String, String> stringStringMap = new HashMap<>();
		stringStringMap.put($("yes"), "NO_VALUE");
		stringStringMap.put($("no"), "NO_VALUE");
		return newTaskWithType(getTaskTypeByCode(ExtensionRequest.SCHEMA_NAME))
					.setTitle($("borrowExtensionRequest")).setAssignee(assignerId).setAssigner(assignerId)
					.setAssignedOn(LocalDate.now()).setLinkedFolders(asList(folderId))
				.setNextTasksDecisions(stringStringMap)
				.set(ExtensionRequest.EXTENSION_VALUE, value);
	}

	//KEEP
	public Task newBorrowContainerRequestTask(String assignerId, String containerId) {
		Map<String, String> stringStringMap = new HashMap<>();
		stringStringMap.put($("yes"), "NO_VALUE");
		stringStringMap.put($("no"), "NO_VALUE");
		return newTaskWithType(getTaskTypeByCode(BorrowRequest.SCHEMA_NAME))
				.setTitle($("borrowRequest")).setAssignee(assignerId).setAssigner(assignerId)
				.setAssignedOn(LocalDate.now()).setLinkedFolders(asList(containerId))
				.setNextTasksDecisions(stringStringMap);
	}

	//KEEP
	public Task newReturnContainerRequestTask(String assignerId, String containerId) {
		Map<String, String> stringStringMap = new HashMap<>();
		stringStringMap.put($("yes"), "NO_VALUE");
		stringStringMap.put($("no"), "NO_VALUE");
		return newTaskWithType(getTaskTypeByCode(ReturnRequest.SCHEMA_NAME))
				.setTitle($("returnRequest")).setAssignee(assignerId).setAssigner(assignerId)
				.setAssignedOn(LocalDate.now()).setLinkedFolders(asList(containerId))
				.setNextTasksDecisions(stringStringMap);
	}

	//KEEP
	public Task newReactivationContainerRequestTask(String assignerId, String containerId) {
		Map<String, String> stringStringMap = new HashMap<>();
		stringStringMap.put($("yes"), "NO_VALUE");
		stringStringMap.put($("no"), "NO_VALUE");
		return newTaskWithType(getTaskTypeByCode(ReactivationRequest.SCHEMA_NAME))
				.setTitle($("reactivateRequest")).setAssignee(assignerId).setAssigner(assignerId)
				.setAssignedOn(LocalDate.now()).setLinkedFolders(asList(containerId))
				.setNextTasksDecisions(stringStringMap);
	}

	//KEEP
	public Task newBorrowContainerExtensionRequestTask(String assignerId, String containerId, LocalDate value) {
		Map<String, String> stringStringMap = new HashMap<>();
		stringStringMap.put($("yes"), "NO_VALUE");
		stringStringMap.put($("no"), "NO_VALUE");
		return newTaskWithType(getTaskTypeByCode(ExtensionRequest.SCHEMA_NAME))
				.setTitle($("extensionRequest")).setAssignee(assignerId).setAssigner(assignerId)
				.setAssignedOn(LocalDate.now()).setLinkedFolders(asList(containerId))
				.setNextTasksDecisions(stringStringMap)
				.set(ExtensionRequest.EXTENSION_VALUE, value);
	}
}
