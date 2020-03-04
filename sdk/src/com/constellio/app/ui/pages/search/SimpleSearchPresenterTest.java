package com.constellio.app.ui.pages.search;

import com.constellio.app.entities.schemasDisplay.SchemaTypesDisplayConfig;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.conf.ModelLayerConfiguration;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.enums.SearchSortType;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.SavedSearch;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.extensions.ConstellioModulesManager;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.model.services.security.roles.Roles;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.MockedFactories;
import com.constellio.sdk.tests.MockedNavigation;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class SimpleSearchPresenterTest extends ConstellioTest {
	public static final String EXPRESSION = "zexpression";

	@Mock ConstellioModulesManager modulesManager;
	@Mock SimpleSearchView view;
	MockedNavigation navigator;
	@Mock SchemasDisplayManager schemasDisplayManager;
	@Mock SchemaTypesDisplayConfig typesDisplayConfig;
	@Mock MetadataSchemasManager metadataSchemasManager;
	@Mock ModelLayerConfiguration modelLayerConfiguration;
	MockedFactories factories = new MockedFactories();

	SchemasRecordsServices schemasRecordsServices;

	SimpleSearchPresenter presenter;
	@Mock
	private ConstellioEIMConfigs mockedConfigs;

	@Mock UserServices userServices;

	@Mock
	Record record;

	@Mock Roles roles;

	@Mock MetadataSchemaTypes metadataSchemaTypes;
	@Mock AuthorizationsServices authorizationsServices;

	@Before
	public void setUp() {

		when(factories.getModelLayerFactory().getConfiguration()).thenReturn(modelLayerConfiguration);
		when(modelLayerConfiguration.getMainDataLanguage()).thenReturn("fr");

		schemasRecordsServices = new SchemasRecordsServices(zeCollection, factories.getModelLayerFactory());
		when(view.getConstellioFactories()).thenReturn(factories.getConstellioFactories());
		when(view.getSessionContext()).thenReturn(FakeSessionContext.gandalfInCollection(zeCollection));

		when(view.navigate()).thenReturn(navigator);
		when(view.getCollection()).thenReturn(zeCollection);

		when(factories.getAppLayerFactory().getMetadataSchemasDisplayManager()).thenReturn(schemasDisplayManager);
		when(factories.getAppLayerFactory().getModelLayerFactory().getMetadataSchemasManager()).thenReturn(metadataSchemasManager);
		when(schemasDisplayManager.getTypes(zeCollection)).thenReturn(typesDisplayConfig);
		when(factories.getModelLayerFactory().getSystemConfigs()).thenReturn(mockedConfigs);
		when(mockedConfigs.getSearchSortType()).thenReturn(SearchSortType.RELEVENCE);
		when(factories.getAppLayerFactory().getModulesManager()).thenReturn(modulesManager);
		when(factories.getModelLayerFactory().newUserServices()).thenReturn(userServices);
		when(metadataSchemaTypes.getLanguages()).thenReturn(Arrays.asList(Language.French));
		when(record.getSchemaCode()).thenReturn(User.DEFAULT_SCHEMA);
		when(roles.getSchemasRecordsServices()).thenReturn(schemasRecordsServices);
		doReturn(authorizationsServices).when(factories.getModelLayerFactory()).newAuthorizationsServices();

		User user = new User(record, metadataSchemaTypes, roles) {
			@Override
			public boolean isApplyFacetsEnabled() {
				return false;
			}
		};

		when(userServices.getUserInCollection(any(String.class), any(String.class))).thenReturn(user);

		presenter = spy(new SimpleSearchPresenter(view) {
			@Override
			protected User getCurrentUser() {
				return user;
			}
		});
		doReturn(null).when(presenter).allowedSchemaTypes();
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
