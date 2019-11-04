package com.constellio.app.modules.es.connectors.ldap;

import com.constellio.app.modules.es.model.connectors.ConnectorDocument;
import com.constellio.app.modules.es.model.connectors.ldap.ConnectorLDAPInstance;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;

import java.util.Map.Entry;
import java.util.UUID;

import static com.constellio.app.modules.es.connectors.ldap.ConnectorLDAPDocumentType.USER;
import static com.constellio.app.modules.es.model.connectors.ldap.ConnectorLDAPUserDocument.DISTINGUISHED_NAME;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class ConnectorLDAPCrawlerHelperAcceptanceTest extends ConstellioTest {
	ConnectorLDAPCrawlerHelper crawlerHelper;
	RecordServices recordServices;
	ESSchemasRecordsServices es;

	ConnectorLDAPInstance connectorInstance, connectorInstance2;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(withZeCollection().withConstellioESModule().withAllTestUsers());

		es = new ESSchemasRecordsServices(zeCollection, getAppLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();

		connectorInstance = newConnectorLDAPInstance("code1");
		connectorInstance2 = newConnectorLDAPInstance("code2");
		crawlerHelper = new ConnectorLDAPCrawlerHelper(es);
	}

	private ConnectorLDAPInstance newConnectorLDAPInstance(String code)
			throws RecordServicesException {
		String newTraversalCode = UUID.randomUUID()
				.toString();
		ConnectorLDAPInstance returnInstance = (ConnectorLDAPInstance) es.newConnectorLDAPInstance().setPassword("pass")
				.setUrls(asList("url"))
				.setUsersBaseContextList(asList("BCL"))
				.setConnectionUsername(
						"username").setTitle("title").setCode(code).setTraversalCode(newTraversalCode);
		recordServices.add(returnInstance);
		return returnInstance;
	}

	@Test
	public void givenDocumentsWithSameDnOrUrlWhenGetOrCreateDocumentByDNAndUrlThenGetTheCorrectDocument()
			throws Exception {
		Transaction transaction = new Transaction();
		transaction
				.add(es.newConnectorLDAPUserDocumentWithId("id_1", connectorInstance).setURL("url").setDistinguishedName("dn1"));
		transaction
				.add(es.newConnectorLDAPUserDocumentWithId("id_2", connectorInstance).setURL("url1").setDistinguishedName("dn1"));
		transaction
				.add(es.newConnectorLDAPUserDocumentWithId("id_3", connectorInstance).setURL("url").setDistinguishedName("dn3"));
		recordServices.execute(transaction);
		ConnectorDocument document = crawlerHelper
				.getOrCreateDocumentByDNAndUrl("dn1", "url", connectorInstance);
		assertThat(document.getId()).isEqualTo("id_1");
	}

	@Test
	public void givenDocumentsWithSameDnAndUrlAndDifferentConnectorInstancesWhenGetOrCreateDocumentByDNAndUrlThenGetTheCorrectDocument()
			throws Exception {
		Transaction transaction = new Transaction();
		transaction
				.add(es.newConnectorLDAPUserDocumentWithId("id_1", connectorInstance).setURL("url").setDistinguishedName("dn"));
		transaction
				.add(es.newConnectorLDAPUserDocumentWithId("id_2", connectorInstance2).setURL("url").setDistinguishedName("dn"));
		recordServices.execute(transaction);
		ConnectorDocument document = crawlerHelper
				.getOrCreateDocumentByDNAndUrl("dn", "url", connectorInstance);
		assertThat(document.getId()).isEqualTo("id_1");
	}

	@Test
	public void whenWrapDocumentThenDocumentUrlAndDNAreSetCorrectly()
			throws Exception {
		Entry<String, LDAPObjectAttributes> entry = new TestEntry();
		ConnectorDocument document = crawlerHelper.wrapDocument(connectorInstance, entry, USER, "zUrl");
		assertThat(document.getURL()).isEqualTo("zUrl");
		assertThat(document.<String>get(DISTINGUISHED_NAME)).isEqualTo("DN");
	}

	private class TestEntry implements Entry<String, LDAPObjectAttributes> {
		@Override
		public String getKey() {
			return "DN";
		}

		@Override
		public LDAPObjectAttributes getValue() {
			LDAPObjectAttributes att = new LDAPObjectAttributes();
			att.addAttribute(DISTINGUISHED_NAME, new LDAPObjectAttribute().setValue("DN"));
			return att;
		}

		@Override
		public LDAPObjectAttributes setValue(LDAPObjectAttributes value) {
			//Not supported
			return null;
		}
	}
}
