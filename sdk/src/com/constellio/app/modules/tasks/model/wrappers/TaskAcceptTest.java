package com.constellio.app.modules.tasks.model.wrappers;

import com.constellio.app.modules.tasks.model.wrappers.structures.TaskFollower;
import com.constellio.app.modules.tasks.model.wrappers.structures.TaskReminder;
import com.constellio.app.modules.tasks.model.wrappers.types.TaskStatus;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.model.entities.records.RecordRuntimeException;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.RecordServicesException.ValidationException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.assertj.core.api.BooleanAssert;
import org.joda.time.LocalDate;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.modules.tasks.model.wrappers.TaskStatusType.IN_PROGRESS;
import static com.constellio.app.modules.tasks.model.wrappers.types.TaskStatus.STANDBY_CODE;
import static com.constellio.model.entities.schemas.Schemas.IDENTIFIER;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

public class TaskAcceptTest extends ConstellioTest {

	LocalDate zeDate = new LocalDate().minusDays(666);

	Users users = new Users();
	RecordServices recordServices;
	SearchServices searchServices;
	private LocalDate now = LocalDate.now();
	private Task zeTask;
	private TasksSchemasRecordsServices schemas;

	User alice, bob, chuckNorris, charles, dakota, edouard, gandalf, admin, sasquatch, robin;
	String aliceId, bobId, charlesId, chuckNorrisId, dakotaId, edouardId, gandalfId, sasquatchId, robinId;
	List<User> allUsers;

	Group legends, heroes, rumors, sidekicks;
	String legendsId, heroesId, rumorsId, sidekicksId;

	public void setUpWithOneCollection()
			throws Exception {
		prepareSystem(withZeCollection().withTasksModule().withAllTest(users));
		givenTimeIs(now);

		recordServices = getModelLayerFactory().newRecordServices();
		searchServices = getModelLayerFactory().newSearchServices();

		schemas = new TasksSchemasRecordsServices(zeCollection, getAppLayerFactory());
		zeTask = schemas.newTask();
		zeTask.setTitle("zeTask");

		admin = users.adminIn(zeCollection);
		aliceId = (alice = users.aliceIn(zeCollection)).getId();
		bobId = (bob = users.bobIn(zeCollection)).getId();
		chuckNorrisId = (chuckNorris = users.chuckNorrisIn(zeCollection)).getId();
		charlesId = (charles = users.charlesIn(zeCollection)).getId();
		dakotaId = (dakota = users.dakotaIn(zeCollection)).getId();
		edouardId = (edouard = users.edouardIn(zeCollection)).getId();
		gandalfId = (gandalf = users.gandalfIn(zeCollection)).getId();
		robinId = (robin = users.robinIn(zeCollection)).getId();
		sasquatchId = (sasquatch = users.sasquatchIn(zeCollection)).getId();
		legendsId = (legends = users.legendsIn(zeCollection)).getId();
		heroesId = (heroes = users.heroesIn(zeCollection)).getId();
		rumorsId = (rumors = users.rumorsIn(zeCollection)).getId();
		sidekicksId = (sidekicks = users.sidekicksIn(zeCollection)).getId();
		allUsers = asList(alice, bob, chuckNorris, charles, dakota, edouard, gandalf, sasquatch, robin);
		recordServices.update(alice.setCollectionReadAccess(true));
	}

	public void setUpWithTwoCollections()
			throws Exception {
		prepareSystem(
				withZeCollection().withTasksModule().withAllTest(users),
				withCollection("anotherCollection").withTasksModule().withAllTest(users));
		givenTimeIs(now);

		recordServices = getModelLayerFactory().newRecordServices();
		searchServices = getModelLayerFactory().newSearchServices();

		schemas = new TasksSchemasRecordsServices(zeCollection, getAppLayerFactory());
		zeTask = schemas.newTask();
		zeTask.setTitle("zeTask");

		admin = users.adminIn(zeCollection);
		aliceId = (alice = users.aliceIn(zeCollection)).getId();
		bobId = (bob = users.bobIn(zeCollection)).getId();
		chuckNorrisId = (chuckNorris = users.chuckNorrisIn(zeCollection)).getId();
		charlesId = (charles = users.charlesIn(zeCollection)).getId();
		dakotaId = (dakota = users.dakotaIn(zeCollection)).getId();
		edouardId = (edouard = users.edouardIn(zeCollection)).getId();
		gandalfId = (gandalf = users.gandalfIn(zeCollection)).getId();
		robinId = (robin = users.robinIn(zeCollection)).getId();
		sasquatchId = (sasquatch = users.sasquatchIn(zeCollection)).getId();
		legendsId = (legends = users.legendsIn(zeCollection)).getId();
		heroesId = (heroes = users.heroesIn(zeCollection)).getId();
		rumorsId = (rumors = users.rumorsIn(zeCollection)).getId();
		sidekicksId = (sidekicks = users.sidekicksIn(zeCollection)).getId();
		allUsers = asList(alice, bob, chuckNorris, charles, dakota, edouard, gandalf, sasquatch, robin);
		recordServices.update(alice.setCollectionReadAccess(true));
	}

