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
package com.constellio.app.modules.tasks.services;

import static com.constellio.app.modules.tasks.model.wrappers.TaskStatusType.FINISHED;
import static com.constellio.app.modules.tasks.model.wrappers.types.TaskStatus.CLOSED_CODE;
import static com.constellio.app.modules.tasks.model.wrappers.types.TaskStatus.STANDBY_CODE;
import static com.constellio.model.entities.schemas.Schemas.IDENTIFIER;
import static com.constellio.model.services.search.query.ReturnedMetadatasFilter.onlyFields;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;

import com.constellio.app.modules.tasks.model.wrappers.types.TaskStatus;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
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
		String closedStatusId = getClosedStatus().getId();
		return new LogicalSearchQuery(
				from(tasksSchemas.userTask.schema()).where(tasksSchemas.userTask.assigner()).isEqualTo(user.getId())
						.andWhere(tasksSchemas.userTask.status()).isNotEqual(closedStatusId))
				.filteredWithUser(user).sortDesc(Schemas.CREATED_ON);
	}

	public LogicalSearchQuery getNonAssignedTasksQuery(User currentUser) {
		String closedStatusId = getClosedStatus().getId();
		return new LogicalSearchQuery(from(tasksSchemas.userTask.schema()).where(tasksSchemas.userTask.assignee()).isNull()
				.andWhere(tasksSchemas.userTask.assigneeGroupsCandidates()).isNull()
				.andWhere(tasksSchemas.userTask.assigneeUsersCandidates()).isNull()
				.andWhere(tasksSchemas.userTask.status()).isNotEqual(closedStatusId))
				.filteredWithUser(currentUser).sortDesc(Schemas.CREATED_ON);
	}

	public LogicalSearchQuery getTasksAssignedToUserQuery(User user) {
		String closedStatusId = getClosedStatus().getId();
		LogicalSearchCondition userInAssignation = from(tasksSchemas.userTask.schema()).where(tasksSchemas.userTask.assignee())
				.isEqualTo(user.getId())
				.orWhere(tasksSchemas.userTask.assigneeGroupsCandidates()).isIn(user.getUserGroups())
				.orWhere(tasksSchemas.userTask.assigneeUsersCandidates()).isEqualTo(user.getId());
		return new LogicalSearchQuery(userInAssignation.andWhere(tasksSchemas.userTask.status()).isNotEqual(closedStatusId))
				.filteredWithUser(user).sortDesc(Schemas.CREATED_ON);
	}

	public LogicalSearchQuery getDirectSubTasks(String taskId, User currentUser) {
		String closedStatusId = getClosedStatus().getId();
		return new LogicalSearchQuery(
				from(tasksSchemas.userTask.schema()).where(tasksSchemas.userTask.parentTask()).isEqualTo(taskId)
						.andWhere(tasksSchemas.userTask.status()).isNotEqual(closedStatusId))
				.filteredWithUser(currentUser).sortDesc(Schemas.CREATED_ON);
	}

	public LogicalSearchQuery getRecentlyCompletedTasks(User currentUser) {
		/*List<Record> finishedStatusesRecords = searchServices
				.search(new LogicalSearchQuery(from(tasksSchemas.ddvTaskStatus.schema()).where(
						tasksSchemas.ddvTaskStatus.statusType())
						.is(FINISHED)).setReturnedMetadatas(onlyFields(IDENTIFIER)).sortDesc(Schemas.CREATED_ON));
		List<String> finishedStatusesAndClosedStatus = new ArrayList<>();
		for (Record finishedStatus : finishedStatusesRecords) {
			finishedStatusesAndClosedStatus.add(finishedStatus.getId());
		}
		finishedStatusesAndClosedStatus.add(getClosedStatus().getId());*/
		return new LogicalSearchQuery(
				from(tasksSchemas.userTask.schema()).where(tasksSchemas.userTask.status()).isEqualTo(getClosedStatus().getId()))
				.filteredWithUser(currentUser).sortDesc(Schemas.CREATED_ON);
	}

	public TaskStatus getClosedStatus() {
		return tasksSchemas.getTaskStatusWithCode(CLOSED_CODE);
	}

	public TaskStatus getStandbyStatus() {
		return tasksSchemas.getTaskStatusWithCode(STANDBY_CODE);
	}

	public TaskStatus getFirstFinishedStatus() {
		LogicalSearchQuery firstClosedTaskStatusQuery = new LogicalSearchQuery(
				from(tasksSchemas.ddvTaskStatus.schema()).where(tasksSchemas.ddvTaskStatus.statusType())
						.is(FINISHED)).sortDesc(tasksSchemas.ddvTaskStatus.createdOn()).setNumberOfRows(1);
		List<Record> result = searchServices.search(firstClosedTaskStatusQuery);
		if (result.isEmpty()) {
			return null;
		}
		return tasksSchemas.wrapTaskStatus(result.get(0));
	}

}
