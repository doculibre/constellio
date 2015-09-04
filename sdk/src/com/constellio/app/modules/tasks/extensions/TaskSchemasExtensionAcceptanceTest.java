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

import static com.constellio.app.modules.tasks.model.wrappers.types.TaskStatus.STANDBY_CODE;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.tasks.TasksEmailTemplates;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.model.wrappers.structures.TaskFollower;
import com.constellio.app.modules.tasks.model.wrappers.structures.TaskReminder;
import com.constellio.app.modules.tasks.model.wrappers.types.TaskStatus;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.EmailToSend;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;

import static com.constellio.app.modules.tasks.model.wrappers.types.TaskStatus.CLOSED_CODE;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static org.assertj.core.api.Assertions.assertThat;

public class TaskSchemasExtensionAcceptanceTest extends ConstellioTest {
	private TaskSchemasExtension taskSchemasExtension;

	Users users = new Users();
	RecordServices recordServices;
	private LocalDateTime now = LocalDateTime.now();
	private Task zeTask;
	private Task validParentTaskFollowingSubTasks;
	private TasksSchemasRecordsServices tasksSchemas;
	private SearchServices searchServices;
	private MetadataSchema emailToSendSchema;
	private List<TaskFollower> zeFollowers;
	private TaskFollower zeTaskStatusModificationFollower;
	private TaskFollower zeTaskDeletionFollower;
	private TaskFollower zeTaskFinishedEventFollower;
	private TaskFollower zeTaskAssigneeModificationFollower;
	private TaskFollower zeTaskSubTasksModificationFollower;
	private LocalDate newStartDate = LocalDate.now().minusDays(2);
	private TaskReminder processedReminderWithRelativeDateBeforeStartDate_0;
	private TaskReminder processedReminderWithRelativeDateAfterStartDate_1;
	private TaskReminder processedReminderWithFixedDateAfterNewStartDate_3;
	private TaskReminder processedReminderWithRelativeDateEqualsStartDate_2;
	private TaskReminder processedReminderWithRelativeDateAfterEndDate_4;

	@Before
	public void setUp()
			throws Exception {
		givenTimeIs(now);
		givenCollection(zeCollection).withTaskModule().withAllTestUsers();
		users.setUp(getModelLayerFactory().newUserServices());

		recordServices = getModelLayerFactory().newRecordServices();
		searchServices = getModelLayerFactory().newSearchServices();
		tasksSchemas = new TasksSchemasRecordsServices(zeCollection, getAppLayerFactory());
		emailToSendSchema = tasksSchemas.emailToSend();
		taskSchemasExtension = new TaskSchemasExtension(zeCollection, getAppLayerFactory());
		initTasks();

	}

	private void initTasks()
			throws RecordServicesException {
		zeTaskDeletionFollower = new TaskFollower().setFollowerId(users.aliceIn(zeCollection).getId())
				.setFollowTaskDeleted(true);
		zeTaskFinishedEventFollower = new TaskFollower().setFollowerId(users.charlesIn(zeCollection).getId())
				.setFollowTaskCompleted(true);
		zeTaskStatusModificationFollower = new TaskFollower().setFollowerId(users.chuckNorrisIn(zeCollection).getId())
				.setFollowTaskStatusModified(true);
		zeTaskAssigneeModificationFollower = new TaskFollower().setFollowerId(users.bobIn(zeCollection).getId())
				.setFollowTaskAssigneeModified(true);
		zeTaskSubTasksModificationFollower = new TaskFollower().setFollowerId(users.adminIn(zeCollection).getId())
				.setFollowSubTasksModified(true);
		zeFollowers = new ArrayList<>(asList(zeTaskDeletionFollower, zeTaskFinishedEventFollower,
				zeTaskStatusModificationFollower, zeTaskAssigneeModificationFollower, zeTaskSubTasksModificationFollower));

		validParentTaskFollowingSubTasks = tasksSchemas.newTask().setTitle("parentTask");
		validParentTaskFollowingSubTasks.setTaskFollowers(asList(zeTaskSubTasksModificationFollower));
		recordServices.add(validParentTaskFollowingSubTasks);

		zeTask = tasksSchemas.newTask();
		recordServices.add(zeTask.setTitle("taskTitle")
						.setTaskFollowers(zeFollowers)
						.setAssignee(users.chuckNorrisIn(zeCollection).getId())
						.setAssigner(users.aliceIn(zeCollection).getId())
						.setAssignationDate(now.toLocalDate())
						.setParentTask(validParentTaskFollowingSubTasks.getId())
		);
		recordServices.flush();

	}

