package com.constellio.app.modules.es.services.mapping.connectors;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.ListAssert;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.es.model.connectors.ConnectorInstance;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbDocument;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbFolder;
import com.constellio.app.modules.es.services.ConnectorManager;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.app.modules.es.services.mapping.ConnectorField;
import com.constellio.app.modules.es.services.mapping.ConnectorMappingService;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;

public class ConnectorSmbMappingServiceAcceptanceTest extends ConstellioTest {

	ConnectorInstance<?> smbConnectorInstance;

	Users users = new Users();

	ConnectorManager connectorManager;
	ESSchemasRecordsServices es;
	ConnectorMappingService service;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(withZeCollection().withConstellioESModule().withAllTest(users));

		es = new ESSchemasRecordsServices(zeCollection, getAppLayerFactory());
		service = new ConnectorMappingService(es);
		connectorManager = es.getConnectorManager();

		smbConnectorInstance = connectorManager.createConnector(es.newConnectorSmbInstance()
				.setCode("smb").setTitle("Smb connector").setEnabled(false)
				.setDomain("domain").setSeeds(asList("seeds")).setUsername("username").setPassword("password"));
		smbConnectorInstance = es.wrapConnectorInstance(smbConnectorInstance.getWrappedRecord());
	}

	@Test
	public void whenGetConnectorSchemaTypesThenReturnAllTypes()
			throws Exception {

		assertThat(service.getDocumentTypes(smbConnectorInstance)).containsOnly(
				ConnectorSmbDocument.SCHEMA_TYPE,
				ConnectorSmbFolder.SCHEMA_TYPE);

	}

	@Test
	public void givenAConnectorHasFetchedNothingThenReturnDefaultDeclaredFields()
			throws Exception {

		assertThatConnectorFields(smbConnectorInstance, ConnectorSmbFolder.SCHEMA_TYPE).isEmpty();

		assertThatConnectorFields(smbConnectorInstance, ConnectorSmbDocument.SCHEMA_TYPE).isEmpty();

	}

	private ListAssert<ConnectorField> assertThatConnectorFields(ConnectorInstance<?> connectorInstance,
			String connectorDocumentSchemaType) {
		return assertThat(service.getConnectorFields(connectorInstance, connectorDocumentSchemaType))
				.usingFieldByFieldElementComparator();
	}
}
