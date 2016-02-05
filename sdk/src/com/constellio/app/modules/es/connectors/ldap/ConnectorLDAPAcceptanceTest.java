package com.constellio.app.modules.es.connectors.ldap;

import static com.constellio.app.modules.es.model.connectors.ConnectorDocument.URL;
import static com.constellio.app.modules.es.model.connectors.ldap.ConnectorLDAPUserDocument.DISTINGUISHED_NAME;
import static com.constellio.app.modules.es.model.connectors.ldap.ConnectorLDAPUserDocument.EMAIL;
import static com.constellio.app.modules.es.model.connectors.ldap.ConnectorLDAPUserDocument.WORK_TITLE;
import static com.constellio.app.modules.es.sdk.TestConnectorEvent.ADD_EVENT;
import static com.constellio.app.modules.es.sdk.TestConnectorEvent.DELETE_EVENT;
import static com.constellio.app.modules.es.sdk.TestConnectorEvent.MODIFY_EVENT;
import static com.constellio.model.entities.schemas.Schemas.IDENTIFIER;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.where;
import static com.constellio.sdk.tests.TestUtils.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.constellio.sdk.tests.annotations.SlowTest;
import org.assertj.core.api.Assertions;
import org.eclipse.jetty.server.Server;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.es.connectors.ldap.ConnectorLDAP.InvalidDocumentsBatchRuntimeException;
import com.constellio.app.modules.es.connectors.ldap.ConnectorLDAP.InvalidJobsBatchRuntimeException;
import com.constellio.app.modules.es.connectors.spi.Connector;
import com.constellio.app.modules.es.connectors.spi.ConnectorInstanciator;
import com.constellio.app.modules.es.connectors.spi.ConnectorLogger;
import com.constellio.app.modules.es.connectors.spi.ConsoleConnectorLogger;
import com.constellio.app.modules.es.model.connectors.ConnectorInstance;
import com.constellio.app.modules.es.model.connectors.ldap.ConnectorLDAPInstance;
import com.constellio.app.modules.es.model.connectors.ldap.ConnectorLDAPUserDocument;
import com.constellio.app.modules.es.sdk.TestConnectorEvent;
import com.constellio.app.modules.es.sdk.TestConnectorEventObserver;
import com.constellio.app.modules.es.services.ConnectorManager;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.app.modules.es.services.crawler.ConnectorCrawler;
import com.constellio.app.modules.es.services.crawler.DefaultConnectorEventObserver;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.query.logical.criteria.MeasuringUnitTime;
import com.constellio.sdk.tests.ConstellioTest;

public class ConnectorLDAPAcceptanceTest extends ConstellioTest {
	Server server;

	LocalDateTime TIME1 = new LocalDateTime();
	LocalDateTime ONE_WEEKS_AFTER_TIME1 = TIME1.plusDays(7);
	LocalDateTime TWO_WEEKS_AFTER_TIME1 = TIME1.plusDays(14);

	ConnectorManager connectorManager;
	RecordServices recordServices;
	ESSchemasRecordsServices es;

	ConnectorLDAPInstance connectorInstance;
	ConnectorLogger logger = new ConsoleConnectorLogger();
	private List<ConnectorLDAPUserDocument> connectorDocuments;

	private TestConnectorEventObserver eventObserver;
	ConnectorCrawler crawler;
	private TestLDAPServices ldapServices;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(withZeCollection().withConstellioESModule().withAllTestUsers());

		es = new ESSchemasRecordsServices(zeCollection, getAppLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();
		connectorManager = es.getConnectorManager();
		givenTimeIs(TIME1);
		ldapServices = new TestLDAPServices(TIME1.toLocalDate());

		connectorManager.setConnectorInstanciator(new ConnectorLDAPAcceptanceTestConnectorInstanciator());

		eventObserver = new TestConnectorEventObserver(es, new DefaultConnectorEventObserver(es, logger, "crawlerObserver"));
		connectorManager.setCrawler(ConnectorCrawler.runningJobsSequentially(es, eventObserver).withoutSleeps());

		connectorInstance = connectorManager.createConnector(newConnectorLDAPInstance("code"));
	}

	private ConnectorLDAPInstance newConnectorLDAPInstance(String code) {
		String newTraversalCode = UUID.randomUUID()
				.toString();
		return (ConnectorLDAPInstance) es.newConnectorLDAPInstance().setPassword("pass").setUrls(asList("url"))
				.setUsersBaseContextList(asList("url"))
				.setNumberOfJobsInParallel(2).setDocumentsPerJobs(1)
				.setConnectionUsername("username").setTitle("title").setCode(code).setTraversalCode(newTraversalCode);
	}

