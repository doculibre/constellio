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
package com.constellio.app.ui.pages.user;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URLEncoder;

import org.junit.Before;
import org.mockito.Mock;

import com.constellio.app.ui.application.ConstellioNavigator;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.entities.UserCredentialVO;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.MockedFactories;

public class ListUserCredentialsPresenterTest extends ConstellioTest {

	ListUserCredentialsPresenter presenter;
	MockedFactories mockedFactories = new MockedFactories();

	@Mock ListUsersCredentialsView userView;
	@Mock UserCredentialVO userCredentialVO;
	@Mock ConstellioNavigator navigator;

	@Before
	public void setUp()
			throws Exception {

		when(userView.getSessionContext()).thenReturn(FakeSessionContext.dakotaInCollection(zeCollection));
		when(userView.getConstellioFactories()).thenReturn(mockedFactories.getConstellioFactories());

		when(userView.navigateTo()).thenReturn(navigator);

		when(userCredentialVO.getUsername()).thenReturn("dakota");

		presenter = spy(new ListUserCredentialsPresenter(userView));
	}

	//@Test
	public void whenAddButtonClickedThenNavigateToAddEditUserCredentialView()
			throws Exception {

		presenter.addButtonClicked();

		verify(userView.navigateTo(), times(1)).addUserCredential(NavigatorConfigurationService.USER_LIST + "/");
	}

	//@Test
	public void whenEditButtonClickedThenNavigateToAddEditUserCredentialView()
			throws Exception {

		presenter.editButtonClicked(userCredentialVO);

		verify(userView.navigateTo(), times(1))
				.editUserCredential(
						NavigatorConfigurationService.USER_LIST + "/" + URLEncoder.encode("username=dakota", "UTF-8"));
	}

	//@Test
	public void whenDisplayButtonClickedThenNavigateToDisplayUserCredentialView()
			throws Exception {

		presenter.displayButtonClicked(userCredentialVO);

		verify(userView.navigateTo(), times(1)).displayUserCredential(
				NavigatorConfigurationService.USER_LIST + "/" + URLEncoder.encode("username=dakota", "UTF-8"));
	}
}
