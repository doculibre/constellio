package com.constellio.app.modules.es.ui.pages;

import com.constellio.app.modules.es.model.connectors.ConnectorInstance;
import com.constellio.app.modules.es.model.connectors.ConnectorType;
import com.constellio.app.modules.es.services.ConnectorManager;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.SDKViewNavigation;
import com.constellio.sdk.tests.setups.Users;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static com.constellio.app.ui.i18n.i18n.$;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class EditConnectorInstancePresenterAcceptTest extends ConstellioTest {

	@Mock EditConnectorInstanceView view;
	@Mock RecordVO recordVO;
	Users users = new Users();
	ConnectorManager connectorManager;
	RecordServices recordServices;
	UserServices userServices;
	ESSchemasRecordsServices es;

	ConnectorType connectorType;
	ConnectorInstance connectorInstance;

	EditConnectorInstancePresenter presenter;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(withZeCollection().withConstellioESModule().withAllTestUsers());
		ConstellioFactories constellioFactories = getConstellioFactories();

		SessionContext sessionContext;
		when(view.getSessionContext()).thenReturn(FakeSessionContext.adminInCollection(zeCollection));
		when(view.getCollection()).thenReturn(zeCollection);
		when(view.getConstellioFactories()).thenReturn(constellioFactories);
		new SDKViewNavigation(view);

		es = new ESSchemasRecordsServices(zeCollection, getAppLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();
		userServices = getModelLayerFactory().newUserServices();
		users.setUp(userServices);
		connectorManager = es.getConnectorManager();

		configureConnectorsInstances();

		presenter = new EditConnectorInstancePresenter(view);
	}

	private void configureConnectorsInstances() {
		connectorInstance = connectorManager
				.createConnector(es.newConnectorHttpInstance()
						.setCode("zeConnector")
						.setTitle("Ze Connector")
						.setTraversalCode("traversalCode")
						.setEnabled(true)
						.setSeeds("http://constellio.com"));

	}

	@Test
	public void givenConnectorInstanceWhenEditThenOk()
			throws Exception {

		presenter.forParams(connectorInstance.getId());

		RecordVO recordVO = new RecordToVOBuilder()
				.build(connectorInstance.getWrappedRecord(), VIEW_MODE.FORM, view.getSessionContext());
		recordVO.set(Schemas.TITLE_CODE, "new Title");

		presenter.saveButtonClicked(recordVO);

		assertThat(recordServices.getDocumentById(connectorInstance.getId()).<String>get(Schemas.TITLE)).isEqualTo("new Title");
	}

	@Test
	public void whenGetConnectorInstaceTitleThenOk()
			throws Exception {

		assertThat(presenter.getTitle()).isEqualTo($("EditConnectorInstanceView.viewTitle"));
	}
}
