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
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.setups.Users;

public class EditConnectorInstancePresenterAcceptTest extends ConstellioTest {

	@Mock EditConnectorInstanceView view;
	@Mock ConstellioNavigator navigator;
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

		presenter = new EditConnectorInstancePresenter(view);
	}

	private void configureConnectorsInstances() {
		connectorInstance = connectorManager
				.createConnector(es.newConnectorHttpInstance()
						.setCode("zeConnector")
						.setTitle("Ze Connector")
						.setTraversalCode("traversalCode")
						.setEnabled(true)
						.setSeeds(asList("http://constellio.com")));

	}

	@Test
	public void givenConnectorInstanceWhenEditThenOk()
			throws Exception {

		presenter.forParams(connectorInstance.getId());

		RecordVO recordVO = new RecordToVOBuilder()
				.build(connectorInstance.getWrappedRecord(), VIEW_MODE.FORM, view.getSessionContext());
		recordVO.set(Schemas.TITLE_CODE, "new Title");

		presenter.saveButtonClicked(recordVO);

		assertThat(recordServices.getDocumentById(connectorInstance.getId()).get(Schemas.TITLE)).isEqualTo("new Title");
	}

	@Test
	public void whenGetConnectorInstaceTitleThenOk()
			throws Exception {

		assertThat(presenter.getTitle()).isEqualTo($("EditConnectorInstanceView.viewTitle"));
	}
}