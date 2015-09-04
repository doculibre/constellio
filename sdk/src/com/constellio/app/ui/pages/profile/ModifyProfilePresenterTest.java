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
package com.constellio.app.ui.pages.profile;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.modules.rm.RMNavigationConfiguration;
import com.constellio.app.modules.rm.model.enums.DefaultTabInFolderDisplay;
import com.constellio.app.ui.application.ConstellioNavigator;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.ContentVersionVO.InputStreamProvider;
import com.constellio.app.ui.framework.data.TaxonomyVODataProvider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.records.RecordServicesImpl;
import com.constellio.model.services.users.UserPhotosServices;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.MockedFactories;

public class ModifyProfilePresenterTest extends ConstellioTest {
	@Mock ConstellioNavigator navigator;
	@Mock ModifyProfileView view;
	@Mock UserServices userServices;
	@Mock RecordServicesImpl recordServices;
	@Mock UserCredential userCredential, userCredentialWithFirstName, userCredentialWithLastName, userCredentialWithEmail;
	@Mock User bob;
	@Mock TaxonomyVODataProvider taxonomyVODataProvider;
	@Mock Record bobRecord;
	@Mock UserPhotosServices userPhotosServices;
	@Mock ContentVersionVO contentVersionVO;
	@Mock InputStreamProvider inputStreamProvider;
	@Mock InputStream inputStream;
	ProfileVO profileVO;
	ModifyProfilePresenter presenter;

	MockedFactories mockedFactories = new MockedFactories();

	@Before
	public void setUp()
			throws Exception {

		when(view.getConstellioFactories()).thenReturn(mockedFactories.getConstellioFactories());
		when(view.getSessionContext()).thenReturn(FakeSessionContext.bobInCollection(zeCollection));
		when(view.navigateTo()).thenReturn(navigator);
		when(view.getCollection()).thenReturn(zeCollection);

		profileVO = new ProfileVO(contentVersionVO, "bob.gratton", "bob", "Gratton", "bob@constellio.com", "3333333",
				RMNavigationConfiguration.LAST_VIEWED_FOLDERS, DefaultTabInFolderDisplay.METADATA, "taxo1", null, null, null);

		when(mockedFactories.getModelLayerFactory().newUserServices()).thenReturn(userServices);
		when(mockedFactories.getModelLayerFactory().newRecordServices()).thenReturn(recordServices);
		when(mockedFactories.getModelLayerFactory().newUserPhotosServices()).thenReturn(userPhotosServices);
		when(userServices.getUserCredential(profileVO.getUsername())).thenReturn(userCredential);
		when(userCredential.getFirstName()).thenReturn("bob");
		when(userCredential.getLastName()).thenReturn("Gratton");
		when(userCredential.getEmail()).thenReturn("bob@constellio.com");

		when(userCredential.withFirstName("bob")).thenReturn(userCredentialWithFirstName);
		when(userCredentialWithFirstName.withLastName("Gratton")).thenReturn(userCredentialWithLastName);
		when(userCredentialWithLastName.withEmail("bob@constellio.com")).thenReturn(userCredentialWithEmail);
		when(userServices.getUserInCollection("bob.gratton", zeCollection)).thenReturn(bob);
		when(bob.getPhone()).thenReturn("3333333");
		when(bob.getStartTab()).thenReturn(RMNavigationConfiguration.LAST_VIEWED_FOLDERS);
		when(bob.getDefaultTaxonomy()).thenReturn("taxo1");
		when(bob.getWrappedRecord()).thenReturn(bobRecord);
		doNothing().when(recordServices).update(bobRecord);

		presenter = spy(new ModifyProfilePresenter(view));

		presenter.setParameters("url1/url2/url3");
	}

	@Test
	public void whenSaveButtonClickedThenUpdateUserCredentialAndUser()
			throws Exception {

		//		when(profilVO.getImage()).thenReturn(contentVersionVO);
		doNothing().when(presenter).changePhoto(contentVersionVO);

		presenter.saveButtonClicked(profileVO);

		verify(userServices).addUpdateUserCredential(userCredentialWithEmail);
		verify(bob).setPhone("3333333");
		verify(bob).setStartTab(RMNavigationConfiguration.LAST_VIEWED_FOLDERS);
		verify(bob).setDefaultTaxonomy("taxo1");
		verify(recordServices).update(bobRecord);
		verify(view.navigateTo()).url(presenter.getParameters());

	}

	@Test
	public void whenGetProfilVOThenOk()
			throws Exception {

		presenter.getProfilVO("bob.gratton");

		verify(presenter)
				.newProfilVO("bob.gratton", "bob", "Gratton", "bob@constellio.com", "3333333",
						RMNavigationConfiguration.LAST_VIEWED_FOLDERS, DefaultTabInFolderDisplay.METADATA, "taxo1");
	}

	@Test
	public void whenCancelButtonClickedThenNavigateToHome()
			throws Exception {

		presenter.cancelButtonClicked();

		verify(view.navigateTo()).url(presenter.getParameters());
	}

	@Test
	public void whenGetEnableTaxonomiesThenReturnTheirCodes()
			throws Exception {

		doReturn(taxonomyVODataProvider).when(presenter).newDataProvider();

		presenter.getEnableTaxonomies();

		verify(taxonomyVODataProvider).getTaxonomyVOs();
	}
}
