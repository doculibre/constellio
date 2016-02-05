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
