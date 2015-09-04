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
package com.constellio.app.modules.tasks.model.wrappers.types;

import static com.constellio.app.modules.tasks.model.wrappers.TaskStatusType.CLOSED;
import static com.constellio.app.modules.tasks.model.wrappers.TaskStatusType.FINISHED;
import static com.constellio.app.modules.tasks.model.wrappers.TaskStatusType.IN_PROGRESS;
import static com.constellio.app.modules.tasks.model.wrappers.TaskStatusType.STANDBY;
import static com.constellio.app.modules.tasks.model.wrappers.types.TaskStatus.CLOSED_CODE;
import static com.constellio.app.modules.tasks.model.wrappers.types.TaskStatus.STANDBY_CODE;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.tasks.extensions.TaskStatusSchemasExtension.AtLeastOneRecordWithStatusRuntimeException;
import com.constellio.app.modules.tasks.model.wrappers.TaskStatusType;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;

public class TaskStatusAcceptanceTest extends ConstellioTest {
	Users users = new Users();
	RecordServices recordServices;
	SearchServices searchServices;
	private TasksSchemasRecordsServices tasksSchemas;

	@Before
	public void setUp()
			throws Exception {
		givenCollection(zeCollection).withTaskModule().withAllTestUsers();
		users.setUp(getModelLayerFactory().newUserServices());

		recordServices = getModelLayerFactory().newRecordServices();
		tasksSchemas = new TasksSchemasRecordsServices(zeCollection, getAppLayerFactory());
		searchServices = getModelLayerFactory().newSearchServices();
	}

	@Test(expected = RecordServicesException.ValidationException.class)
	public void whenAddClosedStatusThenValidationException()
			throws Exception {
		recordServices.add(tasksSchemas.newTaskStatus().setStatusType(CLOSED).setCode("newClosed").setTitle("newClosedTitle"));
	}

	@Test(expected = RecordServicesException.ValidationException.class)
	public void whenAddNullStatusTypeWithClosedCodeThenValidationException()
			throws Exception {
		recordServices.add(tasksSchemas.newTaskStatus().setStatusType(null).setCode(CLOSED_CODE).setTitle("newClosedTitle"));
	}

	@Test(expected = AtLeastOneRecordWithStatusRuntimeException.class)
	public void whenClosedStatusDeletedThenException()
			throws Exception {
		List<TaskStatus> statuses = getAllStatusWithType(CLOSED);
		assertThat(statuses.size()).isEqualTo(1);
		recordServices.logicallyDelete(statuses.get(0).getWrappedRecord(), null);
	}

	@Test(expected = RecordServicesException.ValidationException.class)
	public void whenAddStandByStatusThenValidationException()
			throws Exception {
		recordServices.add(tasksSchemas.newTaskStatus().setStatusType(STANDBY).setCode("newStandBy").setTitle("newStandBy"));
	}

	@Test(expected = RecordServicesException.ValidationException.class)
	public void whenAddNullStatusTypeWithStandByCodeThenValidationException()
			throws Exception {
		recordServices.add(tasksSchemas.newTaskStatus().setStatusType(null).setCode(STANDBY_CODE).setTitle("newClosedTitle"));
	}

	@Test(expected = AtLeastOneRecordWithStatusRuntimeException.class)
	public void whenStandByStatusDeletedThenException()
			throws Exception {
		List<TaskStatus> statuses = getAllStatusWithType(STANDBY);
		assertThat(statuses.size()).isEqualTo(1);
		recordServices.logicallyDelete(statuses.get(0).getWrappedRecord(), null);
	}

	@Test
	public void givenTwoFinishedStatusWhenAllFinishedStatusDeletedThenExceptionForTheLastDelete()
			throws Exception {
		recordServices
				.add(tasksSchemas.newTaskStatus().setStatusType(FINISHED).setCode("newFinished").setTitle("newFinishedTitle"));
		List<TaskStatus> statuses = getAllStatusWithType(FINISHED);
		for (int i = 0; i < statuses.size() - 1; i++) {
			TaskStatus status = statuses.get(i);
			recordServices.logicallyDelete(status.getWrappedRecord(), null);
		}
		try {
			recordServices.logicallyDelete(statuses.get(statuses.size() - 1).getWrappedRecord(), null);
		} catch (AtLeastOneRecordWithStatusRuntimeException e) {
			//OK
		}
	}

	@Test
	public void givenTwoInProgressStatusWhenAllInProgressStatusDeletedThenException()
			throws Exception {
		recordServices
				.add(tasksSchemas.newTaskStatus().setStatusType(IN_PROGRESS).setCode("newInProgress").setTitle("newInProgress"));
		List<TaskStatus> statuses = getAllStatusWithType(IN_PROGRESS);
		for (int i = 0; i < statuses.size() - 1; i++) {
			TaskStatus status = statuses.get(i);
			recordServices.logicallyDelete(status.getWrappedRecord(), null);
		}
		try {
			recordServices.logicallyDelete(statuses.get(statuses.size() - 1).getWrappedRecord(), null);
		} catch (AtLeastOneRecordWithStatusRuntimeException e) {
			//OK
		}
	}

	private List<TaskStatus> getAllStatusWithType(TaskStatusType type) {
		LogicalSearchQuery query = new LogicalSearchQuery(LogicalSearchQueryOperators.from(tasksSchemas.ddvTaskStatus.schema())
				.where(tasksSchemas.ddvTaskStatus.statusType()).isEqualTo(type));
		return tasksSchemas.wrapTaskStatuss(searchServices.search(query));
	}
}
