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
package com.constellio.app.modules.es.ui.pages;

import static com.constellio.app.ui.i18n.i18n.$;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.modules.es.model.connectors.ConnectorInstance;
import com.constellio.app.modules.es.model.connectors.ConnectorType;
import com.constellio.app.modules.es.model.connectors.http.ConnectorHttpDocument;
import com.constellio.app.modules.es.model.connectors.http.ConnectorHttpInstance;
import com.constellio.app.modules.es.services.ConnectorManager;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.app.ui.application.ConstellioNavigator;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.setups.Users;

public class WizardConnectorInstancePresenterAcceptTest extends ConstellioTest {

	@Mock WizardConnectorInstanceView view;
	@Mock ConstellioNavigator navigator;
	@Mock RecordVO recordVO;
	Users users = new Users();
	ConnectorManager connectorManager;
	RecordServices recordServices;
	UserServices userServices;
	ESSchemasRecordsServices es;

	ConnectorType connectorType;
	ConnectorInstance connectorInstance, anotherConnectorInstace;
	MetadataSchemasManager metadataSchemasManager;

	WizardConnectorInstancePresenter presenter;

	@Before
	public void setUp()
			throws Exception {

		givenCollection(zeCollection).withConstellioESModule().withAllTestUsers();

		when(view.getSessionContext()).thenReturn(FakeSessionContext.adminInCollection(zeCollection));
		when(view.getCollection()).thenReturn(zeCollection);
		when(view.getConstellioFactories()).thenReturn(getConstellioFactories());
		when(view.navigateTo()).thenReturn(navigator);

		es = new ESSchemasRecordsServices(zeCollection, getAppLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();
		userServices = getModelLayerFactory().newUserServices();
		users.setUp(userServices);
		connectorManager = es.getConnectorManager();
		metadataSchemasManager = getModelLayerFactory().getMetadataSchemasManager();

		configureConnectorsInstances();

		presenter = new WizardConnectorInstancePresenter(view);
	}

	private void configureConnectorsInstances() {
		connectorInstance = connectorManager
				.createConnector(es.newConnectorHttpInstance()
						.setCode("zeConnector")
						.setTitle("Ze Connector")
						.setTraversalCode("traversalCode")
						.setEnabled(true)
						.setSeeds(asList("http://constellio.com")));

		anotherConnectorInstace = connectorManager
				.createConnector(es.newConnectorHttpInstance()
						.setCode("anotherConnector")
						.setTitle("Another Connector")
						.setTraversalCode("anotherTraversalCode")
						.setEnabled(true)
						.setSeeds(asList("http://constellio.com")));
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
	public void whenCancelButtonClickedThenNavigateSelectConnectorTypeTab()
			throws Exception {

		presenter.cancelButtonClicked();

		verify(view).setConnectorTypeListTable();
	}

	@Test
	public void whenConnectorTypeButtonClickedThenSetRecordVOType()
			throws Exception {

		presenter.connectorTypeButtonClicked(es.getHttpConnectorTypeId());

		assertThat(presenter.connectorTypeId).isEqualTo(es.getHttpConnectorTypeId());
		assertThat(presenter.recordVO).isNotNull();
		assertThat(presenter.currentSchemaCode).isEqualTo(ConnectorInstance.SCHEMA_TYPE + "_" + ConnectorType.CODE_HTTP);
	}

	@Test
	public void givenTwoConnectorTypesWhenGetDataProviderConnectorTypesThenSizeOk()
			throws Exception {

		RecordVODataProvider recordVODataProvider = presenter.getDataProviderSelectConnectorType();

		assertThat(recordVODataProvider.size()).isEqualTo(1);
		//assertThat(recordVODataProvider.getRecordVO(0).getTitle()).contains("Connecteur HTTP");
		assertThat(recordVODataProvider.getRecordVO(0).getTitle()).contains("Connecteur Smb");
	}

	@Test
	public void whenBackButtonClickedThenNavigateToList()
			throws Exception {

		presenter.backButtonClicked();

		verify(view.navigateTo()).listConnectorInstances();
	}

	@Test
	public void whenSaveButtonClickedThenCreateSchemaAndRecord()
			throws Exception {

		presenter.connectorTypeButtonClicked(es.getHttpConnectorTypeId());
		presenter.recordVO.set(ConnectorHttpInstance.TITLE, "new Title");
		presenter.recordVO.set(ConnectorHttpInstance.CODE, "newCode");
		presenter.recordVO.set(ConnectorHttpInstance.SEEDS, asList("constellio.com"));

		presenter.saveButtonClicked(presenter.recordVO);

		String id = es.getConnectorHttpInstanceWithCode("newCode").getId();

		assertThat(recordServices.getDocumentById(id).get(Schemas.TITLE)).isEqualTo("new Title");
		assertThat(metadataSchemasManager.getSchemaTypes(zeCollection).getSchema(ConnectorHttpDocument.SCHEMA_TYPE + "_" + id))
				.isNotNull();
	}
}