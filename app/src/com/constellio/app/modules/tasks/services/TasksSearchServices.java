package com.constellio.app.modules.tasks.services;

import static com.constellio.app.modules.tasks.model.wrappers.TaskStatusType.FINISHED;
import static com.constellio.app.modules.tasks.model.wrappers.types.TaskStatus.CLOSED_CODE;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.allConditions;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.anyConditions;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.where;

import java.util.List;

import com.constellio.app.modules.tasks.model.wrappers.types.TaskStatus;
import com.constellio.app.modules.tasks.model.wrappers.types.TaskType;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

public class TasksSearchServices {
	TasksSchemasRecordsServices tasksSchemas;
	SearchServices searchServices;

	public TasksSearchServices(TasksSchemasRecordsServices tasksSchemas) {
		this.tasksSchemas = tasksSchemas;
		this.searchServices = tasksSchemas.getModelLayerFactory().newSearchServices();
	}

	public LogicalSearchQuery getTasksAssignedByUserQuery(User user) {
		return new LogicalSearchQuery(
				from(tasksSchemas.userTask.schemaType()).where(tasksSchemas.userTask.assigner()).isEqualTo(user)
						.andWhere(Schemas.LOGICALLY_DELETED_STATUS).isNotEqual(true)
						.andWhere(tasksSchemas.userTask.assignee()).isNotEqual(user)
						.andWhere(tasksSchemas.userTask.status()).isNotEqual(getClosedStatus())
						.andWhere(tasksSchemas.userTask.isModel()).isFalseOrNull())
				.filteredWithUser(user).sortAsc(tasksSchemas.userTask.dueDate());
	}

	public LogicalSearchQuery getUnassignedTasksQuery(User user) {
		return new LogicalSearchQuery(
				from(tasksSchemas.userTask.schemaType()).where(tasksSchemas.userTask.assignee()).isNull()
						.andWhere(Schemas.LOGICALLY_DELETED_STATUS).isNotEqual(true)
						.andWhere(tasksSchemas.userTask.assigneeGroupsCandidates()).isNull()
						.andWhere(tasksSchemas.userTask.assigneeUsersCandidates()).isNull()
						.andWhere(tasksSchemas.userTask.status()).isNotEqual(getClosedStatus())
						.andWhere(tasksSchemas.userTask.isModel()).isFalseOrNull())
				.filteredWithUser(user).sortAsc(tasksSchemas.userTask.dueDate());
	}

	public LogicalSearchQuery getTasksAssignedToUserQuery(User user) {
		LogicalSearchCondition condition = from(tasksSchemas.userTask.schemaType()).whereAllConditions(
				where(tasksSchemas.userTask.isModel()).isFalseOrNull(),
				where(tasksSchemas.userTask.status()).isNotEqual(getClosedStatus()),
				anyConditions(
						where(tasksSchemas.userTask.assignee()).isEqualTo(user).andWhere(Schemas.LOGICALLY_DELETED_STATUS)
								.isNotEqual(true),
						allConditions(
								where(tasksSchemas.userTask.assignee()).isNull(),
								anyConditions(
										where(tasksSchemas.userTask.assigneeGroupsCandidates()).isIn(user.getUserGroups()),
										where(tasksSchemas.userTask.assigneeUsersCandidates()).isEqualTo(user)
								)
						)
				));
		return new LogicalSearchQuery(condition).filteredWithUser(user).sortAsc(tasksSchemas.userTask.dueDate());
	}

	public LogicalSearchQuery getDirectSubTasks(String taskId, User user) {
		return new LogicalSearchQuery(
				from(tasksSchemas.userTask.schemaType()).where(tasksSchemas.userTask.parentTask()).isEqualTo(taskId)
						.andWhere(Schemas.LOGICALLY_DELETED_STATUS).isNotEqual(true)
						.andWhere(tasksSchemas.userTask.status()).isNotEqual(getClosedStatus()))
				.filteredWithUser(user).sortAsc(tasksSchemas.userTask.dueDate());
	}

	public LogicalSearchQuery getRecentlyCompletedTasks(User user) {
		return new LogicalSearchQuery(
				from(tasksSchemas.userTask.schemaType())
						.where(tasksSchemas.userTask.status()).isIn(getFinishedStatuses())
						.andWhere(Schemas.LOGICALLY_DELETED_STATUS).isNotEqual(true)
						.andWhere(tasksSchemas.userTask.isModel()).isFalseOrNull())
				.filteredWithUser(user).sortAsc(tasksSchemas.userTask.dueDate());
	}

	public TaskStatus getClosedStatus() {
		return tasksSchemas.getTaskStatusWithCode(CLOSED_CODE);
	}

	public TaskStatus getFirstFinishedStatus() {
		LogicalSearchQuery firstClosedTaskStatusQuery = new LogicalSearchQuery(
				from(tasksSchemas.ddvTaskStatus.schema()).where(tasksSchemas.ddvTaskStatus.statusType())
						.is(FINISHED)
						.andWhere(Schemas.LOGICALLY_DELETED_STATUS).isNotEqual(true))
				.sortDesc(tasksSchemas.ddvTaskStatus.createdOn()).setNumberOfRows(1);
		List<Record> result = searchServices.search(firstClosedTaskStatusQuery);
		if (result.isEmpty()) {
			return null;
		}
		return tasksSchemas.wrapTaskStatus(result.get(0));
	}

	public List<TaskStatus> getFinishedStatuses() {
		return tasksSchemas.searchTaskStatuss(
				where(tasksSchemas.ddvTaskStatus.statusType()).is(FINISHED).andWhere(Schemas.LOGICALLY_DELETED_STATUS).isNotEqual(true));
	}

	public String getSchemaCodeForTaskTypeRecordId(String taskTypeRecordId) {
		ModelLayerFactory modelLayerFactory = tasksSchemas.getModelLayerFactory();
		RecordServices recordServices = modelLayerFactory.newRecordServices();
		Record schemaRecord = recordServices.getDocumentById(taskTypeRecordId);
		TaskType taskType = new TaskType(schemaRecord, tasksSchemas.getTypes());
		String linkedSchemaCode = taskType.getLinkedSchema();
		return linkedSchemaCode;
	}
}
