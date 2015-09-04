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

import static com.constellio.app.modules.tasks.model.wrappers.TaskStatusType.CLOSED;
import static com.constellio.app.modules.tasks.model.wrappers.types.TaskStatus.CLOSED_CODE;
import static com.constellio.app.modules.tasks.model.wrappers.types.TaskStatus.STANDBY_CODE;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.sdk.tests.TestUtils.asList;
import static com.constellio.sdk.tests.TestUtils.assertThatRecord;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.model.wrappers.structures.TaskFollower;
import com.constellio.app.modules.tasks.model.wrappers.types.TaskStatus;
import com.constellio.app.modules.tasks.ui.entities.TaskFollowerVO;
import com.constellio.app.modules.tasks.ui.entities.TaskReminderVO;
import com.constellio.app.modules.tasks.ui.entities.TaskVO;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.global.GlobalGroup;
import com.constellio.model.entities.security.global.GlobalGroupStatus;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;

public class TaskPresenterServicesAcceptanceTest extends ConstellioTest {
	TaskPresenterServices taskPresenterServices;

	Users users = new Users();
	RecordServices recordServices;
	SearchServices searchServices;
	private LocalDate now = LocalDate.now();
	private Task zeTask;
	private TasksSchemasRecordsServices tasksSchemas;

	private User aliceHasWriteAccessOnZeTask;
	private User bobHasReadAccessOnTask;
	private User chuckNorrisHasDeleteAccessOnTask;
	private User charlesWithNoAccessOnTask;
	@Mock
	private TaskVO taskVOMock;

	@Before
	public void setUp()
			throws Exception {
		givenTimeIs(now);
		givenCollection(zeCollection).withTaskModule().withAllTestUsers();
		users.setUp(getModelLayerFactory().newUserServices());

		recordServices = getModelLayerFactory().newRecordServices();
		tasksSchemas = new TasksSchemasRecordsServices(zeCollection, getAppLayerFactory());

		searchServices = getModelLayerFactory().newSearchServices();
		TasksSearchServices taskSearchServices = new TasksSearchServices(tasksSchemas);
		taskPresenterServices = new TaskPresenterServices(tasksSchemas, recordServices, taskSearchServices,
				getModelLayerFactory().newLoggingServices());

		bobHasReadAccessOnTask = users.bobIn(zeCollection);
		aliceHasWriteAccessOnZeTask = users.aliceIn(zeCollection);
		chuckNorrisHasDeleteAccessOnTask = users.chuckNorrisIn(zeCollection);
		charlesWithNoAccessOnTask = users.charlesIn(zeCollection);

		zeTask = tasksSchemas.newTask();
		recordServices.add(zeTask.setTitle("zeTitle")
				.setTaskFollowers(asList(new TaskFollower().setFollowerId(bobHasReadAccessOnTask.getId())))
				.setAssignee(aliceHasWriteAccessOnZeTask.getId()).setAssigner(chuckNorrisHasDeleteAccessOnTask.getId())
				.setAssignationDate(LocalDate.now())
				.setCreatedBy(chuckNorrisHasDeleteAccessOnTask.getId()));
		assertThat(bobHasReadAccessOnTask.hasReadAccess().on(zeTask)).isTrue();
		assertThat(aliceHasWriteAccessOnZeTask.hasWriteAccess().on(zeTask)).isTrue();
		assertThat(chuckNorrisHasDeleteAccessOnTask.hasDeleteAccess().on(zeTask)).isTrue();
		assertThat(charlesWithNoAccessOnTask.hasReadAccess().on(zeTask)).isFalse();
	}

	@Test
	public void whenCloseTaskThenTaskClosed()
			throws Exception {
		assertThat(zeTask.getStatus()).isEqualTo(tasksSchemas.getTaskStatusWithCode(STANDBY_CODE).getId());
		taskPresenterServices.closeTask(zeTask.getWrappedRecord(), aliceHasWriteAccessOnZeTask);
		assertThat(tasksSchemas.getTaskStatus(zeTask.getStatus()).getStatusType()).isEqualTo(CLOSED);
	}

	@Test
	public void whenDeleteTaskThenTaskDeletedCorrectly()
			throws Exception {
		taskPresenterServices.deleteTask(zeTask.getWrappedRecord(), chuckNorrisHasDeleteAccessOnTask);
		LogicalSearchCondition allTasksQuery = from(
				tasksSchemas.userTask.schema()).returnAll();
		assertThat(searchServices.getResultsCount(allTasksQuery)).isEqualTo(0);
	}

