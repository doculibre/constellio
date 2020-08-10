package com.constellio.app.ui.pages.login;

import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.setups.Users;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class LoginPresenter_enAcceptanceTest extends ConstellioTest {
	Users users = new Users();
	LoginPresenter loginPresenter;
	RecordServices recordServices;

	@Before
	public void setUp()
			throws Exception {
		givenSystemLanguageIs("en");
		//TODO Francis
		givenCollection(zeCollection, Arrays.asList("en", "fr")).withConstellioRMModule().withAllTestUsers();
		givenCollection(businessCollection, Arrays.asList("en")).withConstellioRMModule().withAllTestUsers();

		LoginView view = mock(LoginView.class);
		doReturn(zeCollection).when(view).getCollection();
		doReturn(FakeSessionContext.adminInCollection(zeCollection)).when(view).getSessionContext();
		loginPresenter = new LoginPresenter(view);
		users.setUp(getModelLayerFactory().newUserServices(), zeCollection);
		recordServices = getModelLayerFactory().newRecordServices();

	}
	//GetSessionLanguage

	@Test
	public void whenGetSessionLanguageThenOk()
			throws Exception {
		givenUserPreferredLanguageIsEnWhenGetSessionLanguageThenOk();
		givenUserWithoutPreferredLanguageWhenGetSessionLanguageThenOk();
		givenUserPreferredLanguageIsFrWhenGetSessionLanguageThenOk();
	}

	private void givenUserPreferredLanguageIsFrWhenGetSessionLanguageThenOk() throws RecordServicesException {
		User adminInZeCollection = users.adminIn(zeCollection);
		recordServices.add(adminInZeCollection.setLoginLanguageCode("fr"));
		User adminInBusiness = users.adminIn(businessCollection);
		recordServices.add(adminInBusiness.setLoginLanguageCode("fr"));
		assertThat(loginPresenter.getSessionLanguage(adminInZeCollection)).isEqualTo(new Locale("fr"));
		assertThat(loginPresenter.getSessionLanguage(adminInBusiness)).isEqualTo(new Locale("en"));
	}

	private void givenUserWithoutPreferredLanguageWhenGetSessionLanguageThenOk() throws RecordServicesException {
		User adminInZeCollection = users.adminIn(zeCollection);
		recordServices.add(adminInZeCollection.setLoginLanguageCode(null));
		User adminInBusiness = users.adminIn(businessCollection);
		recordServices.add(adminInBusiness.setLoginLanguageCode(null));
		assertThat(loginPresenter.getSessionLanguage(adminInZeCollection)).isEqualTo(new Locale("en"));
		assertThat(loginPresenter.getSessionLanguage(adminInBusiness)).isEqualTo(new Locale("en"));

	}

	private void givenUserPreferredLanguageIsEnWhenGetSessionLanguageThenOk() throws RecordServicesException {
		User adminInZeCollection = users.adminIn(zeCollection);
		recordServices.add(adminInZeCollection.setLoginLanguageCode("en"));
		User adminInBusiness = users.adminIn(businessCollection);
		recordServices.add(adminInBusiness.setLoginLanguageCode("en"));
		assertThat(loginPresenter.getSessionLanguage(adminInZeCollection)).isEqualTo(new Locale("en"));
		assertThat(loginPresenter.getSessionLanguage(adminInBusiness)).isEqualTo(new Locale("en"));
	}

}
