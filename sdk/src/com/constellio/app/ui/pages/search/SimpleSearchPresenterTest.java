package com.constellio.app.ui.pages.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.entities.schemasDisplay.SchemaTypesDisplayConfig;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.enums.SearchSortType;
import com.constellio.model.entities.records.wrappers.SavedSearch;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.parser.LanguageDetectionManager;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.MockedFactories;
import com.constellio.sdk.tests.MockedNavigation;

public class SimpleSearchPresenterTest extends ConstellioTest {
	public static final String EXPRESSION = "zexpression";
	public static final String FACET_CODE = "zeField_s";

	@Mock SimpleSearchView view;
	MockedNavigation navigator;
	@Mock LanguageDetectionManager detectionManager;
	@Mock LogicalSearchQuery query;
	@Mock Metadata metadata;
	@Mock SchemasDisplayManager schemasDisplayManager;
	@Mock SchemaTypesDisplayConfig typesDisplayConfig;
	MockedFactories factories = new MockedFactories();

	SimpleSearchPresenter presenter;
	@Mock
	private ConstellioEIMConfigs mockedConfigs;

	@Before
	public void setUp() {
		when(view.getConstellioFactories()).thenReturn(factories.getConstellioFactories());
		when(view.getSessionContext()).thenReturn(FakeSessionContext.gandalfInCollection(zeCollection));

		when(view.navigate()).thenReturn(navigator);
		when(view.getCollection()).thenReturn(zeCollection);

		when(factories.getAppLayerFactory().getMetadataSchemasDisplayManager()).thenReturn(schemasDisplayManager);
		when(schemasDisplayManager.getTypes(zeCollection)).thenReturn(typesDisplayConfig);
		when(factories.getModelLayerFactory().getSystemConfigs()).thenReturn(mockedConfigs);
		when(mockedConfigs.getSearchSortType()).thenReturn(SearchSortType.RELEVENCE);
		presenter = spy(new SimpleSearchPresenter(view));
	}

	@Test
	public void givenParametersWithSearchExpressionAndPageNumberThenBothAreSaved() {
		doReturn(mock(SavedSearch.class)).when(presenter).saveTemporarySearch(anyBoolean());
		presenter.forRequestParameters("q/zexpression/42");
		assertThat(presenter.getUserSearchExpression()).isEqualTo(EXPRESSION);
		assertThat(presenter.getPageNumber()).isEqualTo(42);
		assertThat(presenter.mustDisplayResults()).isTrue();
	}

	@Test
	public void givenParametersWithSearchExpressionWithoutPageNumberThenSearchExpressionIsSavedAndPageNumberIsSetToOne() {
		doReturn(mock(SavedSearch.class)).when(presenter).saveTemporarySearch(anyBoolean());
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