	@Test
	public void givenTaskStatusModifiedToCompletedThenOneValidEmailToSendCreatedAndTaskEndDateSetToNow()
			throws RecordServicesException {
		recordServices.add(zeTask.setStatus(FIN()));
		recordServices.flush();
		EmailToSend emailToSend = getEmailToSend();
		assertThat(emailToSend).isNotNull();
		assertThat(emailToSend.getTemplate()).isEqualTo(TasksEmailTemplates.TASK_COMPLETED);
		assertThat(emailToSend.getParameters().get(0))
				.isEqualTo(TasksEmailTemplates.TASK_TITLE_PARAMETER + ":" + zeTask.getTitle());
		assertThat(emailToSend.getFrom()).isNull();
		assertThat(emailToSend.getSendOn()).isEqualTo(now);
		assertThat(emailToSend.getTo().size()).isEqualTo(1);
		assertThat(emailToSend.getTo().get(0).getEmail()).isEqualTo(getUserEmail(zeTaskFinishedEventFollower.getFollowerId()));
		assertThat(tasksSchemas.getTask(zeTask.getId()).getEndDate()).isEqualTo(now.toLocalDate());
	}

	@Test
	public void givenTaskWithCompletedStatusWhenStatusSetToCompletedThenEndDateNotModified()
			throws RecordServicesException {
		recordServices.add(zeTask.setStatus(FIN()));
		assertThat(tasksSchemas.getTask(zeTask.getId()).getEndDate()).isEqualTo(now.toLocalDate());
		givenTimeIs(now.plusDays(1));
		recordServices.add(zeTask.setStatus(FIN()));
		assertThat(tasksSchemas.getTask(zeTask.getId()).getEndDate()).isEqualTo(now.toLocalDate());
	}

	@Test
	public void givenTaskStandbyStatusWhenStatusSetToCompletedThenEndDateSetCorrectly()
			throws RecordServicesException {
		recordServices.add(zeTask.setStatus(STB()));
		assertThat(tasksSchemas.getTask(zeTask.getId()).getEndDate()).isEqualTo(null);
		givenTimeIs(now.plusDays(1));
		recordServices.add(zeTask.setStatus(FIN()));
		assertThat(tasksSchemas.getTask(zeTask.getId()).getEndDate()).isEqualTo(now.plusDays(1).toLocalDate());
	}

	@Test
	public void givenTaskStandbyStatusWhenStatusSetToAfterCompletedThenEndDateSetCorrectly()
			throws RecordServicesException {
		recordServices.add(zeTask.setStatus(STB()));
		assertThat(tasksSchemas.getTask(zeTask.getId()).getEndDate()).isEqualTo(null);
		givenTimeIs(now);
		recordServices.add(zeTask.setStatus(AFTER_FIN()));
		assertThat(tasksSchemas.getTask(zeTask.getId()).getEndDate()).isEqualTo(now.toLocalDate());
	}

	@Test
	public void givenTaskBeforeCompletedStatusWhenStatusSetToAfterCompletedThenEndDateSetCorrectly()
			throws RecordServicesException {
		recordServices.add(zeTask.setStatus(INP()));
		assertThat(tasksSchemas.getTask(zeTask.getId()).getEndDate()).isEqualTo(null);
		givenTimeIs(now);
		recordServices.add(zeTask.setStatus(AFTER_FIN()));
		assertThat(tasksSchemas.getTask(zeTask.getId()).getEndDate()).isEqualTo(now.toLocalDate());
	}

