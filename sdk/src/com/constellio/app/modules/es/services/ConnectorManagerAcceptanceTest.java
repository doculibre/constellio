package com.constellio.app.modules.es.services;

import com.constellio.app.modules.es.connectors.spi.Connector;
import com.constellio.app.modules.es.model.connectors.ConnectorInstance;
import com.constellio.app.modules.es.model.connectors.http.ConnectorHttpDocument;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class ConnectorManagerAcceptanceTest extends ConstellioTest {

	@Mock Connector connector1, connector2, connector3;

	MetadataSchemasManager schemasManager;
	ConnectorInstance instance1, instance2, instance3;
	ESSchemasRecordsServices es;
	ConnectorManager connectorManager;
	Users users = new Users();

	User dakota, edouard;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(withZeCollection().withConstellioESModule());
		users.setUp(getModelLayerFactory().newUserServices());
		es = new ESSchemasRecordsServices(zeCollection, getAppLayerFactory());
		instance1 = es.newConnectorHttpInstanceWithId("instance1").setCode("connector1").setTitle("connector1").setEnabled(false)
				.setSeeds("http://constellio.com");
		instance2 = es.newConnectorHttpInstanceWithId("instance2").setCode("connector2").setTitle("connector2").setEnabled(false)
				.setSeeds("http://doculibre.com");
		instance3 = es.newConnectorHttpInstanceWithId("instance3").setCode("connector3").setTitle("connector3").setEnabled(false)
				.setSeeds("http://perdu.com");

		connectorManager = spy(es.getConnectorManager());
		connectorManager.save(instance1);
		connectorManager.save(instance2);
		connectorManager.save(instance3);

		doReturn(connector1).when(connectorManager).instanciate(instance1);
		doReturn(connector2).when(connectorManager).instanciate(instance2);
		doReturn(connector3).when(connectorManager).instanciate(instance3);
		getModelLayerFactory().newUserServices().execute(users.edouardLechat().getUsername(), (req) -> req.addToCollection(zeCollection));
		getModelLayerFactory().newUserServices().execute(users.dakotaLIndien().getUsername(), (req) -> req.addToCollection(zeCollection));
		schemasManager = getModelLayerFactory().getMetadataSchemasManager();

		dakota = users.dakotaLIndienIn(zeCollection);
		edouard = users.edouardIn(zeCollection);
	}

	@Test
	public void whenCreateOrUpdateConnectorInstanceThenUpdateSchemaTitle()
			throws Exception {

		ConnectorInstance zeInstance = es.newConnectorHttpInstanceWithId("zeConnector").setCode("zeConnector")
				.setTitle("Ze connector").setEnabled(false).setSeeds("http://constellio.com");
		connectorManager.createConnector(zeInstance);
		String schemaCode = ConnectorHttpDocument.SCHEMA_TYPE + "_" + zeInstance.getId();
		assertThat(schemasManager.getSchemaTypes(zeCollection).getSchema(schemaCode).getLabel(Language.French)).isEqualTo("Ze connector");

		zeInstance.setTitle("Ze ultimate connector");
		getModelLayerFactory().newRecordServices().update(zeInstance);

		assertThat(schemasManager.getSchemaTypes(zeCollection).getSchema(schemaCode).getLabel(Language.French))
				.isEqualTo("Ze ultimate connector");
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
