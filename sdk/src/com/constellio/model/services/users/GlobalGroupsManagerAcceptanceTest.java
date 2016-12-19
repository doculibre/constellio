package com.constellio.model.services.users;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import com.constellio.model.entities.security.global.GlobalGroup;
import com.constellio.model.entities.security.global.GlobalGroupStatus;
import com.constellio.sdk.tests.ConstellioTest;

public class GlobalGroupsManagerAcceptanceTest extends ConstellioTest {
	GlobalGroupsManager manager;
	GlobalGroup globalGroup1;
	GlobalGroup globalGroup2;
	GlobalGroup globalGroup3;
	GlobalGroup globalGroup1_1;
	GlobalGroup globalGroup1_2;
	GlobalGroup globalGroup2_1;
	GlobalGroup globalGroup1_1_1;

	@Before
	public void setUp()
			throws Exception {
		manager = getModelLayerFactory().getGlobalGroupsManager();

		globalGroup1 = manager.create("group1", null, GlobalGroupStatus.ACTIVE, true);
		globalGroup2 = manager.create("group2", null, GlobalGroupStatus.ACTIVE, true);
		globalGroup3 = manager.create("group3", null, GlobalGroupStatus.ACTIVE, true);
		globalGroup1_1 = manager.create("group1_1", "group1", GlobalGroupStatus.ACTIVE, true);
		globalGroup1_2 = manager.create("group1_2", "group1", GlobalGroupStatus.ACTIVE, true);
		globalGroup1_1_1 = manager.create("group1_1_1", "group1_1", GlobalGroupStatus.ACTIVE, true);
		globalGroup2_1 = manager.create("group2_1", "group2", GlobalGroupStatus.ACTIVE, true);
	}

	@Test
	public void whenAddGlobalGroupsThenTheyAreAddedInList()
			throws Exception {
		manager.addUpdate(globalGroup1);
		manager.addUpdate(globalGroup2);
		manager.addUpdate(globalGroup1);

		assertThat(manager.getActiveGroups()).extracting("code", "status").containsExactly(
				tuple(globalGroup1.getCode(), globalGroup1.getStatus()), tuple(globalGroup2.getCode(), globalGroup2.getStatus()));
	}

	@Test
	public void givenGlobalGroupInListWhenUpdateItThenItIsUpdated()
			throws Exception {
		manager.addUpdate(globalGroup1);

		globalGroup1 = manager.create("group1", "group1Name", Arrays.asList("user1"), null, GlobalGroupStatus.ACTIVE, true);

		manager.addUpdate(globalGroup1);

		assertThat(manager.getActiveGroups()).hasSize(1);
		assertThat(manager.getGlobalGroupWithCode("group1").getName()).isEqualTo("group1Name");
		assertThat(manager.getGlobalGroupWithCode("group1").getUsersAutomaticallyAddedToCollections().get(0)).isEqualTo("user1");
	}

	@Test
	public void givenGlobalGroupInListWhenRemoveItThenItIsLogicallyRemoved()
			throws Exception {
		manager.addUpdate(globalGroup1);

		globalGroup1 = manager.create("group1", "group1Name", Arrays.asList("user1"), null, GlobalGroupStatus.ACTIVE, true);

		manager.logicallyRemoveGroup(globalGroup1);

		assertThat(manager.getActiveGroups()).isEmpty();
		assertThat(manager.getAllGroups()).hasSize(1);
		assertThat(manager.getGlobalGroupWithCode("group1").getStatus()).isEqualTo(GlobalGroupStatus.INACTIVE);
	}

	@Test
	public void givenMultipleGlobalGroupsWhenRemoveFromStartThenAllRemoved()
			throws Exception {
		manager.addUpdate(globalGroup1);
		manager.addUpdate(globalGroup2);
		manager.addUpdate(globalGroup3);

		assertThat(manager.getActiveGroups()).containsExactly(globalGroup1, globalGroup2, globalGroup3);

		manager.logicallyRemoveGroup(globalGroup1);
		assertThat(manager.getActiveGroups()).containsExactly(globalGroup2, globalGroup3);

		manager.logicallyRemoveGroup(globalGroup2);
		assertThat(manager.getActiveGroups()).containsExactly(globalGroup3);

		manager.logicallyRemoveGroup(globalGroup3);
		assertThat(manager.getActiveGroups()).isEmpty();
	}

