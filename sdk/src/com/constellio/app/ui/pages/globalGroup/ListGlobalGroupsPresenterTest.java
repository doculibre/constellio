/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.ui.pages.globalGroup;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.mockito.Mock;

import com.constellio.app.ui.application.ConstellioNavigator;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.entities.GlobalGroupVO;
import com.constellio.app.ui.framework.data.GlobalGroupVODataProvider;
import com.constellio.model.entities.security.global.GlobalGroup;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.users.GlobalGroupsManager;
import com.constellio.model.services.users.UserCredentialsManager;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.MockedFactories;

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
	@Mock ConstellioNavigator navigator;
	@Mock UserCredentialsManager userCredentialsManager;
	@Mock GlobalGroupsManager globalGroupsManager;
	@Mock UserCredential dakotaCredential;
	@Mock GlobalGroup heroesGlobalGroup, legendsGlobalGroup;
	@Mock GlobalGroupVODataProvider globalGroupVODataProvider;

	@Before
	public void setUp()
			throws Exception {

		List<GlobalGroup> globalGroups = new ArrayList<>();
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
		when(mockedFactories.getModelLayerFactory().getUserCredentialsManager()).thenReturn(userCredentialsManager);
		when(mockedFactories.getModelLayerFactory().getGlobalGroupsManager()).thenReturn(globalGroupsManager);
		when(globalGroupsManager.getActiveGroups()).thenReturn(globalGroups);

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
		when(userCredentialsManager.getUserCredential(dakota)).thenReturn(dakotaCredential);

		presenter.deleteButtonClicked(heroesGlobalGroupVO);

		verify(userServices).logicallyRemoveGroupHierarchy(dakotaCredential, heroesGlobalGroup);
		verify(globalGroupView).refreshTable();
	}
}
