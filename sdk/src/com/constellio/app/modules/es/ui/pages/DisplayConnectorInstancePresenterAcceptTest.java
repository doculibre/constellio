package com.constellio.app.modules.es.ui.pages;

import com.constellio.app.modules.es.model.connectors.ConnectorInstance;
import com.constellio.app.modules.es.model.connectors.ConnectorType;
import com.constellio.app.modules.es.navigation.ESViews;
import com.constellio.app.modules.es.services.ConnectorManager;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.MockedNavigation;
import com.constellio.sdk.tests.setups.Users;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static com.constellio.app.ui.i18n.i18n.$;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DisplayConnectorInstancePresenterAcceptTest extends ConstellioTest {

	@Mock DisplayConnectorInstanceView view;
	@Mock RecordVO recordVO;
	RMTestRecords records = new RMTestRecords(zeCollection);
	ConnectorManager connectorManager;
	RecordServices recordServices;
	ESSchemasRecordsServices es;
	UserServices userServices;
	MockedNavigation navigator;

	Users users = new Users();
	ConnectorType connectorType;
	ConnectorInstance connectorInstance, anotherConnectorInstace;

	DisplayConnectorInstancePresenter presenter;

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
		connectorManager = es.getConnectorManager();

		users.setUp(userServices, zeCollection);

		connectorInstance = connectorManager
				.createConnector(es.newConnectorHttpInstance()
						.setCode("zeConnector")
						.setTitle("Ze Connector")
						.setTraversalCode("traversalCode").setEnabled(true)
						.setSeeds("http://constellio.com"));

		anotherConnectorInstace = connectorManager
				.createConnector(es.newConnectorHttpInstance()
						.setCode("anotherConnector")
						.setTitle("Another Connector")
						.setTraversalCode("anotherTraversalCode").setEnabled(true)
						.setSeeds("http://constellio.com"));

		presenter = new DisplayConnectorInstancePresenter(view);
	}

	@Test
	public void whenFormParamThenNewRecordVO()
			throws Exception {

		assertThat(presenter.getRecordVO()).isNull();

		presenter.forParams(connectorInstance.getId());

		assertThat(presenter.getRecordVO().getId()).isEqualTo(connectorInstance.getId());
	}

	@Test
	public void givenUserWithoutManagerConnectorPermissionWhenHasPageAccessThenReturnFalse()
			throws Exception {

		assertThat(presenter.hasPageAccess("", users.chuckNorrisIn(zeCollection))).isFalse();
	}

	@Test
	public void givenUserWithManagerConnectorPermissionWhenHasPageAccessThenReturnFalse()
			throws Exception {

		assertThat(presenter.hasPageAccess("", users.adminIn(zeCollection))).isTrue();
	}

	@Test
	public void whenGetConnectorInstaceTitleThenOk()
			throws Exception {

		presenter.forParams(connectorInstance.getId());
		assertThat(presenter.getTitle()).isEqualTo(
				$("DisplayConnectorInstanceView.viewTitle") + " Ze Connector");
	}

	@Test
	public void whenEditButtonClickedThenNavigateToEdit()
			throws Exception {

		when(recordVO.getId()).thenReturn(connectorInstance.getId());

		presenter.forParams(connectorInstance.getId());
		presenter.editConnectorInstanceButtonClicked();

		verify(view.navigate().to(ESViews.class)).editConnectorInstance(connectorInstance.getId());
	}

	@Test
	public void whenGetRestrictedRecordIdsThenReturnConnectorId()
			throws Exception {

		presenter.forParams(connectorInstance.getId());
		assertThat(presenter.getRestrictedRecordIds(connectorInstance.getId())).isEqualTo(asList(connectorInstance.getId()));
	}

	@Test
	public void whenGetLastDocumentsThenOk()
			throws Exception {

		addFetchedDocument(connectorInstance);
		presenter.forParams(connectorInstance.getId());
		assertThat(presenter.getLastDocuments()).contains("http://constellio.com/document1");
		assertThat(presenter.getLastDocuments()).contains("http://constellio.com/document2");
	}

	@Test
	public void whenIsStartButtonVisibleThenStopButtonIsNot()
			throws Exception {

		connectorInstance = es.getConnectorInstance(connectorInstance.getId());
		presenter.forParams(connectorInstance.getId());
		assertThat(presenter.isStartButtonVisible()).isFalse();
		assertThat(presenter.isStopButtonVisible()).isTrue();
	}

	@Test
	public void whenStartButtonClickedThenStart()
			throws Exception {

		presenter.forParams(connectorInstance.getId());
		presenter.start();

		connectorInstance = es.getConnectorInstance(connectorInstance.getId());
		presenter.forParams(connectorInstance.getId());
		assertThat(connectorInstance.isEnabled()).isTrue();
		assertThat(presenter.isStartButtonVisible()).isFalse();
		assertThat(presenter.isStopButtonVisible()).isTrue();
	}

	@Test
	public void givenConnectorInstanceStartedWhenStopButtonClickedThenStop()
			throws Exception {

		presenter.forParams(connectorInstance.getId());
		presenter.start();

		connectorInstance = es.getConnectorInstance(connectorInstance.getId());
		presenter.forParams(connectorInstance.getId());
		assertThat(connectorInstance.isEnabled()).isTrue();
		assertThat(presenter.isStartButtonVisible()).isFalse();
		assertThat(presenter.isStopButtonVisible()).isTrue();

		presenter.forParams(connectorInstance.getId());
		presenter.stop();

		connectorInstance = es.getConnectorInstance(connectorInstance.getId());
		presenter.forParams(connectorInstance.getId());
		assertThat(connectorInstance.isEnabled()).isFalse();
		assertThat(presenter.isStartButtonVisible()).isTrue();
		assertThat(presenter.isStopButtonVisible()).isFalse();
	}

	@Test
	public void whenEditSchemasButtonClickedThenNavigateToEdit()
			throws Exception {

		presenter.forParams(connectorInstance.getId());
		presenter.editSchemasButtonClicked();

		verify(view.navigate().to(ESViews.class)).displayConnectorMappings(connectorInstance.getId());
	}

	private void addFetchedDocument(ConnectorInstance connectorInstance)
			throws Exception {
		Transaction transaction = new Transaction();

		transaction.add(es.newConnectorHttpDocumentWithId("olderTraversalRecord", connectorInstance))
				.setURL("http://constellio.com/document1").setTitle("Titre1").setFetched(true)
				.setModifiedOn(new LocalDateTime()).setTraversalCode(connectorInstance.getTraversalCode());

		transaction.add(es.newConnectorHttpDocumentWithId("record2", connectorInstance))
				.setModifiedOn(new LocalDateTime().plusSeconds(3)).setURL("http://constellio.com/document2").setTitle("Titre2")
				.setFetched(true).setTraversalCode(connectorInstance.getTraversalCode());

		recordServices.execute(transaction);
	}

}
