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

import static com.constellio.app.modules.tasks.model.wrappers.types.TaskStatus.CLOSED_CODE;
import static com.constellio.sdk.tests.TestUtils.asList;
import static com.constellio.sdk.tests.TestUtils.assertThatRecord;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.model.wrappers.types.TaskStatus;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.global.GlobalGroup;
import com.constellio.model.entities.security.global.GlobalGroupStatus;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;

public class TasksSearchServicesAcceptanceTest extends ConstellioTest {
	TasksSearchServices tasksSearchServices;

	Users users = new Users();
	RecordServices recordServices;
	SearchServices searchServices;
	private LocalDate now = LocalDate.now();
	private Task taskAssignedByChuckToAlice;
	private Task taskAssignedByChuckToAliceClosed;
	private Task nonAssignedTaskCreatedByChuck;
	private Task nonAssignedTaskClosed;
	private Task subTaskOfNonAssignedTaskClosed;
	private Task taskAssignedByBobToChuckClosed;
	private Task taskWithDakotaInAssignationUsersCandidates;
	private Task taskWithCharlesInAssignationGroupCandidates;
	private TasksSchemasRecordsServices tasksSchemas;

	private User alice;
	private User bob;
	private User chuck;
	private User dakotaIndien;
	private User charles;
	private UserServices userServices;

	@Before
	public void setUp()
			throws Exception {
		givenTimeIs(now);
		givenCollection(zeCollection).withTaskModule().withAllTestUsers().andUsersWithWriteAndDeleteAccess(admin);
		users.setUp(getModelLayerFactory().newUserServices());

		recordServices = getModelLayerFactory().newRecordServices();
		tasksSchemas = new TasksSchemasRecordsServices(zeCollection, getAppLayerFactory());

		searchServices = getModelLayerFactory().newSearchServices();
		TasksSearchServices taskSearchServices = new TasksSearchServices(tasksSchemas);
		tasksSearchServices = new TasksSearchServices(tasksSchemas);

		bob = users.bobIn(zeCollection);
		alice = users.aliceIn(zeCollection);
		chuck = users.chuckNorrisIn(zeCollection);
		dakotaIndien = users.dakotaIn(zeCollection);
		charles = users.charlesIn(zeCollection);

		TaskStatus closedStatus = tasksSchemas.getTaskStatusWithCode(CLOSED_CODE);

		Transaction transaction = new Transaction();
		taskAssignedByChuckToAlice = tasksSchemas.newTask().setTitle("taskAssignedByChuckToAlice").setAssigner(chuck.getId())
				.setAssignationDate(now).setAssignee(alice.getId());
		transaction.add(taskAssignedByChuckToAlice.setCreatedBy(chuck.getId()));

		taskAssignedByChuckToAliceClosed = tasksSchemas.newTask().setTitle("taskAssignedByChuckToAliceClosed")
				.setAssigner(chuck.getId())
				.setAssignationDate(now).setAssignee(alice.getId()).setStatus(closedStatus.getId());
		transaction.add(taskAssignedByChuckToAliceClosed.setCreatedBy(chuck.getId()));

		nonAssignedTaskCreatedByChuck = tasksSchemas.newTask().setTitle("nonAssignedTaskCreatedByChuck");
		transaction.add(nonAssignedTaskCreatedByChuck.setCreatedBy(chuck.getId()));

		nonAssignedTaskClosed = tasksSchemas.newTask().setTitle("nonAssignedTaskClosed").setStatus(closedStatus.getId());
		transaction.add(nonAssignedTaskClosed);

		subTaskOfNonAssignedTaskClosed = tasksSchemas.newTask().setTitle("subTaskOfNonAssignedTaskClosed")
				.setParentTask(nonAssignedTaskClosed.getId());
		transaction.add(subTaskOfNonAssignedTaskClosed
		);

		taskAssignedByBobToChuckClosed = tasksSchemas.newTask().setTitle("taskAssignedByBobToChuckClosed")
				.setAssigner(bob.getId())
				.setAssignationDate(now).setAssignee(chuck.getId())
				.setStatus(taskSearchServices.getClosedStatus().getId());
		transaction.add(taskAssignedByBobToChuckClosed.setCreatedBy(bob.getId()));

		String sasquatchId = users.sasquatchIn(zeCollection).getId();
		taskWithDakotaInAssignationUsersCandidates = tasksSchemas.newTask().setTitle("taskWithDakotaInAssignationUsersCandidates")
				.setAssigner(sasquatchId)
				.setAssignationDate(now).setAssigneeUsersCandidates(asList(dakotaIndien.getId()));
		transaction.add(taskWithDakotaInAssignationUsersCandidates);

		userServices = getModelLayerFactory().newUserServices();

		String newGlobalGroup = "newGlobalGroup";
		addGroup(newGlobalGroup);

		String charlesNewGlobalGroup = "charlesNewGlobalGroup";
		addGroup(charlesNewGlobalGroup);

		String taskNewGlobalGroup = "taskNewGlobalGroup";
		addGroup(taskNewGlobalGroup);

		Group newGroup = userServices.getGroupInCollection(newGlobalGroup, zeCollection);
		Group taskNewGroup = userServices.getGroupInCollection(taskNewGlobalGroup, zeCollection);
		userServices.addUpdateUserCredential(users.charles().withGlobalGroups(asList(newGlobalGroup, charlesNewGlobalGroup)));
		taskWithCharlesInAssignationGroupCandidates = tasksSchemas.newTask()
				.setTitle("taskWithCharlesInAssignationGroupCandidates")
				.setAssigner(sasquatchId)
				.setAssignationDate(now).setAssigneeGroupsCandidates(asList(newGroup.getId(), taskNewGroup.getId()));
		transaction.add(taskWithCharlesInAssignationGroupCandidates);

		recordServices.execute(transaction);
	}

