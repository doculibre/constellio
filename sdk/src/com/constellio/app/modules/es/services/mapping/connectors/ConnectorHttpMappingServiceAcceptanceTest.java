package com.constellio.app.modules.es.services.mapping.connectors;

import static com.constellio.model.entities.schemas.MetadataValueType.DATE_TIME;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.ListAssert;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.es.model.connectors.ConnectorInstance;
import com.constellio.app.modules.es.model.connectors.http.ConnectorHttpDocument;
import com.constellio.app.modules.es.services.ConnectorManager;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.app.modules.es.services.mapping.ConnectorField;
import com.constellio.app.modules.es.services.mapping.ConnectorMappingService;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;

public class ConnectorHttpMappingServiceAcceptanceTest extends ConstellioTest {

	ConnectorInstance<?> httpConnectorInstance;

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

		httpConnectorInstance = connectorManager.createConnector(es.newConnectorHttpInstance()
				.setCode("http").setTitle("Http connector").setEnabled(false)
				.setSeeds("seeds").setIncludePatterns("username"));
		httpConnectorInstance = es.wrapConnectorInstance(httpConnectorInstance.getWrappedRecord());
	}

	@Test
	public void whenGetConnectorSchemaTypesThenReturnAllTypes()
			throws Exception {

		assertThat(service.getDocumentTypes(httpConnectorInstance)).containsOnly(
				ConnectorHttpDocument.SCHEMA_TYPE);

	}

	@Test
	public void givenAConnectorHasFetchedNothingThenReturnDefaultDeclaredFields()
			throws Exception {
		assertThatConnectorFields(httpConnectorInstance, ConnectorHttpDocument.SCHEMA_TYPE).containsOnly(
				new ConnectorField(ConnectorHttpDocument.SCHEMA_TYPE + ":charset", "Encodage", STRING),
				new ConnectorField(ConnectorHttpDocument.SCHEMA_TYPE + ":language", "Langue", STRING),
				new ConnectorField(ConnectorHttpDocument.SCHEMA_TYPE + ":lastModification", "Dernière modification", DATE_TIME)
		);

	}

	private ListAssert<ConnectorField> assertThatConnectorFields(ConnectorInstance<?> connectorInstance,
			String connectorDocumentSchemaType) {
		return assertThat(service.getConnectorFields(connectorInstance, connectorDocumentSchemaType))
				.usingFieldByFieldElementComparator();
	}
}
