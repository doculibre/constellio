package com.constellio.app.ui.pages.profile;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.model.enums.DefaultTabInFolderDisplay;
import com.constellio.app.modules.rm.navigation.RMNavigationConfiguration;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.ContentVersionVO.InputStreamProvider;
import com.constellio.app.ui.framework.data.TaxonomyVODataProvider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServicesImpl;
import com.constellio.model.services.users.UserPhotosServices;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.MockedFactories;
import com.constellio.sdk.tests.MockedNavigation;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.InputStream;
import java.util.Arrays;

import static com.constellio.app.modules.rm.model.enums.DefaultTabInFolderDisplay.METADATA;
import static org.mockito.Mockito.*;

public class ModifyProfilePresenterTest extends ConstellioTest {
	MockedNavigation navigator;
	@Mock ModifyProfileView view;
	@Mock UserServices userServices;
	@Mock RecordServicesImpl recordServices;
	@Mock UserCredential userCredential, userCredentialWithFirstName, userCredentialWithLastName, userCredentialWithEmail, userCredentialWithPersonalEmails;
	@Mock User bob;
	@Mock TaxonomyVODataProvider taxonomyVODataProvider;
	@Mock Record bobRecord;
	@Mock UserPhotosServices userPhotosServices;
	@Mock ContentVersionVO contentVersionVO;
	@Mock InputStreamProvider inputStreamProvider;
	@Mock InputStream inputStream;
	@Mock SystemConfigurationsManager configurationsManager;
	ProfileVO profileVO;
	ModifyProfilePresenter presenter;

	MockedFactories mockedFactories = new MockedFactories();

	@Before
	public void setUp()
			throws Exception {

		navigator = new MockedNavigation();

		when(view.getConstellioFactories()).thenReturn(mockedFactories.getConstellioFactories());
		ModelLayerFactory modelLayerFactory = mockedFactories.getModelLayerFactory();
		when(modelLayerFactory.getSystemConfigurationsManager()).thenReturn(configurationsManager);
		when(view.getSessionContext()).thenReturn(FakeSessionContext.bobInCollection(zeCollection));
		when(view.navigate()).thenReturn(navigator);
		when(view.getCollection()).thenReturn(zeCollection);

		profileVO = new ProfileVO(contentVersionVO, "bob.gratton", "bob", "Gratton", "bob@constellio.com",
				"bob@doculibre.com\nbob@gmail.com", "3333333",
				RMNavigationConfiguration.LAST_VIEWED_FOLDERS, METADATA, "taxo1", null, null, null);
		profileVO.setLoginLanguageCode("fr");

		when(mockedFactories.getModelLayerFactory().newUserServices()).thenReturn(userServices);
		when(mockedFactories.getModelLayerFactory().newRecordServices()).thenReturn(recordServices);
		when(mockedFactories.getModelLayerFactory().newUserPhotosServices()).thenReturn(userPhotosServices);
		when(userServices.getUserCredential(profileVO.getUsername())).thenReturn(userCredential);
		when(userCredential.getFirstName()).thenReturn("bob");
		when(userCredential.getLastName()).thenReturn("Gratton");
		when(userCredential.getEmail()).thenReturn("bob@constellio.com");
		when(userCredential.getPersonalEmails()).thenReturn(Arrays.asList("bob@doculibre.com", "bob@gmail.com"));

		when(userCredential.withFirstName("bob")).thenReturn(userCredentialWithFirstName);
		when(userCredentialWithFirstName.withLastName("Gratton")).thenReturn(userCredentialWithLastName);
		when(userCredentialWithLastName.withEmail("bob@constellio.com")).thenReturn(userCredentialWithEmail);
		when(userCredentialWithEmail.withPersonalEmails(Arrays.asList("bob@doculibre.com", "bob@gmail.com")))
				.thenReturn(userCredentialWithPersonalEmails);
		when(userServices.getUserInCollection("bob.gratton", zeCollection)).thenReturn(bob);
		when(bob.getPhone()).thenReturn("3333333");
		when(bob.getStartTab()).thenReturn(RMNavigationConfiguration.LAST_VIEWED_FOLDERS);
		when(bob.getDefaultTaxonomy()).thenReturn("taxo1");
		when(bob.getWrappedRecord()).thenReturn(bobRecord);
		when(bob.getPersonalEmails()).thenReturn(Arrays.asList("bob@doculibre.com", "bob@gmail.com"));
		doNothing().when(recordServices).update(bobRecord);

		presenter = spy(new ModifyProfilePresenter(view));

		presenter.setParameters("url1/url2/url3");
	}

	@Test
	public void whenGetProfilVOThenOk()
			throws Exception {

		when(configurationsManager.getValue(RMConfigs.DEFAULT_TAB_IN_FOLDER_DISPLAY)).thenReturn("metadata");

		presenter.getProfileVO("bob.gratton");

		verify(presenter)
				.newProfileVO("bob.gratton", "bob", "Gratton", "bob@constellio.com",
						Arrays.asList("bob@doculibre.com", "bob@gmail.com"), "3333333",
						RMNavigationConfiguration.LAST_VIEWED_FOLDERS, null, "taxo1");
	}

	@Test
	public void whenCancelButtonClickedThenNavigateToHome()
			throws Exception {

		presenter.cancelButtonClicked();

		verify(view.navigate().to()).url(presenter.getParameters());
	}

	@Test
	public void whenGetEnableTaxonomiesThenReturnTheirCodes()
			throws Exception {

		doReturn(taxonomyVODataProvider).when(presenter).newDataProvider();

		presenter.getEnabledTaxonomies();

		verify(taxonomyVODataProvider).getTaxonomyVOs();
	}
}
