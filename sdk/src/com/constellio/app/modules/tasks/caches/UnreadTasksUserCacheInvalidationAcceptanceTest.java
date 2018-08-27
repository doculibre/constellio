package com.constellio.app.modules.tasks.caches;

import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.app.modules.tasks.services.TasksSearchServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.assertj.core.api.ListAssert;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.constellio.sdk.tests.TestUtils.asList;
import static com.constellio.sdk.tests.TestUtils.linkEventBus;
import static org.assertj.core.api.Assertions.assertThat;

public class UnreadTasksUserCacheInvalidationAcceptanceTest extends ConstellioTest {

	Users users = new Users();

	UnreadTasksUserCache instance1Cache;
	UnreadTasksUserCache instance2Cache;

	Task task;
	TasksSchemasRecordsServices schemas;
	RecordServices recordServices;

	@Before
	public void setUp() throws Exception {
		prepareSystem(withCollection(zeCollection).withTasksModule().withAllTest(users));
		instance1Cache = getModelLayerFactory().getCachesManager().getUserCache(UnreadTasksUserCache.NAME);
		instance2Cache = getModelLayerFactory("other-instance").getCachesManager().getUserCache(UnreadTasksUserCache.NAME);
		linkEventBus(getDataLayerFactory(), getDataLayerFactory("other-instance"));
		schemas = new TasksSchemasRecordsServices(zeCollection, getAppLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();
		loadCache();
	}


	@Test
	public void whenCreatingNewTaskThenInvalidateAssigneesTest() {
		//TODO Rabab : Effectuer des changements dans l'application et valider que la cache est bien invalidée.

		instance1Cache.invalidateUser(users.aliceIn(zeCollection));

		assertThatInvalidatedUsers().containsOnly(alice);
		//La méthode assertThatInvalidatedUsers reload la cache avant le return

		instance1Cache.invalidateUsersInGroup(users.legends());
		assertThatInvalidatedUsers().containsOnly(alice, edouard, gandalf, sasquatch);


		instance1Cache.invalidateUsersInGroup(users.heroes());
		assertThatInvalidatedUsers().containsOnly(charles, dakota, robin, gandalf);

	}

	@Test
	public void whenCreatingNewTaskThenInvalidateAssignees() throws RecordServicesException {
		String aliceId = users.aliceIn(zeCollection).getId();
		String adminId = users.adminIn(zeCollection).getId();
		String bobId = users.bobIn(zeCollection).getId();
		String chuckId = users.chuckNorrisIn(zeCollection).getId();
		List<String> usersCandidates = asList(bobId, chuckId);
		List<String> groupsCandidates = asList(users.legendsIn(zeCollection).getId());
		task = schemas.newTask();
		task.setTitle("task").setAssignee(aliceId).setAssigner(adminId)
				.setAssigneeUsersCandidates(usersCandidates)
				.setAssigneeGroupsCandidates(groupsCandidates)
				.setAssignationDate(LocalDate.now()).setAssignedOn(LocalDate.now());
		save(task);

		assertThatInvalidatedUsers().containsOnly(alice, bob, chuck, edouard, gandalf, sasquatch);
	}

	@Test
	public void whenDeletingTaskThenInvalidateAssignees() throws RecordServicesException {
		String aliceId = users.aliceIn(zeCollection).getId();
		String adminId = users.adminIn(zeCollection).getId();
		String bobId = users.bobIn(zeCollection).getId();
		String chuckId = users.chuckNorrisIn(zeCollection).getId();
		List<String> usersCandidates = asList(bobId, chuckId);
		List<String> groupsCandidates = asList(users.legendsIn(zeCollection).getId());
		task = schemas.newTask();
		task.setTitle("task").setAssignee(aliceId).setAssigner(adminId)
				.setAssigneeUsersCandidates(usersCandidates)
				.setAssigneeGroupsCandidates(groupsCandidates)
				.setAssignationDate(LocalDate.now()).setAssignedOn(LocalDate.now());
		save(task);

		task.getWrappedRecord().set(Schemas.LOGICALLY_DELETED_STATUS, true);
		recordServices.logicallyDelete(task.getWrappedRecord(),User.GOD);
		assertThatInvalidatedUsers().containsOnly(alice, bob, chuck, edouard, gandalf, sasquatch);
	}

	@Test
	public void whenModifyingTaskThenInvalidateAssignees() throws RecordServicesException {
		String aliceId = users.aliceIn(zeCollection).getId();
		String adminId = users.adminIn(zeCollection).getId();
		String bobId = users.bobIn(zeCollection).getId();
		String charlesId = users.charlesIn(zeCollection).getId();
		String chuckId = (users.chuckNorrisIn(zeCollection)).getId();

		List<String> usersCandidates = asList(bobId, chuckId);
		List<String> groupsCandidates = asList(users.legendsIn(zeCollection).getId());
		task = schemas.newTask();
		task.setTitle("task").setAssignee(aliceId).setAssigner(adminId)
				.setAssigneeUsersCandidates(usersCandidates)
				.setAssigneeGroupsCandidates(groupsCandidates)
				.setAssignationDate(LocalDate.now()).setAssignedOn(LocalDate.now());
		save(task);
		task.setAssignee(charlesId);
		task.setAssigneeUsersCandidates(new ArrayList<String>());
		recordServices.update(task.getWrappedRecord());
		System.out.println("AssigneeUsersCandidates:" + task.getAssigneeUsersCandidates());
		assertThatInvalidatedUsers().containsOnly(alice, bob, charles, chuck, edouard, gandalf, sasquatch);
	}

	private void save(Task task)
			throws RecordServicesException {
		recordServices.add(task.getWrappedRecord());
	}

	private void loadCache() {
		for (User user : getModelLayerFactory().newUserServices().getAllUsersInCollection(zeCollection)) {
			for (AppLayerFactory appLayerFactory : asList(getAppLayerFactory(), getAppLayerFactory("other-instance"))) {
				TasksSchemasRecordsServices schemas = new TasksSchemasRecordsServices(zeCollection, appLayerFactory);
				TasksSearchServices tasksSearchServices = new TasksSearchServices(schemas);
				tasksSearchServices.getCountUnreadTasksToUserQuery(user);
			}
		}
	}

	private ListAssert<String> assertThatInvalidatedUsers() {

		List<String> invalidatedUsersInInstance1 = new ArrayList<>();
		List<String> invalidatedUsersInInstance2 = new ArrayList<>();

		for (User user : getModelLayerFactory().newUserServices().getAllUsersInCollection(zeCollection)) {
			if (instance1Cache.getCachedUnreadTasks(user) == null) {
				invalidatedUsersInInstance1.add(user.getUsername());
			}
		}

		for (User user : getModelLayerFactory().newUserServices().getAllUsersInCollection(zeCollection)) {
			if (instance2Cache.getCachedUnreadTasks(user) == null) {
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
}
