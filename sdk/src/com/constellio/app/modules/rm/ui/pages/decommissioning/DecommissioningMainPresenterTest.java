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
package com.constellio.app.modules.rm.ui.pages.decommissioning;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.MockedFactories;

public class DecommissioningMainPresenterTest extends ConstellioTest {
	@Mock DecommissioningMainView view;
	@Mock RecordVODataProvider dataProvider;
	MockedFactories factories = new MockedFactories();

	DecommissioningMainPresenter presenter;

	@Before
	public void setUp()
			throws Exception {
		when(view.getConstellioFactories()).thenReturn(factories.getConstellioFactories());
		when(view.getSessionContext()).thenReturn(FakeSessionContext.gandalfInCollection(zeCollection));

		presenter = spy(new DecommissioningMainPresenter(view));
	}

	@Test
	public void givenTheCreateListTabIsRequestedThenDisplayTheCreateListTab() {
		presenter.tabSelected(DecommissioningMainPresenter.CREATE);
		verify(view, times(1)).displayListCreation();
	}

	@Test
	public void givenTheGeneratedListsTabIsRequestedThenDisplayTheGeneratedListsInEditableTable() {
		doReturn(dataProvider).when(presenter).getGeneratedLists();

		presenter.tabSelected(DecommissioningMainPresenter.GENERATED);
		verify(view, times(1)).displayEditableTable(dataProvider);
	}

	@Test
	public void givenTheProcessedListsTabIsRequestedThenDisplayTheProcessedListsInReadOnlyTable() {
		doReturn(dataProvider).when(presenter).getProcessedLists();

		presenter.tabSelected(DecommissioningMainPresenter.PROCESSED);
		verify(view, times(1)).displayReadOnlyTable(dataProvider);
	}

	@Test
	public void givenTheSentForValidationTabIsRequestedThenTheSentForValidationListsInReadOnlyTable() {
		doReturn(dataProvider).when(presenter).getListsPendingValidation();
		presenter.tabSelected(DecommissioningMainPresenter.PENDING_VALIDATION);
		verify(view, times(1)).displayReadOnlyTable(dataProvider);
	}

	@Test
	public void givenTheToValidateTabIsRequestedThenTheToValidateListsInReadOnlyTable() {
		doReturn(dataProvider).when(presenter).getListsToValidate();
		presenter.tabSelected(DecommissioningMainPresenter.TO_VALIDATE);
		verify(view, times(1)).displayReadOnlyTable(dataProvider);
	}

}
