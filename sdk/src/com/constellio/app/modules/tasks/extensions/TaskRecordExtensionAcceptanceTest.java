package com.constellio.app.modules.tasks.extensions;

import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.model.wrappers.structures.TaskFollower;
import com.constellio.app.modules.tasks.model.wrappers.structures.TaskReminder;
import com.constellio.app.modules.tasks.model.wrappers.types.TaskStatus;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.EmailToSend;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.structures.EmailAddress;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static com.constellio.app.modules.tasks.TasksEmailTemplates.*;
import static com.constellio.app.modules.tasks.model.wrappers.Task.START_DATE;
import static com.constellio.app.modules.tasks.model.wrappers.types.TaskStatus.CLOSED_CODE;
import static com.constellio.app.modules.tasks.model.wrappers.types.TaskStatus.STANDBY_CODE;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class TaskRecordExtensionAcceptanceTest extends ConstellioTest {
	private TaskRecordExtension taskRecordExtension;

	Users users = new Users();
	RecordServices recordServices;
	UserServices userServices;
	ConstellioEIMConfigs eimConfigs;
	String constellioUrl;
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
		prepareSystem(withZeCollection().withTasksModule().withAllTest(users));

		recordServices = getModelLayerFactory().newRecordServices();
		searchServices = getModelLayerFactory().newSearchServices();
		tasksSchemas = new TasksSchemasRecordsServices(zeCollection, getAppLayerFactory());
		emailToSendSchema = tasksSchemas.emailToSend();
		taskRecordExtension = new TaskRecordExtension(zeCollection, getAppLayerFactory());
		userServices = getModelLayerFactory().newUserServices();
		eimConfigs = new ConstellioEIMConfigs(getModelLayerFactory().getSystemConfigurationsManager());
		constellioUrl = eimConfigs.getConstellioUrl();
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
	public void givenTaskStatusModifiedToCompletedThenOneValidEmailToSendToAssignerAndFollowerIsCreatedAndTaskEndDateSetToNow()
			throws RecordServicesException {
		recordServices.add(zeTask.setStatus(FIN()));
		recordServices.flush();
		EmailToSend emailToSend = getEmailToSendNotHavingAssignedToYouTemplateId();
		assertThat(emailToSend).isNotNull();
		assertThat(emailToSend.getTemplate()).isEqualTo(TASK_COMPLETED);
		assertThatParametersAreOk(zeTask, emailToSend);
		assertThat(emailToSend.getFrom()).isNull();
		assertThat(emailToSend.getSendOn()).isEqualTo(now);
		assertThat(emailToSend.getTo().size()).isEqualTo(2);
		assertThat(tasksSchemas.getTask(zeTask.getId()).getEndDate()).isEqualTo(now.toLocalDate());

		final Set<String> expectedRecipients = new HashSet<>();
		for (EmailAddress emailAddress : emailToSend.getTo()) {
			expectedRecipients.add(emailAddress.getEmail());
		}
		assertThat(expectedRecipients).isEqualTo(new HashSet<>(Arrays.asList(getUserEmail(users.aliceIn(zeCollection).getId()), getUserEmail(zeTaskFinishedEventFollower.getFollowerId()))));
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
		assertThat(getEmailToSendNotHavingAssignedToYouTemplateId()).isNull();
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
		EmailToSend emailToSend = getEmailToSendNotHavingAssignedToYouTemplateId();
		assertThat(emailToSend).isNotNull();
		assertThat(emailToSend.getTemplate()).isEqualTo(TASK_STATUS_MODIFIED);
		assertThatParametersAreOk(zeTask, emailToSend);
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
				.setAssigner(users.adminIn(zeCollection).getId())
				.setAssignee(users.adminIn(zeCollection).getId())
				.setAssignationDate(now.plusDays(1).toLocalDate())
		);

		recordServices.add(newTask.setParentTask((String) null));
		recordServices.flush();
		EmailToSend emailToSend = getEmailToSendNotHavingAssignedToYouTemplateId();
		assertThat(emailToSend).isNotNull();
		assertThat(emailToSend.getTemplate()).isEqualTo(TASK_SUB_TASKS_MODIFIED);
		assertThatParametersAreOk(newTask, emailToSend);

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
		EmailToSend emailToSend = getEmailToSendNotHavingAssignedToYouTemplateId();
		assertThat(emailToSend).isNotNull();
		assertThat(emailToSend.getTemplate()).isEqualTo(TASK_SUB_TASKS_MODIFIED);
		assertThatParametersAreOk(zeTask, emailToSend);
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
		EmailToSend emailToSend = getEmailToSendNotHavingAssignedToYouTemplateId();
		assertThat(emailToSend).isNotNull();
		assertThat(emailToSend.getTemplate()).isEqualTo(TASK_ASSIGNEE_MODIFIED);
		assertThatParametersAreOk(zeTask, emailToSend);
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
		assertThat(getEmailToSendNotHavingAssignedToYouTemplateId()).isNull();
	}

	@Test
	public void givenTaskAssigneeModifiedAndNoFollowersThenNoEmailToSendCreated()
			throws RecordServicesException {
		String aliceId = users.aliceIn(zeCollection).getId();
		recordServices.add(zeTask.setAssignee(aliceId).setTaskFollowers(new ArrayList<TaskFollower>()));
		recordServices.flush();
		assertThat(getEmailToSendNotHavingAssignedToYouTemplateId(

		))
				.isNull();
	}

	@Test
	public void givenTaskDeletedLogicallyThenValidEmailToSendCreated() {
		recordServices.logicallyDelete(zeTask.getWrappedRecord(), null);
		recordServices.flush();
		EmailToSend emailToSend = getEmailToSendNotHavingAssignedToYouTemplateId();
		assertThat(emailToSend).isNotNull();
		assertThat(emailToSend.getTemplate()).isEqualTo(TASK_DELETED);
		assertThatParametersAreOk(zeTask, emailToSend);
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
		taskRecordExtension.sendDeletionEventToFollowers(zeTask);
		assertThat(getEmailToSendNotHavingAssignedToYouTemplateId()).isNull();
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
		assertThat(getEmailToSendNotHavingAssignedToYouTemplateId()).isNull();
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
		assertThat(getEmailToSendNotHavingAssignedToYouTemplateId()).isNull();
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
		processedReminderWithRelativeDateBeforeStartDate_0 = new TaskReminder().setRelativeDateMetadataCode(START_DATE)
				.setBeforeRelativeDate(true).setNumberOfDaysToRelativeDate(1).setProcessed(true);
		reminders.add(processedReminderWithRelativeDateBeforeStartDate_0);

		processedReminderWithRelativeDateAfterStartDate_1 = new TaskReminder().setRelativeDateMetadataCode(START_DATE)
				.setBeforeRelativeDate(false).setNumberOfDaysToRelativeDate(3).setProcessed(true);
		reminders.add(processedReminderWithRelativeDateAfterStartDate_1);

		processedReminderWithRelativeDateEqualsStartDate_2 = new TaskReminder().setRelativeDateMetadataCode(START_DATE)
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

	private EmailToSend getEmailToSendNotHavingAssignedToYouTemplateId() {
		LogicalSearchCondition condition = from(emailToSendSchema)
				.where(tasksSchemas.emailToSend().getMetadata(EmailToSend.TEMPLATE))
				.isNotEqual(TASK_ASSIGNED_TO_YOU);
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

	private void assertThatParametersAreOk(Task task, EmailToSend emailToSend) {
		String assignerFullName = getUserFullNameById(task.getAssigner());
		assertThat(assignerFullName).isNotEmpty();

		String assigneeFullName = getUserFullNameById(task.getAssignee());
		assertThat(assigneeFullName).isNotEmpty();

		assertThat(task.getAssignedOn()).isNotNull();

		String parentTaskTitle = "";
		if (task.getParentTask() != null) {
			Task parentTask = tasksSchemas.getTask(task.getParentTask());
			parentTaskTitle = parentTask.getTitle();
		}

		String status = tasksSchemas.getTaskStatus(task.getStatus()).getTitle();

		assertThat(emailToSend.getParameters()).contains(
				TASK_TITLE_PARAMETER + ":" + task.getTitle(),
				PARENT_TASK_TITLE + ":" + parentTaskTitle,
				TASK_ASSIGNED_BY + ":" + assignerFullName,
				TASK_ASSIGNED_ON + ":" + task.getAssignedOn(),
				TASK_ASSIGNED + ":" + assigneeFullName,
				TASK_DUE_DATE + ":" + "",
				TASK_STATUS + ":" + StringEscapeUtils.escapeHtml4(status),
				TASK_DESCRIPTION + ":" + "",
				DISPLAY_TASK + ":" + constellioUrl + "#!displayTask/" + task.getId(),
				COMPLETE_TASK + ":" + constellioUrl + "#!editTask/completeTask%253Dtrue%253Bid%253D" + task.getId(),
				CONSTELLIO_URL + ":" + constellioUrl
		);

		assertThat(emailToSend.getSubject()).isNull();
	}

	private String getUserNameById(String userId) {
		if (StringUtils.isBlank(userId)) {
			return "";
		}
		return tasksSchemas.wrapUser(recordServices.getDocumentById(userId)).getUsername();
	}

	private String getUserFullNameById(String userId) {
		if (org.apache.commons.lang3.StringUtils.isBlank(userId)) {
			return "";
		}
		return tasksSchemas.wrapUser(recordServices.getDocumentById(userId)).getFirstName() + " " +
			   tasksSchemas.wrapUser(recordServices.getDocumentById(userId)).getLastName();
	}

	@Test
	public void givenTaskCreatedThenAssignerIsAddedByDefaultToCompletionEventFollowers()
			throws RecordServicesException {
		// Given
		final User someAssigner = users.aliceIn(zeCollection);
		final User someAssignee = users.bobIn(zeCollection);

		final Task someTask = tasksSchemas.newTask().
				setTitle("title").
				setAssigner(someAssigner.getId()).
				setAssignee(someAssignee.getId()).
				setAssignationDate(now.toLocalDate()).
				setAssignedOn(now.toLocalDate());

		// When
		recordServices.add(someTask);
		recordServices.flush();

		// Then
		assertThat(tasksSchemas.getTask(someTask.getId()).getTaskFollowers()).isNotEmpty();
		assertThat(tasksSchemas.getTask(someTask.getId()).getTaskFollowers()).contains(new TaskFollower().setFollowerId(someAssigner.getId()).setFollowTaskCompleted(true));
	}

}
