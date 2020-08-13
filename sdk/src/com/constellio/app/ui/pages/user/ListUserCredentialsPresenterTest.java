package com.constellio.app.ui.pages.user;

import com.constellio.app.ui.application.CoreViews;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.entities.UserCredentialVO;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.MockedFactories;
import org.junit.Before;
import org.mockito.Mock;

import java.net.URLEncoder;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ListUserCredentialsPresenterTest extends ConstellioTest {

	ListUserCredentialsPresenter presenter;
	MockedFactories mockedFactories = new MockedFactories();

	@Mock ListUsersCredentialsView userView;
	@Mock UserCredentialVO userCredentialVO;
	@Mock CoreViews navigator;

	@Before
	public void setUp()
			throws Exception {

		when(userView.getSessionContext()).thenReturn(FakeSessionContext.dakotaInCollection(zeCollection));
		when(userView.getConstellioFactories()).thenReturn(mockedFactories.getConstellioFactories());

		when(userView.navigate().to()).thenReturn(navigator);

		when(userCredentialVO.getUsername()).thenReturn("dakota");

		presenter = spy(new ListUserCredentialsPresenter(userView));
	}

	//@Test
	public void whenAddButtonClickedThenNavigateToAddEditUserCredentialView()
			throws Exception {

		presenter.addButtonClicked();

		verify(userView.navigate().to(), times(1)).addEditUserCredential(NavigatorConfigurationService.USER_LIST + "/");
	}

	//@Test
	public void whenEditButtonClickedThenNavigateToAddEditUserCredentialView()
			throws Exception {

		presenter.editButtonClicked(userCredentialVO);

		verify(userView.navigate().to(), times(1))
				.editUserCredential(
						NavigatorConfigurationService.USER_LIST + "/" + URLEncoder.encode("username=dakota", "UTF-8"));
	}

	//@Test
	public void whenDisplayButtonClickedThenNavigateToDisplayUserCredentialView()
			throws Exception {

		presenter.displayButtonClicked(userCredentialVO);

		verify(userView.navigate().to(), times(1)).displayUserCredential(
				NavigatorConfigurationService.USER_LIST + "/" + URLEncoder.encode("username=dakota", "UTF-8"));
	}
}
