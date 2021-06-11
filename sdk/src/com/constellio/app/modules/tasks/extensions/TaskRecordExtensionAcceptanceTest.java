package com.constellio.app.modules.tasks.extensions;

import com.constellio.app.modules.rm.wrappers.structures.Comment;
import com.constellio.app.modules.tasks.TaskConfigs;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.model.wrappers.TaskUser;
import com.constellio.app.modules.tasks.model.wrappers.structures.TaskFollower;
import com.constellio.app.modules.tasks.model.wrappers.structures.TaskReminder;
import com.constellio.app.modules.tasks.model.wrappers.types.TaskStatus;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.app.ui.i18n.i18n;
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
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.constellio.app.modules.tasks.TasksEmailTemplates.COMPLETE_TASK;
import static com.constellio.app.modules.tasks.TasksEmailTemplates.CONSTELLIO_URL;
import static com.constellio.app.modules.tasks.TasksEmailTemplates.DISPLAY_TASK;
import static com.constellio.app.modules.tasks.TasksEmailTemplates.PARENT_TASK_TITLE;
import static com.constellio.app.modules.tasks.TasksEmailTemplates.TASK_ASSIGNED;
import static com.constellio.app.modules.tasks.TasksEmailTemplates.TASK_ASSIGNED_BY;
import static com.constellio.app.modules.tasks.TasksEmailTemplates.TASK_ASSIGNED_ON;
import static com.constellio.app.modules.tasks.TasksEmailTemplates.TASK_ASSIGNED_TO_YOU;
import static com.constellio.app.modules.tasks.TasksEmailTemplates.TASK_ASSIGNEE_MODIFIED;
import static com.constellio.app.modules.tasks.TasksEmailTemplates.TASK_COMPLETED;
import static com.constellio.app.modules.tasks.TasksEmailTemplates.TASK_DELETED;
import static com.constellio.app.modules.tasks.TasksEmailTemplates.TASK_DESCRIPTION;
import static com.constellio.app.modules.tasks.TasksEmailTemplates.TASK_DUE_DATE;
import static com.constellio.app.modules.tasks.TasksEmailTemplates.TASK_FOLLOWER_ADDED;
import static com.constellio.app.modules.tasks.TasksEmailTemplates.TASK_STATUS;
import static com.constellio.app.modules.tasks.TasksEmailTemplates.TASK_STATUS_MODIFIED;
import static com.constellio.app.modules.tasks.TasksEmailTemplates.TASK_SUB_TASKS_MODIFIED;
import static com.constellio.app.modules.tasks.TasksEmailTemplates.TASK_TITLE_PARAMETER;
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
	private TaskReminder processedReminderWithRelativeDateAfterCreationDate_5;

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
		assertThat(expectedRecipients).isEqualTo(new HashSet<>(asList(getUserEmail(users.aliceIn(zeCollection).getId()), getUserEmail(zeTaskFinishedEventFollower.getFollowerId()))));
	}

	@Test
	public void givenTaskWithSetCommentAndConfigEnabledThenOneValidEmailToSendToAssignerAndFollowerIsCreated()
			throws RecordServicesException {
		givenConfig(TaskConfigs.SHOW_COMMENTS, true);

		LocalDateTime time = new LocalDateTime(2001, 12, 19, 21, 0);
		Comment comment = new Comment("You shall not pass!", users.gandalfIn(zeCollection), time);
		recordServices.add(zeTask.setComments(asList(comment)).setStatus(FIN()));
		recordServices.flush();
		EmailToSend emailToSend = getEmailToSendNotHavingAssignedToYouTemplateId();
		assertThat(emailToSend).isNotNull();
		assertThat(emailToSend.getTemplate()).isEqualTo(TASK_COMPLETED);
		assertThatParametersAreOk(zeTask, emailToSend);
		assertThat(emailToSend.getFrom()).isNull();
		assertThat(emailToSend.getSendOn()).isEqualTo(this.now);
		assertThat(emailToSend.getTo().size()).isEqualTo(2);
		assertThat(emailToSend.getParameters()).contains("taskComments:Commentaires :<br/>Gandalf Leblanc : 2001-12-19T21:00:00.000<br/>You shall not pass!<br/>");
		assertThat(tasksSchemas.getTask(zeTask.getId()).getEndDate()).isEqualTo(this.now.toLocalDate());

		final Set<String> expectedRecipients = new HashSet<>();
		for (EmailAddress emailAddress : emailToSend.getTo()) {
			expectedRecipients.add(emailAddress.getEmail());
		}
		assertThat(expectedRecipients).isEqualTo(new HashSet<>(asList(getUserEmail(users.aliceIn(zeCollection).getId()), getUserEmail(zeTaskFinishedEventFollower.getFollowerId()))));
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
	public void givenTaskFollowersModifiedThenValidEmailToSendCreated() throws Exception {
		clearFollowerAddedEmails();

		String aliceId = users.aliceIn(zeCollection).getId();
		TaskFollower taskFollower = new TaskFollower();
		taskFollower.setFollowerId(aliceId);

		recordServices.add(zeTask.setTaskFollowers(asList(taskFollower)));
		recordServices.flush();

		EmailToSend emailToSend = getEmailToSendHavingFollowerAddedTemplateId();
		assertThat(emailToSend).isNotNull();
		assertThat(emailToSend.getTemplate()).isEqualTo(TASK_FOLLOWER_ADDED);
		assertThatParametersAreOk(zeTask, emailToSend);
		assertThat(emailToSend.getFrom()).isNull();
		assertThat(emailToSend.getSendOn()).isEqualTo(now);
		assertThat(emailToSend.getTo().size()).isEqualTo(1);
		assertThat(emailToSend.getTo().get(0).getEmail()).isEqualTo(getUserEmail(aliceId));
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

		assertThat(reminders.get(5).computeDate(newTask))
				.isEqualTo(processedReminderWithRelativeDateAfterCreationDate_5.computeDate(newTask));
		assertThat(reminders.get(5).isProcessed()).isTrue();
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

	@Test
	public void givenTaskAssignedToBobWhenBobDelegateTaskToAliceThenAliceIsAssignee() throws RecordServicesException {
		User alice = users.aliceIn(zeCollection);
		User bob = users.bobIn(zeCollection);
		User admin = users.adminIn(zeCollection);
		Task newTask = tasksSchemas.newTask().setTitle("title").setAssigner(admin.getId()).setAssignedOn(LocalDate.now()).setAssignee(bob.getId());

		bob.set(TaskUser.DELEGATION_TASK_USER, alice);
		recordServices.add(bob);
		recordServices.add(newTask);

		assertThat(newTask.getAssignee()).isEqualTo(alice.getId());
		assertThat(newTask.getComments().size()).isEqualTo(1);
		assertThat(newTask.getComments().get(0).getMessage()).isEqualTo(i18n.$("TaskManagementView.taskDelegationAssigneeComment", bob.getUsername(), alice.getUsername()));
	}

	@Test
	public void givenTaskAssignedToBobWhenBobDelegateTaskToAliceAndAliceDelegateToCharlesThenCharlesIsAssignee()
			throws RecordServicesException {
		User alice = users.aliceIn(zeCollection);
		User bob = users.bobIn(zeCollection);
		User admin = users.adminIn(zeCollection);
		User charles = users.charlesIn(zeCollection);
		Task newTask = tasksSchemas.newTask().setTitle("title").setAssigner(admin.getId()).setAssignedOn(LocalDate.now()).setAssignee(bob.getId());

		bob.set(TaskUser.DELEGATION_TASK_USER, alice);
		alice.set(TaskUser.DELEGATION_TASK_USER, charles);
		recordServices.add(bob);
		recordServices.add(alice);
		recordServices.add(newTask);

		assertThat(newTask.getAssignee()).isEqualTo(charles.getId());
		assertThat(newTask.getComments().size()).isEqualTo(2);
		List<Comment> comments = newTask.getComments();
		assertThat(comments.size()).isEqualTo(2);
		for (Comment comment : comments) {
			assertThat(comment.getMessage()).isIn(i18n.$("TaskManagementView.taskDelegationAssigneeComment", bob.getUsername(), alice.getUsername()),
					i18n.$("TaskManagementView.taskDelegationAssigneeComment", alice.getUsername(), charles.getUsername()));
		}
	}

	@Test
	public void givenTaskWhenAssignToBobAndBobDelegateTaskToAliceAndAliceDelegateToCharlesThenCharlesIsAssignee()
			throws RecordServicesException {
		User alice = users.aliceIn(zeCollection);
		User bob = users.bobIn(zeCollection);
		User admin = users.adminIn(zeCollection);
		User charles = users.charlesIn(zeCollection);
		Task newTask = tasksSchemas.newTask().setTitle("title");
		bob.set(TaskUser.DELEGATION_TASK_USER, alice);
		alice.set(TaskUser.DELEGATION_TASK_USER, charles);
		recordServices.add(bob);
		recordServices.add(alice);
		recordServices.add(newTask);

		newTask.setAssigner(admin.getId()).setAssignedOn(LocalDate.now()).setAssignee(bob.getId());
		recordServices.add(newTask);

		assertThat(newTask.getAssignee()).isEqualTo(charles.getId());
		assertThat(newTask.getComments().size()).isEqualTo(2);
		List<Comment> comments = newTask.getComments();
		assertThat(comments.size()).isEqualTo(2);
		for (Comment comment : comments) {
			assertThat(comment.getMessage()).isIn(i18n.$("TaskManagementView.taskDelegationAssigneeComment", bob.getUsername(), alice.getUsername()),
					i18n.$("TaskManagementView.taskDelegationAssigneeComment", alice.getUsername(), charles.getUsername()));
		}
	}

	@Test
	public void givenTaskWithBobInAssigneeCandidatesWhenBobDelegateTaskToAliceThenAliceIsInAssigneeCandidatesInsteadOfBob()
			throws RecordServicesException {
		User alice = users.aliceIn(zeCollection);
		User bob = users.bobIn(zeCollection);
		User charles = users.charlesIn(zeCollection);
		User dakota = users.dakotaIn(zeCollection);
		Task newTask = tasksSchemas.newTask().setTitle("title").setAssigneeUsersCandidates(asList(bob, charles, dakota));

		bob.set(TaskUser.DELEGATION_TASK_USER, alice);
		recordServices.add(bob);
		recordServices.add(newTask);

		assertThat(newTask.getAssigneeUsersCandidates()).containsAll(asList(alice.getId(), charles.getId(), dakota.getId()));
		assertThat(newTask.getComments().size()).isEqualTo(1);
		assertThat(newTask.getComments().get(0).getMessage()).isEqualTo(i18n.$("TaskManagementView.taskDelegationAssigneeCandidatComment", bob.getUsername(), alice.getUsername()));
	}

	@Test
	public void givenTaskWithBobInAssigneeCandidatesWhenBobDelegateTaskToAliceAndAliceToChuckThenChuckIsInAssigneeCandidatesInsteadOfBob()
			throws RecordServicesException {
		User alice = users.aliceIn(zeCollection);
		User bob = users.bobIn(zeCollection);
		User charles = users.charlesIn(zeCollection);
		User dakota = users.dakotaIn(zeCollection);
		User chuck = users.chuckNorrisIn(zeCollection);
		Task newTask = tasksSchemas.newTask().setTitle("title").setAssigneeUsersCandidates(asList(bob, charles, dakota));

		bob.set(TaskUser.DELEGATION_TASK_USER, alice);
		alice.set(TaskUser.DELEGATION_TASK_USER, chuck);
		recordServices.add(bob);
		recordServices.add(alice);
		recordServices.add(newTask);

		assertThat(newTask.getAssigneeUsersCandidates()).containsAll(asList(chuck.getId(), charles.getId(), dakota.getId()));
		List<Comment> comments = newTask.getComments();
		assertThat(comments.size()).isEqualTo(2);
		for (Comment comment : comments) {
			assertThat(comment.getMessage()).isIn(i18n.$("TaskManagementView.taskDelegationAssigneeCandidatComment", bob.getUsername(), alice.getUsername()),
					i18n.$("TaskManagementView.taskDelegationAssigneeCandidatComment", alice.getUsername(), chuck.getUsername()));
		}
	}


	@Test
	public void givenTaskWithBobAndAliceInAssigneeCandidatesWhenBobDelegateTaskToAliceThenBobIsRemovedFromAssigneeCandidates()
			throws RecordServicesException {
		User alice = users.aliceIn(zeCollection);
		User bob = users.bobIn(zeCollection);
		User admin = users.adminIn(zeCollection);
		User charles = users.charlesIn(zeCollection);
		User dakota = users.dakotaIn(zeCollection);
		Task newTask = tasksSchemas.newTask().setTitle("title").setAssigneeUsersCandidates(asList(bob, charles, dakota, alice));

		bob.set(TaskUser.DELEGATION_TASK_USER, alice);
		recordServices.add(bob);
		recordServices.add(newTask);

		assertThat(newTask.getAssigneeUsersCandidates()).containsAll(asList(charles.getId(), dakota.getId(), alice.getId()));
		assertThat(newTask.getAssigneeUsersCandidates()).doesNotContain(bob.getId());
		assertThat(newTask.getComments().size()).isEqualTo(1);
		assertThat(newTask.getComments().get(0).getMessage()).isEqualTo(i18n.$("TaskManagementView.taskDelegationAssigneeCandidatComment", bob.getUsername(), alice.getUsername()));
	}

	@Test
	public void givenTaskbWhenBobDelegateTaskToAliceAndModifyTaskToAssignToBobThenAliceIsAssignee()
			throws RecordServicesException {
		User alice = users.aliceIn(zeCollection);
		User bob = users.bobIn(zeCollection);
		User admin = users.adminIn(zeCollection);
		Task newTask = tasksSchemas.newTask().setTitle("title");
		bob.set(TaskUser.DELEGATION_TASK_USER, alice);
		recordServices.add(bob);
		recordServices.add(newTask);

		newTask.setAssigner(admin.getId()).setAssignedOn(LocalDate.now()).setAssignee(bob.getId());
		recordServices.add(newTask);

		assertThat(newTask.getAssignee()).isEqualTo(alice.getId());
		assertThat(newTask.getComments().size()).isEqualTo(1);
		assertThat(newTask.getComments().get(0).getMessage()).isEqualTo(i18n.$("TaskManagementView.taskDelegationAssigneeComment", bob.getUsername(), alice.getUsername()));
	}

	@Test
	public void givenTaskWhenBobInAssigneeCandidatesWhenBobDelegateTaskToAliceThenAliceIsInAssigneeCandidatesInsteadOfBob()
			throws RecordServicesException {
		User alice = users.aliceIn(zeCollection);
		User bob = users.bobIn(zeCollection);
		User admin = users.adminIn(zeCollection);
		User charles = users.charlesIn(zeCollection);
		User dakota = users.dakotaIn(zeCollection);
		Task newTask = tasksSchemas.newTask().setTitle("title");
		bob.set(TaskUser.DELEGATION_TASK_USER, alice);
		recordServices.add(bob);
		recordServices.add(newTask);

		newTask.setAssigneeUsersCandidates(asList(bob, charles, dakota));
		recordServices.add(newTask);

		assertThat(newTask.getAssigneeUsersCandidates()).containsAll(asList(alice.getId(), charles.getId(), dakota.getId()));
		assertThat(newTask.getComments().size()).isEqualTo(1);
		assertThat(newTask.getComments().get(0).getMessage()).isEqualTo(i18n.$("TaskManagementView.taskDelegationAssigneeCandidatComment", bob.getUsername(), alice.getUsername()));
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

		processedReminderWithRelativeDateAfterCreationDate_5 = new TaskReminder().setRelativeDateMetadataCode(Task.DUE_DATE)
				.setBeforeRelativeDate(false).setNumberOfDaysToRelativeDate(1).setProcessed(true);
		reminders.add(processedReminderWithRelativeDateAfterCreationDate_5);

		return reminders;
	}

	private EmailToSend getEmailToSendNotHavingAssignedToYouTemplateId() {
		LogicalSearchCondition condition = from(emailToSendSchema)
				.where(tasksSchemas.emailToSend().getMetadata(EmailToSend.TEMPLATE)).isNotEqual(TASK_ASSIGNED_TO_YOU)
				.andWhere(tasksSchemas.emailToSend().getMetadata(EmailToSend.TEMPLATE)).isNotEqual(TASK_FOLLOWER_ADDED);
		Record emailRecord = searchServices.searchSingleResult(condition);
		if (emailRecord != null) {
			return tasksSchemas.wrapEmailToSend(emailRecord);
		} else {
			return null;
		}
	}

	private EmailToSend getEmailToSendHavingFollowerAddedTemplateId() {
		LogicalSearchCondition condition = from(emailToSendSchema)
				.where(tasksSchemas.emailToSend().getMetadata(EmailToSend.TEMPLATE)).isEqualTo(TASK_FOLLOWER_ADDED)
				.andWhere(Schemas.LOGICALLY_DELETED_STATUS).isFalseOrNull();
		Record emailRecord = searchServices.searchSingleResult(condition);
		if (emailRecord != null) {
			return tasksSchemas.wrapEmailToSend(emailRecord);
		} else {
			return null;
		}
	}

	private void clearFollowerAddedEmails() {
		LogicalSearchCondition condition = from(emailToSendSchema)
				.where(tasksSchemas.emailToSend().getMetadata(EmailToSend.TEMPLATE)).isEqualTo(TASK_FOLLOWER_ADDED);
		List<Record> emailRecords = searchServices.search(new LogicalSearchQuery(condition));
		for (Record email : emailRecords) {
			recordServices.logicallyDelete(email, null);
		}
		recordServices.flush();
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
