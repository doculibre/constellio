package com.constellio.app.ui.pages.user;

import com.constellio.app.ui.application.CoreViews;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.entities.UserCredentialVO;
import com.constellio.app.ui.framework.builders.GlobalGroupToVOBuilder;
import com.constellio.app.ui.framework.builders.UserCredentialToVOBuilder;
import com.constellio.app.ui.framework.data.GlobalGroupVODataProvider;
import com.constellio.model.entities.security.global.GlobalGroup;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.users.SolrUserCredentialsManager;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.MockedFactories;
import org.junit.Before;
import org.mockito.Mock;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DisplayUserCredentialPresenterTest extends ConstellioTest {

	public static final String HEROES = "heroes";
	public static final String DAKOTA_INDIEN = "dakota.indien";

	@Mock DisplayUserCredentialView userCredentialView;
	@Mock UserServices userServices;
	@Mock CoreViews navigator;
	@Mock SolrUserCredentialsManager userCredentialsManager;
	@Mock UserCredential dakotaCredential, newDakotaCredential;
	@Mock GlobalGroup heroesGlobalGroup;
	@Mock GlobalGroupVODataProvider globalGroupVODataProvider;
	@Mock UserCredentialVO dakotaCredentialVO;
	@Mock UserCredentialToVOBuilder voBuilder;
	@Mock GlobalGroupToVOBuilder globalGroupToVOBuilder;

	DisplayUserCredentialPresenter presenter;
	MockedFactories mockedFactories = new MockedFactories();

	@Before
	public void setUp()
			throws Exception {

		List<GlobalGroup> globalGroups = new ArrayList<>();
		globalGroups.add(heroesGlobalGroup);

		when(userCredentialView.getConstellioFactories()).thenReturn(mockedFactories.getConstellioFactories());
		when(userCredentialView.getSessionContext()).thenReturn(FakeSessionContext.dakotaInCollection(zeCollection));
		when(userCredentialView.navigate().to()).thenReturn(navigator);

		when(mockedFactories.getModelLayerFactory().newUserServices()).thenReturn(userServices);

		presenter = spy(new DisplayUserCredentialPresenter(userCredentialView));

		givenBreadCrumbAndParameters();

		when(userServices.getUserCredential(DAKOTA_INDIEN)).thenReturn(dakotaCredential);
		when(presenter.newUserCredentialToVOBuilder()).thenReturn(voBuilder);
		when(voBuilder.build(dakotaCredential)).thenReturn(dakotaCredentialVO);
		when(dakotaCredentialVO.getUsername()).thenReturn(DAKOTA_INDIEN);
		when(dakotaCredentialVO.getGlobalGroups()).thenReturn(Arrays.asList(HEROES));
	}

	void givenBreadCrumbAndParameters() {
		Map<String, String> paramsMap = new HashMap<>();
		paramsMap.put("username", DAKOTA_INDIEN);
		presenter.setParamsMap(paramsMap);
		presenter.setBreadCrumb("url1/url2/url3");
	}

	//@Test
	public void givenUsernameWhenGetUserCredentialVOThenReturnVO()
			throws Exception {

		UserCredentialVO userCredentialVO = presenter.getUserCredentialVO(DAKOTA_INDIEN);

		assertThat(userCredentialVO.getUsername()).isEqualTo(DAKOTA_INDIEN);
	}

	//@Test
	public void whenBackButtonClickedThenNavigateToLastBreadCrumb()
			throws Exception {

		presenter.backButtonClicked();

		verify(userCredentialView.navigate().to(), times(1)).url("url3/url1/url2/" + URLEncoder.encode("username=dakota.indien",
				"UTF-8"));
	}

	//@Test
	public void whenDisplayGlobalGroupButtonClickedThenNavigateToDisplayGlobalGroupWithUsernameInParam()
			throws Exception {

		presenter.displayGlobalGroupButtonClicked(HEROES, DAKOTA_INDIEN);

		verify(userCredentialView.navigate().to(), times(1))
				.displayGlobalGroup("url1/url2/url3/" + NavigatorConfigurationService.USER_DISPLAY + "/" + URLEncoder
						.encode("username=dakota.indien;globalGroupCode=heroes",
								"UTF-8"));
	}

	//@Test
	public void whenEditGlobalGroupButtonClickedThenNavigateToDisplayGlobalGroupWithUsernameInParam()
			throws Exception {

		presenter.editGlobalGroupButtonClicked(HEROES, DAKOTA_INDIEN);

		verify(userCredentialView.navigate().to(), times(1))
				.editGlobalGroup("url1/url2/url3/" + NavigatorConfigurationService.USER_DISPLAY + "/" + URLEncoder
						.encode("username=dakota.indien;globalGroupCode=heroes",
								"UTF-8"));
	}

	//@Test
	public void whenDeleteGlobalGroupButtonClickedThenMoveItToAvailableGlobalGroupsList()
			throws Exception {

		presenter.deleteGlobalGroupButtonClicked(dakotaCredentialVO.getUsername(), HEROES);

		verify(userServices).removeUserFromGlobalGroup(DAKOTA_INDIEN, HEROES);
		verify(userCredentialView).refreshTable();
	}

	//@Test
	public void whenAddGlobalGroupButtonClickedThenMoveItToUsersGlobalGroupsList()
			throws Exception {

		List<String> dakotaGlobalGroups = new ArrayList();
		dakotaGlobalGroups.add("Legends");
		List<String> newDakotaGlobalGroups = new ArrayList();
		newDakotaGlobalGroups.add("Legends");
		newDakotaGlobalGroups.add(HEROES);
		when(dakotaCredential.getGlobalGroups()).thenReturn(dakotaGlobalGroups);
		when(newDakotaCredential.getGlobalGroups()).thenReturn(newDakotaGlobalGroups);
		when(dakotaCredential.setGlobalGroups(newDakotaGlobalGroups)).thenReturn(newDakotaCredential);

		presenter.addGlobalGroupButtonClicked(dakotaCredentialVO.getUsername(), HEROES);

		verify(dakotaCredential).setGlobalGroups(newDakotaGlobalGroups);
		verify(userServices).addUpdateUserCredential(newDakotaCredential);
		verify(userCredentialView).refreshTable();
	}

	//@Test
	public void whenGetGlobalGroupVODataProviderTheReturnIt()
			throws Exception {

		doReturn(globalGroupToVOBuilder).when(presenter).newGlobalGroupVOBuilder();
		doReturn(globalGroupVODataProvider).when(presenter).newGlobalGroupVODataProvider(globalGroupToVOBuilder);

		presenter.getGlobalGroupVODataProvider();

		verify(presenter).newGlobalGroupVODataProvider(globalGroupToVOBuilder);
	}
}
