package com.constellio.app.modules.es.connectors.ldap;

import static com.constellio.app.modules.es.connectors.ldap.ConnectorLDAPDocumentType.USER;
import static com.constellio.app.modules.es.model.connectors.ldap.ConnectorLDAPUserDocument.EMAIL;
import static com.constellio.app.modules.es.model.connectors.ldap.ConnectorLDAPUserDocument.WORK_TITLE;
import static com.constellio.model.entities.schemas.Schemas.IDENTIFIER;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.where;
import static com.constellio.sdk.tests.TestUtils.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.eclipse.jetty.server.Server;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.modules.es.connectors.ldap.ConnectorLDAP.InvalidDocumentsBatchRuntimeException;
import com.constellio.app.modules.es.connectors.ldap.ConnectorLDAP.InvalidJobsBatchRuntimeException;
import com.constellio.app.modules.es.model.connectors.ldap.ConnectorLDAPInstance;
import com.constellio.app.modules.es.model.connectors.ldap.ConnectorLDAPUserDocument;
import com.constellio.app.modules.es.sdk.TestConnectorEventObserver;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.structures.MapStringListStringStructure;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.sdk.tests.ConstellioTest;

public class ConnectorLDAPCrawlerJobAcceptanceTest extends ConstellioTest {
	Server server;

	LocalDateTime TIME1 = new LocalDateTime();
	LocalDateTime TWO_WEEKS_AFTER_TIME1 = TIME1.plusDays(14);

	RecordServices recordServices;
	ESSchemasRecordsServices es;

	ConnectorLDAPInstance connectorInstance;
	ConnectorLDAP connectorLDAP;
	ConnectorLDAPCrawlerJob crawler;
	private List<String> recordsIds;
	@Mock
	TestConnectorEventObserver eventObserver;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(withZeCollection().withConstellioESModule().withAllTestUsers());

		es = new ESSchemasRecordsServices(zeCollection, getAppLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();
		givenTimeIs(TIME1);

		connectorInstance = newConnectorLDAPInstance("code");
		Map<String, List<String>> values = new HashMap<>();
		values.put(ConnectorLDAPUserDocument.DEFAULT_SCHEMA + "_" + ConnectorLDAPUserDocument.FIRST_NAME, asList("firstName"));
		values.put(ConnectorLDAPUserDocument.DEFAULT_SCHEMA + "_" + EMAIL, asList("email"));
		MapStringListStringStructure propertiesMapping = new MapStringListStringStructure(values);
		recordServices.add(connectorInstance.setPropertiesMapping(propertiesMapping));
		connectorLDAP = new ConnectorLDAP(new TestLDAPServices(TIME1.toLocalDate()));
		connectorLDAP.initialize(null, connectorInstance.getWrappedRecord(), eventObserver, es);
		recordsIds = initTestRecords();
		crawler = new ConnectorLDAPCrawlerJob(connectorLDAP, connectorInstance, USER, "url", recordsIds);
	}

	private ConnectorLDAPInstance newConnectorLDAPInstance(String code) {
		String newTraversalCode = UUID.randomUUID()
				.toString();
		return (ConnectorLDAPInstance) es.newConnectorLDAPInstance().setPassword("pass").setUrls(asList("url"))
				.setUsersBaseContextList(asList("url"))
				.setConnectionUsername("username").setTitle("title").setCode(code).setTraversalCode(newTraversalCode);
	}

	private List<String> initTestRecords()
			throws RecordServicesException {
		Transaction transaction = new Transaction();
		transaction
				.add(es.newConnectorLDAPUserDocumentWithId("id1", connectorInstance).setURL("url").setDistinguishedName("id1"));
		transaction
				.add(es.newConnectorLDAPUserDocumentWithId("id2", connectorInstance).setURL("url").setDistinguishedName("id2"));
		transaction
				.add(es.newConnectorLDAPUserDocumentWithId("id3", connectorInstance).setURL("url").setDistinguishedName("id3"));
		recordServices.execute(transaction);
		return asList("id1", "id2", "id3");
	}




	private List<ConnectorLDAPUserDocument> getConnectorDocuments() {
		return es.searchConnectorLDAPUserDocuments(where(IDENTIFIER).isNotNull());
	}


}
