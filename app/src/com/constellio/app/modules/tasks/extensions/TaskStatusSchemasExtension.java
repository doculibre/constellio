/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
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