	private void addGroup(String groupCode) {
		List<String> usersAutomaticallyAddedToCollections = new ArrayList<>();
		GlobalGroup group = new GlobalGroup(groupCode, groupCode, usersAutomaticallyAddedToCollections, null,
				GlobalGroupStatus.ACTIVE);
		userServices.addUpdateGlobalGroup(group);
	}

	@Test
	public void whenSearchTasksAssignedByChuckThenReturnTaskAssignedByChuckToAlice()
			throws Exception {
		List<Record> results = searchServices
				.search(tasksSearchServices.getTasksAssignedByUserQuery(chuck));
		assertThat(results.size()).isEqualTo(1);
		assertThatRecord(results.get(0)).hasMetadataValue(Schemas.TITLE, "taskAssignedByChuckToAlice");
	}

	@Test
	public void whenSearchNonAssignedTasksThenReturnNonAssignedTasksCreatedByChuckToChuckAndNothingToBob()
			throws Exception {
		List<Record> results = searchServices
				.search(tasksSearchServices.getNonAssignedTasksQuery(chuck));
		assertThat(results.size()).isEqualTo(1);
		assertThatRecord(results.get(0)).hasMetadataValue(Schemas.TITLE, "nonAssignedTaskCreatedByChuck");
		results = searchServices
				.search(tasksSearchServices.getNonAssignedTasksQuery(bob));
		assertThat(results.size()).isEqualTo(0);
	}

	@Test
	public void whenSearchTasksAssignedToAliceThenReturnTaskAssignedByChuckToAlice()
			throws Exception {
		List<Record> results = searchServices
				.search(tasksSearchServices.getTasksAssignedToUserQuery(alice));
		assertThat(results.size()).isEqualTo(1);
		assertThatRecord(results.get(0)).hasMetadataValue(Schemas.TITLE, "taskAssignedByChuckToAlice");
	}

	@Test
	public void whenSearchTasksAssignedToDakotaThenReturnTaskWithDakotaInAssignationUsersCandidates()
			throws Exception {
		List<Record> results = searchServices
				.search(tasksSearchServices.getTasksAssignedToUserQuery(dakotaIndien));
		assertThat(results.size()).isEqualTo(1);
		assertThatRecord(results.get(0)).hasMetadataValue(Schemas.TITLE, "taskWithDakotaInAssignationUsersCandidates");
	}

	@Test
	public void whenSearchTasksAssignedToCharlesThenReturnTaskWithCharlesInAssignationGroupCandidates()
			throws Exception {
		List<Record> results = searchServices
				.search(tasksSearchServices.getTasksAssignedToUserQuery(charles));
		assertThat(results.size()).isEqualTo(1);
		assertThatRecord(results.get(0)).hasMetadataValue(Schemas.TITLE, "taskWithCharlesInAssignationGroupCandidates");
	}

	@Test
	public void givenBobWhenSearchRecentlyCompletedTasksThenReturnCompletedAndClosedTasksVisibleToBob()
			throws Exception {
		List<Record> results = searchServices
				.search(tasksSearchServices.getRecentlyCompletedTasks(bob));
		assertThat(results.size()).isEqualTo(1);
		assertThatRecord(results.get(0)).hasMetadataValue(Schemas.TITLE, "taskAssignedByBobToChuckClosed");
	}

	@Test
	public void whenSearchRecentlyCompletedTasksByChuckThenThenReturnAllCompletedAndClosedTasksVisibleToChuck()
			throws Exception {
		List<Record> results = searchServices
				.search(tasksSearchServices.getRecentlyCompletedTasks(chuck));
		assertThat(results.size()).isEqualTo(2);
	}

	@Test
	public void whenSearchDirectSubTasksThenOk()
			throws Exception {
		Task taskCreatedByChuckHavingSubTaskCreatedByAlice = tasksSchemas.newTask().setTitle("zeTask");
		Transaction transaction = new Transaction();
		transaction.add(taskCreatedByChuckHavingSubTaskCreatedByAlice.setCreatedBy(chuck.getId()));
		Task subTaskCreatedByAlice = tasksSchemas.newTask().setTitle("zeSubTask")
				.setParentTask(taskCreatedByChuckHavingSubTaskCreatedByAlice.getId());
		transaction.add(subTaskCreatedByAlice.setCreatedBy(alice.getId()));
		recordServices.execute(transaction);

		whenSearchSubTasksForBobThenNoTasFound(taskCreatedByChuckHavingSubTaskCreatedByAlice.getId());
		whenSearchSubTasksForUserThenOk(taskCreatedByChuckHavingSubTaskCreatedByAlice.getId(), alice);
		whenSearchSubTasksForUserThenOk(taskCreatedByChuckHavingSubTaskCreatedByAlice.getId(), chuck);
	}

	private void whenSearchSubTasksForBobThenNoTasFound(String taskId) {
		List<Record> results = searchServices
				.search(tasksSearchServices.getDirectSubTasks(taskId, bob));
		assertThat(results.size()).isEqualTo(0);
	}

	private void whenSearchSubTasksForUserThenOk(String taskId, User user) {
		List<Record> results = searchServices
				.search(tasksSearchServices.getDirectSubTasks(taskId, user));
		assertThat(results.size()).isEqualTo(1);
		assertThatRecord(results.get(0)).hasMetadataValue(Schemas.TITLE, "zeSubTask");
	}
}