	@Test(expected = RecordServicesException.ValidationException.class)
	public void whenSaveTaskWithoutTitleThenValidationException()
			throws Exception {
		setUpWithOneCollection();
		zeTask.setTitle(null);
		saveAndReload(zeTask);
	}

	@Test(expected = RecordServicesException.ValidationException.class)
	public void givenTaskWithAssigneeAndNoAssignationDateWhenSaveThenValidationException()
			throws Exception {
		setUpWithOneCollection();
		zeTask.setAssignee(users.aliceIn(zeCollection).getId());
		saveAndReload(zeTask);
	}

	@Test(expected = RecordServicesException.ValidationException.class)
	public void givenTaskWithoutAssigneeAndWithAssignationDateWhenSaveThenValidationException()
			throws Exception {
		setUpWithOneCollection();
		zeTask.setAssignationDate(now);
		saveAndReload(zeTask);
	}

	@Test
	public void givenTaskWithAssigneeAndWithAssignationDateWhenSaveThenOk()
			throws Exception {
		setUpWithOneCollection();
		zeTask.setAssignationDate(now).setAssignee(aliceId).setAssigner(aliceId);
		saveAndReload(zeTask);
	}

	@Test
	public void whenSaveTaskThenMetadataValuesSaved()
			throws Exception {
		setUpWithOneCollection();
		String zeAssignee = users.aliceIn(zeCollection).getId();
		String zeCreator = users.chuckNorrisIn(zeCollection).getId();

		zeTask.setAssignee(zeAssignee);
		zeTask.setAssignationDate(now);
		List<String> groups = asList(users.legendsIn(zeCollection).getId());
		zeTask.setAssigneeGroupsCandidates(groups);
		List<String> assigneeUsersCandidates = asList(zeAssignee, users.adminIn(zeCollection).getId());
		zeTask.setAssigneeUsersCandidates(assigneeUsersCandidates);
		zeTask.setCreatedBy(zeCreator);
		zeTask.setAssigner(zeCreator);
		LocalDate endDate = now.plusDays(2);
		zeTask.setDueDate(endDate);
		zeTask.setStartDate(now);
		zeTask.setProgressPercentage(20d);
		TaskStatus zeStatus = schemas.newTaskStatus().setCode("zeStatus").setStatusType(IN_PROGRESS).setTitle("status title");
		recordServices.add(zeStatus.getWrappedRecord());
		zeTask.setStatus(zeStatus.getId());
		TaskReminder reminder1 = new TaskReminder().setFixedDate(now.plusDays(1000));
		TaskReminder reminder2 = new TaskReminder().setBeforeRelativeDate(true).setNumberOfDaysToRelativeDate(1)
				.setRelativeDateMetadataCode(Task.DUE_DATE);
		zeTask.setReminders(asList(reminder1, reminder2));
		TaskFollower aliceFollowingTask = new TaskFollower().setFollowerId(users.aliceIn(zeCollection).getId());

		List<TaskFollower> followers = new ArrayList<>();
		followers.add(aliceFollowingTask);
		zeTask.setTaskFollowers(followers);

		zeTask = saveAndReload(zeTask);
		assertThat(zeTask.getAssignee()).isEqualTo(zeAssignee);
		assertThat(zeTask.getAssignedOn()).isEqualTo(now);
		assertThat(zeTask.getAssigneeGroupsCandidates()).containsAll(groups);
		assertThat(zeTask.getAssigneeUsersCandidates()).containsAll(assigneeUsersCandidates);
		assertThat(zeTask.getCreatedBy()).isEqualTo(zeCreator);
		assertThat(zeTask.getDueDate()).isEqualTo(endDate);
		assertThat(zeTask.getStartDate()).isEqualTo(now);
		assertThat(zeTask.getStatus()).isEqualTo(zeStatus.getId());
		assertThat(zeTask.getReminders()).containsOnly(reminder1, reminder2);
		assertThat(zeTask.getTaskFollowers()).hasSize(2);
		assertThat(zeTask.getTaskFollowers()).contains(aliceFollowingTask);
		assertThat(zeTask.getFollowersIds()).containsOnly(users.aliceIn(zeCollection).getId());
		assertThat(zeTask.getNextReminderOn()).isEqualTo(now.plusDays(1));
	}