	@Test
	public void whenSendReminderTaskFixedReminderWithFixedNowDateCreated()
			throws Exception {
		taskPresenterServices.sendReminder(zeTask.getWrappedRecord(), chuckNorrisHasDeleteAccessOnTask);
		zeTask = tasksSchemas.getTask(zeTask.getId());
		assertThat(zeTask.getReminders().get(0).getFixedDate()).isEqualTo(now);
	}

	@Test
	public void whenAutoAssignTaskThenAssignationSetCorrectly()
			throws Exception {
		taskPresenterServices.autoAssignTask(zeTask.getWrappedRecord(), chuckNorrisHasDeleteAccessOnTask);
		zeTask = tasksSchemas.getTask(zeTask.getId());
		assertThat(zeTask.getAssignee()).isEqualTo(chuckNorrisHasDeleteAccessOnTask.getId());
		assertThat(zeTask.getAssigner()).isEqualTo(chuckNorrisHasDeleteAccessOnTask.getId());
		assertThat(zeTask.getAssignedOn()).isEqualTo(now);
	}

	@Test
	public void givenUserWithoutAccessOnTaskThenHasNoAccessOnTaskButtons()
			throws Exception {
		assertThat(taskPresenterServices.isEditTaskButtonVisible(zeTask.getWrappedRecord(), charlesWithNoAccessOnTask)).isFalse();
		assertThat(taskPresenterServices.isCompleteTaskButtonVisible(zeTask.getWrappedRecord(), charlesWithNoAccessOnTask))
				.isFalse();
		assertThat(taskPresenterServices.isCloseTaskButtonVisible(zeTask.getWrappedRecord(), charlesWithNoAccessOnTask))
				.isFalse();
		assertThat(taskPresenterServices.isSendReminderButtonVisible(zeTask.getWrappedRecord(), charlesWithNoAccessOnTask))
				.isFalse();
		assertThat(taskPresenterServices.isDeleteTaskButtonVisible(zeTask.getWrappedRecord(),
				charlesWithNoAccessOnTask)).isFalse();
	}

	@Test
	public void givenUserWithReadAccessOnTaskThenHasNoAccessOnTaskModificationButtons()
			throws Exception {
		assertThat(taskPresenterServices.isEditTaskButtonVisible(zeTask.getWrappedRecord(), bobHasReadAccessOnTask)).isFalse();
		assertThat(taskPresenterServices.isCompleteTaskButtonVisible(zeTask.getWrappedRecord(), bobHasReadAccessOnTask))
				.isFalse();
		assertThat(taskPresenterServices.isCloseTaskButtonVisible(zeTask.getWrappedRecord(), bobHasReadAccessOnTask))
				.isFalse();
		assertThat(taskPresenterServices.isSendReminderButtonVisible(zeTask.getWrappedRecord(), bobHasReadAccessOnTask))
				.isFalse();
		assertThat(taskPresenterServices.isDeleteTaskButtonVisible(zeTask.getWrappedRecord(),
				bobHasReadAccessOnTask)).isFalse();
	}

	@Test
	public void givenUserWithWriteAccessOnTaskThenCanEditAndCompleteTaskAndSendReminder()
			throws Exception {
		assertThat(taskPresenterServices.isEditTaskButtonVisible(zeTask.getWrappedRecord(), aliceHasWriteAccessOnZeTask))
				.isTrue();
		assertThat(taskPresenterServices.isCompleteTaskButtonVisible(zeTask.getWrappedRecord(), aliceHasWriteAccessOnZeTask))
				.isTrue();
		assertThat(taskPresenterServices.isSendReminderButtonVisible(zeTask.getWrappedRecord(), aliceHasWriteAccessOnZeTask))
				.isTrue();
		assertThat(taskPresenterServices.isCloseTaskButtonVisible(zeTask.getWrappedRecord(), aliceHasWriteAccessOnZeTask))
				.isFalse();
		assertThat(taskPresenterServices.isDeleteTaskButtonVisible(zeTask.getWrappedRecord(),
				aliceHasWriteAccessOnZeTask)).isFalse();
	}

