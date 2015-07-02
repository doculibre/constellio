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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.entities.schemasDisplay.SchemaTypesDisplayConfig;
import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplateManager;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.app.ui.application.ConstellioNavigator;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.MockedFactories;

public class AdvancedSearchPresenterTest extends ConstellioTest {
	public static final String FACET_CODE = "schemaType_default_zeField";

	@Mock AdvancedSearchView view;
	@Mock ConstellioNavigator navigator;
	@Mock SchemasDisplayManager schemasDisplayManager;
	@Mock LabelTemplateManager labelTemplateManager;
	@Mock SchemaTypesDisplayConfig typesDisplayConfig;
	MockedFactories factories = new MockedFactories();

	AdvancedSearchPresenter presenter;

	@Before
	public void setUp()
			throws Exception {
		when(view.getConstellioFactories()).thenReturn(factories.getConstellioFactories());
		when(view.getSessionContext()).thenReturn(FakeSessionContext.gandalfInCollection(zeCollection));

		when(view.navigateTo()).thenReturn(navigator);
		when(view.getCollection()).thenReturn(zeCollection);

		when(factories.getAppLayerFactory().getMetadataSchemasDisplayManager()).thenReturn(schemasDisplayManager);
		when(schemasDisplayManager.getTypes(zeCollection)).thenReturn(typesDisplayConfig);
		when(typesDisplayConfig.getFacetMetadataCodes()).thenReturn(Arrays.asList(FACET_CODE));

		when(factories.getAppLayerFactory().getLabelTemplateManager()).thenReturn(labelTemplateManager);

		when(view.getSchemaType()).thenReturn("zeSchemaType");

		presenter = spy(new AdvancedSearchPresenter(view));
	}

	@Test
	public void givenEmptyParametersThenItResetsTheFacetSelection() {
		presenter.forRequestParameters("");
		verify(presenter, times(1)).resetFacetSelection();
	}

	@Test
	public void givenEmptyParametersThenItSetsTheSearchExpressionFromTheView() {
		when(view.getSearchExpression()).thenReturn("zeExpression");
		presenter.forRequestParameters("");
		assertThat(presenter.searchExpression).isEqualTo("zeExpression");
	}

	@Test
	public void givenEmptyParametersThenItSetsTheSchemaTypeFromTheView() {
		presenter.forRequestParameters("");
		assertThat(presenter.schemaTypeCode).isEqualTo("zeSchemaType");
	}

	@Test
	public void givenSchemaTypeIsNotEmptyAndSearchConditionIsValidThenMustDisplayResultsIsTrue()
			throws Exception {
		doNothing().when(presenter).buildSearchCondition();
		assertThat(presenter.forRequestParameters("").mustDisplayResults()).isTrue();
	}

	@Test
	public void givenSchemaTypeIsEmptyThenMustDisplayResultsIsFalse() {
		when(view.getSchemaType()).thenReturn("");
		assertThat(presenter.forRequestParameters("").mustDisplayResults()).isFalse();
	}

	@Test
	public void givenSchemaTypeIsNullThenMustDisplayResultsIsFalse() {
		when(view.getSchemaType()).thenReturn(null);
		assertThat(presenter.forRequestParameters("").mustDisplayResults()).isFalse();
	}

	@Test
	public void givenNullSchemaTypeWhenGetTemplateThenReturnDefaultEmptyTemplate()
			throws Exception {
		when(view.getSchemaType()).thenReturn(null);
		presenter.forRequestParameters("");

		presenter.getTemplates();

		verify(labelTemplateManager).listTemplates(null);
	}

	@Test
	public void givenFolderSchemaTypeWhenGetTemplateThenReturnDefaultEmptyTemplate()
			throws Exception {
		when(view.getSchemaType()).thenReturn(Folder.SCHEMA_TYPE);
		presenter.forRequestParameters("");

		presenter.getTemplates();

		verify(labelTemplateManager).listTemplates(Folder.SCHEMA_TYPE);
	}

	@Test
	public void givenContainerSchemaTypeWhenGetTemplateThenReturnDefaultEmptyTemplate()
			throws Exception {
		when(view.getSchemaType()).thenReturn(ContainerRecord.SCHEMA_TYPE);
		presenter.forRequestParameters("");

		presenter.getTemplates();

		verify(labelTemplateManager).listTemplates(ContainerRecord.SCHEMA_TYPE);
	}
}