	@Test
	public void whenStatusNotSetThenTaskWithStandbyStatus()
			throws Exception {
		setUpWithOneCollection();
		zeTask = saveAndReload(zeTask);
		String standbyStatusId = schemas.getTaskStatusWithCode(STANDBY_CODE).getId();
		assertThat(zeTask.getStatus()).isEqualTo(standbyStatusId);
	}

	@Test
	public void whenTrySetCalculatedMetadataThenThrowUnsupportedSetOnCalculatedMetadata()
			throws Exception {
		setUpWithOneCollection();
		try {
			zeTask.set(Task.FOLLOWERS_IDS, asList("pouet"));
			fail("");
		} catch (RecordRuntimeException.CannotSetManualValueInAutomaticField e) {
			//OK
		}

		try {
			zeTask.set(Task.NEXT_REMINDER_ON, new LocalDate());
			fail("");
		} catch (RecordRuntimeException.CannotSetManualValueInAutomaticField e) {
			//OK
		}
	}

	@Test
	public void givenTaskWithReminderHavingFixedDateWhenGetNextReminderOnThenReminderFixedDate()
			throws Exception {
		setUpWithOneCollection();
		TaskReminder reminder1 = new TaskReminder().setFixedDate(now.plusDays(1000));
		zeTask.setReminders(asList(reminder1));
		zeTask = saveAndReload(zeTask);
		assertThat(zeTask.getNextReminderOn()).isEqualTo(reminder1.getFixedDate());
	}

	@Test
	public void whenTrySetInvalidPercentageThenThrowInvalidPercentage()
			throws Exception {
		setUpWithOneCollection();
		zeTask.set(Task.PROGRESS_PERCENTAGE, null);
		saveAndReload(zeTask);
		zeTask.set(Task.PROGRESS_PERCENTAGE, 0);
		saveAndReload(zeTask);
		zeTask.set(Task.PROGRESS_PERCENTAGE, 100.0);
		saveAndReload(zeTask);
		zeTask.set(Task.PROGRESS_PERCENTAGE, 22.1);
		saveAndReload(zeTask);
		try {
			zeTask.set(Task.PROGRESS_PERCENTAGE, -1);
			saveAndReload(zeTask);
			fail("");
		} catch (RecordServicesException.ValidationException e) {
			//OK
		}

		try {
			zeTask.set(Task.PROGRESS_PERCENTAGE, 100.01);
			saveAndReload(zeTask);
			fail("");
		} catch (RecordServicesException.ValidationException e1) {
			//OK
		}
	}

	@Test
	public void givenAssignedTaskWithFollowersThenUsersHaveGoodAccess()
			throws Exception {
		setUpWithOneCollection();
		zeTask.setCreatedBy(dakotaId);
		zeTask.setAssigner(dakotaId);
		zeTask.setAssignee(bobId);
		zeTask.setAssignationDate(new LocalDate());
		zeTask.setTaskFollowers(asList(
				new TaskFollower().setFollowerId(chuckNorrisId),
				new TaskFollower().setFollowerId(edouardId)
		));
		saveAndReload(zeTask);

		validateThat(zeTask).canOnlyBeReadBy(alice, dakota, bob, chuckNorris, edouard, admin);
		validateThat(zeTask).canOnlyBeWrittenBy(dakota, bob, admin);
		validateThat(zeTask).canOnlyBeDeletedBy(dakota, admin);

	}