	@Test
	public void givenUserWithWriteAccessOnTaskWhenTaskIsClosedThenCloseTaskButtonInvisible()
			throws Exception {
		recordServices.add(zeTask.setStatus(tasksSchemas.getTaskStatusWithCode(CLOSED_CODE).getId()));
		assertThat(taskPresenterServices.isCloseTaskButtonVisible(zeTask.getWrappedRecord(),
				aliceHasWriteAccessOnZeTask)).isFalse();
	}

	@Test
	public void givenUserWithWriteAccessOnTaskWhenTaskIsCompletedButNotAssignedThenCloseTaskButtonInvisible()
			throws Exception {
		recordServices.add(zeTask.setStatus(FIN()).setAssignee(null).setAssignationDate(null)
				.setAssigneeGroupsCandidates(null).setAssigneeUsersCandidates(null).setAssigner(null));
		zeTask = tasksSchemas.getTask(zeTask.getId());
		assertThat(chuckNorrisHasDeleteAccessOnTask.hasDeleteAccess().on(zeTask)).isTrue();
		assertThat(taskPresenterServices.isCloseTaskButtonVisible(zeTask.getWrappedRecord(),
				chuckNorrisHasDeleteAccessOnTask)).isFalse();
	}

	@Test
	public void givenUserWithWriteAccessOnTaskWhenTaskIsBeforeCompletedButNotAssignedThenCompleteTaskButtonInvisible()
			throws Exception {
		recordServices.add(zeTask.setAssignee(null).setAssignationDate(null)
				.setAssigneeGroupsCandidates(null).setAssigneeUsersCandidates(null).setAssigner(null));
		zeTask = tasksSchemas.getTask(zeTask.getId());
		assertThat(chuckNorrisHasDeleteAccessOnTask.hasDeleteAccess().on(zeTask)).isTrue();
		assertThat(taskPresenterServices.isCompleteTaskButtonVisible(zeTask.getWrappedRecord(),
				chuckNorrisHasDeleteAccessOnTask)).isFalse();
	}

	@Test
	public void givenUserWithWriteAccessOnTaskWhenTaskIsCompletedThenCloseTaskButtonVisible()
			throws Exception {
		recordServices.add(zeTask.setStatus(FIN()));
		assertThat(taskPresenterServices.isCloseTaskButtonVisible(zeTask.getWrappedRecord(),
				aliceHasWriteAccessOnZeTask)).isTrue();
	}

	@Test
	public void givenUserWithWriteAccessOnTaskWhenTaskIsCompletedThenCompleteTaskButtonInvisible()
			throws Exception {
		recordServices.add(zeTask.setStatus(FIN()));
		assertThat(taskPresenterServices.isCompleteTaskButtonVisible(zeTask.getWrappedRecord(),
				aliceHasWriteAccessOnZeTask)).isFalse();
	}

	@Test
	public void givenUserWithWriteAccessOnTaskWhenTaskIsNotAssignedThenAutoAssignTaskButtonVisible()
			throws Exception {
		recordServices.add(zeTask.setAssignee(null).setAssignationDate(null)
				.setAssigneeGroupsCandidates(null).setAssigneeUsersCandidates(null).setAssigner(null));
		zeTask = tasksSchemas.getTask(zeTask.getId());
		assertThat(chuckNorrisHasDeleteAccessOnTask.hasDeleteAccess().on(zeTask)).isTrue();
		assertThat(taskPresenterServices.isAutoAssignButtonEnabled(zeTask.getWrappedRecord(),
				chuckNorrisHasDeleteAccessOnTask)).isTrue();
	}

	@Test
	public void givenUserWithoutWriteAccessOnTaskWhenTaskIsNotAssignedThenAutoAssignTaskButtonInvisible()
			throws Exception {
		recordServices.add(zeTask.setAssignee(null).setAssignationDate(null)
				.setAssigneeGroupsCandidates(null).setAssigneeUsersCandidates(null).setAssigner(null));
		zeTask = tasksSchemas.getTask(zeTask.getId());
		assertThat(bobHasReadAccessOnTask.hasWriteAccess().on(zeTask)).isFalse();
		assertThat(taskPresenterServices.isAutoAssignButtonEnabled(zeTask.getWrappedRecord(),
				bobHasReadAccessOnTask)).isFalse();
	}