	@Test
	public void givenConnectorInstanceWithMaxJobs2AndMaxDocumentPerJob1WhenGetJobsThenOnly2DocumentsReturned()
			throws Exception {
		connectorDocuments = tickAndGetAllDocuments();
		assertThat(connectorDocuments.size()).isEqualTo(2);
	}

	@Test
	public void whenFetchAllDocumentsThenAllLDAPDocumentsCreated()
			throws Exception {
		connectorDocuments = fullyFetchConnectorDocuments();
		assertThat(connectorDocuments.size()).isEqualTo(3);
		assertThat(connectorDocuments).extracting(DISTINGUISHED_NAME, WORK_TITLE, EMAIL).containsOnly(
				tuple("id1", "titleid1", "mailid1"),
				tuple("id2", "titleid2", "mailid2"),
				tuple("id3", "titleid3", "mailid3")
		);
	}

	@Test
		 public void whenServerNotAvailableAfterACompleteTraversalThenDoNotDeleteExistingDocuments()
			throws Exception {
		connectorDocuments = fullyFetchConnectorDocuments();
		assertThat(connectorDocuments.size()).isEqualTo(3);
		assertThat(connectorDocuments).extracting(DISTINGUISHED_NAME, WORK_TITLE, EMAIL).containsOnly(
				tuple("id1", "titleid1", "mailid1"),
				tuple("id2", "titleid2", "mailid2"),
				tuple("id3", "titleid3", "mailid3")
		);
		ldapServices.setThrowExceptionWhenCommunicatingWithLdap(true);
		connectorDocuments = tickAndGetAllDocuments();
		assertThat(eventObserver.newEvents()).isEmpty();

		connectorDocuments = tickAndGetAllDocuments();
		assertThat(eventObserver.newEvents()).isEmpty();
	}

	@Test
	public void whenErrorWhenSearchingRemoteIdsThenDoNotDeleteExistingDocuments()
			throws Exception {
		connectorDocuments = fullyFetchConnectorDocuments();
		assertThat(connectorDocuments.size()).isEqualTo(3);
		assertThat(connectorDocuments).extracting(DISTINGUISHED_NAME, WORK_TITLE, EMAIL).containsOnly(
				tuple("id1", "titleid1", "mailid1"),
				tuple("id2", "titleid2", "mailid2"),
				tuple("id3", "titleid3", "mailid3")
		);

		ldapServices.setErrorWhenFetchingRemoteIds(true);
		givenTimeIs(ONE_WEEKS_AFTER_TIME1);
		connectorDocuments = fullyFetchConnectorDocuments();
		assertThat(connectorDocuments.size()).isEqualTo(4);
		assertThat(connectorDocuments).extracting(DISTINGUISHED_NAME, WORK_TITLE, EMAIL).containsOnly(
				tuple("id1", "titleid1_", "mailid1_"),
				tuple("id2", "titleid2_", "mailid2_"),
				tuple("id3", "titleid3", "mailid3"),
				tuple("id4", "titleid4_", "mailid4_")
		);
	}

