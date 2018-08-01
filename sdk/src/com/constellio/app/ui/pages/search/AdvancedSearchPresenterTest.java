package com.constellio.app.ui.pages.search;

import com.constellio.app.entities.schemasDisplay.SchemaTypesDisplayConfig;
import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplateManager;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.enums.SearchSortType;
import com.constellio.model.services.extensions.ConstellioModulesManager;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.MockedFactories;
import com.constellio.sdk.tests.MockedNavigation;
import org.junit.Before;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class AdvancedSearchPresenterTest extends ConstellioTest {
	public static final String FACET_CODE = "zeField_s";

	@Mock AdvancedSearchView view;
	MockedNavigation navigator;
	@Mock SchemasDisplayManager schemasDisplayManager;
	@Mock LabelTemplateManager labelTemplateManager;
	@Mock SchemaTypesDisplayConfig typesDisplayConfig;
	@Mock RecordServices recordServices;
	@Mock SearchServices searchServices;
	@Mock ConstellioModulesManager modulesManager;
	MockedFactories factories = new MockedFactories();

	AdvancedSearchPresenter presenter;
	@Mock
	private ConstellioEIMConfigs mockedConfigs;

	@Before
	public void setUp()
			throws Exception {
		when(view.getConstellioFactories()).thenReturn(factories.getConstellioFactories());
		when(view.getSessionContext()).thenReturn(FakeSessionContext.gandalfInCollection(zeCollection));

		when(view.navigate()).thenReturn(navigator);
		when(view.getCollection()).thenReturn(zeCollection);

		when(factories.getAppLayerFactory().getMetadataSchemasDisplayManager()).thenReturn(schemasDisplayManager);
		when(schemasDisplayManager.getTypes(zeCollection)).thenReturn(typesDisplayConfig);

		when(factories.getAppLayerFactory().getLabelTemplateManager()).thenReturn(labelTemplateManager);
		when(factories.getModelLayerFactory().newRecordServices()).thenReturn(recordServices);
		when(factories.getModelLayerFactory().newSearchServices()).thenReturn(searchServices);

		when(view.getSchemaType()).thenReturn("zeSchemaType");
		when(factories.getModelLayerFactory().getSystemConfigs()).thenReturn(mockedConfigs);
		when(mockedConfigs.getSearchSortType()).thenReturn(SearchSortType.RELEVENCE);
		when(factories.getAppLayerFactory().getModulesManager()).thenReturn(modulesManager);

		presenter = spy(new AdvancedSearchPresenter(view));

		doReturn(null).when(presenter).saveTemporarySearch(true);
	}

	//@Test
	public void givenEmptyParametersThenItSetsTheSearchExpressionFromTheView() {
		when(view.getSearchExpression()).thenReturn("zeExpression");
		presenter.forRequestParameters("");
		assertThat(presenter.searchExpression).isEqualTo("zeExpression");
	}

	//@Test
	public void givenEmptyParametersThenItSetsTheSchemaTypeFromTheView() {
		presenter.forRequestParameters("");
		assertThat(presenter.schemaTypeCode).isEqualTo("zeSchemaType");
	}

	//@Test
	public void givenSchemaTypeIsNotEmptyAndSearchConditionIsValidThenMustDisplayResultsIsTrue()
			throws Exception {
		doNothing().when(presenter).buildSearchCondition();
		assertThat(presenter.forRequestParameters("").mustDisplayResults()).isTrue();
	}

	//@Test
	public void givenSchemaTypeIsEmptyThenMustDisplayResultsIsFalse() {
		when(view.getSchemaType()).thenReturn("");
		assertThat(presenter.forRequestParameters("").mustDisplayResults()).isFalse();
	}

	//@Test
	public void givenSchemaTypeIsNullThenMustDisplayResultsIsFalse() {
		when(view.getSchemaType()).thenReturn(null);
		assertThat(presenter.forRequestParameters("").mustDisplayResults()).isFalse();
	}

	//@Test
	public void givenNullSchemaTypeWhenGetTemplateThenReturnDefaultEmptyTemplate()
			throws Exception {
		when(view.getSchemaType()).thenReturn(null);
		presenter.forRequestParameters("");

		presenter.getTemplates();

		verify(labelTemplateManager).listTemplates(null);
	}

	//@Test
	public void givenFolderSchemaTypeWhenGetTemplateThenReturnDefaultEmptyTemplate()
			throws Exception {
		when(view.getSchemaType()).thenReturn(Folder.SCHEMA_TYPE);
		presenter.forRequestParameters("");

		presenter.getDefaultTemplates();

		verify(labelTemplateManager).listTemplates(Folder.SCHEMA_TYPE);
	}

	//@Test
	public void givenContainerSchemaTypeWhenGetTemplateThenReturnDefaultEmptyTemplate()
			throws Exception {
		when(view.getSchemaType()).thenReturn(ContainerRecord.SCHEMA_TYPE);
		presenter.forRequestParameters("");

		presenter.getDefaultTemplates();

		verify(labelTemplateManager).listTemplates(ContainerRecord.SCHEMA_TYPE);
	}
}
