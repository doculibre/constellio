package com.constellio.app.modules.rm.ui.pages.decommissioning;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import com.constellio.sdk.tests.MockedNavigation;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.entities.schemasDisplay.SchemaTypesDisplayConfig;
import com.constellio.app.modules.rm.services.decommissioning.SearchType;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.app.ui.application.CoreViews;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.MockedFactories;

public class DecommissioningBuilderPresenterTest extends ConstellioTest {
	public static final String FACET_CODE = "schemaType_default_zeField";

	@Mock DecommissioningBuilderView view;
	MockedNavigation navigator;
	@Mock SchemasDisplayManager schemasDisplayManager;
	@Mock SchemaTypesDisplayConfig typesDisplayConfig;
	MockedFactories factories = new MockedFactories();

	DecommissioningBuilderPresenter presenter;

	@Before
	public void setUp() {
		when(view.getConstellioFactories()).thenReturn(factories.getConstellioFactories());
		when(view.getSessionContext()).thenReturn(FakeSessionContext.gandalfInCollection(zeCollection));

		when(view.navigate()).thenReturn(navigator);
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

}
