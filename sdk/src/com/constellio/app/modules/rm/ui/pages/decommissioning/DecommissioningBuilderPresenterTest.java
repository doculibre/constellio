package com.constellio.app.modules.rm.ui.pages.decommissioning;

import com.constellio.app.entities.schemasDisplay.SchemaTypesDisplayConfig;
import com.constellio.app.modules.rm.services.decommissioning.SearchType;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.conf.ModelLayerConfiguration;
import com.constellio.model.entities.enums.SearchSortType;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.extensions.ConstellioModulesManager;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.FakeUIContext;
import com.constellio.sdk.tests.MockedFactories;
import com.constellio.sdk.tests.MockedNavigation;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DecommissioningBuilderPresenterTest extends ConstellioTest {
	public static final String FACET_CODE = "schemaType_default_zeField";

	@Mock DecommissioningBuilderView view;
	MockedNavigation navigator;
	@Mock SchemasDisplayManager schemasDisplayManager;
	@Mock SchemaTypesDisplayConfig typesDisplayConfig;
	@Mock ConstellioModulesManager modulesManager;
	MockedFactories factories = new MockedFactories();

	@Mock
	ModelLayerConfiguration modelLayerConfiguration;

	@Mock
	DecommissioningBuilderPresenter presenter;
	@Mock
	private ConstellioEIMConfigs mockedConfigs;

	@Mock UserServices userServices;

	@Mock User user;

	@Before
	public void setUp() {
		when(view.getConstellioFactories()).thenReturn(factories.getConstellioFactories());
		when(view.getSessionContext()).thenReturn(FakeSessionContext.gandalfInCollection(zeCollection));

		when(view.navigate()).thenReturn(navigator);
		when(view.getCollection()).thenReturn(zeCollection);

		when(factories.getAppLayerFactory().getMetadataSchemasDisplayManager()).thenReturn(schemasDisplayManager);
		when(schemasDisplayManager.getTypes(zeCollection)).thenReturn(typesDisplayConfig);
		when(typesDisplayConfig.getFacetMetadataCodes()).thenReturn(Arrays.asList(FACET_CODE));
		when(factories.getModelLayerFactory().getSystemConfigs()).thenReturn(mockedConfigs);
		when(mockedConfigs.getSearchSortType()).thenReturn(SearchSortType.RELEVENCE);
		when(factories.getAppLayerFactory().getModulesManager()).thenReturn(modulesManager);
		when(factories.getModelLayerFactory().getConfiguration()).thenReturn(modelLayerConfiguration);
		when(modelLayerConfiguration.getMainDataLanguage()).thenReturn(Locale.FRENCH.getLanguage());
		when(factories.getModelLayerFactory().newUserServices()).thenReturn(userServices);
		when(view.getUIContext()).thenReturn(new FakeUIContext());


		presenter = spy(new DecommissioningBuilderPresenter(view) {
			@Override
			protected User getCurrentUser() {
				return user;
			}
		});
		//		doReturn(new ArrayList<>()).when(presenter).getFoldersAlreadyInNonProcessedDecommissioningLists();
		//		doReturn(new ArrayList<>()).when(presenter).getDocumentsAlreadyInNonProcessedDecommissioningLists();
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
