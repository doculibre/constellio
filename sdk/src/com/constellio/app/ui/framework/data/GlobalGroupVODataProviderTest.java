package com.constellio.app.ui.framework.data;

import com.constellio.app.ui.entities.GlobalGroupVO;
import com.constellio.app.ui.framework.builders.GlobalGroupToVOBuilder;
import com.constellio.model.entities.security.global.SystemWideGroup;
import com.constellio.model.entities.security.global.GlobalGroupStatus;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.users.SystemWideUserInfos;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.MockedFactories;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class GlobalGroupVODataProviderTest extends ConstellioTest {

	public static final String HEROES = "Heroes";
	public static final String HEROES_GROUP = "Heroes group";
	public static final String HEROES_1 = "Heroes_1";
	public static final String HEROES_GROUP_1 = "Heroes group 1";
	public static final String LEGENDS = "Legends";
	public static final String LEGENDS_GROUP = "Legends group";
	MockedFactories mockedFactories = new MockedFactories();
	GlobalGroupVODataProvider dataProvider;
	@Mock SystemWideUserInfos bob, dakota;
	@Mock UserServices userServices;
	@Mock GlobalGroupToVOBuilder voBuilder;
	@Mock SystemWideGroup heroesGroup, heroesGroup1, legendsGroup;
	@Mock GlobalGroupVO heroesGroupVO, heroesGroupVO1, legendsGroupVO;
	@Mock ModelLayerFactory modelLayerFactory;

	List<SystemWideGroup> globalGroups;
	Set<String> collections;

	@Before
	public void setUp()
			throws Exception {

		when(heroesGroup.getParent()).thenReturn(null);
		when(heroesGroup.getCode()).thenReturn(HEROES);
		when(heroesGroup.getName()).thenReturn(HEROES_GROUP);
		when(heroesGroup.getStatus()).thenReturn(GlobalGroupStatus.ACTIVE);
		when(heroesGroupVO.getParent()).thenReturn(null);
		when(heroesGroupVO.getCode()).thenReturn(HEROES);
		when(heroesGroupVO.getName()).thenReturn(HEROES_GROUP);
		when(heroesGroupVO.getStatus()).thenReturn(GlobalGroupStatus.ACTIVE);

		when(heroesGroup1.getParent()).thenReturn(HEROES);
		when(heroesGroup1.getCode()).thenReturn(HEROES_1);
		when(heroesGroup1.getName()).thenReturn(HEROES_GROUP_1);
		when(heroesGroup1.getStatus()).thenReturn(GlobalGroupStatus.ACTIVE);
		when(heroesGroupVO1.getParent()).thenReturn(HEROES);
		when(heroesGroupVO1.getCode()).thenReturn(HEROES_1);
		when(heroesGroupVO1.getName()).thenReturn(HEROES_GROUP_1);
		when(heroesGroupVO1.getStatus()).thenReturn(GlobalGroupStatus.ACTIVE);

		when(legendsGroup.getParent()).thenReturn(null);
		when(legendsGroup.getCode()).thenReturn(LEGENDS);
		when(legendsGroup.getName()).thenReturn(LEGENDS_GROUP);
		when(legendsGroup.getStatus()).thenReturn(GlobalGroupStatus.ACTIVE);
		when(legendsGroupVO.getParent()).thenReturn(null);
		when(legendsGroupVO.getCode()).thenReturn(LEGENDS);
		when(legendsGroupVO.getName()).thenReturn(LEGENDS_GROUP);
		when(legendsGroupVO.getStatus()).thenReturn(GlobalGroupStatus.ACTIVE);

		globalGroups = new ArrayList<>();
		globalGroups.add(legendsGroup);
		globalGroups.add(heroesGroup);
		globalGroups.add(heroesGroup1);

		collections = new HashSet<>();
		collections.add(zeCollection);

		when(userServices.getAllGroups()).thenReturn(globalGroups);
		when(mockedFactories.getModelLayerFactory().newUserServices()).thenReturn(userServices);
		when(voBuilder.build(heroesGroup)).thenReturn(heroesGroupVO);
		when(voBuilder.build(heroesGroup1)).thenReturn(heroesGroupVO1);
		when(voBuilder.build(legendsGroup)).thenReturn(legendsGroupVO);

		dataProvider = spy(new GlobalGroupVODataProvider(voBuilder, mockedFactories.getModelLayerFactory(), true));
	}

	@Test
	public void whenNewDataProviderThenItsNotNull()
			throws Exception {

		assertThat(dataProvider.listGlobalGroupVOs()).hasSize(3);
		assertThat(dataProvider.listGlobalGroupVOs().get(0)).isEqualTo(heroesGroupVO);
		assertThat(dataProvider.listGlobalGroupVOs().get(1)).isEqualTo(heroesGroupVO1);
		assertThat(dataProvider.listGlobalGroupVOs().get(2)).isEqualTo(legendsGroupVO);
	}

	@Test
	public void whenListThenListIndexes()
			throws Exception {
		List<Integer> indexes = dataProvider.list();

		assertThat(indexes).hasSize(3);
		assertThat(indexes.get(0)).isEqualTo(0);
		assertThat(dataProvider.getGlobalGroupVO(0)).isEqualTo(heroesGroupVO);
		assertThat(indexes.get(1)).isEqualTo(1);
		assertThat(dataProvider.getGlobalGroupVO(1)).isEqualTo(heroesGroupVO1);
		assertThat(indexes.get(2)).isEqualTo(2);
		assertThat(dataProvider.getGlobalGroupVO(2)).isEqualTo(legendsGroupVO);

	}

	@Test
	public void whenSizeThenOk()
			throws Exception {
		assertThat(dataProvider.size()).isEqualTo(3);
	}

	@Test
	public void whenGetGlobalGroupWithCodeThenOk()
			throws Exception {
		assertThat(dataProvider.getGlobalGroupVO(HEROES)).isEqualTo(heroesGroupVO);
	}

	@Test
	public void whenGetGlobalGroupWithIndexThenOk()
			throws Exception {
		assertThat(dataProvider.getGlobalGroupVO(0)).isEqualTo(heroesGroupVO);
	}

	@Test
	public void whenListAllGlobalGroupVOsThenOk()
			throws Exception {

		assertThat(dataProvider.listGlobalGroupVOs()).hasSize(3);
		assertThat(dataProvider.listGlobalGroupVOs().get(0)).isEqualTo(heroesGroupVO);
		assertThat(dataProvider.listGlobalGroupVOs().get(1)).isEqualTo(heroesGroupVO1);
		assertThat(dataProvider.listGlobalGroupVOs().get(2)).isEqualTo(legendsGroupVO);
	}

	@Test
	public void whenListCodesThenOk()
			throws Exception {
		assertThat(dataProvider.listCodes(Arrays.asList(legendsGroupVO, heroesGroupVO, heroesGroupVO1)))
				.containsOnly(LEGENDS, HEROES, HEROES_1);

	}

	@Test
	public void whenListGlobalGroupsVOFromUserThenReturnSortedList()
			throws Exception {

		when(bob.getUsername()).thenReturn("bob");
		when(userServices.getGlobalGroupActifUsers(HEROES)).thenReturn(Arrays.asList(bob));
		when(userServices.getGlobalGroupActifUsers(LEGENDS)).thenReturn(Arrays.asList(bob));

		assertThat(dataProvider.listActiveGlobalGroupVOsFromUser("bob")).containsOnly(heroesGroupVO, legendsGroupVO);
		assertThat(dataProvider.listActiveGlobalGroupVOsFromUser("bob").get(0)).isEqualTo(heroesGroupVO);
	}

	@Test
	public void whenListGlobalGroupsNotContainingUserThenOk()
			throws Exception {

		when(bob.getUsername()).thenReturn("bob");
		when(userServices.getGlobalGroupActifUsers(LEGENDS)).thenReturn(Arrays.asList(bob));

		assertThat(dataProvider.listGlobalGroupVOsNotContainingUser("bob")).containsOnly(heroesGroupVO, heroesGroupVO1);
	}

	@Test
	public void whenSetFilterThenOk()
			throws Exception {

		dataProvider.setFilter("heroes");

		assertThat(dataProvider.listGlobalGroupVOs()).hasSize(2);
		assertThat(dataProvider.size()).isEqualTo(2);
	}

	@Test
	public void whenListGlobalGroupVOsWithUsersInCollectionThenOk()
			throws Exception {

		when(heroesGroupVO.getCollections()).thenReturn(collections);
		when(legendsGroupVO.getCollections()).thenReturn(new HashSet<String>());
		when(dataProvider.getGlobalGroupVOs()).thenReturn(Arrays.asList(heroesGroupVO, legendsGroupVO));

		assertThat(dataProvider.listGlobalGroupVOsWithUsersInCollection(zeCollection)).containsOnly(heroesGroupVO);
	}

	@Test
	public void whenListActiveGlobalGroupVOsWithUsersInCollectionThenOk()
			throws Exception {

		when(heroesGroupVO.getCollections()).thenReturn(collections);
		when(heroesGroupVO.getStatus()).thenReturn(GlobalGroupStatus.ACTIVE);
		when(legendsGroupVO.getCollections()).thenReturn(collections);
		when(legendsGroupVO.getStatus()).thenReturn(GlobalGroupStatus.INACTIVE);
		when(dataProvider.getGlobalGroupVOs()).thenReturn(Arrays.asList(heroesGroupVO, legendsGroupVO));

		assertThat(dataProvider.listActiveGlobalGroupVOsWithUsersInCollection(zeCollection)).containsOnly(heroesGroupVO);
	}

	@Test
	public void whenListSubGroupVOsThenOk()
			throws Exception {

		assertThat(dataProvider.listActiveSubGlobalGroupsVOsFromGroup(HEROES)).containsOnly(heroesGroupVO1);
		assertThat(dataProvider.listActiveSubGlobalGroupsVOsFromGroup(HEROES_1)).isEmpty();
		assertThat(dataProvider.listActiveSubGlobalGroupsVOsFromGroup(LEGENDS)).isEmpty();
	}

	@Test
	public void whenListSubGroupVOsThenListOnlyActivesGroups()
			throws Exception {

		when(heroesGroupVO1.getStatus()).thenReturn(GlobalGroupStatus.INACTIVE).thenReturn(GlobalGroupStatus.ACTIVE);

		assertThat(dataProvider.listActiveSubGlobalGroupsVOsFromGroup(HEROES)).isEmpty();
		assertThat(dataProvider.listActiveSubGlobalGroupsVOsFromGroup(HEROES)).containsOnly(heroesGroupVO1);
	}

	@Test
	public void whenListBaseGroupVOsThenOk()
			throws Exception {

		assertThat(dataProvider.listBaseGlobalGroupsVOs()).containsOnly(heroesGroupVO, legendsGroupVO);
	}

	@Test
	public void givenDeletedGlobalGroupWhenListGlobalGroupsWithStatusDeletedThenOk()
			throws Exception {

		when(heroesGroupVO.getStatus()).thenReturn(GlobalGroupStatus.INACTIVE);

		assertThat(dataProvider.listBaseGlobalGroupsVOsWithStatus(GlobalGroupStatus.INACTIVE)).hasSize(1);
		assertThat(dataProvider.listBaseGlobalGroupsVOsWithStatus(GlobalGroupStatus.INACTIVE).get(0).getCode())
				.isEqualTo(HEROES);
	}

	@Test
	public void whenSubListThenOk()
			throws Exception {
		assertThat(dataProvider.listGlobalGroupVOs(0, 1)).hasSize(1);
		assertThat(dataProvider.listGlobalGroupVOs(0, 1).get(0).getCode()).isEqualTo(HEROES);
	}

	@Test
	public void givenGreaterStartIndexWhenSubListThenReturnEmptyList()
			throws Exception {
		assertThat(dataProvider.listGlobalGroupVOs(10, 19)).hasSize(0);
	}

	@Test
	public void givenGreaterCounterWhenSubListThenOk()
			throws Exception {

		assertThat(dataProvider.listGlobalGroupVOs(0, 20)).hasSize(3);
	}
}


