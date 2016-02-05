package com.constellio.app.modules.es.connectors.ldap;

import static com.constellio.sdk.tests.TestUtils.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import javax.naming.ldap.LdapContext;

import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.es.model.connectors.ldap.ConnectorLDAPInstance;
import com.constellio.app.modules.es.model.connectors.ldap.ConnectorLDAPUserDocument;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.model.conf.LDAPTestConfig;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.InternetTest;

@InternetTest
public class ConnectorLDAPUserDocumentFactoryRealTest extends ConstellioTest {
	ESSchemasRecordsServices es;

	ConnectorLDAPServices connectorLDAPServices = new ConnectorLDAPServicesImpl();
	private ConnectorLDAPInstance connectorInstance;
	private ConnectorLDAPUserDocument document;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(withZeCollection().withConstellioESModule().withAllTestUsers());

		es = new ESSchemasRecordsServices(zeCollection, getAppLayerFactory());
		connectorInstance = newConnectorLDAPInstance();
		document = es.newConnectorLDAPUserDocument(connectorInstance);
	}

	@Test
	public void whenPopulateUserThenOk()
			throws Exception {

		LdapContext ctx = getValidContext();
		String id = "CN=username0,OU=Departement1,OU=doculibre,DC=test,DC=doculibre,DC=ca";
		LDAPObjectAttributes attributes = connectorLDAPServices.getObjectAttributes(ctx, id);
		ConnectorLDAPUserDocument user = ConnectorLDAPUserDocumentFactory
				.populateUser(document, attributes, connectorInstance);
		assertThat(user.getFirstName()).isEqualTo("firstname0");
		assertThat(user.getLastName()).isEqualTo("lastname0");
		assertThat(user.getEmail()).isEqualTo("username0@doculibre.com");
		assertThat(user.getWorkTitle()).isEqualTo("jobtitle0");
		assertThat(user.getDistinguishedName())
				.isEqualTo("CN=username0,OU=Departement1,OU=doculibre,DC=test,DC=doculibre,DC=ca");
		assertThat(user.getDisplayName()).isEqualTo("username0");
		assertThat(user.getUsername()).isEqualTo("username0");
		assertThat(user.getAddress()).isEqualTo("office0 street0 city0 zip0 state0 Canada CA ");
		assertThat(user.getTelephone()).containsOnly("4184184180", "4184184185", "4184184182");
		assertThat(user.getCompany()).isEqualTo("company0");
		assertThat(user.getDepartment()).isEqualTo("dep0");
		assertThat(user.getManager()).isEqualTo("CN=bfay,CN=Users,DC=test,DC=doculibre,DC=ca");
	}

	@Test
	public void whenPopulateEnabledADUserThenEnsabled()
			throws Exception {

		LdapContext ctx = getValidContext();
		String id = getActiveUserId();
		LDAPObjectAttributes attributes = connectorLDAPServices.getObjectAttributes(ctx, id);

		ConnectorLDAPUserDocument user = ConnectorLDAPUserDocumentFactory
				.populateUser(document, attributes, connectorInstance);
		assertThat(user.getEnabled()).isTrue();
	}

	private String getActiveUserId() {
		return "CN=bfay,CN=Users,DC=test,DC=doculibre,DC=ca";
	}

	@Test
	public void whenPopulateDisabledADUserThenDisabled()
			throws Exception {

		LdapContext ctx = getValidContext();
		String id = getInactiveUserId();
		LDAPObjectAttributes attributes = connectorLDAPServices.getObjectAttributes(ctx, id);

		ConnectorLDAPUserDocument user = ConnectorLDAPUserDocumentFactory
				.populateUser(document, attributes, connectorInstance);
		assertThat(user.getEnabled()).isFalse();
	}

	private String getInactiveUserId() {
		return "CN=krbtgt,CN=Users,DC=test,DC=doculibre,DC=ca";
	}

	private LdapContext getValidContext() {
		String url = LDAPTestConfig.getUrls().get(0);
		String user = LDAPTestConfig.getUser() + "@" + LDAPTestConfig.getDomains().get(0);
		String password = LDAPTestConfig.getPassword();
		Boolean followReferences = false;
		boolean activeDirectory = true;
		LdapContext ctx = connectorLDAPServices.connectToLDAP(url, user, password, followReferences, activeDirectory);
		return ctx;
	}

	private ConnectorLDAPInstance newConnectorLDAPInstance() {
		String newTraversalCode = UUID.randomUUID()
				.toString();
		return (ConnectorLDAPInstance) es.newConnectorLDAPInstance().setPassword("pass").setUrls(asList("url"))
				.setConnectionUsername(
						"username").setTitle("title").setCode("code").setTraversalCode(newTraversalCode);
	}
}