	@Test
	public void givenAssignedTaskWithMultipleCandidatesAndFollowersThenUsersHaveGoodAccess()
			throws Exception {
		setUpWithOneCollection();
		zeTask.setCreatedBy(charlesId);
		zeTask.setAssigner(charlesId);
		zeTask.setAssignee(bobId);
		zeTask.setAssigneeUsersCandidates(asList(bobId, gandalfId));
		zeTask.setAssignationDate(new LocalDate());
		zeTask.setTaskFollowers(asList(
				new TaskFollower().setFollowerId(chuckNorrisId),
				new TaskFollower().setFollowerId(bobId)
		));
		saveAndReload(zeTask);

		validateThat(zeTask).canOnlyBeReadBy(alice, charles, bob, gandalf, chuckNorris, admin);
		validateThat(zeTask).canOnlyBeWrittenBy(charles, bob, gandalf, admin);
		validateThat(zeTask).canOnlyBeDeletedBy(charles, admin);

	}

	@Test
	public void givenAssignedTaskWithMultipleCandidateGroupsAndFollowersThenUsersHaveGoodAccess()
			throws Exception {
		setUpWithOneCollection();
		zeTask.setCreatedBy(charlesId);
		zeTask.setAssignee(bobId);
		zeTask.setAssigner(charlesId);
		zeTask.setAssigneeGroupsCandidates(asList(legendsId));
		zeTask.setAssignationDate(new LocalDate());
		zeTask.setTaskFollowers(asList(
				new TaskFollower().setFollowerId(chuckNorrisId),
				new TaskFollower().setFollowerId(bobId)
		));
		saveAndReload(zeTask);

		validateThat(zeTask).canOnlyBeReadBy(alice, charles, bob, edouard, gandalf, chuckNorris, admin);
		validateThat(zeTask).canOnlyBeWrittenBy(alice, charles, bob, edouard, gandalf, admin);
		validateThat(zeTask).canOnlyBeDeletedBy(charles, admin);
	}

	@Test
	public void givenTaskHasADueDateSettedAfterItsParentThenException()
			throws Exception {
		setUpWithOneCollection();
		Task rootTask = schemas.newTaskWithId("rootTask");
		rootTask.setTitle("root task");
		rootTask.setCreatedBy(charlesId);
		rootTask.setAssigneeGroupsCandidates(asList(legendsId));
		rootTask.setDueDate(zeDate);
		saveAndReload(rootTask);

		Task childTaskWithDueDateBefore = schemas.newTaskWithId("childTask1");
		childTaskWithDueDateBefore.setTitle("sub task");
		childTaskWithDueDateBefore.setCreatedBy(bobId);
		childTaskWithDueDateBefore.setParentTask(rootTask);
		childTaskWithDueDateBefore.setAssigneeGroupsCandidates(asList(legendsId));
		childTaskWithDueDateBefore.setDueDate(zeDate.minusDays(1));
		saveAndReload(childTaskWithDueDateBefore);

		Task childTaskWithDueDateSameDay = schemas.newTaskWithId("childTask2");
		childTaskWithDueDateSameDay.setTitle("sub task");
		childTaskWithDueDateSameDay.setCreatedBy(bobId);
		childTaskWithDueDateSameDay.setParentTask(rootTask);
		childTaskWithDueDateSameDay.setAssigneeGroupsCandidates(asList(legendsId));
		childTaskWithDueDateSameDay.setDueDate(zeDate);
		saveAndReload(childTaskWithDueDateSameDay);

		Task childTaskWithDueDateAfter = schemas.newTaskWithId("childTask3");
		childTaskWithDueDateAfter.setTitle("sub task");
		childTaskWithDueDateAfter.setCreatedBy(bobId);
		childTaskWithDueDateAfter.setParentTask(rootTask);
		childTaskWithDueDateAfter.setAssigneeGroupsCandidates(asList(legendsId));
		childTaskWithDueDateAfter.setDueDate(zeDate.plusDays(1));

		try {
			saveAndReload(childTaskWithDueDateAfter);
			fail("Exception expected");
		} catch (ValidationException ve) {
			assertThat(ve.getErrors().getValidationErrors()).extracting("code").containsOnly(
					"com.constellio.app.modules.tasks.model.validators.TaskValidator_dueDateMustBeLesserOrEqualThanParentDueDate");
		}
	}

