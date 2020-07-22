package com.constellio.app.ui.pages.profile;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.model.enums.DefaultTabInFolderDisplay;
import com.constellio.app.modules.rm.navigation.RMNavigationConfiguration;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.events.RMEventsSearchServices;
import com.constellio.app.ui.entities.UserCredentialVO;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.base.UIContext;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.security.roles.RolesManager;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.SDKViewNavigation;
import com.constellio.sdk.tests.setups.Users;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.HashMap;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class ModifyProfilePresenterAcceptanceTest extends ConstellioTest {

	LocalDateTime shishOClock = new LocalDateTime().plusDays(1);

	Users users = new Users();
	@Mock
	ModifyProfileView view;
	SDKViewNavigation viewNavigation;
	@Mock UserCredentialVO chuckCredentialVO;
	RMTestRecords records = new RMTestRecords(zeCollection);
	SearchServices searchServices;
	ModifyProfilePresenter presenter;
	SessionContext sessionContext;
	@Mock
	UIContext uiContext;
	LocalDate nowDate = new LocalDate();
	RMEventsSearchServices rmEventsSearchServices;
	RolesManager rolesManager;

	RMSchemasRecordsServices rmSchemasRecordsServices;
	MetadataSchemasManager metadataSchemasManager;
	RecordServices recordServices;
	ProfileVO profileVO;
	UserServices userServices;

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withConstellioESModule().withAllTestUsers()
						.withRMTest(records).withFoldersAndContainersOfEveryStatus().withDocumentsDecommissioningList()
		);
		//inCollection("LaCollectionDeRida").setCollectionTitleTo("Collection d'entreprise");
		userServices = getModelLayerFactory().newUserServices();
		users = new Users();
		users.setUp(userServices);
		inCollection(zeCollection).setCollectionTitleTo("Collection de test");

		profileVO = new ProfileVO(users.bobIn(zeCollection).getUsername(), "bob", "Gratton", "bob@constellio.com", "bob@hotmail.com\nbob@gmail.com", "3333333",
				RMNavigationConfiguration.LAST_VIEWED_FOLDERS, DefaultTabInFolderDisplay.METADATA, "taxo1", null, null, null, false);
		profileVO.setLoginLanguageCode("fr");

		sessionContext = FakeSessionContext.forRealUserIncollection(users.chuckNorrisIn(zeCollection));
		sessionContext.setCurrentLocale(Locale.FRENCH);

		viewNavigation = new SDKViewNavigation(view);
		when(view.getSessionContext()).thenReturn(sessionContext);
		when(view.getCollection()).thenReturn(zeCollection);
		when(view.getConstellioFactories()).thenReturn(getConstellioFactories());
		when(view.getUIContext()).thenReturn(uiContext);

		presenter = new ModifyProfilePresenter(view);
		presenter.saveButtonClicked(profileVO, new HashMap<String, Object>());

		rolesManager = getModelLayerFactory().getRolesManager();

		givenTimeIs(nowDate);
	}

	@Test
	public void whenSaveButtonClickedThenUpdateUserCredentialAndUser()
			throws Exception {

		presenter.saveButtonClicked(profileVO, new HashMap<String, Object>());

		assertThat(userServices.getUserConfigs(bobGratton).getPersonalEmails()).containsOnly("bob@hotmail.com", "bob@gmail.com");
	}
}