	@Test
	public void givenMultipleGlobalGroupsWhenRemoveFromEndThenAllRemoved()
			throws Exception {
		manager.addUpdate(globalGroup1);
		manager.addUpdate(globalGroup2);
		manager.addUpdate(globalGroup3);

		assertThat(manager.getActiveGroups()).containsOnlyOnce(globalGroup1, globalGroup2, globalGroup3);

		manager.logicallyRemoveGroup(globalGroup3);
		assertThat(manager.getActiveGroups()).containsOnlyOnce(globalGroup1, globalGroup2);

		manager.logicallyRemoveGroup(globalGroup2);
		assertThat(manager.getActiveGroups()).containsOnlyOnce(globalGroup1);

		manager.logicallyRemoveGroup(globalGroup1);
		assertThat(manager.getActiveGroups()).isEmpty();
	}

	@Test
	public void whenAddGroupWithAccentsThenCorrectlySaved()
			throws Exception {
		GlobalGroup groupWithAccents = manager.create("<é=e>", "<à=a>", Arrays.asList("<ç=c>"), null,
				GlobalGroupStatus.ACTIVE, true);

		manager.addUpdate(groupWithAccents);

		assertThat(manager.getGlobalGroupWithCode("<é=e>")).isEqualTo(groupWithAccents);
	}

	@Test
	public void whenAddSubGroupThenOk()
			throws Exception {
		manager.addUpdate(globalGroup1);
		manager.addUpdate(globalGroup2);
		manager.addUpdate(globalGroup1_1);
		manager.addUpdate(globalGroup1_2);
		manager.addUpdate(globalGroup1_1_1);

		assertThat(manager.getGlobalGroupWithCode(globalGroup1_1.getCode()).getParent()).isEqualTo(globalGroup1.getCode());
		assertThat(manager.getGlobalGroupWithCode(globalGroup1_2.getCode()).getParent()).isEqualTo(globalGroup1.getCode());
		assertThat(manager.getGlobalGroupWithCode(globalGroup1_1_1.getCode()).getParent()).isEqualTo(globalGroup1_1.getCode());
	}

	@Test
	public void whenAddSubGroupWithParentThatNotExistsThenException()
			throws Exception {
		globalGroup1 = manager.create("group1", "inexistentGroup", GlobalGroupStatus.ACTIVE, true);

		try {
			manager.addUpdate(globalGroup1);
		} catch (Exception e) {
			assertThat(e.getMessage()).isEqualTo("Global group parent not found!");
		} finally {
			assertThat(manager.getActiveGroups()).isEmpty();
		}
	}

	@Test
	public void givenExistentGroupWhenUpdateSubGroupWithParentIsItselfThenException()
			throws Exception {
		manager.addUpdate(globalGroup1);
		globalGroup1 = manager.create("group1", "group1", GlobalGroupStatus.ACTIVE, true);

		assertThat(manager.getActiveGroups()).hasSize(1);
		try {
			manager.addUpdate(globalGroup1);
		} catch (Exception e) {
			assertThat(e.getMessage()).isEqualTo("Invalid parent: group1");
		} finally {
			assertThat(manager.getActiveGroups()).hasSize(1);
			assertThat(manager.getActiveGroups().get(0).getParent()).isNull();
		}
	}

	@Test
	public void whenAddSubGroupWithParentIsItsChildThenException()
			throws Exception {
		manager.addUpdate(globalGroup1);
		manager.addUpdate(globalGroup1_1);

		globalGroup1 = manager.create("group1", "group1_1", GlobalGroupStatus.ACTIVE, true);

		try {
			manager.addUpdate(globalGroup1);
		} catch (Exception e) {
			assertThat(e.getMessage()).isEqualTo("Invalid parent: group1_1");
		} finally {
			assertThat(manager.getActiveGroups()).hasSize(2);
			assertThat(manager.getGlobalGroupWithCode("group1").getParent()).isNull();
			assertThat(manager.getGlobalGroupWithCode("group1_1").getParent()).isEqualTo("group1");
		}
	}