	@Test
	public void givenAMultilevelTasksHierarchyThenGoodAccessToUsersAndCreatorOfRootTaskCanDeleteCompleteHierarchy()
			throws Exception {
		setUpWithOneCollection();
		Task rootTask = schemas.newTaskWithId("rootTask");
		rootTask.setTitle("root task");
		rootTask.setCreatedBy(charlesId);
		rootTask.setAssigner(charlesId);
		rootTask.setAssignee(bobId);
		rootTask.setAssigneeGroupsCandidates(asList(legendsId));
		rootTask.setAssignationDate(new LocalDate());
		rootTask.setTaskFollowers(asList(
				new TaskFollower().setFollowerId(chuckNorrisId),
				new TaskFollower().setFollowerId(bobId)
		));
		saveAndReload(rootTask);

		Task childTask = schemas.newTaskWithId("childTask");
		childTask.setTitle("sub task");
		childTask.setCreatedBy(bobId);
		childTask.setParentTask(rootTask);
		childTask.setAssigner(charlesId);
		childTask.setAssignee(sasquatchId);
		childTask.setAssignationDate(new LocalDate());
		childTask.setTaskFollowers(asList(
				new TaskFollower().setFollowerId(dakotaId)
		));
		saveAndReload(childTask);

		Task childChildTask = schemas.newTaskWithId("childChildTask");
		childChildTask.setTitle("sub sub task");
		childChildTask.setCreatedBy(gandalfId);
		childChildTask.setParentTask(childTask);
		childChildTask.setAssigneeGroupsCandidates(asList(sidekicksId));
		saveAndReload(childChildTask);

		validateThat(rootTask).canOnlyBeReadBy(alice, charles, bob, edouard, gandalf, chuckNorris, admin);
		validateThat(rootTask).canOnlyBeWrittenBy(alice, charles, bob, edouard, gandalf, admin);
		validateThat(rootTask).canOnlyBeDeletedBy(charles, admin);

		validateThat(childTask).canOnlyBeReadBy(alice, charles, bob, edouard, gandalf, chuckNorris, admin, dakota, sasquatch);
		validateThat(childTask).canOnlyBeWrittenBy(alice, charles, bob, edouard, gandalf, admin, sasquatch);
		validateThat(childTask).canOnlyBeDeletedBy(charles, admin, bob);

		validateThat(childChildTask)
				.canOnlyBeReadBy(alice, charles, bob, edouard, gandalf, chuckNorris, admin, dakota, sasquatch, robin);
		validateThat(childChildTask).canOnlyBeWrittenBy(alice, charles, bob, edouard, gandalf, admin, sasquatch, robin);
		validateThat(childChildTask).canOnlyBeDeletedBy(charles, admin, bob, gandalf);

		assertThat(searchServices.searchRecordIds(new LogicalSearchQuery(from(schemas.userTask.schemaType()).returnAll())))
				.contains("rootTask", "childTask", "childChildTask");

		recordServices.logicallyDelete(rootTask.getWrappedRecord(), charles);
		recordServices.physicallyDelete(rootTask.getWrappedRecord(), charles);

		assertThat(searchServices.searchRecordIds(new LogicalSearchQuery(from(schemas.userTask.schemaType()).returnAll())))
				.doesNotContain("rootTask", "childTask", "childChildTask");

	}

	@Test
	public void givenTwoCollectionsWithTaskModuleThenBothHasValidDefaultStatus()
			throws Exception {
		setUpWithTwoCollections();

		TasksSchemasRecordsServices tasks = new TasksSchemasRecordsServices("anotherCollection", getAppLayerFactory());
		TaskStatus status = tasks.getTaskStatusWithCode(TaskStatus.STANDBY_CODE);
		System.out.println(tasks.getTypes().getMetadata(Task.DEFAULT_SCHEMA + "_" + Task.STATUS).getDefaultValue());
		assertThat(tasks.getTypes().getMetadata(Task.DEFAULT_SCHEMA + "_" + Task.STATUS)
				.getDefaultValue()).isEqualTo(status.getId());

		tasks = new TasksSchemasRecordsServices(zeCollection, getAppLayerFactory());
		status = tasks.getTaskStatusWithCode(TaskStatus.STANDBY_CODE);
		System.out.println(tasks.getTypes().getMetadata(Task.DEFAULT_SCHEMA + "_" + Task.STATUS).getDefaultValue());
		assertThat(tasks.getTypes().getMetadata(Task.DEFAULT_SCHEMA + "_" + Task.STATUS)
				.getDefaultValue()).isEqualTo(status.getId());

	}