	@Test
	public void whenStatusSetToNullThenEndDateSetToNull()
			throws RecordServicesException {
		givenStandbyStatusWhenStatusSetToNullThenEndDateSetToNull();
		givenCompletedStatusWhenStatusSetToStandbyThenEndDateSetToNull();
		givenUnCompletedStatusWhenStatusSetToStandbyThenEndDateSetToNull();
		givenAfterCompletedStatusWhenStatusSetToStandbyThenEndDateSetToNull();
	}

	private void givenAfterCompletedStatusWhenStatusSetToStandbyThenEndDateSetToNull()
			throws RecordServicesException {
		givenTimeIs(now);
		recordServices.add(zeTask.setStatus(AFTER_FIN()));
		assertThat(tasksSchemas.getTask(zeTask.getId()).getEndDate()).isEqualTo(now.toLocalDate());
		recordServices.add(zeTask.setStatus(STB()));
		assertThat(tasksSchemas.getTask(zeTask.getId()).getEndDate()).isEqualTo(null);
	}

	private void givenUnCompletedStatusWhenStatusSetToStandbyThenEndDateSetToNull()
			throws RecordServicesException {
		givenTimeIs(now);
		recordServices.add(zeTask.setStatus(FIN()));
		assertThat(tasksSchemas.getTask(zeTask.getId()).getEndDate()).isEqualTo(now.toLocalDate());
		recordServices.add(zeTask.setStatus(STB()));
		assertThat(tasksSchemas.getTask(zeTask.getId()).getEndDate()).isEqualTo(null);
	}

	private void givenCompletedStatusWhenStatusSetToStandbyThenEndDateSetToNull()
			throws RecordServicesException {
		givenTimeIs(now.plusDays(1));
		recordServices.add(zeTask.setStatus(FIN()));
		assertThat(tasksSchemas.getTask(zeTask.getId()).getEndDate()).isEqualTo(now.plusDays(1).toLocalDate());
		recordServices.add(zeTask.setStatus(STB()));
		assertThat(tasksSchemas.getTask(zeTask.getId()).getEndDate()).isEqualTo(null);
	}

	private void givenStandbyStatusWhenStatusSetToNullThenEndDateSetToNull()
			throws RecordServicesException {
		recordServices.add(zeTask.setStatus(STB()));
		assertThat(tasksSchemas.getTask(zeTask.getId()).getEndDate()).isEqualTo(null);
	}

	@Test
	public void givenTaskWithUnCompletedStatusWhenStatusSetToCompletedThenEndDateSetCorrectly()
			throws RecordServicesException {
		recordServices.add(zeTask.setStatus(INP()));
		assertThat(tasksSchemas.getTask(zeTask.getId()).getEndDate()).isEqualTo(null);
		givenTimeIs(now.plusDays(1));
		recordServices.add(zeTask.setStatus(FIN()));
		assertThat(tasksSchemas.getTask(zeTask.getId()).getEndDate()).isEqualTo(now.plusDays(1).toLocalDate());
	}

	@Test
	public void givenTaskWithCompletedStatusWhenStatusSetToBeforeFinishedThenEndDateSetToNull()
			throws RecordServicesException {
		recordServices.add(zeTask.setStatus(FIN()));
		assertThat(tasksSchemas.getTask(zeTask.getId()).getEndDate()).isEqualTo(now.toLocalDate());
		givenTimeIs(now.plusDays(1));
		recordServices.add(zeTask.setStatus((INP())));
		assertThat(tasksSchemas.getTask(zeTask.getId()).getEndDate()).isEqualTo(null);
	}

	@Test
	public void givenTaskCreatedWithCompletedStatusThenEndDateSetAndNoEmailToSendCreated()
			throws RecordServicesException {
		Task newTask = tasksSchemas.newTask().setTitle("title").setStatus(FIN());
		recordServices.add(newTask);
		recordServices.flush();
		assertThat(tasksSchemas.getTask(newTask.getId()).getEndDate()).isEqualTo(now.toLocalDate());
		assertThat(getEmailToSend()).isNull();
	}

