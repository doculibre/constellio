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
package com.constellio.app.modules.tasks.ui.pages;

import static org.mockito.Mockito.when;

import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.ui.application.ConstellioNavigator;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.setups.Users;

public class TaskManagementPresenterAcceptanceTest extends ConstellioTest {
	Users users = new Users();
	@Mock
	TaskManagementView view;
	@Mock
	ConstellioNavigator navigator;
	SessionContext sessionContext;
	private RecordServices recordServices;
	private SearchServices searchServices;
	TaskManagementPresenter presenter;

	@Before
	public void setUp()
			throws Exception {
		givenCollection(zeCollection).withTaskModule().withAllTestUsers();

		users.setUp(getModelLayerFactory().newUserServices());

		recordServices = getModelLayerFactory().newRecordServices();
		searchServices = getModelLayerFactory().newSearchServices();

		sessionContext = FakeSessionContext.adminInCollection(zeCollection);
		sessionContext.setCurrentLocale(Locale.FRENCH);

		when(view.getSessionContext()).thenReturn(sessionContext);
		when(view.getCollection()).thenReturn(zeCollection);
		when(view.getConstellioFactories()).thenReturn(getConstellioFactories());
		when(view.navigateTo()).thenReturn(navigator);
	}

	@Test
	public void givenFolderIdMetadataWhenIsRecordIdMetadataThenReturnTrue()
			throws Exception {

		//assertThat(presenter.isRecordIdMetadata(metadataValueVO)).isTrue();
	}
}
