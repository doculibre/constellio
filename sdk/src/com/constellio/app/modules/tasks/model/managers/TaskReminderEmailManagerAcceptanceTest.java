package com.constellio.app.modules.tasks.model.managers;

import com.constellio.app.modules.tasks.TasksEmailTemplates;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.model.wrappers.structures.TaskReminder;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.model.entities.records.wrappers.EmailToSend;
import com.constellio.model.entities.structures.EmailAddress;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.modules.tasks.TasksEmailTemplates.TASK_ASSIGNED_TO_YOU;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class TaskReminderEmailManagerAcceptanceTest extends ConstellioTest {
	private LocalDate now = LocalDate.now();
	Users users = new Users();
	Task zeTask;
	private TasksSchemasRecordsServices schemas;
	private TaskReminderEmailManager manager;
	private SearchServices searchServices;
	private UserServices userServices;
	private List<EmailAddress> allAssigneesAddresses;
	private List<EmailAddress> allAssigneeGroupsAddresses;
	private List<EmailAddress> allAssigneeUsersAddresses;
	private TaskReminder reminderAfterNow;
	private TaskReminder reminderBeforeNow;
	private TaskReminder reminderNow;
	private RecordServices recordServices;

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(withZeCollection().withTasksModule().withAllTest(users));
		givenTimeIs(now);

		schemas = new TasksSchemasRecordsServices(zeCollection, getAppLayerFactory());
		manager = schemas.getTaskReminderEmailManager();
		searchServices = getModelLayerFactory().newSearchServices();
		userServices = getModelLayerFactory().newUserServices();
		recordServices = getModelLayerFactory().newRecordServices();
		setupTestData();

	}

	private void setupTestData()
			throws RecordServicesException {
		String aliceAndBobGroupId = users.heroesIn(zeCollection).getId();
		users.aliceIn(zeCollection);

		userServices.execute(users.aliceAddUpdateRequest().setCollections(asList(zeCollection))
				.addToGroupsInEachCollection(users.heroesIn(zeCollection).getCode()));
		userServices.execute(users.bobAddUpdateRequest().setCollections(asList(zeCollection))
				.addToGroupsInEachCollection(users.heroesIn(zeCollection).getCode()));
		userServices.execute(users.chuckNorrisAddUpdateRequest());

		EmailAddress aliceEmailAddress = new EmailAddress(users.alice().getTitle(), users.alice().getEmail());
		EmailAddress bobEmailAddress = new EmailAddress(users.bob().getTitle(), users.bob().getEmail());
		EmailAddress chuckEmailAddress = new EmailAddress(users.chuckNorris().getTitle(), users.chuckNorris().getEmail());

		reminderBeforeNow = new TaskReminder().setFixedDate(now.minusDays(1));
		reminderAfterNow = new TaskReminder().setFixedDate(now.plusDays(1));
		reminderNow = new TaskReminder().setFixedDate(now);

		allAssigneeUsersAddresses = asList(aliceEmailAddress, chuckEmailAddress);
		allAssigneeGroupsAddresses = asList(aliceEmailAddress, bobEmailAddress);
		allAssigneesAddresses = asList(aliceEmailAddress, chuckEmailAddress, bobEmailAddress);
		zeTask = schemas.newTask()
				.setReminders(asList(reminderBeforeNow, reminderAfterNow, reminderNow))
				.setAssigneeGroupsCandidates(asList(aliceAndBobGroupId))
				.setAssigneeUsersCandidates(
						asList(users.aliceIn(zeCollection).getId(), users.chuckNorrisIn(zeCollection).getId()));
		zeTask.setTitle("zeTitle");
		zeTask = saveAndReload(zeTask);
		assertThat(zeTask.getNextReminderOn()).isEqualTo(now.minusDays(1));
	}

	private Task saveAndReload(Task task)
			throws RecordServicesException {
		recordServices.add(task.getWrappedRecord());
		return schemas.getTask(task.getId());
	}

	@Test
	public void givenTaskWithoutNextReminderOnWhenManagerCalledThenNoReminderEmailGenerated()
			throws Exception {
		zeTask.setReminders(null);
		recordServices.add(zeTask);
		zeTask = schemas.getTask(zeTask.getId());
		assertThat(zeTask.getNextReminderOn()).isNull();
		manager.generateReminderEmails();
		assertThat(getEmailToSendWithTemplateIdDifferentFromAssignedToYouCount()).isEqualTo(0);
	}

	@Test
	public void givenTaskWithAliceAssigneeAndAliceWithBlankEmailWhenManagerCalledThenNoReminderEmailGenerated()
			throws Exception {
		userServices.execute(users.aliceAddUpdateRequest().setEmail(null));
		zeTask = saveAndReload(zeTask.setAssignee(users.aliceIn(zeCollection).getId())
				.setAssignationDate(now)
				.setAssigner(users.adminIn(zeCollection).getId())
				.setAssigneeGroupsCandidates(new ArrayList<String>())
				.setAssigneeUsersCandidates(new ArrayList<String>()));
		manager.generateReminderEmails();
		assertThat(getEmailToSendWithTemplateIdDifferentFromAssignedToYouCount()).isEqualTo(0);
	}

	@Test
	public void givenTaskWithoutAssigneesWhenManagerCalledThenNoReminderEmailGenerated()
			throws Exception {
		zeTask = saveAndReload(
				zeTask.setAssigneeGroupsCandidates(new ArrayList<String>()).setAssigneeUsersCandidates(new ArrayList<String>()));
		assertThat(zeTask.getAssigneeGroupsCandidates()).isEqualTo(new ArrayList<String>());
		assertThat(zeTask.getAssigneeUsersCandidates()).isEqualTo(new ArrayList<String>());
		manager.generateReminderEmails();
		assertThat(getEmailToSendWithTemplateIdDifferentFromAssignedToYouCount()).isEqualTo(0);
	}

	@Test
	public void givenTaskWithNextReminderOnBeforeNowAndValidAssignedToWhenManagerCalledThenReminderEmailGenerated()
			throws Exception {
		manager.generateReminderEmails();
		assertValidEmailToSendCreated(allAssigneesAddresses);
	}

	@Test
	public void givenTaskWithNextReminderOnAfterNowWhenManagerCalledThenNoReminderNoEmailGenerated()
			throws Exception {
		zeTask = saveAndReload(zeTask.setReminders(asList(reminderAfterNow)));
		assertThat(zeTask.getNextReminderOn()).isEqualTo(reminderAfterNow.getFixedDate());
		manager.generateReminderEmails();
		assertThat(getEmailToSendWithTemplateIdDifferentFromAssignedToYouCount()).isEqualTo(0);
	}

	@Test
	public void givenTaskWithValidNextReminderOnButRemindersProcessedThenNoReminderEmailGenerated()
			throws Exception {
		List<TaskReminder> processedReminders = zeTask.getReminders();
		for (TaskReminder taskReminder : processedReminders) {
			taskReminder.setProcessed(true);
		}
		zeTask = saveAndReload(zeTask.setAssigneeUsersCandidates(new ArrayList<String>()).setReminders(processedReminders));
		assertThat(zeTask.getAssigneeUsersCandidates()).isEqualTo(new ArrayList<String>());
		manager.generateReminderEmails();
		assertThat(getEmailToSendWithTemplateIdDifferentFromAssignedToYouCount()).isEqualTo(0);
	}

	@Test
	public void givenTaskWithValidNextReminderOnAndAssignedToGroupWhenManagerCalledThenReminderEmailGeneratedToBeSentToAllGroupUsers()
			throws Exception {
		zeTask = saveAndReload(zeTask.setAssigneeUsersCandidates(new ArrayList<String>()));
		assertThat(zeTask.getAssigneeUsersCandidates()).isEqualTo(new ArrayList<String>());
		manager.generateReminderEmails();
		assertValidEmailToSendCreated(allAssigneeGroupsAddresses);
	}

	@Test
	public void givenTaskWithReminderBeforeNowAndReminderAfterNowWhenManagerCalledThenReminderBeforeNowSetToProcessed()
			throws Exception {
		manager.generateReminderEmails();
		zeTask = schemas.getTask(zeTask.getId());
		List<TaskReminder> reminders = zeTask.getReminders();
		assertThat(reminders.size()).isEqualTo(3);
		reminderBeforeNow = reminders.get(0);
		assertThat(reminderBeforeNow.isProcessed()).isTrue();
		reminderAfterNow = reminders.get(1);
		assertThat(reminderAfterNow.isProcessed()).isFalse();
		reminderNow = reminders.get(2);
		assertThat(reminderBeforeNow.isProcessed()).isTrue();
		assertThat(zeTask.getNextReminderOn()).isEqualTo(now.plusDays(1));
	}

	@Test
	public void givenTaskWithValidNextReminderOnAndAssignedToUsersWhenManagerCalledThenReminderEmailGeneratedToBeSentToAllUsers()
			throws Exception {
		zeTask = saveAndReload(zeTask.setAssigneeGroupsCandidates(new ArrayList<String>()));
		manager.generateReminderEmails();
		assertValidEmailToSendCreated(allAssigneeUsersAddresses);
	}

	@Test
	public void testPagination()
			throws Exception {
		TaskReminderEmailManager.RECORDS_BATCH = 1;
		givenTowValidTasksThenTowEmailsCreated();
	}

	private void givenTowValidTasksThenTowEmailsCreated()
			throws RecordServicesException {
		Task task2 = schemas.newTask()
				.setAssignee(users.aliceIn(zeCollection).getId())
				.setAssignationDate(now)
				.setAssigner(users.adminIn(zeCollection).getId())
				.setReminders(asList(reminderBeforeNow))
				.setTitle("task2");
		recordServices.add(task2);
		manager.generateReminderEmails();
		assertThat(getEmailToSendWithTemplateIdDifferentFromAssignedToYouCount()).isEqualTo(2);
	}

	private Long getEmailToSendWithTemplateIdDifferentFromAssignedToYouCount() {
		LogicalSearchCondition condition = from(schemas.emailToSend())
				.where(schemas.emailToSend().getMetadata(EmailToSend.TEMPLATE)).isNotEqual(TASK_ASSIGNED_TO_YOU);
		return searchServices.getResultsCount(condition);
	}

	private void assertValidEmailToSendCreated(List<EmailAddress> expectedToEmails) {
		LogicalSearchCondition condition = from(schemas.emailToSend())
				.where(schemas.emailToSend().getMetadata(EmailToSend.TEMPLATE)).isNotEqual(TASK_ASSIGNED_TO_YOU);
		EmailToSend emailToSend = schemas
				.wrapEmailToSend(searchServices.searchSingleResult(condition));
		assertThat(emailToSend.getTo().size()).isEqualTo(expectedToEmails.size());
		assertThat(emailToSend.getTo()).containsAll(expectedToEmails);
		assertThat(emailToSend.getTemplate()).isEqualTo(TasksEmailTemplates.TASK_REMINDER);
		assertThat(emailToSend.getFrom()).isNull();
		assertThat(emailToSend.getSendOn().toLocalDate()).isEqualTo(now);
		assertThat(emailToSend.getSubject()).isNull();
	}

}
