package com.constellio.app.modules.es.ui.pages;

import static com.constellio.app.ui.i18n.i18n.$;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.modules.es.model.connectors.ConnectorInstance;
import com.constellio.app.modules.es.model.connectors.ConnectorType;
import com.constellio.app.modules.es.model.connectors.http.ConnectorHttpDocument;
import com.constellio.app.modules.es.model.connectors.http.ConnectorHttpInstance;
import com.constellio.app.modules.es.services.ConnectorManager;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.SDKViewNavigation;
import com.constellio.sdk.tests.setups.Users;

public class WizardConnectorInstancePresenterAcceptTest extends ConstellioTest {

	@Mock WizardConnectorInstanceView view;
	SDKViewNavigation viewNavigation;
	@Mock RecordVO recordVO;
	Users users = new Users();
	ConnectorManager connectorManager;
	RecordServices recordServices;
	UserServices userServices;
	ESSchemasRecordsServices es;

	ConnectorType connectorType;
	ConnectorInstance<?> connectorInstance, anotherConnectorInstace;
	MetadataSchemasManager metadataSchemasManager;

	WizardConnectorInstancePresenter presenter;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(withZeCollection().withConstellioESModule().withAllTestUsers());
		ConstellioFactories constellioFactories = getConstellioFactories();

		when(view.getSessionContext()).thenReturn(FakeSessionContext.adminInCollection(zeCollection));
		when(view.getCollection()).thenReturn(zeCollection);
		when(view.getConstellioFactories()).thenReturn(constellioFactories);
		viewNavigation = new SDKViewNavigation(view);

		es = new ESSchemasRecordsServices(zeCollection, getAppLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();
		userServices = getModelLayerFactory().newUserServices();
		users.setUp(userServices, zeCollection);
		connectorManager = es.getConnectorManager();
		metadataSchemasManager = getModelLayerFactory().getMetadataSchemasManager();

		configureConnectorsInstances();

		presenter = spy(new WizardConnectorInstancePresenter(view));
		doNothing().when(presenter).validateConnectionInfoAreValid(any(Record.class));
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
	public void whenGetConnectorInstaceTitleThenOk()
			throws Exception {

		assertThat(presenter.getTitle()).isEqualTo($("WizardConnectorInstanceView.viewTitle"));
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
	public void whenConnectorTypeButtonClickedThenSetRecordVOType()
			throws Exception {

		presenter.connectorTypeSelected(es.getHttpConnectorTypeId());

		assertThat(presenter.connectorTypeId).isEqualTo(es.getHttpConnectorTypeId());
		assertThat(presenter.recordVO).isNotNull();
		assertThat(presenter.currentSchemaCode).isEqualTo(ConnectorInstance.SCHEMA_TYPE + "_" + ConnectorType.CODE_HTTP);
	}

	@Test
	public void givenThreeConnectorTypesWhenGetDataProviderConnectorTypesThenSizeOk()
			throws Exception {

		RecordVODataProvider recordVODataProvider = presenter.getConnectorTypeDataProvider();

		List<String> titles = new ArrayList<>();
		for (int i = 0; i < recordVODataProvider.size(); i++) {
			titles.add(recordVODataProvider.getRecordVO(i).getTitle());
		}

		assertThat(titles).containsOnly("Connecteur HTTP", "Connecteur SMB", "Connecteur LDAP");
	}

	@Test
	public void whenSaveButtonClickedThenCreateSchemaAndRecord()
			throws Exception {

		presenter.connectorTypeSelected(es.getHttpConnectorTypeId());
		presenter.recordVO.set(ConnectorHttpInstance.TITLE, "new Title");
		presenter.recordVO.set(ConnectorHttpInstance.CODE, "newCode");
		presenter.recordVO.set(ConnectorHttpInstance.SEEDS, "constellio.com");

		presenter.saveButtonClicked(presenter.recordVO);

		String id = es.getConnectorHttpInstanceWithCode("newCode").getId();

		assertThat(recordServices.getDocumentById(id).<String>get(Schemas.TITLE)).isEqualTo("new Title");
		assertThat(metadataSchemasManager.getSchemaTypes(zeCollection).getSchema(ConnectorHttpDocument.SCHEMA_TYPE + "_" + id))
				.isNotNull();
	}
}