	@Test
	public void givenTaskWithCompletedStatusWhenStatusSetToAfterFinishedThenEndDateNotSet()
			throws RecordServicesException {
		recordServices.add(zeTask.setStatus(FIN()));
		assertThat(tasksSchemas.getTask(zeTask.getId()).getEndDate()).isEqualTo(now.toLocalDate());
		givenTimeIs(now.plusDays(1));
		recordServices.add(zeTask.setStatus((AFTER_FIN())));
		assertThat(tasksSchemas.getTask(zeTask.getId()).getEndDate()).isEqualTo(now.toLocalDate());
	}

	@Test
	public void givenTaskStatusModifiedThenValidEmailToSendCreated()
			throws RecordServicesException {
		recordServices.add(zeTask.setStatus(INP()));
		recordServices.flush();
		EmailToSend emailToSend = getEmailToSend();
		assertThat(emailToSend).isNotNull();
		assertThat(emailToSend.getTemplate()).isEqualTo(TasksEmailTemplates.TASK_STATUS_MODIFIED);
		assertThat(emailToSend.getParameters().get(0))
				.isEqualTo(TasksEmailTemplates.TASK_TITLE_PARAMETER + ":" + zeTask.getTitle());
		assertThat(emailToSend.getFrom()).isNull();
		assertThat(emailToSend.getSendOn()).isEqualTo(now);
		assertThat(emailToSend.getTo().size()).isEqualTo(1);
		assertThat(emailToSend.getTo().get(0).getEmail())
				.isEqualTo(getUserEmail(zeTaskStatusModificationFollower.getFollowerId()));
	}

	@Test
	public void givenTaskWithPreviousParentNullWhenParentSetToNewNewParentFollowingSubTasksThenValidEmailToSendCreated()
			throws RecordServicesException {
		Task newTask = tasksSchemas.newTask();
		recordServices.add(newTask.setTitle("new task")
						.setParentTask(validParentTaskFollowingSubTasks.getId())
						.setTaskFollowers(zeFollowers)
		);
		recordServices.add(newTask.setParentTask((String) null));
		recordServices.flush();
		EmailToSend emailToSend = getEmailToSend();
		assertThat(emailToSend).isNotNull();
		assertThat(emailToSend.getTemplate()).isEqualTo(TasksEmailTemplates.TASK_SUB_TASKS_MODIFIED);
		assertThat(emailToSend.getParameters().get(0))
				.isEqualTo(TasksEmailTemplates.TASK_TITLE_PARAMETER + ":" + validParentTaskFollowingSubTasks.getTitle());
		assertThat(emailToSend.getFrom()).isNull();
		assertThat(emailToSend.getSendOn()).isEqualTo(now);
		assertThat(emailToSend.getTo().size()).isEqualTo(1);
		assertThat(emailToSend.getTo().get(0).getEmail())
				.isEqualTo(getUserEmail(zeTaskSubTasksModificationFollower.getFollowerId()));
	}

	@Test
	public void givenTaskWithPreviousParentFollowingSubTasksWhenParentSetToNullThenValidEmailToSendCreated()
			throws RecordServicesException {
		recordServices.add(zeTask.setParentTask((String) null));
		recordServices.flush();
		EmailToSend emailToSend = getEmailToSend();
		assertThat(emailToSend).isNotNull();
		assertThat(emailToSend.getTemplate()).isEqualTo(TasksEmailTemplates.TASK_SUB_TASKS_MODIFIED);
		assertThat(emailToSend.getParameters().get(0))
				.isEqualTo(TasksEmailTemplates.TASK_TITLE_PARAMETER + ":" + validParentTaskFollowingSubTasks.getTitle());
		assertThat(emailToSend.getFrom()).isNull();
		assertThat(emailToSend.getSendOn()).isEqualTo(now);
		assertThat(emailToSend.getTo().size()).isEqualTo(1);
		assertThat(emailToSend.getTo().get(0).getEmail())
				.isEqualTo(getUserEmail(zeTaskSubTasksModificationFollower.getFollowerId()));
	}

