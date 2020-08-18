package com.constellio.app.modules.tasks.extensions;

import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.model.wrappers.TaskUser;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.EmailToSend;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.entities.structures.EmailAddress;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.constellio.app.modules.tasks.TasksEmailTemplates.TASK_ASSIGNED_TO_YOU;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class TaskSchemasExtensionTestAssigneeAlertAcceptanceTest extends ConstellioTest {
	Users users = new Users();
	RecordServices recordServices;
	private LocalDateTime now = LocalDateTime.now();
	private Task zeTask;
	private TasksSchemasRecordsServices tasksSchemas;
	private SearchServices searchServices;
	private MetadataSchema emailToSendSchema;
	User alice;
	String aliceId;

	@Before
	public void setUp()
			throws Exception {
		givenTimeIs(now);
		prepareSystem(withZeCollection().withTasksModule().withAllTest(users));

		alice = users.aliceIn(zeCollection);
		aliceId = alice.getId();

		recordServices = getModelLayerFactory().newRecordServices();
		searchServices = getModelLayerFactory().newSearchServices();
		tasksSchemas = new TasksSchemasRecordsServices(zeCollection, getAppLayerFactory());
		emailToSendSchema = tasksSchemas.emailToSend();
		zeTask = tasksSchemas.newTask();
		recordServices.add(zeTask.setTitle("taskTitle"));
		recordServices.flush();
	}

	@Test
	public void givenTaskAssigneeModifiedToAliceThenEmailToSendToAliceCreatedWithTaskAssignedToYouTemplate()
			throws RecordServicesException {
		recordServices.add(zeTask.setAssignee(aliceId).setAssignationDate(now.toLocalDate()).setAssigner(aliceId));
		recordServices.flush();
		EmailToSend toAlice = getEmailToSendByTemplateId(TASK_ASSIGNED_TO_YOU);
		assertThat(toAlice.getTo().get(0).getEmail()).isEqualTo(alice.getEmail());
	}

	@Test
	public void givenTaskAssigneeModifiedToHeroesThenEmailToSendToHeroesCreatedWithTaskAssignedToYouTemplate()
			throws RecordServicesException {
		Group heroes = users.heroesIn(zeCollection);
		for (final UserCredential user : getModelLayerFactory().newUserServices().getGlobalGroupActifUsers(heroes.getCode())) {
			user.setPersonalEmails(Arrays.asList(user.getUsername() + ".personal.mail@gmail.com"));
			getModelLayerFactory().newUserServices().addUpdateUserCredential(user);
		}
		List<String> heroesEmails = getGroupUsersEmails(heroes);
		assertThat(heroesEmails).isNotEmpty();
		recordServices.add(zeTask.setAssigneeGroupsCandidates(asList(heroes.getId())).setAssignationDate(now.toLocalDate())
				.setAssigner(aliceId));
		recordServices.flush();
		EmailToSend emailToSend = getEmailToSendByTemplateId(TASK_ASSIGNED_TO_YOU);
		assertThat(emailToSend).isNotNull();
		List<String> emailToSendRecipients = getRecipients(emailToSend);
		assertThat(emailToSendRecipients.size()).isEqualTo(heroesEmails.size());
		assertThat(emailToSendRecipients).containsAll(heroesEmails);
	}

	@Test
	public void givenTaskAssigneeSetToNullThenNoEmailToSendCreatedWithTaskAssignedToYouTemplate()
			throws RecordServicesException {
		recordServices.add(zeTask.setAssignee(null).setAssigner(null).setAssignationDate(null));
		recordServices.flush();
		assertThat(getEmailToSendByTemplateId(TASK_ASSIGNED_TO_YOU)).isNull();
	}

	@Test
	public void givenTaskCreatedWithoutAssigneeThenNoEmailToSendCreatedWithTaskAssignedToYouTemplate()
			throws RecordServicesException {
		Task newTask = tasksSchemas.newTask();
		recordServices.add(newTask.setTitle("newTask"));
		recordServices.flush();
		assertThat(getEmailToSendByTemplateId(TASK_ASSIGNED_TO_YOU)).isNull();
	}

	@Test
	public void givenTaskCreatedWithAssigneeThenEmailToSendCreatedWithTaskAssignedToYouTemplate()
			throws RecordServicesException {
		User alice = users.aliceIn(zeCollection);
		alice.setPersonalEmails(Arrays.asList("alice.personal.mail@gmail.com"));
		recordServices.update(alice);
		Task newTask = tasksSchemas.newTask();
		recordServices
				.add(newTask.setTitle("newTask").setAssignee(alice.getId()).setAssigner(alice.getId())
						.setAssignationDate(now.toLocalDate()));
		recordServices.flush();
		EmailToSend toAlice = getEmailToSendByTemplateId(TASK_ASSIGNED_TO_YOU);

		assertThat(toAlice.getTo().size()).isEqualTo(2);

		final Set<String> actualRecipents = new HashSet<>(Arrays.asList(toAlice.getTo().get(0).getEmail(), toAlice.getTo().get(1).getEmail()));
		final List<String> expectedRecipents = new ArrayList<>(alice.getPersonalEmails());
		expectedRecipents.add(alice.getEmail());
		assertThat(actualRecipents).isEqualTo(new HashSet<>(expectedRecipents));
	}

	@Test
	public void givenTaskAssignedToBobWhenBobDelegateTaskToAliceThenAliceReceivesEmailInsteadOfBob()
			throws RecordServicesException {
		User alice = users.aliceIn(zeCollection);
		User bob = users.bobIn(zeCollection);
		User admin = users.adminIn(zeCollection);
		Task newTask = tasksSchemas.newTask().setTitle("title").setAssigner(admin.getId()).setAssignedOn(LocalDate.now()).setAssignee(bob.getId());

		bob.set(TaskUser.DELEGATION_TASK_USER, alice);
		recordServices.add(bob);
		recordServices.add(newTask);
		recordServices.flush();

		EmailToSend emailToSend = getEmailToSendByTemplateId(TASK_ASSIGNED_TO_YOU);
		assertThat(emailToSend).isNotNull();
		final Set<String> actualRecipients = new HashSet<>();
		for (EmailAddress emailAddress : emailToSend.getTo()) {
			actualRecipients.add(emailAddress.getEmail());
		}

		assertThat(newTask.getAssignee()).isEqualTo(alice.getId());
		assertThat(actualRecipients).isEqualTo(new HashSet<>(asList(alice.getEmail())));
	}

	@Test
	public void givenTaskAssignedToBobWhenBobDelegateTaskToAliceAndAliceToChuckThenChuckReceivesEmailInsteadOfBob()
			throws RecordServicesException {
		User alice = users.aliceIn(zeCollection);
		User bob = users.bobIn(zeCollection);
		User admin = users.adminIn(zeCollection);
		User chuck = users.chuckNorrisIn(zeCollection);
		Task newTask = tasksSchemas.newTask().setTitle("title").setAssigner(admin.getId()).setAssignedOn(LocalDate.now()).setAssignee(bob.getId());

		bob.set(TaskUser.DELEGATION_TASK_USER, alice);
		alice.set(TaskUser.DELEGATION_TASK_USER, chuck);
		recordServices.add(bob);
		recordServices.add(alice);
		recordServices.add(newTask);
		recordServices.flush();

		EmailToSend emailToSend = getEmailToSendByTemplateId(TASK_ASSIGNED_TO_YOU);
		assertThat(emailToSend).isNotNull();
		final Set<String> actualRecipients = new HashSet<>();
		for (EmailAddress emailAddress : emailToSend.getTo()) {
			actualRecipients.add(emailAddress.getEmail());
		}

		assertThat(newTask.getAssignee()).isEqualTo(chuck.getId());
		assertThat(actualRecipients).isEqualTo(new HashSet<>(asList(chuck.getEmail())));
	}

	@Test
	public void givenTaskWithBobInAssigneeCandidatesWhenBobDelegateTaskToAliceThenAliceReceivesEmailInsteadOfBob()
			throws RecordServicesException {
		User alice = users.aliceIn(zeCollection);
		User bob = users.bobIn(zeCollection);
		User admin = users.adminIn(zeCollection);
		User charles = users.charlesIn(zeCollection);
		User dakota = users.dakotaIn(zeCollection);
		Task newTask = tasksSchemas.newTask().setTitle("title").setAssigner(admin.getId()).setAssignedOn(LocalDate.now()).setAssigneeUsersCandidates(asList(bob, charles, dakota));

		bob.set(TaskUser.DELEGATION_TASK_USER, alice);
		recordServices.add(bob);
		recordServices.add(newTask);
		recordServices.flush();

		EmailToSend emailToSend = getEmailToSendByTemplateId(TASK_ASSIGNED_TO_YOU);
		assertThat(emailToSend).isNotNull();
		final Set<String> actualRecipients = new HashSet<>();
		for (EmailAddress emailAddress : emailToSend.getTo()) {
			actualRecipients.add(emailAddress.getEmail());
		}

		assertThat(newTask.getAssigneeUsersCandidates()).containsAll(asList(alice.getId(), charles.getId(), dakota.getId()));
		assertThat(actualRecipients).isEqualTo(new HashSet<>(asList(alice.getEmail(), dakota.getEmail(), charles.getEmail())));
	}

	@Test
	public void givenTaskWithBobInAssigneeCandidatesWhenBobDelegateTaskToAliceAndAliceDelegateToChuckThenChuckReceivesEmailInsteadOfBob()
			throws RecordServicesException {
		User alice = users.aliceIn(zeCollection);
		User bob = users.bobIn(zeCollection);
		User admin = users.adminIn(zeCollection);
		User charles = users.charlesIn(zeCollection);
		User dakota = users.dakotaIn(zeCollection);
		User chuck = users.chuckNorrisIn(zeCollection);
		Task newTask = tasksSchemas.newTask().setTitle("title").setAssigner(admin.getId()).setAssignedOn(LocalDate.now()).setAssigneeUsersCandidates(asList(bob, charles, dakota));

		bob.set(TaskUser.DELEGATION_TASK_USER, alice);
		alice.set(TaskUser.DELEGATION_TASK_USER, chuck);
		recordServices.add(bob);
		recordServices.add(alice);
		recordServices.add(newTask);
		recordServices.flush();

		EmailToSend emailToSend = getEmailToSendByTemplateId(TASK_ASSIGNED_TO_YOU);
		assertThat(emailToSend).isNotNull();
		final Set<String> actualRecipients = new HashSet<>();
		for (EmailAddress emailAddress : emailToSend.getTo()) {
			actualRecipients.add(emailAddress.getEmail());
		}

		assertThat(newTask.getAssigneeUsersCandidates()).containsAll(asList(chuck.getId(), charles.getId(), dakota.getId()));
		assertThat(actualRecipients).isEqualTo(new HashSet<>(asList(chuck.getEmail(), dakota.getEmail(), charles.getEmail())));
	}


	@Test
	public void givenTaskWhenAssignToBobAndBobDelegateTaskToAliceThenAliceReceivesEmailInsteadOfBob()
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
		recordServices.flush();

		EmailToSend emailToSend = getEmailToSendByTemplateId(TASK_ASSIGNED_TO_YOU);
		assertThat(emailToSend).isNotNull();

		final Set<String> actualRecipients = new HashSet<>();
		for (EmailAddress emailAddress : emailToSend.getTo()) {
			actualRecipients.add(emailAddress.getEmail());
		}

		assertThat(newTask.getAssignee()).isEqualTo(alice.getId());
		assertThat(actualRecipients).isEqualTo(new HashSet<>(asList(alice.getEmail())));
	}

	@Test
	public void givenTaskWhenAddBobInAssigneeCandidateAndBobDelegateTaskToAliceThenAliceReceivesEmailInsteadOfBob()
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

		newTask.setAssigner(admin.getId()).setAssignedOn(LocalDate.now()).setAssigneeUsersCandidates(asList(bob, charles, dakota));
		recordServices.add(newTask);
		recordServices.flush();

		EmailToSend emailToSend = getEmailToSendByTemplateId(TASK_ASSIGNED_TO_YOU);
		assertThat(emailToSend).isNotNull();
		final Set<String> actualRecipients = new HashSet<>();
		for (EmailAddress emailAddress : emailToSend.getTo()) {
			actualRecipients.add(emailAddress.getEmail());
		}

		assertThat(newTask.getAssigneeUsersCandidates()).containsAll(asList(alice.getId(), charles.getId(), dakota.getId()));
		assertThat(actualRecipients).isEqualTo(new HashSet<>(asList(alice.getEmail(), dakota.getEmail(), charles.getEmail())));
	}

	private List<String> getGroupUsersEmails(Group group) {
		List<String> returnList = new ArrayList<>();
		UserServices userServices = getModelLayerFactory().newUserServices();
		List<UserCredential> groupUsers = userServices
				.getGlobalGroupActifUsers(group.getCode());
		for (UserCredential user : groupUsers) {
			String email = user.getEmail();
			if (StringUtils.isNotBlank(email)) {
				returnList.add(email);
			}

			if (!CollectionUtils.isEmpty(user.getPersonalEmails())) {
				for (final String personalEmail : user.getPersonalEmails()) {
					returnList.add(personalEmail);
				}
			}
		}
		return returnList;
	}

	private List<String> getRecipients(EmailToSend emailToSend) {
		List<String> recipients = new ArrayList<>();
		for (EmailAddress emailAddress : emailToSend.getTo()) {
			recipients.add(emailAddress.getEmail());
		}
		for (EmailAddress emailAddress : emailToSend.getBCC()) {
			recipients.add(emailAddress.getEmail());
		}
		for (EmailAddress emailAddress : emailToSend.getCC()) {
			recipients.add(emailAddress.getEmail());
		}
		return recipients;
	}

	private EmailToSend getEmailToSendByTemplateId(String templateId) {
		LogicalSearchCondition condition = from(emailToSendSchema)
				.where(tasksSchemas.emailToSend().getMetadata(EmailToSend.TEMPLATE)).is(templateId);
		Record emailRecord = searchServices.searchSingleResult(condition);
		if (emailRecord != null) {
			return tasksSchemas.wrapEmailToSend(emailRecord);
		} else {
			return null;
		}
	}

}
