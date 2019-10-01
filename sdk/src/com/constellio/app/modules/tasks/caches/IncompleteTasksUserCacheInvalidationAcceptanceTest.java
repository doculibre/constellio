package com.constellio.app.modules.tasks.caches;

import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.model.wrappers.TaskStatusType;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.app.modules.tasks.services.TasksSearchServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.assertj.core.api.ListAssert;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.sdk.tests.TestUtils.linkEventBus;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class IncompleteTasksUserCacheInvalidationAcceptanceTest extends ConstellioTest {

	Users users = new Users();

	IncompleteTasksUserCache instance1Cache;
	IncompleteTasksUserCache instance2Cache;

	Task task;
	TasksSchemasRecordsServices schemas;
	RecordServices recordServices;

	@Before
	public void setUp() throws Exception {
		prepareSystem(withCollection(zeCollection).withTasksModule().withAllTest(users));
		instance1Cache = getModelLayerFactory().getCachesManager().getUserCache(IncompleteTasksUserCache.NAME);
		instance2Cache = getModelLayerFactory("other-instance").getCachesManager().getUserCache(IncompleteTasksUserCache.NAME);
		linkEventBus(getDataLayerFactory(), getDataLayerFactory("other-instance"));
		schemas = new TasksSchemasRecordsServices(zeCollection, getAppLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();
		loadCache();
	}

	@Test
	public void whenCreatingNewTaskThenInvalidateAssignee()
			throws RecordServicesException {
		String adminId = users.adminIn(zeCollection).getId();
		String aliceId = users.aliceIn(zeCollection).getId();

		task = schemas.newTask();
		task.setTitle("task").setAssigner(adminId).setAssignee(aliceId)
				.setAssignationDate(LocalDate.now()).setAssignedOn(LocalDate.now());
		save(task);

		assertThatInvalidatedUsers().containsOnly(alice);
	}

	@Test
	public void whenCreatingNewTaskThenInvalidateAssigneeUserCandidates()
			throws RecordServicesException {
		String adminId = users.adminIn(zeCollection).getId();
		String aliceId = users.aliceIn(zeCollection).getId();
		String edouardId = users.edouardIn(zeCollection).getId();

		task = schemas.newTask();
		task.setTitle("task").setAssigner(adminId).setAssigneeUsersCandidates(asList(aliceId, edouardId))
				.setAssignationDate(LocalDate.now()).setAssignedOn(LocalDate.now());
		save(task);

		assertThatInvalidatedUsers().containsOnly(alice, edouard);
	}

	@Test
	public void whenCreatingNewTaskThenInvalidateAssigneeGroupCandidates()
			throws RecordServicesException {
		String adminId = users.adminIn(zeCollection).getId();

		task = schemas.newTask();
		task.setTitle("task").setAssigner(adminId).setAssigneeGroupsCandidates(asList(users.heroesIn(zeCollection).getId()))
				.setAssignationDate(LocalDate.now()).setAssignedOn(LocalDate.now());
		save(task);

		assertThatInvalidatedUsers().containsOnly(charles, dakota, gandalf, robin);
	}

	@Test
	public void whenCreatingNewTaskWithNoAssigneeThenInvalidatedUsersIsEmpty()
			throws RecordServicesException {
		task = schemas.newTask();
		task.setTitle("task");
		save(task);

		assertThatInvalidatedUsers().isEmpty();
	}

	@Test
	public void whenLogicallyDeleteIncompleteAndNonClosedTaskWithAssigneeThenInvalidateAssignee()
			throws RecordServicesException {
		String aliceId = users.aliceIn(zeCollection).getId();
		String adminId = users.adminIn(zeCollection).getId();
		task = schemas.newTask();
		task.setTitle("task").setAssignee(aliceId).setAssigner(adminId)
				.setAssignationDate(LocalDate.now()).setAssignedOn(LocalDate.now());
		save(task);
		loadCache();

		recordServices.logicallyDelete(task.getWrappedRecord(), User.GOD);

		assertThatInvalidatedUsers().containsOnly(alice);
	}

	@Test
	public void whenLogicallyDeleteIncompleteAndNonClosedTaskWithAssigneeThenInvalidateAssigneeCandidates()
			throws RecordServicesException {
		String aliceId = users.aliceIn(zeCollection).getId();
		String adminId = users.adminIn(zeCollection).getId();
		String bobId = users.bobIn(zeCollection).getId();

		task = schemas.newTask();
		task.setTitle("task").setAssigneeUsersCandidates(asList(aliceId, bobId)).setAssigner(adminId)
				.setAssignationDate(LocalDate.now()).setAssignedOn(LocalDate.now());
		save(task);
		loadCache();

		recordServices.logicallyDelete(task.getWrappedRecord(), User.GOD);

		assertThatInvalidatedUsers().containsOnly(alice, bob);
	}

	@Test
	public void whenLogicallyDeleteIncompleteAndNonClosedTaskWithAssigneeThenInvalidateAssigneeGroupCandidates()
			throws RecordServicesException {
		String adminId = users.adminIn(zeCollection).getId();


		task = schemas.newTask();
		task.setTitle("task").setAssigneeGroupsCandidates(asList(users.legendsIn(zeCollection).getId())).setAssigner(adminId)
				.setAssignationDate(LocalDate.now()).setAssignedOn(LocalDate.now());
		save(task);
		loadCache();

		recordServices.logicallyDelete(task.getWrappedRecord(), User.GOD);

		assertThatInvalidatedUsers().containsOnly(alice, edouard, gandalf, sasquatch);
	}

	@Test
	public void whenLogicallyDeleteCompleteTaskWithAssigneeThenDontInvalidateAssignee()
			throws RecordServicesException {
		String aliceId = users.aliceIn(zeCollection).getId();
		String adminId = users.adminIn(zeCollection).getId();
		task = schemas.newTask();
		task.setTitle("task").setAssignee(aliceId).setAssigner(adminId)
				.setAssignationDate(LocalDate.now()).setAssignedOn(LocalDate.now()).setStatus(getFinishedStatus());
		save(task);
		loadCache();

		recordServices.logicallyDelete(task.getWrappedRecord(), User.GOD);

		assertThatInvalidatedUsers().isEmpty();
	}

	@Test
	public void whenLogicallyDeleteClosedTaskWithAssigneeThenDontInvalidateAssignee()
			throws RecordServicesException {
		String aliceId = users.aliceIn(zeCollection).getId();
		String adminId = users.adminIn(zeCollection).getId();
		task = schemas.newTask();
		task.setTitle("task").setAssignee(aliceId).setAssigner(adminId)
				.setAssignationDate(LocalDate.now()).setAssignedOn(LocalDate.now()).setStatus(getClosedStatus());
		save(task);
		loadCache();

		recordServices.logicallyDelete(task.getWrappedRecord(), User.GOD);

		assertThatInvalidatedUsers().isEmpty();
	}

	@Test
	public void whenLogicallyDeleteTaskWithNoAssigneeThenInvalidatedUsersIsEmpty()
			throws RecordServicesException {
		task = schemas.newTask();
		task.setTitle("task");
		save(task);
		loadCache();

		recordServices.logicallyDelete(task.getWrappedRecord(), User.GOD);

		assertThatInvalidatedUsers().isEmpty();
	}

	@Test
	public void whenLogicallyDeleteCompleteTaskWithAssigneeThenNothingIsInvalidated()
			throws RecordServicesException {
		String adminId = users.adminIn(zeCollection).getId();
		String bobId = users.bobIn(zeCollection).getId();
		String chuckId = users.chuckNorrisIn(zeCollection).getId();
		List<String> usersCandidates = asList(bobId, chuckId);
		List<String> groupsCandidates = asList(users.legendsIn(zeCollection).getId());
		task = schemas.newTask();
		task.setTitle("task").setAssigner(adminId)
				.setAssigneeUsersCandidates(usersCandidates)
				.setAssigneeGroupsCandidates(groupsCandidates)
				.setStatus(getFinishedStatus())
				.setAssignationDate(LocalDate.now()).setAssignedOn(LocalDate.now());
		save(task);
		loadCache();

		recordServices.logicallyDelete(task.getWrappedRecord(), User.GOD);

		assertThatInvalidatedUsers().isEmpty();
	}

	@Test
	public void whenPhysicallyDeleteIncompleteTaskWithAssigneeThenInvalidateAssignee()
			throws RecordServicesException {
		String aliceId = users.aliceIn(zeCollection).getId();
		String adminId = users.adminIn(zeCollection).getId();
		task = schemas.newTask();
		task.setTitle("task").setAssignee(aliceId).setAssigner(adminId)
				.setAssignationDate(LocalDate.now()).setAssignedOn(LocalDate.now());
		task.getWrappedRecord().set(Schemas.LOGICALLY_DELETED_STATUS, true);
		save(task);
		loadCache();

		recordServices.physicallyDelete(task.getWrappedRecord(), User.GOD);

		assertThatInvalidatedUsers().containsOnly(alice);
	}

	@Test
	public void whenPhysicallyDeleteClosedTaskWithAssigneeThenNothingIsInvalidated()
			throws RecordServicesException {
		String aliceId = users.aliceIn(zeCollection).getId();
		String adminId = users.adminIn(zeCollection).getId();
		task = schemas.newTask();
		task.setTitle("task").setAssignee(aliceId).setAssigner(adminId)
				.setAssignationDate(LocalDate.now()).setAssignedOn(LocalDate.now()).setStatus(getClosedStatus());
		task.getWrappedRecord().set(Schemas.LOGICALLY_DELETED_STATUS, true);
		save(task);
		loadCache();

		recordServices.physicallyDelete(task.getWrappedRecord(), User.GOD);

		assertThatInvalidatedUsers().isEmpty();
	}

	@Test
	public void whenPhysicallyDeleteCompletedTaskWithAssigneeThenNothingIsInvalidated()
			throws RecordServicesException {
		String aliceId = users.aliceIn(zeCollection).getId();
		String adminId = users.adminIn(zeCollection).getId();
		task = schemas.newTask();
		task.setTitle("task").setAssignee(aliceId).setAssigner(adminId)
				.setAssignationDate(LocalDate.now()).setAssignedOn(LocalDate.now()).setStatus(getFinishedStatus());
		task.getWrappedRecord().set(Schemas.LOGICALLY_DELETED_STATUS, true);
		save(task);
		loadCache();

		recordServices.physicallyDelete(task.getWrappedRecord(), User.GOD);

		assertThatInvalidatedUsers().isEmpty();
	}

	@Test
	public void whenPhysicallyDeleteTaskWithNoAssigneeThenInvalidatedUsersIsEmpty()
			throws RecordServicesException {
		task = schemas.newTask();
		task.setTitle("task");
		task.getWrappedRecord().set(Schemas.LOGICALLY_DELETED_STATUS, true);
		save(task);
		loadCache();

		recordServices.physicallyDelete(task.getWrappedRecord(), User.GOD);

		assertThatInvalidatedUsers().isEmpty();
	}

	@Test
	public void whenModifyingTaskAssigneeThenInvalidateOldAndNewAssignee()
			throws RecordServicesException {
		String aliceId = users.aliceIn(zeCollection).getId();
		String adminId = users.adminIn(zeCollection).getId();
		String charlesId = users.charlesIn(zeCollection).getId();

		task = schemas.newTask();
		task.setTitle("task").setAssignee(aliceId).setAssigner(adminId)
				.setAssignationDate(LocalDate.now()).setAssignedOn(LocalDate.now());
		save(task);

		loadCache();

		task.setAssignee(charlesId);
		recordServices.update(task.getWrappedRecord());

		assertThatInvalidatedUsers().containsOnly(alice, charles);
	}

	@Test
	public void whenModifyingTaskStatusThenInvalidateAssignee()
			throws RecordServicesException {
		String aliceId = users.aliceIn(zeCollection).getId();
		String adminId = users.adminIn(zeCollection).getId();

		task = schemas.newTask();
		task.setTitle("task").setAssignee(aliceId).setAssigner(adminId)
				.setAssignationDate(LocalDate.now()).setAssignedOn(LocalDate.now());
		save(task);

		loadCache();

		task.setStatus(getClosedStatus());
		recordServices.update(task.getWrappedRecord());

		assertThatInvalidatedUsers().containsOnly(alice);
	}

	@Test
	public void whenModifyingTaskStatusThenInvalidateAssigneeUserCandidates()
			throws RecordServicesException {
		String aliceId = users.aliceIn(zeCollection).getId();
		String adminId = users.adminIn(zeCollection).getId();
		String edouardId = users.edouardIn(zeCollection).getId();

		task = schemas.newTask();
		task.setTitle("task").setAssigneeUsersCandidates(asList(aliceId, edouardId)).setAssigner(adminId)
				.setAssignationDate(LocalDate.now()).setAssignedOn(LocalDate.now());
		save(task);

		loadCache();

		task.setStatus(getClosedStatus());
		recordServices.update(task.getWrappedRecord());

		assertThatInvalidatedUsers().containsOnly(alice, edouard);
	}

	@Test
	public void whenModifyingTaskStatusThenInvalidateAssigneeGroupCandidates()
			throws RecordServicesException {
		String adminId = users.adminIn(zeCollection).getId();

		task = schemas.newTask();
		task.setTitle("task").setAssigneeGroupsCandidates(asList(users.legendsIn(zeCollection).getId())).setAssigner(adminId)
				.setAssignationDate(LocalDate.now()).setAssignedOn(LocalDate.now());
		save(task);

		loadCache();

		task.setStatus(getClosedStatus());
		recordServices.update(task.getWrappedRecord());

		assertThatInvalidatedUsers().containsOnly(edouard, alice, gandalf, sasquatch);
	}

	@Test
	public void whenRestoringDeletedUnreadTaskWithAssigneesThenInvalidateAllAssignees()
			throws RecordServicesException {
		String bobId = users.bobIn(zeCollection).getId();
		String adminId = users.adminIn(zeCollection).getId();
		task = schemas.newTask();
		task.setTitle("task").setAssignee(bobId).setAssigner(adminId)
				.setAssignationDate(LocalDate.now()).setAssignedOn(LocalDate.now());
		save(task);
		loadCache();

		recordServices.logicallyDelete(task.getWrappedRecord(), User.GOD);
		loadCache();

		recordServices.restore(task.getWrappedRecord(), User.GOD);
		assertThatInvalidatedUsers().containsOnly(bob);
	}

	@Test
	public void whenRestoringDeletedCompletedTaskWithAssigneesThenNothingIsInvalidated()
			throws RecordServicesException {
		String adminId = users.adminIn(zeCollection).getId();
		String bobId = users.bobIn(zeCollection).getId();
		task = schemas.newTask();
		task.setTitle("task").setAssigner(adminId).setStatus(getFinishedStatus()).setAssignee(bobId)
				.setAssignationDate(LocalDate.now()).setAssignedOn(LocalDate.now());
		save(task);
		loadCache();

		recordServices.logicallyDelete(task.getWrappedRecord(), User.GOD);
		loadCache();

		recordServices.restore(task.getWrappedRecord(), User.GOD);
		assertThatInvalidatedUsers().isEmpty();
	}

	@Test
	public void whenRestoringDeletedClosedTaskWithAssigneesThenNothingIsInvalidated()
			throws RecordServicesException {
		String adminId = users.adminIn(zeCollection).getId();
		String bobId = users.bobIn(zeCollection).getId();
		task = schemas.newTask();
		task.setTitle("task").setAssigner(adminId).setStatus(getClosedStatus()).setAssignee(bobId)
				.setAssignationDate(LocalDate.now()).setAssignedOn(LocalDate.now());
		save(task);
		loadCache();

		recordServices.logicallyDelete(task.getWrappedRecord(), User.GOD);
		loadCache();

		recordServices.restore(task.getWrappedRecord(), User.GOD);
		assertThatInvalidatedUsers().isEmpty();
	}

	@Test
	public void whenRestoringDeletedTaskWithNoAssigneeThenInvalidatedUsersIsEmpty()
			throws RecordServicesException {
		task = schemas.newTask();
		task.setTitle("task");
		save(task);
		loadCache();

		recordServices.logicallyDelete(task.getWrappedRecord(), User.GOD);
		loadCache();

		recordServices.restore(task.getWrappedRecord(), User.GOD);
		assertThatInvalidatedUsers().isEmpty();
	}

	@Test
	public void givenTrivialFieldOfTaskIsModifiedThenNoAssignesAreInvalidated()
			throws RecordServicesException {
		String adminId = users.adminIn(zeCollection).getId();
		String bobId = users.bobIn(zeCollection).getId();
		String chuckId = users.chuckNorrisIn(zeCollection).getId();
		List<String> usersCandidates = asList(bobId, chuckId);
		List<String> groupsCandidates = asList(users.legendsIn(zeCollection).getId());
		task = schemas.newTask();
		task.setTitle("task").setAssigner(adminId)
				.setAssigneeUsersCandidates(usersCandidates)
				.setAssigneeGroupsCandidates(groupsCandidates)
				.setAssignationDate(LocalDate.now()).setAssignedOn(LocalDate.now());
		save(task);
		loadCache();

		recordServices.update(task.setTitle("Ze new title"));
		assertThatInvalidatedUsers().isEmpty();
	}

	private void loadCache() {
		for (User user : getModelLayerFactory().newUserServices().getAllUsersInCollection(zeCollection)) {
			for (AppLayerFactory appLayerFactory : asList(getAppLayerFactory(), getAppLayerFactory("other-instance"))) {
				TasksSchemasRecordsServices schemas = new TasksSchemasRecordsServices(zeCollection, appLayerFactory);
				TasksSearchServices tasksSearchServices = new TasksSearchServices(schemas);
				tasksSearchServices.getCountIncompleteTasksToUserQuery(user);
			}
		}
	}

	private ListAssert<String> assertThatInvalidatedUsers() {
		List<String> invalidatedUsersInInstance1 = new ArrayList<>();
		List<String> invalidatedUsersInInstance2 = new ArrayList<>();

		for (User user : getModelLayerFactory().newUserServices().getAllUsersInCollection(zeCollection)) {
			if (instance1Cache.getCachedIncompleteTasks(user) == null) {
				invalidatedUsersInInstance1.add(user.getUsername());
			}
		}

		for (User user : getModelLayerFactory().newUserServices().getAllUsersInCollection(zeCollection)) {
			if (instance2Cache.getCachedIncompleteTasks(user) == null) {
				invalidatedUsersInInstance2.add(user.getUsername());
			}
		}

		Collections.sort(invalidatedUsersInInstance1);
		Collections.sort(invalidatedUsersInInstance2);

		assertThat(invalidatedUsersInInstance1).describedAs("Ensuring users are invalidated equally on both instances")
				.isEqualTo(invalidatedUsersInInstance2);

		loadCache();
		return assertThat(invalidatedUsersInInstance1);

	}

	private String getFinishedStatus() {
		SearchServices searchServices = getAppLayerFactory().getModelLayerFactory().newSearchServices();
		TasksSchemasRecordsServices tasksSchemasRecordsServices = new TasksSchemasRecordsServices(zeCollection, getAppLayerFactory());
		return searchServices.searchSingleResult(
				from(tasksSchemasRecordsServices.ddvTaskStatus.schemaType()).where(tasksSchemasRecordsServices.ddvTaskStatus.statusType()).isEqualTo(
						TaskStatusType.FINISHED)).getId();
	}

	private String getClosedStatus() {
		SearchServices searchServices = getAppLayerFactory().getModelLayerFactory().newSearchServices();
		TasksSchemasRecordsServices tasksSchemasRecordsServices = new TasksSchemasRecordsServices(zeCollection, getAppLayerFactory());
		return searchServices.searchSingleResult(
				from(tasksSchemasRecordsServices.ddvTaskStatus.schemaType()).where(tasksSchemasRecordsServices.ddvTaskStatus.statusType()).isEqualTo(
						TaskStatusType.CLOSED)).getId();
	}

	private void save(Task task)
			throws RecordServicesException {
		recordServices.add(task.getWrappedRecord());
	}
}
