package com.constellio.app.modules.tasks.services;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;

import com.constellio.app.modules.rm.wrappers.type.SchemaLinkingType;
import com.constellio.app.modules.tasks.TaskModule;
import com.constellio.app.modules.tasks.model.managers.TaskReminderEmailManager;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.model.wrappers.types.TaskStatus;
import com.constellio.app.modules.tasks.model.wrappers.types.TaskType;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

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

		public Metadata content() {
			return metadata("content");
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

		public Metadata nextReminderOn() {
			return metadata("nextReminderOn");
		}

		public Metadata parentTask() {
			return metadata("parentTask");
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

		public Metadata taskFollowers() {
			return metadata("taskFollowers");
		}

		public Metadata taskFollowersIds() {
			return metadata("taskFollowersIds");
		}
	}
	/** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** **/
	// Auto-generated methods by GenerateHelperClassAcceptTest -- end

	/** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** **/

	public TaskReminderEmailManager getTaskReminderEmailManager() {
		return appLayerFactory.getRegisteredManager(collection, TaskModule.ID, TaskReminderEmailManager.ID);
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

}