	@Test
	public void whenServerNotAvailableDuringTraversalThenDoNotDeleteExistingDocuments()
			throws Exception {
		// phase 1 : get documents 1 and 2
		connectorDocuments = tickAndGetAllDocuments();
		assertThat(eventObserver.newEvents()).extracting("eventType")
				.containsOnly(ADD_EVENT, ADD_EVENT);
		assertThat(connectorDocuments).extracting(DISTINGUISHED_NAME, WORK_TITLE, EMAIL).containsOnly(
				tuple("id1", "titleid1", "mailid1"),
				tuple("id2", "titleid2", "mailid2")
		);

		// phase 2: document 3 but exception then do not remove previous documents
		ldapServices.setThrowExceptionWhenCommunicatingWithLdap(true);
		connectorDocuments = tickAndGetAllDocuments();
		assertThat(eventObserver.newEvents()).isEmpty();
		assertThat(connectorDocuments).extracting(DISTINGUISHED_NAME, WORK_TITLE, EMAIL).containsOnly(
				tuple("id1", "titleid1", "mailid1"),
				tuple("id2", "titleid2", "mailid2")
		);

		// phase 2 bis: end of traversal
		connectorDocuments = tickAndGetAllDocuments();
		assertThat(eventObserver.newEvents()).isEmpty();

		// phase 3: server on refetch documents 1 and 2
		givenTimeIs(ONE_WEEKS_AFTER_TIME1);
		ldapServices.setThrowExceptionWhenCommunicatingWithLdap(false);

		connectorDocuments = tickAndGetAllDocuments();
		assertThat(eventObserver.newEvents()).extracting("eventType")
				.containsOnly(MODIFY_EVENT, MODIFY_EVENT);
		assertThat(connectorDocuments).extracting(DISTINGUISHED_NAME, WORK_TITLE, EMAIL).containsOnly(
				tuple("id1", "titleid1_", "mailid1_"),
				tuple("id2", "titleid2_", "mailid2_")
		);

		// phase 4: server on fetch document 4
		connectorDocuments = tickAndGetAllDocuments();
		assertThat(eventObserver.newEvents()).extracting("eventType")
				.containsOnly(ADD_EVENT);
		assertThat(connectorDocuments).extracting(DISTINGUISHED_NAME, WORK_TITLE, EMAIL).containsOnly(
				tuple("id1", "titleid1_", "mailid1_"),
				tuple("id2", "titleid2_", "mailid2_"),
				tuple("id4", "titleid4_", "mailid4_")
		);
	}

	@Test
	public void whenReFetchAllDocumentsThenAllLDAPDocumentsFetchedAgainAndNonExistingDocumentRemoved()
			throws Exception {
		connectorDocuments = fullyFetchConnectorDocuments();
		assertThat(connectorDocuments.size()).isEqualTo(3);
		givenTimeIs(TWO_WEEKS_AFTER_TIME1);
		connectorDocuments = fullyFetchConnectorDocuments();
		assertThat(connectorDocuments).extracting(DISTINGUISHED_NAME, WORK_TITLE, EMAIL).containsOnly(
				tuple("id1", "titleid1_", "mailid1_"),
				tuple("id2", "titleid2_", "mailid2_"),
				tuple("id4", "titleid4_", "mailid4_")
		);
	}

	@Test
	public void whenCallingConnectorSeveralTimesThenDocumentsAreNotFetchedSeveralTimesExceptAfterAnEmptyJob() {
		// phase 1 : get documents 1 and 2
		connectorDocuments = tickAndGetAllDocuments();
		assertThat(eventObserver.newEvents()).extracting("eventType")
				.containsOnly(ADD_EVENT, ADD_EVENT);
		assertThat(connectorDocuments).extracting(DISTINGUISHED_NAME, WORK_TITLE, EMAIL).containsOnly(
				tuple("id1", "titleid1", "mailid1"),
				tuple("id2", "titleid2", "mailid2")
		);

		// phase 2: document 3
		givenTimeIs(ONE_WEEKS_AFTER_TIME1);
		connectorDocuments = tickAndGetAllDocuments();
		assertThat(eventObserver.newEvents()).extracting("eventType")
				.containsOnly(ADD_EVENT);
		assertThat(connectorDocuments).extracting(DISTINGUISHED_NAME, WORK_TITLE, EMAIL).containsOnly(
				tuple("id1", "titleid1", "mailid1"),
				tuple("id2", "titleid2", "mailid2"),
				tuple("id3", "titleid3_", "mailid3_")
		);
		// phase 3: all Documents fetched return empty job
		givenTimeIs(TWO_WEEKS_AFTER_TIME1);
		connectorDocuments = tickAndGetAllDocuments();
		assertThat(eventObserver.newEvents()).isEmpty();
		assertThat(connectorDocuments).extracting(DISTINGUISHED_NAME, WORK_TITLE, EMAIL).containsOnly(
				tuple("id1", "titleid1", "mailid1"),
				tuple("id2", "titleid2", "mailid2"),
				tuple("id3", "titleid3_", "mailid3_")
		);

		// phase 4: refetch 1, 2
		connectorDocuments = tickAndGetAllDocuments();
		assertThat(eventObserver.newEvents()).extracting("eventType")
				.containsOnly(MODIFY_EVENT, MODIFY_EVENT);
		assertThat(connectorDocuments).extracting(DISTINGUISHED_NAME, WORK_TITLE, EMAIL).containsOnly(
				tuple("id1", "titleid1_", "mailid1_"),
				tuple("id2", "titleid2_", "mailid2_"),
				tuple("id3", "titleid3_", "mailid3_")
		);

		// phase 5: refetch 3
		connectorDocuments = tickAndGetAllDocuments();
		assertThat(eventObserver.newEvents()).extracting("eventType")
				.containsOnly(DELETE_EVENT, ADD_EVENT);
		assertThat(connectorDocuments).extracting(DISTINGUISHED_NAME, WORK_TITLE, EMAIL).containsOnly(
				tuple("id1", "titleid1_", "mailid1_"),
				tuple("id2", "titleid2_", "mailid2_"),
				tuple("id4", "titleid4_", "mailid4_")
		);

		// phase 6: end of traversal return empty jobs
		connectorDocuments = tickAndGetAllDocuments();
		assertThat(eventObserver.newEvents()).isEmpty();
		assertThat(connectorDocuments).extracting(DISTINGUISHED_NAME, WORK_TITLE, EMAIL).containsOnly(
				tuple("id1", "titleid1_", "mailid1_"),
				tuple("id2", "titleid2_", "mailid2_"),
				tuple("id4", "titleid4_", "mailid4_")
		);
	}

