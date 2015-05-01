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
package com.constellio.app.ui.pages.management.taxonomy;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.modules.rm.services.ValueListServices;
import com.constellio.app.ui.application.ConstellioNavigator;
import com.constellio.app.ui.entities.TaxonomyVO;
import com.constellio.model.entities.Taxonomy;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.MockedFactories;

public class ListTaxonomyPresenterTest extends ConstellioTest {

	@Mock ListTaxonomyViewImpl view;
	@Mock ConstellioNavigator navigator;
	@Mock ValueListServices valueListServices;
	@Mock Taxonomy taxonomy1;
	@Mock TaxonomyVO taxonomyVO;
	ListTaxonomyPresenter presenter;
	String newTaxonomyTitle;
	MockedFactories mockedFactories = new MockedFactories();

	@Before
	public void setUp()
			throws Exception {

		when(view.getConstellioFactories()).thenReturn(mockedFactories.getConstellioFactories());
		when(view.getSessionContext()).thenReturn(FakeSessionContext.dakotaInCollection(zeCollection));
		when(view.navigateTo()).thenReturn(navigator);

		newTaxonomyTitle = "taxonomy 1";
		when(taxonomy1.getTitle()).thenReturn(newTaxonomyTitle);

		presenter = spy(new ListTaxonomyPresenter(view));

		doReturn(valueListServices).when(presenter).valueListServices();
	}

	@Test
	public void whenAddButtonClikedThenNavigateToAddEditTaxonomy() {
		presenter.addButtonClicked();
		verify(view.navigateTo()).addTaxonomy();
	}

	@Test
	public void whenEditButtonClikedThenNavigateToAddEditTaxonomyWithCorrectParams() {
		presenter.editButtonClicked("taxo1Code");
		verify(view.navigateTo()).editTaxonomy("taxo1Code");
	}

	@Test
	public void whenDisplayButtonClikedThenNavigateToAddEditTaxonomyWithCorrectParams()
			throws Exception {

		when(taxonomyVO.getCode()).thenReturn("taxoCode");

		presenter.displayButtonClicked(taxonomyVO);

		verify(view.navigateTo()).taxonomyManagement("taxoCode");
	}

}
