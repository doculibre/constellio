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
package com.constellio.app.ui.pages.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.entities.schemasDisplay.SchemaTypesDisplayConfig;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.app.ui.application.ConstellioNavigator;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.services.parser.LanguageDetectionManager;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.MockedFactories;

public class SimpleSearchPresenterTest extends ConstellioTest {
	public static final String EXPRESSION = "zexpression";
	public static final String FACET_CODE = "zeField_s";

	@Mock SimpleSearchView view;
	@Mock ConstellioNavigator navigator;
	@Mock LanguageDetectionManager detectionManager;
	@Mock LogicalSearchQuery query;
	@Mock Metadata metadata;
	@Mock SchemasDisplayManager schemasDisplayManager;
	@Mock SchemaTypesDisplayConfig typesDisplayConfig;
	MockedFactories factories = new MockedFactories();

	SimpleSearchPresenter presenter;

	@Before
	public void setUp() {
		when(view.getConstellioFactories()).thenReturn(factories.getConstellioFactories());
		when(view.getSessionContext()).thenReturn(FakeSessionContext.gandalfInCollection(zeCollection));

		when(view.navigateTo()).thenReturn(navigator);
		when(view.getCollection()).thenReturn(zeCollection);

		when(factories.getAppLayerFactory().getMetadataSchemasDisplayManager()).thenReturn(schemasDisplayManager);
		when(schemasDisplayManager.getTypes(zeCollection)).thenReturn(typesDisplayConfig);
		presenter = spy(new SimpleSearchPresenter(view));
	}

	@Test
	public void givenParametersWithSearchExpressionAndPageNumberThenBothAreSaved() {
		presenter.forRequestParameters("q/zexpression/42");
		assertThat(presenter.getUserSearchExpression()).isEqualTo(EXPRESSION);
		assertThat(presenter.getPageNumber()).isEqualTo(42);
		assertThat(presenter.mustDisplayResults()).isTrue();
	}

	@Test
	public void givenParametersWithSearchExpressionWithoutPageNumberThenSearchExpressionIsSavedAndPageNumberIsSetToOne() {
		presenter.forRequestParameters("q/" + EXPRESSION);
		assertThat(presenter.getUserSearchExpression()).isEqualTo(EXPRESSION);
		assertThat(presenter.getPageNumber()).isEqualTo(1);
		assertThat(presenter.mustDisplayResults()).isTrue();
	}

	@Test
	public void givenEmptyParametersTheSearchExpressionIsEmpty() {
		presenter.forRequestParameters("");
		assertThat(presenter.getUserSearchExpression()).isEqualTo("");
		assertThat(presenter.mustDisplayResults()).isFalse();
	}

	@Test
	public void givenNullParametersTheSearchExpressionIsEmpty() {
		presenter.forRequestParameters(null);
		assertThat(presenter.getUserSearchExpression()).isEqualTo("");
		assertThat(presenter.mustDisplayResults()).isFalse();
	}

}