	private List<ConnectorLDAPUserDocument> connectorDocuments(LocalDateTime date) {
		getModelLayerFactory().newRecordServices().flush();
		return es.searchConnectorLDAPUserDocuments(
				where(IDENTIFIER).isNotNull().andWhere(es.connectorLdapUserDocument.fetchedDateTime()).isEqualTo(date));
	}

	@Test
	public void givenInvalidConnectorInstanceParametersWhenInitFetchThenRuntimeException()
			throws Exception {
		ConnectorLDAPInstance connectorInstanceWith0JobsPerBatch = newConnectorLDAPInstance("code1").setNumberOfJobsInParallel(0);
		ConnectorLDAP connectorLDAP = new ConnectorLDAP(new TestLDAPServices(TIME1.toLocalDate()));
		connectorLDAP.initialize(null, connectorInstanceWith0JobsPerBatch.getWrappedRecord(), eventObserver, es);
		try {
			connectorLDAP.initFetch();
			fail();
		} catch (InvalidJobsBatchRuntimeException e) {
			//OK
		}
		connectorInstanceWith0JobsPerBatch = connectorInstanceWith0JobsPerBatch.setDocumentsPerJobs(0)
				.setNumberOfJobsInParallel(2);
		connectorLDAP.initialize(null, connectorInstanceWith0JobsPerBatch.getWrappedRecord(), eventObserver, es);
		try {
			connectorLDAP.initFetch();
			fail();
		} catch (InvalidDocumentsBatchRuntimeException e) {
			//OK
		}
	}

	/*@Test
	public void givenConnectorIsStoppedThenResumeCorrectly()
			throws Exception {
		// *
		// * ----------------- Fetch phase 1 --------------
		// *
		connectorDocuments = tickAndGetAllDocuments();

		// *
		// * ----------------- Connector is disabled - nothing is fetched --------------
		// *
		recordServices.update(connectorInstance.setEnabled(false));
		connectorDocuments = tickAndGetAllDocuments();

		assertThat(eventObserver.newEvents()).extracting("eventType", "url").isEmpty();

		// *
		// * ----------------- Fetch phase 2 --------------
		// *
		recordServices.update(connectorInstance.setEnabled(true));
		connectorDocuments = tickAndGetAllDocuments();
	}*/

	private List<ConnectorLDAPUserDocument> tickAndGetAllDocuments() {
		connectorManager.getCrawler().crawlNTimes(1);
		return connectorDocuments();
	}

	private List<ConnectorLDAPUserDocument> connectorDocuments() {
		return es.searchConnectorLDAPUserDocuments(where(IDENTIFIER).isNotNull());
	}

	private List<ConnectorLDAPUserDocument> fullyFetchConnectorDocuments() {
		boolean newEvents = true;
		while (newEvents) {
			connectorDocuments = tickAndGetAllDocuments();
			newEvents = !eventObserver.newEvents().isEmpty();
			givenTimeIs(TimeProvider.getLocalDateTime().plusMinutes(1));
		}
		return connectorDocuments;
	}

	public class ConnectorLDAPAcceptanceTestConnectorInstanciator implements ConnectorInstanciator {

		@Override
		public Connector instanciate(ConnectorInstance connectorInstance) {
			return new ConnectorLDAP(ldapServices);
		}
	}
}