	@Test
	public void givenTaskAssigneeModifiedThenValidEmailToSendCreated()
			throws RecordServicesException {
		String aliceId = users.aliceIn(zeCollection).getId();
		recordServices.add(zeTask.setAssignee(aliceId));
		recordServices.flush();
		EmailToSend emailToSend = getEmailToSend();
		assertThat(emailToSend).isNotNull();
		assertThat(emailToSend.getTemplate()).isEqualTo(TasksEmailTemplates.TASK_ASSIGNEE_MODIFIED);
		assertThat(emailToSend.getParameters().get(0))
				.isEqualTo(TasksEmailTemplates.TASK_TITLE_PARAMETER + ":" + zeTask.getTitle());
		assertThat(emailToSend.getFrom()).isNull();
		assertThat(emailToSend.getSendOn()).isEqualTo(now);
		assertThat(emailToSend.getTo().size()).isEqualTo(1);
		assertThat(emailToSend.getTo().get(0).getEmail())
				.isEqualTo(getUserEmail(zeTaskAssigneeModificationFollower.getFollowerId()));
	}

	@Test
	public void givenTaskAssignedOnModifiedThenNoEmailToSendCreated()
			throws RecordServicesException {
		recordServices.add(zeTask.setAssignationDate(now.toLocalDate()));
		recordServices.flush();
		assertThat(getEmailToSend()).isNull();
	}

	@Test
	public void givenTaskAssigneeModifiedAndNoFollowersThenNoEmailToSendCreated()
			throws RecordServicesException {
		String aliceId = users.aliceIn(zeCollection).getId();
		recordServices.add(zeTask.setAssignee(aliceId).setTaskFollowers(new ArrayList<TaskFollower>()));
		recordServices.flush();
		assertThat(getEmailToSend()).isNull();
	}

	@Test
	public void givenTaskDeletedLogicallyThenValidEmailToSendCreated() {
		recordServices.logicallyDelete(zeTask.getWrappedRecord(), null);
		recordServices.flush();
		EmailToSend emailToSend = getEmailToSend();
		assertThat(emailToSend).isNotNull();
		assertThat(emailToSend.getTemplate()).isEqualTo(TasksEmailTemplates.TASK_DELETED);
		assertThat(emailToSend.getParameters().get(0))
				.isEqualTo(TasksEmailTemplates.TASK_TITLE_PARAMETER + ":" + zeTask.getTitle());
		assertThat(emailToSend.getFrom()).isNull();
		assertThat(emailToSend.getSendOn()).isEqualTo(now);
		assertThat(emailToSend.getTo().size()).isEqualTo(1);
		assertThat(emailToSend.getTo().get(0).getEmail()).isEqualTo(getUserEmail(zeTaskDeletionFollower.getFollowerId()));
	}

	@Test
	public void givenTaskDeletedLogicallyAndNoFollowersThenNoEmailToSendCreated()
			throws RecordServicesException {
		zeTask.setTaskFollowers(null);
		recordServices.add(zeTask);
		recordServices.flush();
		taskSchemasExtension.sendDeletionEventToFollowers(zeTask);
		assertThat(getEmailToSend()).isNull();
	}

