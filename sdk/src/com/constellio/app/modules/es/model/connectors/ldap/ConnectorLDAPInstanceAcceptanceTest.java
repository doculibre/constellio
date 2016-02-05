package com.constellio.app.modules.es.model.connectors.ldap;

import static com.constellio.app.modules.es.model.connectors.ldap.enums.DirectoryType.ACTIVE_DIRECTORY;
import static com.constellio.sdk.tests.TestUtils.asList;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.es.services.ConnectorManager;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;

public class ConnectorLDAPInstanceAcceptanceTest extends ConstellioTest {

	RecordServices recordServices;
	RMSchemasRecordsServices schemas;
	Users users = new Users();
	UserServices userServices;
	ESSchemasRecordsServices es;
	ConnectorManager connectorManager;

	@Before
	public void setUp()
			throws Exception {

		//givenBackgroundThreadsEnabled();
		givenTransactionLogIsEnabled();
		prepareSystem(
				withZeCollection().withConstellioESModule().withAllTestUsers()
		);
		inCollection(zeCollection).setCollectionTitleTo("Collection de test");

		recordServices = getModelLayerFactory().newRecordServices();

		es = new ESSchemasRecordsServices(zeCollection, getAppLayerFactory());

	}

	@Test
	public void whenCreateNewConnectorInstanceThenOk()
			throws Exception {
		ConnectorLDAPInstance connectorInstance = es.newConnectorLDAPInstance();
		connectorInstance = (ConnectorLDAPInstance) connectorInstance.setUrls(asList("url"))
				.setConnectionUsername("zeUser")
				.setUsersBaseContextList(asList("BC"))
				.setPassword("zePassword")
				.setCode("code").setTitle("title");
		recordServices.add(connectorInstance);
		assertThat(connectorInstance.getDirectoryType()).isEqualTo(ACTIVE_DIRECTORY);
		assertThat(connectorInstance.getFirstName()).isEqualTo("givenName");
		assertThat(connectorInstance.getLastName()).isEqualTo("sn");
		assertThat(connectorInstance.getEmail()).isEqualTo("mail");
		assertThat(connectorInstance.getAddress()).contains("streetAddress", "postalCode");
		assertThat(connectorInstance.getDistinguishedName()).isEqualTo("distinguishedName");
		assertThat(connectorInstance.getFollowReferences()).isNull();
		assertThat(connectorInstance.getFetchComputers()).isNull();
		assertThat(connectorInstance.getFetchGroups()).isNull();
		assertThat(connectorInstance.getFetchUsers()).isTrue();
		assertThat(connectorInstance.getPassword()).isEqualTo("zePassword");
	}
}
