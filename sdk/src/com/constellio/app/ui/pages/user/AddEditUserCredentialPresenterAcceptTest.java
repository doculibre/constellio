package com.constellio.app.ui.pages.user;

import com.constellio.app.ui.entities.UserCredentialVO;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.MockedNavigation;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AddEditUserCredentialPresenterAcceptTest extends ConstellioTest {

	public static final String HEROES = "heroes";
	public static final String DAKOTA = "dakota";

	@Mock AddEditUserCredentialView userCredentialView;
	MockedNavigation navigator;
	UserServices userServices;
	UserCredential dakotaCredential, newUserCredential;
	UserCredentialVO dakotaCredentialVO, newUserCredentialVO;
	AddEditUserCredentialPresenter presenter;
	SessionContext sessionContext;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withAllTestUsers(),
				withCollection("otherCollection")
		);

		navigator = new MockedNavigation();

		sessionContext = FakeSessionContext.adminInCollection(zeCollection);
		sessionContext.setCurrentLocale(Locale.FRENCH);
		userServices = getModelLayerFactory().newUserServices();
		when(userCredentialView.getSessionContext()).thenReturn(sessionContext);
		when(userCredentialView.getCollection()).thenReturn(zeCollection);
		when(userCredentialView.getConstellioFactories()).thenReturn(getConstellioFactories());
		when(userCredentialView.navigate()).thenReturn(navigator);

		presenter = spy(new AddEditUserCredentialPresenter(userCredentialView));

		doNothing().when(presenter).showErrorMessageView(anyString());

		givenBreadCrumbAndParameters();
	}

	private void givenBreadCrumbAndParameters() {
		Map<String, String> paramsMap = new HashMap<>();
		paramsMap.put("username", DAKOTA);
	}

	@Test
	public void givenUsernameWhenGetUserCredentialVOThenReturnVO()
			throws Exception {

		UserCredentialVO userCredentialVO = presenter.getUserCredentialVO(DAKOTA);

		assertThat(userCredentialVO.getUsername()).isEqualTo(DAKOTA);
	}

	@Test
	public void givenNoUserNameWhenGetUserCredentialVOThenNewUserCredentialVO()
			throws Exception {

		UserCredentialVO userCredentialVO = presenter.getUserCredentialVO("");

		assertThat(userCredentialVO).isNotNull();
		assertThat(userCredentialVO.getUsername()).isNull();
	}

	@Test
	public void givenEditActionWhenSaveButtonClickedThenSaveChanges()
			throws Exception {

		dakotaCredentialVO = presenter.getUserCredentialVO(DAKOTA);
		dakotaCredentialVO.setFirstName("Dakota1");
		Set collectionsSet = new HashSet<>();
		collectionsSet.add(zeCollection);
		collectionsSet.add("otherCollection");
		dakotaCredentialVO.setCollections(collectionsSet);
		dakotaCredentialVO.setEmail("dakota1@constellio.com");
		dakotaCredentialVO.setGlobalGroups(Arrays.asList(HEROES));
		dakotaCredentialVO.setLastName("Lindien1");

		presenter.saveButtonClicked(dakotaCredentialVO);

		dakotaCredential = userServices.getUserCredential(DAKOTA);
		assertThat(dakotaCredential.getFirstName()).isEqualTo("Dakota1");
		assertThat(dakotaCredential.getGlobalGroups()).containsOnly(HEROES);
		assertThat(dakotaCredential.getCollections()).containsOnly(zeCollection, "otherCollection");
		assertThat(dakotaCredential.getLastName()).isEqualTo("Lindien1");
		assertThat(dakotaCredential.getEmail()).isEqualTo("dakota1@constellio.com");
	}

	@Test
	public void givenEditActionAndChangedUsernameWhenSaveButtonClickedThenDoNothing()
			throws Exception {

		dakotaCredentialVO = presenter.getUserCredentialVO(DAKOTA);
		dakotaCredentialVO.setUsername("dakota1");
		dakotaCredentialVO.setFirstName("Dakota1");
		Set collectionsSet = new HashSet<>();
		collectionsSet.add(zeCollection);
		collectionsSet.add("otherCollection");
		dakotaCredentialVO.setCollections(collectionsSet);
		dakotaCredentialVO.setEmail("dakota1@constellio.com");
		dakotaCredentialVO.setGlobalGroups(Arrays.asList(HEROES));
		dakotaCredentialVO.setLastName("Lindien1");

		presenter.saveButtonClicked(dakotaCredentialVO);

		dakotaCredential = userServices.getUserCredential(DAKOTA);
		verify(userCredentialView, never()).navigate();
		assertThat(dakotaCredential.getFirstName()).isEqualTo("Dakota");
		assertThat(dakotaCredential.getGlobalGroups()).containsOnly(HEROES);
		assertThat(dakotaCredential.getCollections()).containsOnly(zeCollection);
		assertThat(dakotaCredential.getLastName()).isEqualTo("L'Indien");
		assertThat(dakotaCredential.getEmail()).isEqualTo("dakota@doculibre.com");
	}

	@Test
	public void givenAddActionWhenSaveButtonClickedThenSaveChanges()
			throws Exception {

		newUserCredentialVO = presenter.getUserCredentialVO("");
		newUserCredentialVO.setUsername("user");
		newUserCredentialVO.setFirstName("User");
		Set collectionsSet = new HashSet<>();
		collectionsSet.add(zeCollection);
		collectionsSet.add("otherCollection");
		newUserCredentialVO.setCollections(collectionsSet);
		newUserCredentialVO.setEmail("user@constellio.com");
		newUserCredentialVO.setGlobalGroups(Arrays.asList(HEROES));
		newUserCredentialVO.setLastName("lastName");
		newUserCredentialVO.setPassword("password");
		newUserCredentialVO.setConfirmPassword("password");

		presenter.saveButtonClicked(newUserCredentialVO);

		newUserCredential = userServices.getUserCredential("user");
		assertThat(newUserCredential.getFirstName()).isEqualTo("User");
		assertThat(newUserCredential.getGlobalGroups()).containsOnly(HEROES);
		assertThat(newUserCredential.getCollections()).containsOnly(zeCollection, "otherCollection");
		assertThat(newUserCredential.getLastName()).isEqualTo("lastName");
		assertThat(newUserCredential.getEmail()).isEqualTo("user@constellio.com");
	}

	@Test
	public void givenAddActionAndDifferentConfirmPasswordWhenSaveButtonClickedThenDoNotSaveChanges()
			throws Exception {

		newUserCredentialVO = presenter.getUserCredentialVO("");
		newUserCredentialVO.setFirstName("User");
		Set collectionsSet = new HashSet<>();
		collectionsSet.add(zeCollection);
		collectionsSet.add("otherCollection");
		newUserCredentialVO.setCollections(collectionsSet);
		newUserCredentialVO.setEmail("user@constellio.com");
		newUserCredentialVO.setGlobalGroups(Arrays.asList(HEROES));
		newUserCredentialVO.setLastName("lastName");
		newUserCredentialVO.setPassword("password");
		newUserCredentialVO.setConfirmPassword("password1");

		presenter.saveButtonClicked(newUserCredentialVO);

		verify(userCredentialView, never()).navigate();
		assertThat(userServices.getUserCredential("user")).isNull();
	}

	@Test
	public void givenAddActionAndExistingUsernameWhenSaveButtonClickedThenDoNotSaveChanges()
			throws Exception {

		newUserCredentialVO = presenter.getUserCredentialVO("");
		newUserCredentialVO.setUsername("bob");
		newUserCredentialVO.setFirstName("User");
		Set collectionsSet = new HashSet<>();
		collectionsSet.add(zeCollection);
		collectionsSet.add("otherCollection");
		newUserCredentialVO.setCollections(collectionsSet);
		newUserCredentialVO.setEmail("user@constellio.com");
		newUserCredentialVO.setGlobalGroups(Arrays.asList(HEROES));
		newUserCredentialVO.setLastName("lastName");
		newUserCredentialVO.setPassword("password");
		newUserCredentialVO.setConfirmPassword("password");

		presenter.saveButtonClicked(newUserCredentialVO);

		verify(userCredentialView, never()).navigate();
		assertThat(userServices.getUserCredential("user")).isNull();
	}

	@Test
	public void givenSaveButtonIsClickedThenPersonalEmailsIsSaved()
			throws Exception {

		newUserCredentialVO = presenter.getUserCredentialVO("");
		newUserCredentialVO.setUsername("user");
		newUserCredentialVO.setFirstName("User");
		Set collectionsSet = new HashSet<>();
		collectionsSet.add(zeCollection);
		collectionsSet.add("otherCollection");
		newUserCredentialVO.setCollections(collectionsSet);
		newUserCredentialVO.setEmail("user@constellio.com");
		newUserCredentialVO.setGlobalGroups(Arrays.asList(HEROES));
		newUserCredentialVO.setLastName("lastName");
		newUserCredentialVO.setPassword("password");
		newUserCredentialVO.setConfirmPassword("password");
		newUserCredentialVO.setPersonalEmails("admin@gmail.com\nadmin@hotmail.com");

		presenter.saveButtonClicked(newUserCredentialVO);

		newUserCredential = userServices.getUserCredential("user");
		assertThat(newUserCredential.getFirstName()).isEqualTo("User");
		assertThat(newUserCredential.getGlobalGroups()).containsOnly(HEROES);
		assertThat(newUserCredential.getCollections()).containsOnly(zeCollection, "otherCollection");
		assertThat(newUserCredential.getLastName()).isEqualTo("lastName");
		assertThat(newUserCredential.getEmail()).isEqualTo("user@constellio.com");
		assertThat(newUserCredential.getPersonalEmails()).isEqualTo(Arrays.asList("admin@gmail.com", "admin@hotmail.com"));
	}
}
