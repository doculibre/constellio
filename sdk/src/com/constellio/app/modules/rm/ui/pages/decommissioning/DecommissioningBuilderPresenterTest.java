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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.entities.schemasDisplay.SchemaTypesDisplayConfig;
import com.constellio.app.modules.rm.services.decommissioning.SearchType;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.app.ui.application.ConstellioNavigator;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.MockedFactories;

public class DecommissioningBuilderPresenterTest extends ConstellioTest {
	public static final String FACET_CODE = "schemaType_default_zeField";

	@Mock DecommissioningBuilderView view;
	@Mock ConstellioNavigator navigator;
	@Mock SchemasDisplayManager schemasDisplayManager;
	@Mock SchemaTypesDisplayConfig typesDisplayConfig;
	MockedFactories factories = new MockedFactories();

	DecommissioningBuilderPresenter presenter;

	@Before
	public void setUp() {
		when(view.getConstellioFactories()).thenReturn(factories.getConstellioFactories());
		when(view.getSessionContext()).thenReturn(FakeSessionContext.gandalfInCollection(zeCollection));

		when(view.navigateTo()).thenReturn(navigator);
		when(view.getCollection()).thenReturn(zeCollection);

		when(factories.getAppLayerFactory().getMetadataSchemasDisplayManager()).thenReturn(schemasDisplayManager);
		when(schemasDisplayManager.getTypes(zeCollection)).thenReturn(typesDisplayConfig);
		when(typesDisplayConfig.getFacetMetadataCodes()).thenReturn(Arrays.asList(FACET_CODE));

		presenter = new DecommissioningBuilderPresenter(view);
	}

	@Test
	public void givenParametersThenItStoresTheSearchType() {
		presenter.forRequestParameters("transfer");
		assertThat(presenter.searchType).isEqualTo(SearchType.transfer);
	}

	@Test
	public void givenParametersThenItAddsTwoEmptySearchCriteria() {
		presenter.forRequestParameters("transfer");
		verify(view, times(2)).addEmptyCriterion();
	}

	@Test
	public void givenParametersThenItSetsTheCriterionSchemaType() {
		presenter.forRequestParameters("transfer");
		verify(view, times(1)).setCriteriaSchemaType("folder");
	}

	@Test
	public void givenAddCriterionRequestedTheItTellsTheViewToAddAnEmptyCriterion() {
		presenter.addCriterionRequested();
		verify(view, times(1)).addEmptyCriterion();
	}

	@Test
	public void givenFilingSpaceSelectedThenItStoresTheFilingSpace() {
		presenter.filingSpaceSelected("zeFilingSpaceId");
		assertThat(presenter.filingSpaceId).isEqualTo("zeFilingSpaceId");
	}
}
