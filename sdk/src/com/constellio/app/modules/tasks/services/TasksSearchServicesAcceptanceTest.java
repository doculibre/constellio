package com.constellio.app.modules.tasks.services;

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
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.constellio.app.modules.tasks.model.wrappers.types.TaskStatus.CLOSED_CODE;
import static com.constellio.sdk.tests.TestUtils.assertThatRecord;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class TasksSearchServicesAcceptanceTest extends ConstellioTest {
	TasksSearchServices tasksSearchServices;

	Users users = new Users();
	RecordServices recordServices;
	SearchServices searchServices;
	private LocalDate now = LocalDate.now();
	private Task taskAssignedByChuckToAlice;
	private Task taskAssignedByChuckToAliceClosed;
	private Task taskAssignedByChuckToAliceFinished;
	private Task nonAssignedTaskCreatedByChuck;
	private Task nonAssignedTaskClosed;
	private Task subTaskOfNonAssignedTaskClosed;
	private Task taskAssignedByBobToChuckClosed;
	private Task taskAssignedByBobToChuckFinished;
	private Task taskWithDakotaInAssignationUsersCandidates;
	private Task taskWithCharlesInAssignationGroupCandidates;
	private TasksSchemasRecordsServices tasksSchemas;

	private User alice;
	private User bob;
	private User chuck;
	private User dakotaIndien;
	private User charles;
	private User edouard;
	private Group legends;
	private UserServices userServices;

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(withZeCollection().withTasksModule().withAllTest(users));
		givenTimeIs(now);
		inCollection(zeCollection).giveWriteAndDeleteAccessTo(admin);

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
		edouard = users.edouardIn(zeCollection);
		legends = users.legendsIn(zeCollection);

		TaskStatus closedStatus = tasksSchemas.getTaskStatusWithCode(CLOSED_CODE);

		Transaction transaction = new Transaction();
		taskAssignedByChuckToAlice = tasksSchemas.newTask().setTitle("taskAssignedByChuckToAlice").setAssigner(chuck.getId())
				.setAssignationDate(now).setAssignee(alice.getId());
		transaction.add(taskAssignedByChuckToAlice.setCreatedBy(chuck.getId()));

		taskAssignedByChuckToAliceClosed = tasksSchemas.newTask().setTitle("taskAssignedByChuckToAliceClosed")
				.setAssigner(chuck.getId())
				.setAssignationDate(now).setAssignee(alice.getId()).setStatus(closedStatus.getId());
		transaction.add(taskAssignedByChuckToAliceClosed.setCreatedBy(chuck.getId()));

		taskAssignedByChuckToAliceFinished = tasksSchemas.newTask().setTitle("taskAssignedByChuckToAliceFinished")
				.setAssigner(chuck.getId())
				.setAssignationDate(now).setAssignee(alice.getId())
				.setStatus(taskSearchServices.getFirstFinishedStatus().getId());
		transaction.add(taskAssignedByChuckToAliceFinished.setCreatedBy(chuck.getId()));

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
				.setStatus(closedStatus.getId());
		transaction.add(taskAssignedByBobToChuckClosed.setCreatedBy(bob.getId()));

		taskAssignedByBobToChuckFinished = tasksSchemas.newTask().setTitle("taskAssignedByBobToChuckFinished")
				.setAssigner(bob.getId())
				.setAssignationDate(now).setAssignee(chuck.getId())
				.setStatus(taskSearchServices.getFirstFinishedStatus().getId());
		transaction.add(taskAssignedByBobToChuckFinished.setCreatedBy(bob.getId()));

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
		userServices.addUpdateUserCredential(users.charles().setGlobalGroups(asList(newGlobalGroup, charlesNewGlobalGroup)));
		charles = users.charlesIn(zeCollection);
		taskWithCharlesInAssignationGroupCandidates = tasksSchemas.newTaskWithId("taskWithCharlesInAssignationGroupCandidates")
				.setTitle("taskWithCharlesInAssignationGroupCandidates")
				.setAssigner(sasquatchId)
				.setAssignationDate(now).setAssigneeGroupsCandidates(asList(newGroup.getId(), taskNewGroup.getId()));
		transaction.add(taskWithCharlesInAssignationGroupCandidates);

		recordServices.execute(transaction);
	}

	private void addGroup(String groupCode) {
		GlobalGroup group = userServices.createGlobalGroup(
				groupCode, groupCode, new ArrayList<String>(), null, GlobalGroupStatus.ACTIVE, true);
		userServices.addUpdateGlobalGroup(group);
	}

	@Test
	public void whenSearchTasksAssignedByChuckThenReturnTaskAssignedByChuckToAlice()
			throws Exception {
		List<Record> results = searchServices
				.search(tasksSearchServices.getTasksAssignedByUserQuery(chuck));
		assertThat(results.size()).isEqualTo(1);
		//TODO: Assert on records
	}

	@Test
	public void whenSearchNonAssignedTasksThenReturnNonAssignedTasksCreatedByChuckToChuckAndNothingToBob()
			throws Exception {
		List<Record> results = searchServices
				.search(tasksSearchServices.getUnassignedTasksQuery(chuck));
		assertThat(results.size()).isEqualTo(1);
		assertThatRecord(results.get(0)).hasMetadataValue(Schemas.TITLE, "nonAssignedTaskCreatedByChuck");
		results = searchServices
				.search(tasksSearchServices.getUnassignedTasksQuery(bob));
		assertThat(results.size()).isEqualTo(0);
	}

	@Test
	public void whenSearchTasksAssignedToAliceThenReturnTaskAssignedByChuckToAlice()
			throws Exception {
		List<Record> results = searchServices
				.search(tasksSearchServices.getTasksAssignedToUserQuery(alice));
		assertThat(results.size()).isEqualTo(1);
		//TODO: Assert on records
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
	public void whenSearchTasksAssignedToCharlesThenReturnTaskWithCharlesInAssignationGroupCandidates() {
		List<Record> results = searchServices
				.search(tasksSearchServices.getTasksAssignedToUserQuery(charles));
		assertThat(results.size()).isEqualTo(1);
		assertThatRecord(results.get(0)).hasMetadataValue(Schemas.TITLE, "taskWithCharlesInAssignationGroupCandidates");
	}

	@Test
	public void givenBobWhenSearchRecentlyCompletedTasksThenReturnCompletedTasksVisibleToBob() {
		List<Record> results = searchServices
				.search(tasksSearchServices.getRecentlyCompletedTasks(bob));
		assertThat(results.size()).isEqualTo(1);
		assertThat(results).extracting("title")
				.containsAll(asList("taskAssignedByBobToChuckFinished"));
	}

	@Test
	public void givenBobWhenSearchRecentlyClosedTasksThenReturnClosedTasksVisibleToBob() {
		List<Record> results = searchServices
				.search(tasksSearchServices.getRecentlyClosedTasks(bob));
		assertThat(results.size()).isEqualTo(1);
		assertThat(results.size()).isEqualTo(1);
		assertThat(results).extracting("title")
				.containsAll(asList("taskAssignedByBobToChuckClosed"));
	}

	@Test
	public void whenSearchRecentlyCompletedTasksByChuckThenThenReturnAllCompletedTasksVisibleToChuck() {
		List<Record> results = searchServices
				.search(tasksSearchServices.getRecentlyClosedTasks(bob));
		assertThat(results.size()).isEqualTo(1);
		assertThat(results).extracting("title")
				.containsAll(asList("taskAssignedByBobToChuckClosed"));
	}

	@Test
	public void whenSearchRecentlyCompletedTasksByChuckThenReturnAllCompletedTasksVisibleToChuck()
			throws Exception {
		List<Record> completed = searchServices
				.search(tasksSearchServices.getRecentlyCompletedTasks(chuck));
		assertThat(completed.size()).isEqualTo(2);
		assertThat(completed).extracting("title").containsAll(asList(
				"taskAssignedByChuckToAliceFinished", "taskAssignedByBobToChuckFinished"));
	}

	@Test
	public void whenSearchRecentlyClosedTasksByChuckThenThenReturnAllClosedTasksVisibleToChuck() {
		List<Record> results = searchServices
				.search(tasksSearchServices.getRecentlyClosedTasks(chuck));
		assertThat(results.size()).isEqualTo(2);
		assertThat(results).extracting("title").containsAll(asList("taskAssignedByChuckToAliceClosed",
				"taskAssignedByBobToChuckClosed"));
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

	@Test
	public void givenAliceAndBobInCandidatesWhenAssignedToAliceThenNotVisibleToBob()
			throws Exception {
		Task taskForCandidatesBobAndAlice = tasksSchemas.newTask().setTitle("zeTask").setAssigneeUsersCandidates(
				Arrays.asList(bob.getId(), alice.getId()));
		Transaction transaction = new Transaction();
		transaction.add(taskForCandidatesBobAndAlice);
		recordServices.execute(transaction);

		assertThat(searchServices.searchRecordIds(tasksSearchServices.getTasksAssignedToUserQuery(bob)))
				.contains(taskForCandidatesBobAndAlice.getId());
		assertThat(searchServices.searchRecordIds(tasksSearchServices.getTasksAssignedToUserQuery(alice))).contains(
				taskForCandidatesBobAndAlice.getId());

		taskForCandidatesBobAndAlice.setAssignee(alice.getId()).setAssignationDate(now).setAssigner(chuck.getId());
		transaction = new Transaction();
		transaction.update(taskForCandidatesBobAndAlice.getWrappedRecord());
		recordServices.execute(transaction);

		assertThat(searchServices.searchRecordIds(tasksSearchServices.getTasksAssignedToUserQuery(bob))).doesNotContain(
				taskForCandidatesBobAndAlice.getId());
		assertThat(searchServices.searchRecordIds(tasksSearchServices.getTasksAssignedToUserQuery(alice))).contains(
				taskForCandidatesBobAndAlice.getId());
	}

	@Test
	public void givenLegendsInCandidatesWhenAssignedToAliceThenNotVisibleToEdouard()
			throws Exception {
		Task taskForCandidatesLegends = tasksSchemas.newTask().setTitle("zeTask").setAssigneeGroupsCandidates(
				Arrays.asList(legends.getId()));
		Transaction transaction = new Transaction();
		transaction.add(taskForCandidatesLegends);
		recordServices.execute(transaction);

		assertThat(searchServices.searchRecordIds(tasksSearchServices.getTasksAssignedToUserQuery(edouard)))
				.contains(taskForCandidatesLegends.getId());
		assertThat(searchServices.searchRecordIds(tasksSearchServices.getTasksAssignedToUserQuery(alice))).contains(
				taskForCandidatesLegends.getId());

		taskForCandidatesLegends.setAssignee(alice.getId()).setAssignationDate(now).setAssigner(chuck.getId());
		transaction = new Transaction();
		transaction.update(taskForCandidatesLegends.getWrappedRecord());
		recordServices.execute(transaction);

		assertThat(searchServices.searchRecordIds(tasksSearchServices.getTasksAssignedToUserQuery(edouard))).doesNotContain(
				taskForCandidatesLegends.getId());
		assertThat(searchServices.searchRecordIds(tasksSearchServices.getTasksAssignedToUserQuery(alice))).contains(
				taskForCandidatesLegends.getId());
	}

	@Test
	public void givenTaskWithCollaboratorUserAliceWhenGetSharedTasksWithAliceThenReturnTask()
			throws Exception {
		Task taskForCollaboratorAlice = tasksSchemas.newTask().setTitle("zeTask").addTaskCollaborator(alice.getId(), false);
		Transaction transaction = new Transaction();
		transaction.add(taskForCollaboratorAlice);
		recordServices.execute(transaction);

		assertThat(searchServices.searchRecordIds(tasksSearchServices.getTasksSharedToUserQuery(alice)))
				.contains(taskForCollaboratorAlice.getId());
		assertThat(alice.hasReadAccess().on(taskForCollaboratorAlice)).isTrue();
		assertThat(alice.hasWriteAccess().on(taskForCollaboratorAlice)).isFalse();
	}

	@Test
	public void givenTaskWithCollaboratorUserAliceAndNotWriteAuthorizationThenAliceHasReadAccessOnTask()
			throws Exception {
		Task taskForCollaboratorAlice = tasksSchemas.newTask().setTitle("zeTask").addTaskCollaborator(alice.getId(), false);
		Transaction transaction = new Transaction();
		transaction.add(taskForCollaboratorAlice);
		recordServices.execute(transaction);

		assertThat(alice.hasReadAccess().on(taskForCollaboratorAlice)).isTrue();
		assertThat(alice.hasWriteAccess().on(taskForCollaboratorAlice)).isFalse();
	}

	@Test
	public void givenTaskWithCollaboratorUserAliceAndWriteAuthorizationThenAliceHasReadAnsWriteAccessOnTask()
			throws Exception {
		Task taskForCollaboratorAlice = tasksSchemas.newTask().setTitle("zeTask").addTaskCollaborator(alice.getId(), true);
		Transaction transaction = new Transaction();
		transaction.add(taskForCollaboratorAlice);
		recordServices.execute(transaction);

		assertThat(alice.hasReadAccess().on(taskForCollaboratorAlice)).isTrue();
		assertThat(alice.hasWriteAccess().on(taskForCollaboratorAlice)).isTrue();
	}

	@Test
	public void givenTaskWithCollaboratorGroupLegendsWhenGetSharedTasksWithEdouardThenReturnTask()
			throws Exception {
		Task taskForCollaboratorGroupLegends = tasksSchemas.newTask().setTitle("zeTask").addTaskCollaboratorGroup(legends.getId(), false);
		Transaction transaction = new Transaction();
		transaction.add(taskForCollaboratorGroupLegends);
		recordServices.execute(transaction);

		assertThat(searchServices.searchRecordIds(tasksSearchServices.getTasksSharedToUserQuery(edouard)))
				.contains(taskForCollaboratorGroupLegends.getId());
	}

	@Test
	public void givenTaskWithCollaboratorGroupLegendsWithoutWriteAuthorizationThenEdouardHasJustReadAcces()
			throws Exception {
		Task taskForCollaboratorGroupLegends = tasksSchemas.newTask().setTitle("zeTask").addTaskCollaboratorGroup(legends.getId(), false);
		Transaction transaction = new Transaction();
		transaction.add(taskForCollaboratorGroupLegends);
		recordServices.execute(transaction);
		assertThat(alice.hasReadAccess().on(taskForCollaboratorGroupLegends)).isTrue();
		assertThat(alice.hasWriteAccess().on(taskForCollaboratorGroupLegends)).isFalse();
	}

	@Test
	public void givenTaskWithCollaboratorGroupLegendsWithoutWriteAuthorizationThenEdouardHasReadAndWriteAcces()
			throws Exception {
		Task taskForCollaboratorGroupLegends = tasksSchemas.newTask().setTitle("zeTask").addTaskCollaboratorGroup(legends.getId(), true);
		Transaction transaction = new Transaction();
		transaction.add(taskForCollaboratorGroupLegends);
		recordServices.execute(transaction);
		assertThat(alice.hasReadAccess().on(taskForCollaboratorGroupLegends)).isTrue();
		assertThat(alice.hasWriteAccess().on(taskForCollaboratorGroupLegends)).isTrue();
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