	@Test
	public void givenUserWithWriteAccessOnTaskWhenTaskIsAssignedThenAutoAssignTaskButtonInvisible()
			throws Exception {
		zeTask = tasksSchemas.getTask(zeTask.getId());
		assertThat(chuckNorrisHasDeleteAccessOnTask.hasDeleteAccess().on(zeTask)).isTrue();
		assertThat(taskPresenterServices.isAutoAssignButtonEnabled(zeTask.getWrappedRecord(),
				chuckNorrisHasDeleteAccessOnTask)).isFalse();
	}

	public String FIN() {
		TaskStatus frenchType = tasksSchemas.getTaskStatusWithCode("TER");
		assertThat(frenchType.isFinished()).isTrue();
		return frenchType.getId();
	}

	@Test
	public void givenUserWithDeleteAccessOnTaskThenCanEditAndDeleteTaskAndSendReminder()
			throws Exception {
		assertThat(taskPresenterServices.isEditTaskButtonVisible(zeTask.getWrappedRecord(), chuckNorrisHasDeleteAccessOnTask))
				.isTrue();
		assertThat(taskPresenterServices.isSendReminderButtonVisible(zeTask.getWrappedRecord(), chuckNorrisHasDeleteAccessOnTask))
				.isTrue();
		assertThat(taskPresenterServices.isDeleteTaskButtonVisible(zeTask.getWrappedRecord(),
				chuckNorrisHasDeleteAccessOnTask)).isTrue();
		assertThat(taskPresenterServices.isCompleteTaskButtonVisible(zeTask.getWrappedRecord(), chuckNorrisHasDeleteAccessOnTask))
				.isFalse();
		assertThat(taskPresenterServices.isCloseTaskButtonVisible(zeTask.getWrappedRecord(), chuckNorrisHasDeleteAccessOnTask))
				.isFalse();
	}

	@Test
	public void givenTaskWithDueDateNotSetThenIsFinishedOnTime()
			throws Exception {
		when(taskVOMock.getDueDate()).thenReturn(null);
		assertThat(taskPresenterServices.isTaskOverDue(taskVOMock)).isFalse();
	}

	@Test
	public void givenTaskWithEndDateAfterDueDateThenIsNotFinishedOnTime()
			throws Exception {
		when(taskVOMock.getDueDate()).thenReturn(now.minusDays(2));
		when(taskVOMock.getEndDate()).thenReturn(now.minusDays(1));
		assertThat(taskPresenterServices.isTaskOverDue(taskVOMock)).isTrue();
	}

	@Test
	public void givenTaskWithEndDateEqualsDueDateThenIsFinishedOnTime()
			throws Exception {
		when(taskVOMock.getDueDate()).thenReturn(now);
		when(taskVOMock.getEndDate()).thenReturn(now);
		assertThat(taskPresenterServices.isTaskOverDue(taskVOMock)).isFalse();
	}

	@Test
	public void givenTaskWithNowDueDateAndNullEndDateThenWhenIsNotFinishedOnTimeThenFalse()
			throws Exception {
		when(taskVOMock.getDueDate()).thenReturn(now);
		when(taskVOMock.getEndDate()).thenReturn(null);
		assertThat(taskPresenterServices.isTaskOverDue(taskVOMock)).isFalse();
	}

	@Test
	public void givenTaskWithEndDateBeforeDueDateThenIsFinishedOnTime()
			throws Exception {
		when(taskVOMock.getDueDate()).thenReturn(now.plusDays(1));
		when(taskVOMock.getEndDate()).thenReturn(now);
		assertThat(taskPresenterServices.isTaskOverDue(taskVOMock)).isFalse();
	}

	@Test
	public void givenTaskWithFixedReminderDateWhenToTaskThenRemindersSetCorrectly()
			throws Exception {
		when(taskVOMock.getTitle()).thenReturn("zeTitle");
		TaskReminderVO taskReminderWithFixedDate = new TaskReminderVO();
		taskReminderWithFixedDate.setFixedDate(now.plusDays(2));
		when(taskVOMock.getReminders()).thenReturn(asList(taskReminderWithFixedDate));
		Task task = taskPresenterServices.toTask(taskVOMock, tasksSchemas.newTask().getWrappedRecord());
		assertThat(task.getReminders().size()).isEqualTo(1);
		assertThat(task.getReminders().get(0).getFixedDate()).isEqualTo(now.plusDays(2));
	}

