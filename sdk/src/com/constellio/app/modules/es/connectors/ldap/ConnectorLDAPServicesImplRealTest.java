package com.constellio.app.modules.es.connectors.ldap;

import static com.constellio.app.modules.es.connectors.ldap.ConnectorLDAPServicesImpl.computeSearchFilter;
import static com.constellio.app.modules.es.connectors.ldap.ConnectorLDAPServicesImpl.getSimpleName;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.naming.ldap.LdapContext;

import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.es.connectors.ldap.ConnectorLDAPServicesImpl.InvalidSearchFilterRuntimeException;
import com.constellio.model.conf.LDAPTestConfig;
import com.constellio.model.conf.ldap.RegexFilter;
import com.constellio.sdk.tests.annotations.InternetTest;

@InternetTest
public class ConnectorLDAPServicesImplRealTest {
	ConnectorLDAPServices connectorLDAPServices;

	@Before
	public void setUp()
			throws Exception {
		connectorLDAPServices = new ConnectorLDAPServicesImpl();
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

	@Test
	@InternetTest
	public void whenConnectToLDAPThenOK()
			throws Exception {
		LdapContext ctx = getValidContext();
		assertThat(ctx).isNotNull();
	}

	//TODO Fix @Test
	public void whenTwoThreadsConnectToLDAPAtSameTimeThenOK()
			throws Exception {
		LDAPConnectionTask task1 = new LDAPConnectionTask();
		LDAPConnectionTask task2 = new LDAPConnectionTask();
		Timer timer = new Timer();
		Date nowPlus1Second = LocalDateTime.now().plusSeconds(1).toDate();
		timer.schedule(task1, nowPlus1Second);
		timer.schedule(task2, nowPlus1Second);
		Thread.sleep(1000 * 2);
		assertThat(task1.getContext()).isNotNull();
		assertThat(task2.getContext()).isNotNull();
	}

	@Test
	@InternetTest
	public void givenAContextWhenGetAllUsersThenOk()
			throws Exception {
		LdapContext ctx = getValidContext();
		Set<String> usersIds = ((ConnectorLDAPServicesImpl) connectorLDAPServices)
				.getAllObjectsUsingFilter(ctx, null, "(objectClass=person)",
						"distinguishedName", "OU=Departement1,OU=doculibre,DC=test,DC=doculibre,DC=ca").getDocumentIds();
		assertThat(usersIds.size()).isEqualTo(3001);
	}

	@Test
	@InternetTest
	public void whenGetAllUsersThenOk()
			throws Exception {
		LdapContext ctx = getValidContext();
		String objectClass = "person";
		String objectCategory = "user";
		RegexFilter filter = null;

		Set<String> usersIds = connectorLDAPServices
				.getAllObjectsUsingFilter(ctx, objectClass, objectCategory, getContextWith3001Users(), filter).getDocumentIds();
		assertThat(usersIds.size()).isEqualTo(3001);
	}

	@Test
	@InternetTest
	public void unitTestForConnectorLDAPServicesImpl()
			throws Exception {
		try {
			computeSearchFilter("", "");
			fail("invalid parameters");
		} catch (InvalidSearchFilterRuntimeException e) {
			//OK
			String filter = computeSearchFilter("oCat", "oClaz");
			assertThat(filter).isEqualTo("(&(objectCategory=oCat)(objectClass=oClaz))");
			filter = computeSearchFilter("oCat", null);
			assertThat(filter).isEqualTo("(objectCategory=oCat)");
			filter = computeSearchFilter(null, "oClaz");
			assertThat(filter).isEqualTo("(objectClass=oClaz)");
		}
	}

	@Test
	@InternetTest
	public void whenGetUsersUsingFilterThenOnlyFilteredUsersAreReturned()
			throws Exception {
		LdapContext ctx = getValidContext();
		String objectClass = "user";
		String objectCategory = null;
		RegexFilter filter = new RegexFilter("administrator", null);
		Set<String> usersIds = connectorLDAPServices
				.getAllObjectsUsingFilter(ctx, objectClass, objectCategory, getContextWithTestUsers(), filter).getDocumentIds();
		assertThat(usersIds).containsOnly("CN=Administrator,CN=Users,DC=test,DC=doculibre,DC=ca");
	}

	@Test
	@InternetTest
	public void unitTest2ForConnectorLDAPServicesImpl()
			throws Exception {
		assertThat(getSimpleName("CN=admin,CN=users,DN=test")).isEqualTo("admin");
		assertThat(getSimpleName("CN=admin, laura,CN=users,DN=test")).isEqualTo("admin, laura");
	}

	//TODO see avec cola
	@Test
	@InternetTest
	public void whenGetObjectAttributesThenAllBasicADAttributesAreOk()
			throws Exception {

		LdapContext ctx = getValidContext();
		String id = "CN=username0,OU=Departement1,OU=doculibre,DC=test,DC=doculibre,DC=ca";
		LDAPObjectAttributes attributes = connectorLDAPServices.getObjectAttributes(ctx, id);
		List<Object> altSecurityIdentities = attributes.get("altSecurityIdentities").getValue();
		assertThat(altSecurityIdentities).containsOnly("secur2", "secur1");
		String commonName = attributes.get("cn").getStringValue();
		assertThat(commonName).isEqualTo("username0");
		String displayName = attributes.get("displayName").getStringValue();
		assertThat(displayName).isEqualTo("firstname0 f.n.0. lastname0");
		String givenName = attributes.get("givenName").getStringValue();
		assertThat(givenName).isEqualTo("firstname0");
		String l = attributes.get("l").getStringValue();
		assertThat(l).isEqualTo("city0");
		String legacyExchangeDN = attributes.get("legacyExchangeDN").getStringValue();
		assertThat(legacyExchangeDN).isEqualTo("lEDN0");
		String mail = attributes.get("mail").getStringValue();
		assertThat(mail).isEqualTo("username0@doculibre.com");
		assertThat(attributes.get("mSMQDigests")).isNull();
		String name = attributes.get("name").getStringValue();
		assertThat(name).isEqualTo("username0");
		String objectCategory = attributes.get("objectCategory").getStringValue();
		assertThat(objectCategory).isEqualTo("CN=Person,CN=Schema,CN=Configuration,DC=test,DC=doculibre,DC=ca");
		String primaryGroupID = attributes.get("primaryGroupID").getStringValue();
		assertThat(primaryGroupID).isEqualTo("513");
		String sAMAccountName = attributes.get("sAMAccountName").getStringValue();
		assertThat(sAMAccountName).isEqualTo("username0");
		String sAMAccountType = attributes.get("sAMAccountType").getStringValue();
		assertThat(sAMAccountType).isEqualTo("805306368");
		Object sIDHistory = attributes.get("sIDHistory");
		assertThat(sIDHistory).isNull();
		assertThat(attributes.get("servicePrincipalName")).isNull();
		String surname = attributes.get("sn").getStringValue();
		assertThat(surname).isEqualTo("lastname0");
		String userAccountControl = attributes.get("userAccountControl").getStringValue();
		assertThat(userAccountControl).isEqualTo("514");
		String userPrincipalName = attributes.get("userPrincipalName").getStringValue();
		assertThat(userPrincipalName).isEqualTo("principalname0");
		//TODO fixer

		String objectGuid = attributes.get("objectGUID").getStringValue();
		//assertThat(objectGuid).isEqualTo(("4F 92 A8 C7 AA 66 E6 4A AD D4 05 D8 A4 1A 38 85"));
		byte[] objectSid = attributes.get("objectSid").getByteValue();
		//assertThat(objectSid).isEqualTo(("1 5 0 0 0 0 0 5 21 0 0 0 51 DD 04 D0 63 52 CE 14 3A 1A 26 7B 80 04 00 00"));

		//TODO a tester servicePrincipalName, sIDHistory
		//TODO trouver
		/*String uNCName = attributes.get("uNC-Name").getStringValue();
		assertThat(uNCName).isEqualTo("");
		List<String> keywords = (List<String>) attributes.get("keywords").getValue();
		assertThat(keywords).containsOnly("");
		String groupType = attributes.get("group-Type").getStringValue();
		assertThat(groupType).isEqualTo("");
		String lDAPDisplayName = attributes.get("lDAP-Display-Name").getStringValue();
		assertThat(lDAPDisplayName).isEqualTo("");
		String location = attributes.get("location").getStringValue();
		assertThat(location).isEqualTo("");
		String mSMQLabel = attributes.get("mSMQ-Label").getStringValue();
		assertThat(mSMQLabel).isEqualTo("");
		String mSMQOwnerID = attributes.get("mSMQ-Owner-ID").getStringValue();
		assertThat(mSMQOwnerID).isEqualTo("");
		String mSMQQueueType = attributes.get("mSMQ-Queue-Type").getStringValue();
		assertThat(mSMQQueueType).isEqualTo("");
		String mSSQLAlias = attributes.get("mS-SQL-Alias").getStringValue();
		assertThat(mSSQLAlias).isEqualTo("");
		String mSSQLDatabase = attributes.get("mS-SQL-Database").getStringValue();
		assertThat(mSSQLDatabase).isEqualTo("");
		String mSSQLName = attributes.get("mS-SQL-Name").getStringValue();
		assertThat(mSSQLName).isEqualTo("");
		String mSSQLVersion = attributes.get("mS-SQL-Version").getStringValue();
		assertThat(mSSQLVersion).isEqualTo("");
		String netbootGUID = attributes.get("netboot-GUID").getStringValue();
		assertThat(netbootGUID).isEqualTo("");
		String organizationalUnitName = attributes.get("organizational-Unit-Name").getStringValue();
		assertThat(organizationalUnitName).isEqualTo("");*/

	}

	private class LDAPConnectionTask extends TimerTask {
		LdapContext ctx;

		@Override
		public void run() {
			ctx = getValidContext();
		}

		public LdapContext getContext() {
			return ctx;
		}
	}

	private Set<String> getContextWith3001Users() {
		return new HashSet<>(Arrays.asList("OU=Departement1,OU=doculibre,DC=test,DC=doculibre,DC=ca"));
	}

	private Set<String> getContextWithTestUsers() {
		return new HashSet<>(Arrays.asList("CN=Users,DC=test,DC=doculibre,DC=ca"));
	}
}
