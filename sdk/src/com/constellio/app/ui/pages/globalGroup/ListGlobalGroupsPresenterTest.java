package com.constellio.app.ui.pages.globalGroup;

import com.constellio.app.ui.application.CoreViews;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.entities.GlobalGroupVO;
import com.constellio.app.ui.framework.data.GlobalGroupVODataProvider;
import com.constellio.model.entities.security.global.SystemWideGroup;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.MockedFactories;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ListGlobalGroupsPresenterTest extends ConstellioTest {

	public static final String HEROES_GLOBAL_GROUP = "Heroes Global Group";
	public static final String HEROES = "heroes";
	public static final String LEGENDS = "Legends";
	public static final String LEGENDS_GROUP = "Legends group";
	ListGlobalGroupsPresenter presenter;
	MockedFactories mockedFactories = new MockedFactories();

	@Mock ListGlobalGroupsViewImpl globalGroupView;
	@Mock UserServices userServices;
	@Mock MetadataSchemasManager schemasManager;
	@Mock LogicalSearchQuery query;
	@Mock GlobalGroupVODataProvider dataProvider;
	@Mock GlobalGroupVO heroesGlobalGroupVO, legendsGlobalGroupVO;
	@Mock CoreViews navigator;
	@Mock UserCredential dakotaCredential;
	@Mock SystemWideGroup heroesGlobalGroup, legendsGlobalGroup;
	@Mock GlobalGroupVODataProvider globalGroupVODataProvider;

	@Before
	public void setUp()
			throws Exception {

		List<SystemWideGroup> globalGroups = new ArrayList<>();
		globalGroups.add(heroesGlobalGroup);
		globalGroups.add(legendsGlobalGroup);

		when(globalGroupView.getConstellioFactories()).thenReturn(mockedFactories.getConstellioFactories());
		when(globalGroupView.getSessionContext()).thenReturn(FakeSessionContext.dakotaInCollection(zeCollection));
		when(globalGroupView.navigateTo()).thenReturn(navigator);

		when(heroesGlobalGroupVO.getCode()).thenReturn(HEROES);
		when(heroesGlobalGroupVO.getName()).thenReturn(HEROES_GLOBAL_GROUP);
		when(legendsGlobalGroupVO.getCode()).thenReturn(LEGENDS);
		when(legendsGlobalGroupVO.getName()).thenReturn(LEGENDS_GROUP);

		when(mockedFactories.getModelLayerFactory().newUserServices()).thenReturn(userServices);
		when(userServices.getGroup(HEROES)).thenReturn(heroesGlobalGroup);
		when(userServices.getActiveGroups()).thenReturn(globalGroups);

		presenter = spy(new ListGlobalGroupsPresenter(globalGroupView));
		doReturn(dataProvider).when(presenter).getDataProvider();

	}

	//@Test
	public void whenAddButtonClickedThenNavigateToAddEditGroupView()
			throws Exception {

		presenter.addButtonClicked();

		verify(globalGroupView.navigateTo(), times(1)).addGlobalGroup(NavigatorConfigurationService.GROUP_LIST + "/");
	}

	//@Test
	public void whenEditButtonClickedThenNavigateAddEditGroupViewWithTheRightGroup()
			throws Exception {

		presenter.editButtonClicked(heroesGlobalGroupVO);

		verify(globalGroupView.navigateTo(), times(1)).editGlobalGroup(
				NavigatorConfigurationService.GROUP_LIST + "/" + URLEncoder.encode("globalGroupCode=heroes", "UTF-8"));
	}

	//@Test
	public void whenDisplayButtonClickedThenNavigateToDisplayGroupView()
			throws Exception {

		when(heroesGlobalGroupVO.getCode()).thenReturn(HEROES);

		presenter.displayButtonClicked(heroesGlobalGroupVO);

		verify(globalGroupView.navigateTo(), times(1)).displayGlobalGroup(
				NavigatorConfigurationService.GROUP_LIST + "/" + URLEncoder.encode("globalGroupCode=heroes", "UTF-8"));
	}

	//@Test
	public void whenDeleteButtonClickedThenRemoveGroup()
			throws Exception {
		when(userServices.getUserCredential(dakota)).thenReturn(dakotaCredential);

		presenter.deleteButtonClicked(heroesGlobalGroupVO);

		verify(userServices).logicallyRemoveGroupHierarchy(dakotaCredential.getUsername(), heroesGlobalGroup);
		verify(globalGroupView).refreshTable();
	}

	@Test
	public void givenLDAPAuthenticationAndSyncronizedWhenListGlobalGroupsThenCannotAddEditOrDeleteGroup()
			throws Exception {

		when(userServices.canAddOrModifyUserAndGroup()).thenReturn(false);

		assertThat(presenter.canAddOrModify()).isFalse();
	}
}
