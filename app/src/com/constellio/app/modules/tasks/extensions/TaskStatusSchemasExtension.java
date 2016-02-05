package com.constellio.app.modules.tasks.extensions;

import static com.constellio.app.modules.tasks.model.wrappers.TaskStatusType.CLOSED;
import static com.constellio.app.modules.tasks.model.wrappers.TaskStatusType.FINISHED;
import static com.constellio.app.modules.tasks.model.wrappers.types.TaskStatus.SCHEMA_TYPE;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import com.constellio.app.modules.tasks.model.wrappers.TaskStatusType;
import com.constellio.app.modules.tasks.model.wrappers.types.TaskStatus;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.extensions.behaviors.RecordExtension;
import com.constellio.model.extensions.events.records.RecordLogicalDeletionEvent;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

public class TaskStatusSchemasExtension extends RecordExtension {
	private final TasksSchemasRecordsServices tasksSchema;
	private final SearchServices searchServices;

	public TaskStatusSchemasExtension(String collection, AppLayerFactory appLayerFactory) {
		tasksSchema = new TasksSchemasRecordsServices(collection, appLayerFactory);
		searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
	}

	@Override
	public void recordLogicallyDeleted(RecordLogicalDeletionEvent event) {
		if (event.getSchemaTypeCode().equals(SCHEMA_TYPE)) {
			TaskStatus taskStatus = tasksSchema.wrapTaskStatus(event.getRecord());
			leaveAtLeastOneRecordWithEachStatusType(taskStatus);
		}
	}

	private void leaveAtLeastOneRecordWithEachStatusType(TaskStatus taskStatus) {
		TaskStatusType type = taskStatus.getStatusType();
		LogicalSearchCondition recordsWithTypeCondition = from(tasksSchema.ddvTaskStatus.schema())
				.where(tasksSchema.ddvTaskStatus.statusType()).isEqualTo(type);
		long count = searchServices.getResultsCount(recordsWithTypeCondition);
		if (count == 1) {
			throw new AtLeastOneRecordWithStatusRuntimeException(type);
		}
	}

	public static class AtLeastOneRecordWithStatusRuntimeException extends RuntimeException {
		public AtLeastOneRecordWithStatusRuntimeException(
				TaskStatusType type) {
			super("At least one task status with code " + type.getCode());
		}
	}
}