	@Test
	public void givenTaskWithFollowerWhenToTaskThenFollowersSetCorrectly()
			throws Exception {
		when(taskVOMock.getTitle()).thenReturn("zeTitle");
		TaskFollowerVO followerVO = new TaskFollowerVO();
		followerVO.setFollowerId(users.adminIn(zeCollection).getId());
		followerVO.setFollowTaskCompleted(true);
		when(taskVOMock.getTaskFollowers()).thenReturn(asList(followerVO));
		Task task = taskPresenterServices.toTask(taskVOMock, tasksSchemas.newTask().getWrappedRecord());
		assertThat(task.getTaskFollowers().size()).isEqualTo(1);
		TaskFollower taskFollower = task.getTaskFollowers().get(0);
		assertThat(taskFollower.getFollowerId()).isEqualTo(followerVO.getFollowerId());
		assertThat(taskFollower.getFollowTaskCompleted()).isTrue();
	}

	@Test
	public void givenTaskNotAssignedWhenIsAssignedToUserThenFalse()
			throws Exception {
		recordServices.update(zeTask.setAssignee(null).setAssignationDate(null)
				.setAssigneeGroupsCandidates(null).setAssigneeUsersCandidates(null).setAssigner(null));
		zeTask = tasksSchemas.getTask(zeTask.getId());
		assertThat(taskPresenterServices.isAssignedToUser(zeTask.getWrappedRecord(), aliceHasWriteAccessOnZeTask)).isFalse();
	}

	@Test
	public void givenTaskAssignedToAliceWhenIsAssignedToAliceThenOk()
			throws Exception {
		recordServices.update(zeTask.setAssignee(aliceHasWriteAccessOnZeTask.getId()).setAssigneeGroupsCandidates(
				null).setAssigneeUsersCandidates(null));
		zeTask = tasksSchemas.getTask(zeTask.getId());
		assertThat(taskPresenterServices.isAssignedToUser(zeTask.getWrappedRecord(), aliceHasWriteAccessOnZeTask)).isTrue();
	}

	@Test
	public void givenTaskAssignedToAliceAndBobWhenIsAssignedToAliceThenOk()
			throws Exception {
		recordServices.update(zeTask.setAssigneeUsersCandidates(
				asList(aliceHasWriteAccessOnZeTask.getId(), bobHasReadAccessOnTask.getId()))
				.setAssigneeUsersCandidates(null).setAssigneeGroupsCandidates(null));
		zeTask = tasksSchemas.getTask(zeTask.getId());
		assertThat(taskPresenterServices.isAssignedToUser(zeTask.getWrappedRecord(), aliceHasWriteAccessOnZeTask)).isTrue();
	}

	@Test
	public void givenTaskAssignedToAliceGroupWhenIsAssignedToAliceThenOk()
			throws Exception {
		UserServices userServices = getModelLayerFactory().newUserServices();
		String newGlobalGroup = "newGlobalGroup";
		addGroup(newGlobalGroup);

		String aliceNewGlobalGroup = "aliceNewGlobalGroup";
		addGroup(aliceNewGlobalGroup);

		String taskNewGlobalGroup = "taskNewGlobalGroup";
		addGroup(taskNewGlobalGroup);

		Group newGroup = userServices.getGroupInCollection(newGlobalGroup, zeCollection);
		Group taskNewGroup = userServices.getGroupInCollection(taskNewGlobalGroup, zeCollection);
		userServices.addUpdateUserCredential(users.alice().withGlobalGroups(asList(newGlobalGroup, aliceNewGlobalGroup)));

		recordServices.update(zeTask.setAssigneeGroupsCandidates(asList(newGroup.getId(), taskNewGroup.getId()))
						.setAssignee(null).setAssigneeUsersCandidates(null)
		);
		zeTask = tasksSchemas.getTask(zeTask.getId());
		assertThat(taskPresenterServices.isAssignedToUser(zeTask.getWrappedRecord(), aliceHasWriteAccessOnZeTask)).isTrue();
	}

	private void addGroup(String groupCode) {
		List<String> usersAutomaticallyAddedToCollections = new ArrayList<>();
		GlobalGroup group = new GlobalGroup(groupCode, groupCode, usersAutomaticallyAddedToCollections, null,
				GlobalGroupStatus.ACTIVE);
		getModelLayerFactory().newUserServices().addUpdateGlobalGroup(group);
	}
}