	@Test
	public void givenProcessedRemindersRelativeToStartDateWhenStartDateSetToNewStartDateThenAllRemindersWithDatesAfterNewStartDateAreSetToUnProcessed()
			throws RecordServicesException {
		Task newTask = tasksSchemas.newTask();
		newTask.setTitle("zeTitle");
		List<TaskReminder> reminders = initReminders();
		newTask.setReminders(reminders);
		recordServices.add(newTask);

		newTask = reloadTask(newTask.getId());
		newTask.setStartDate(newStartDate);
		recordServices.add(newTask);

		reminders = newTask.getReminders();

		assertThat(reminders.get(0).computeDate(newTask))
				.isEqualTo(processedReminderWithRelativeDateBeforeStartDate_0.computeDate(newTask));
		assertThat(reminders.get(0).isProcessed()).isTrue();

		assertThat(reminders.get(1).computeDate(newTask))
				.isEqualTo(processedReminderWithRelativeDateAfterStartDate_1.computeDate(newTask));
		assertThat(reminders.get(1).isProcessed()).isFalse();

		assertThat(reminders.get(2).computeDate(newTask))
				.isEqualTo(processedReminderWithRelativeDateEqualsStartDate_2.computeDate(newTask));
		assertThat(reminders.get(2).isProcessed()).isTrue();

		assertThat(reminders.get(3).computeDate(newTask))
				.isEqualTo(processedReminderWithFixedDateAfterNewStartDate_3.computeDate(newTask));
		assertThat(reminders.get(3).isProcessed()).isTrue();

		assertThat(reminders.get(4).computeDate(newTask))
				.isEqualTo(processedReminderWithRelativeDateAfterEndDate_4.computeDate(newTask));
		assertThat(reminders.get(4).isProcessed()).isTrue();
	}

	//start date
	@Test
	public void givenTaskWithNullStartDateAndWithNotStandbyWhenCreateTaskThenStartDateSet()
			throws RecordServicesException {
		Task newTask = tasksSchemas.newTask().setTitle("title").setStatus(INP());
		recordServices.add(newTask);
		newTask = reloadTask(newTask.getId());
		assertThat(newTask.getStartDate()).isEqualTo(now.toLocalDate());
		assertThat(getEmailToSend()).isNull();
	}

	@Test
	public void givenTaskWithStartDateAndWithStatusNotStandbyWhenCreateTaskThenStartDateNotSet()
			throws RecordServicesException {
		Task newTask = tasksSchemas.newTask().setTitle("title").setStartDate(now.toLocalDate().minusDays(1)).setStatus(INP());
		recordServices.add(newTask);
		newTask = reloadTask(newTask.getId());
		assertThat(newTask.getStartDate()).isEqualTo(now.toLocalDate().minusDays(1));
	}

	@Test
	public void givenTaskWithNullStartDateAndWithStandbyStatusWhenCreateTaskThenStartDateNotSet()
			throws RecordServicesException {
		Task newTask = tasksSchemas.newTask().setTitle("title");
		recordServices.add(newTask);
		newTask = reloadTask(newTask.getId());
		assertThat(newTask.getStatus()).isEqualTo(STB());
		assertThat(newTask.getStartDate()).isNull();
	}

	@Test
	public void givenTaskWithNullStartDateAndWithStatusNonStandbyWhenUpdateTaskThenStartDateSet()
			throws RecordServicesException {
		Task newTask = tasksSchemas.newTask().setTitle("title");
		recordServices.add(newTask);
		assertThat(newTask.getStartDate()).isNull();
		recordServices.add(newTask.setStatus(INP()));
		newTask = reloadTask(newTask.getId());
		assertThat(newTask.getStartDate()).isEqualTo(now.toLocalDate());
		assertThat(getEmailToSend()).isNull();
	}

	@Test
	public void givenTaskWithStartDateAndWithStatusNonStandbyWhenUpdateTaskThenStartDateNotSet()
			throws RecordServicesException {
		Task newTask = tasksSchemas.newTask().setTitle("title");
		recordServices.add(newTask);
		recordServices.add(newTask.setStatus(INP()).setStartDate(now.toLocalDate().minusDays(1)));
		newTask = reloadTask(newTask.getId());
		assertThat(newTask.getStartDate()).isEqualTo(now.toLocalDate().minusDays(1));
	}

	@Test
	public void givenTaskWithNullStartDateAndWithNullStatusWhenUpdateTaskThenStartDateNotSet()
			throws RecordServicesException {
		Task newTask = tasksSchemas.newTask().setTitle("title");
		recordServices.add(newTask);
		recordServices.add(newTask.setTitle("newTitle"));
		newTask = reloadTask(newTask.getId());
		assertThat(newTask.getStartDate()).isNull();
	}

