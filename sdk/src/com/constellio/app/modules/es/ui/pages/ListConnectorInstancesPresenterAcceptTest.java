package com.constellio.app.modules.es.ui.pages;

import com.constellio.app.modules.es.model.connectors.ConnectorInstance;
import com.constellio.app.modules.es.model.connectors.ConnectorType;
import com.constellio.app.modules.es.navigation.ESViews;
import com.constellio.app.modules.es.services.ConnectorManager;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.MockedNavigation;
import com.constellio.sdk.tests.setups.Users;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ListConnectorInstancesPresenterAcceptTest extends ConstellioTest {
	@Mock ListConnectorInstancesView view;
	MockedNavigation navigator;
	@Mock RecordVO recordVO;
	RecordServices recordServices;
	Users users = new Users();
	UserServices userServices;
	ESSchemasRecordsServices es;
	ConnectorManager connectorManager;
	ConnectorType connectorType;
	ConnectorInstance connectorInstance, anotherConnectorInstace;

	ListConnectorInstancesPresenter presenter;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(withZeCollection().withConstellioESModule().withAllTestUsers());
		ConstellioFactories constellioFactories = getConstellioFactories();

		when(view.getSessionContext()).thenReturn(FakeSessionContext.adminInCollection(zeCollection));
		when(view.getCollection()).thenReturn(zeCollection);
		when(view.getConstellioFactories()).thenReturn(constellioFactories);
		navigator = new MockedNavigation();
		when(view.navigate()).thenReturn(navigator);

		es = new ESSchemasRecordsServices(zeCollection, getAppLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();
		userServices = getModelLayerFactory().newUserServices();
		users.setUp(userServices, zeCollection);
		connectorManager = es.getConnectorManager();

		configureConnectorsInstances();

		presenter = new ListConnectorInstancesPresenter(view);
	}

	private void configureConnectorsInstances() {
		connectorInstance = connectorManager
				.createConnector(es.newConnectorHttpInstance()
						.setCode("zeConnector")
						.setTitle("Ze Connector")
						.setTraversalCode("traversalCode")
						.setEnabled(true)
						.setSeeds("http://constellio.com"));

		anotherConnectorInstace = connectorManager
				.createConnector(es.newConnectorHttpInstance()
						.setCode("anotherConnector")
						.setTitle("Another Connector")
						.setTraversalCode("anotherTraversalCode")
						.setEnabled(true)
						.setSeeds("http://constellio.com"));
	}

	@Test
	public void givenUserWithoutManagerConnectorPermissionWhenHasPageAccessThenReturnFalse()
			throws Exception {

		assertThat(presenter.hasPageAccess("", users.chuckNorrisIn(zeCollection))).isFalse();
	}

	@Test
	public void givenUserWithManagerConnectorPermissionWhenHasPageAccessThenReturnTrue()
			throws Exception {

		assertThat(presenter.hasPageAccess("", users.adminIn(zeCollection))).isTrue();
	}

	@Test
	public void whenGetDataProviderThenOk()
			throws Exception {

		assertThat(presenter.getDataProvider().size()).isEqualTo(2);
	}

	@Test
	public void whenDisplayButtonClickedThenNavigateToDisplay()
			throws Exception {

		when(recordVO.getId()).thenReturn("recordId");

		presenter.displayButtonClicked(recordVO);

		verify(view.navigate().to(ESViews.class)).displayConnectorInstance("recordId");
	}

	@Test
	public void whenEditButtonClickedThenNavigateToEdit()
			throws Exception {

		when(recordVO.getId()).thenReturn("recordId");

		presenter.editButtonClicked(recordVO);

		verify(view.navigate().to(ESViews.class)).editConnectorInstance("recordId");
	}
}