	private TaskValidation validateThat(Task task) {
		return new TaskValidation(task);
	}

	private class TaskValidation {

		Task task;

		private TaskValidation(Task task) {
			this.task = task;
		}

		private void canOnlyBeDeletedBy(User... expectedUsers) {
			List<User> expectedUserLists = asList(expectedUsers);
			for (User user : allUsers) {
				BooleanAssert booleanAssert = assertThat(user.hasDeleteAccess().on(task)).describedAs(
						user.getUsername() + " delete access on ze task");
				if (expectedUserLists.contains(user)) {
					booleanAssert.isTrue();
				} else {
					booleanAssert.isFalse();
				}

				List<String> tasks = searchServices.searchRecordIds(new LogicalSearchQuery()
						.filteredWithUserDelete(user)
						.setCondition(from(schemas.userTask.schemaType()).where(IDENTIFIER).isEqualTo(task.getId())));

				booleanAssert = assertThat(!tasks.isEmpty()).describedAs(
						user.getUsername() + " delete access on ze task when searching");
				if (expectedUserLists.contains(user)) {
					booleanAssert.isTrue();
				} else {
					booleanAssert.isFalse();
				}

				booleanAssert = assertThat(recordServices.validateLogicallyThenPhysicallyDeletable(task.getWrappedRecord(), user).isEmpty())
						.describedAs(user.getUsername() + " delete access on ze task when deleting");
				if (expectedUserLists.contains(user)) {
					booleanAssert.isTrue();
				} else {
					booleanAssert.isFalse();
				}
			}
		}

		private void canOnlyBeWrittenBy(User... expectedUsers) {
			List<User> expectedUserLists = asList(expectedUsers);
			for (User user : allUsers) {
				BooleanAssert booleanAssert = assertThat(user.hasWriteAccess().on(task)).describedAs(
						user.getUsername() + " write access on ze task");
				if (expectedUserLists.contains(user)) {
					booleanAssert.isTrue();
				} else {
					booleanAssert.isFalse();
				}

				List<String> tasks = searchServices.searchRecordIds(new LogicalSearchQuery()
						.filteredWithUserWrite(user)
						.setCondition(from(schemas.userTask.schemaType()).where(IDENTIFIER).isEqualTo(task.getId())));

				booleanAssert = assertThat(!tasks.isEmpty()).describedAs(
						user.getUsername() + " write access on ze task when searching");
				if (expectedUserLists.contains(user)) {
					booleanAssert.isTrue();
				} else {
					booleanAssert.isFalse();
				}
			}
		}

		private void canOnlyBeReadBy(User... expectedUsers) {
			List<User> expectedUserLists = asList(expectedUsers);
			for (User user : allUsers) {
				BooleanAssert booleanAssert = assertThat(user.hasReadAccess().on(task)).describedAs(
						user.getUsername() + " read access on ze task");
				if (expectedUserLists.contains(user)) {
					booleanAssert.isTrue();
				} else {
					booleanAssert.isFalse();
				}

				List<String> tasks = searchServices.searchRecordIds(new LogicalSearchQuery()
						.filteredWithUserRead(user)
						.setCondition(from(schemas.userTask.schemaType()).where(IDENTIFIER).isEqualTo(task.getId())));

				booleanAssert = assertThat(!tasks.isEmpty()).describedAs(
						user.getUsername() + " read access on ze task when searching");
				if (expectedUserLists.contains(user)) {
					booleanAssert.isTrue();
				} else {
					booleanAssert.isFalse();
				}
			}
		}
	}

	private Task saveAndReload(Task task)
			throws RecordServicesException {
		recordServices.add(task.getWrappedRecord());
		return schemas.getTask(task.getId());
	}

}
