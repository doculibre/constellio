package com.constellio.app.modules.es.services.mapping.connectors;

import static com.constellio.model.entities.schemas.MetadataValueType.DATE;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.ListAssert;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.es.model.connectors.ConnectorInstance;
import com.constellio.app.modules.es.model.connectors.ldap.ConnectorLDAPUserDocument;
import com.constellio.app.modules.es.services.ConnectorManager;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.app.modules.es.services.mapping.ConnectorField;
import com.constellio.app.modules.es.services.mapping.ConnectorMappingService;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;

public class ConnectorLdapMappingServiceAcceptanceTest extends ConstellioTest {

	ConnectorInstance<?> ldapConnectorInstance;

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

		ldapConnectorInstance = connectorManager.createConnector(es.newConnectorLDAPInstance()
				.setUsername("bob").setPassword("marley").setUrls(asList("url")).setConnectionUsername("user")
				.setUsersBaseContextList(asList("user1"))
				.setCode("http").setTitle("Http connector").setEnabled(false));
		ldapConnectorInstance = es.wrapConnectorInstance(ldapConnectorInstance.getWrappedRecord());
	}

	@Test
	public void whenGetConnectorSchemaTypesThenReturnAllTypes()
			throws Exception {

		assertThat(service.getDocumentTypes(ldapConnectorInstance)).containsOnly(
				ConnectorLDAPUserDocument.SCHEMA_TYPE);

	}

	@Test
	public void givenAConnectorHasFetchedNothingThenReturnDefaultDeclaredFields()
			throws Exception {

		String userDocument = ConnectorLDAPUserDocument.SCHEMA_TYPE;
		assertThatConnectorFields(ldapConnectorInstance, ConnectorLDAPUserDocument.SCHEMA_TYPE).containsOnly(
				new ConnectorField(userDocument + ":userAccountControl", "userAccountControl", STRING),
				new ConnectorField(userDocument + ":sAMAccountType", "sAMAccountType", STRING),
				new ConnectorField(userDocument + ":primaryGroupID", "primaryGroupID", STRING),
				new ConnectorField(userDocument + ":objectSid", "objectSid", STRING),
				new ConnectorField(userDocument + ":objectGUID", "objectGUID", STRING),
				new ConnectorField(userDocument + ":uSNChanged", "uSNChanged", STRING),
				new ConnectorField(userDocument + ":uSNCreated", "uSNCreated", STRING),
				new ConnectorField(userDocument + ":userPrincipalName", "userPrincipalName", STRING),
				new ConnectorField(userDocument + ":primaryGroupID", "primaryGroupID", STRING),
				new ConnectorField(userDocument + ":name", "name", STRING),
				new ConnectorField(userDocument + ":displayName", "displayName", STRING),
				new ConnectorField(userDocument + ":whenChanged", "whenChanged", DATE),
				new ConnectorField(userDocument + ":whenCreated", "whenCreated", DATE)
		);

	}

	private ListAssert<ConnectorField> assertThatConnectorFields(ConnectorInstance<?> connectorInstance,
			String connectorDocumentSchemaType) {
		return assertThat(service.getConnectorFields(connectorInstance, connectorDocumentSchemaType))
				.usingFieldByFieldElementComparator();
	}
}
