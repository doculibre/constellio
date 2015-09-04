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
package com.constellio.app.modules.es.services;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.modules.es.connectors.spi.Connector;
import com.constellio.app.modules.es.model.connectors.ConnectorInstance;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;

public class ConnectorManagerAcceptanceTest extends ConstellioTest {

	@Mock Connector connector1, connector2, connector3;

	ConnectorInstance<?> instance1, instance2, instance3;
	ESSchemasRecordsServices es;
	ConnectorManager connectorManager;
	Users users = new Users();

	User dakota, edouard;

	@Before
	public void setUp()
			throws Exception {

		givenCollection(zeCollection).withConstellioESModule();
		users.setUp(getModelLayerFactory().newUserServices());
		es = new ESSchemasRecordsServices(zeCollection, getAppLayerFactory());
		instance1 = es.newConnectorHttpInstanceWithId("instance1").setCode("connector1").setTitle("connector1").setEnabled(false)
				.setSeeds(asList("http://constellio.com"));
		instance2 = es.newConnectorHttpInstanceWithId("instance2").setCode("connector2").setTitle("connector2").setEnabled(false)
				.setSeeds(asList("http://doculibre.com"));
		instance3 = es.newConnectorHttpInstanceWithId("instance3").setCode("connector3").setTitle("connector3").setEnabled(false)
				.setSeeds(asList("http://perdu.com"));

		connectorManager = spy(es.getConnectorManager());
		connectorManager.save(instance1);
		connectorManager.save(instance2);
		connectorManager.save(instance3);

		doReturn(connector1).when(connectorManager).instanciate(instance1);
		doReturn(connector2).when(connectorManager).instanciate(instance2);
		doReturn(connector3).when(connectorManager).instanciate(instance3);
		getModelLayerFactory().newUserServices().addUserToCollection(users.edouardLechat(), zeCollection);
		getModelLayerFactory().newUserServices().addUserToCollection(users.dakotaLIndien(), zeCollection);

		dakota = users.dakotaLIndienIn(zeCollection);
		edouard = users.edouardIn(zeCollection);
	}

	@Test
	public void whenGetUserTokensThenConnectToEveryConnectorToBuildAListOfTokens()
			throws Exception {

		assertThat(dakota.getManualTokens()).isEmpty();

		when(connector1.fetchTokens("dakota")).thenReturn(asList("rinstance1_token1"));
		when(connector1.fetchTokens("edouard")).thenReturn(asList("rinstance1_token1", "rinstance1_token2"));
		when(connector2.fetchTokens("dakota")).thenReturn(asList("rinstance2_token1"));
		when(connector2.fetchTokens("edouard")).thenReturn(asList("rinstance2_token1", "rinstance2_token2"));
		when(connector3.fetchTokens("dakota")).thenReturn(asList("rinstance3_token1"));
		when(connector3.fetchTokens("edouard")).thenReturn(asList("rinstance3_token1", "rinstance3_token2"));

		connectorManager.updateUserTokens(dakota);
		connectorManager.updateUserTokens(edouard);
		assertThat(dakota.getManualTokens()).containsOnly("rinstance1_token1", "rinstance2_token1", "rinstance3_token1");
		assertThat(edouard.getManualTokens()).containsOnly("rinstance1_token1", "rinstance1_token2", "rinstance2_token1",
				"rinstance2_token2", "rinstance3_token1", "rinstance3_token2");

		when(connector1.fetchTokens("dakota")).thenReturn(asList("rinstance1_token3"));
		when(connector1.fetchTokens("edouard")).thenReturn(asList("rinstance1_token4", "rinstance1_token2"));
		when(connector2.fetchTokens("dakota")).thenReturn(asList("rinstance2_token3"));
		when(connector2.fetchTokens("edouard")).thenReturn(asList("rinstance2_token4", "rinstance2_token2"));
		when(connector3.fetchTokens("dakota")).thenReturn(asList("rinstance3_token3"));
		when(connector3.fetchTokens("edouard")).thenReturn(asList("rinstance3_token4", "rinstance3_token2"));

		connectorManager.updateUserTokens(dakota);
		connectorManager.updateUserTokens(edouard);
		assertThat(dakota.getManualTokens()).containsOnly("rinstance1_token3", "rinstance2_token3", "rinstance3_token3");
		assertThat(edouard.getManualTokens()).containsOnly("rinstance1_token4", "rinstance1_token2", "rinstance2_token4",
				"rinstance2_token2", "rinstance3_token4", "rinstance3_token2");

		connectorManager.delete(instance1);

		connectorManager.updateUserTokens(dakota);
		connectorManager.updateUserTokens(edouard);
		assertThat(dakota.getManualTokens()).containsOnly("rinstance2_token3", "rinstance3_token3");
		assertThat(edouard.getManualTokens()).containsOnly("rinstance2_token4",
				"rinstance2_token2", "rinstance3_token4", "rinstance3_token2");

	}
}