	@Test
	public void givenTaskWithCompletedStatusWhenStatusSetToStandbyThenStartDateSetToNull()
			throws RecordServicesException {
		recordServices.add(zeTask.setStatus(FIN()));
		assertThat(tasksSchemas.getTask(zeTask.getId()).getEndDate()).isEqualTo(now.toLocalDate());
		givenTimeIs(now.plusDays(1));
		recordServices.add(zeTask.setStatus((STB())));
		assertThat(tasksSchemas.getTask(zeTask.getId()).getStartDate()).isEqualTo(null);
	}

	private Task reloadTask(String id) {
		return tasksSchemas.wrapTask(recordServices.getDocumentById(id));
	}

	private List<TaskReminder> initReminders() {
		List<TaskReminder> reminders = new ArrayList<>();
		processedReminderWithRelativeDateBeforeStartDate_0 = new TaskReminder().setRelativeDateMetadataCode(Task.START_DATE)
				.setBeforeRelativeDate(true).setNumberOfDaysToRelativeDate(1).setProcessed(true);
		reminders.add(processedReminderWithRelativeDateBeforeStartDate_0);

		processedReminderWithRelativeDateAfterStartDate_1 = new TaskReminder().setRelativeDateMetadataCode(Task.START_DATE)
				.setBeforeRelativeDate(false).setNumberOfDaysToRelativeDate(1).setProcessed(true);
		reminders.add(processedReminderWithRelativeDateAfterStartDate_1);

		processedReminderWithRelativeDateEqualsStartDate_2 = new TaskReminder().setRelativeDateMetadataCode(Task.START_DATE)
				.setBeforeRelativeDate(false).setNumberOfDaysToRelativeDate(0).setProcessed(true);
		reminders.add(processedReminderWithRelativeDateEqualsStartDate_2);

		processedReminderWithFixedDateAfterNewStartDate_3 = new TaskReminder().setFixedDate(newStartDate.plusDays(3))
				.setProcessed(true);
		reminders.add(processedReminderWithFixedDateAfterNewStartDate_3);

		processedReminderWithRelativeDateAfterEndDate_4 = new TaskReminder().setRelativeDateMetadataCode(Task.DUE_DATE)
				.setBeforeRelativeDate(false).setNumberOfDaysToRelativeDate(1).setProcessed(true);
		reminders.add(processedReminderWithRelativeDateAfterEndDate_4);

		return reminders;
	}

	private EmailToSend getEmailToSend() {
		LogicalSearchCondition condition = from(emailToSendSchema).returnAll();
		Record emailRecord = searchServices.searchSingleResult(condition);
		if (emailRecord != null) {
			return tasksSchemas.wrapEmailToSend(emailRecord);
		} else {
			return null;
		}
	}

	private String getUserEmail(String userId) {
		return tasksSchemas
				.wrapUser(searchServices.searchSingleResult(from(tasksSchemas.userSchema()).where(Schemas.IDENTIFIER).is(userId)))
				.getEmail();
	}

	public String INP() {
		TaskStatus frenchType = tasksSchemas.getTaskStatusWithCode("ENC");
		if (frenchType == null) {
			return tasksSchemas.getTaskStatusWithCode("INP").getId();
		}
		return frenchType.getId();
	}

	public String FIN() {
		TaskStatus frenchType = tasksSchemas.getTaskStatusWithCode("TER");
		assertThat(frenchType.isFinished()).isTrue();
		return frenchType.getId();
	}

	public String AFTER_FIN() {
		TaskStatus frenchType = tasksSchemas.getTaskStatusWithCode(CLOSED_CODE);
		assertThat(frenchType.isAfterFinished()).isTrue();
		return frenchType.getId();
	}

	public String STB() {
		return tasksSchemas.getTaskStatusWithCode(STANDBY_CODE).getId();
	}
}
