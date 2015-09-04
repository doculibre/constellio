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

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.modules.es.model.connectors.ConnectorInstance;
import com.constellio.app.modules.es.model.connectors.ConnectorType;
import com.constellio.app.modules.es.services.ConnectorManager;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.app.ui.application.ConstellioNavigator;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.setups.Users;

public class ListConnectorInstancesPresenterAcceptTest extends ConstellioTest {

	@Mock ListConnectorInstancesView view;
	@Mock ConstellioNavigator navigator;
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

		verify(view.navigateTo()).displayConnectorInstance("recordId");
	}

	@Test
	public void whenEditButtonClickedThenNavigateToEdit()
			throws Exception {

		when(recordVO.getId()).thenReturn("recordId");

		presenter.editButtonClicked(recordVO);

		verify(view.navigateTo()).editConnectorInstances("recordId");
	}

	//	@Test
	public void whenDeleteButtonClickedThenDeleteRecord()
			throws Exception {

		when(recordVO.getId()).thenReturn(connectorInstance.getId());

		presenter.deleteButtonClicked(recordVO);

		verify(view.navigateTo()).listConnectorInstances();
	}
}