	@Test
	public void whenRemoveThenRemoveChildren()
			throws Exception {
		manager.addUpdate(globalGroup1);
		manager.addUpdate(globalGroup1_1);
		manager.addUpdate(globalGroup1_2);
		manager.addUpdate(globalGroup1_1_1);
		manager.addUpdate(globalGroup2);
		manager.addUpdate(globalGroup2_1);

		manager.logicallyRemoveGroup(globalGroup1);

		assertThat(manager.getActiveGroups()).containsOnly(globalGroup2, globalGroup2_1);
		assertThat(manager.getAllGroups()).hasSize(6);
		assertThat(manager.getGlobalGroupWithCode(globalGroup1.getCode()).getStatus()).isEqualTo(GlobalGroupStatus.INACTIVE);
		assertThat(manager.getActiveGlobalGroupWithCode(globalGroup1.getCode())).isNull();
		assertThat(manager.getGlobalGroupWithCode(globalGroup1_1.getCode()).getStatus()).isEqualTo(GlobalGroupStatus.INACTIVE);
		assertThat(manager.getActiveGlobalGroupWithCode(globalGroup1_1.getCode())).isNull();
		assertThat(manager.getGlobalGroupWithCode(globalGroup1_2.getCode()).getStatus()).isEqualTo(GlobalGroupStatus.INACTIVE);
		assertThat(manager.getActiveGlobalGroupWithCode(globalGroup1_2.getCode())).isNull();
		assertThat(manager.getGlobalGroupWithCode(globalGroup1_1_1.getCode()).getStatus()).isEqualTo(GlobalGroupStatus.INACTIVE);
		assertThat(manager.getActiveGlobalGroupWithCode(globalGroup1_1_1.getCode())).isNull();
		assertThat(manager.getGlobalGroupWithCode(globalGroup2.getCode()).getStatus()).isEqualTo(GlobalGroupStatus.ACTIVE);
		assertThat(manager.getActiveGlobalGroupWithCode(globalGroup2.getCode()).getCode()).isEqualTo(globalGroup2.getCode());
		assertThat(manager.getGlobalGroupWithCode(globalGroup2_1.getCode()).getStatus()).isEqualTo(GlobalGroupStatus.ACTIVE);
		assertThat(manager.getActiveGlobalGroupWithCode(globalGroup2_1.getCode()).getCode()).isEqualTo(globalGroup2_1.getCode());
	}

	@Test
	public void whenRemoveAndActiveThenActiveHierarchy()
			throws Exception {
		manager.addUpdate(globalGroup1);
		manager.addUpdate(globalGroup1_1);
		manager.addUpdate(globalGroup1_2);
		manager.addUpdate(globalGroup1_1_1);
		manager.addUpdate(globalGroup2);
		manager.addUpdate(globalGroup2_1);

		manager.logicallyRemoveGroup(globalGroup1);
		manager.activateGlobalGroupHierarchy(globalGroup1);

		assertThat(manager.getActiveGroups()).hasSize(6);
		assertThat(manager.getAllGroups()).hasSize(6);
		assertThat(manager.getGlobalGroupWithCode(globalGroup1.getCode()).getStatus()).isEqualTo(GlobalGroupStatus.ACTIVE);
		assertThat(manager.getGlobalGroupWithCode(globalGroup1_1.getCode()).getStatus()).isEqualTo(GlobalGroupStatus.ACTIVE);
		assertThat(manager.getGlobalGroupWithCode(globalGroup1_2.getCode()).getStatus()).isEqualTo(GlobalGroupStatus.ACTIVE);
		assertThat(manager.getGlobalGroupWithCode(globalGroup1_1_1.getCode()).getStatus()).isEqualTo(GlobalGroupStatus.ACTIVE);
		assertThat(manager.getGlobalGroupWithCode(globalGroup2.getCode()).getStatus()).isEqualTo(GlobalGroupStatus.ACTIVE);
		assertThat(manager.getGlobalGroupWithCode(globalGroup2_1.getCode()).getStatus()).isEqualTo(GlobalGroupStatus.ACTIVE);
	}
}
