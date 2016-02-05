package com.constellio.app.modules.es.services.crawler;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.joda.time.LocalDateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.es.connectors.spi.Connector;
import com.constellio.app.modules.es.connectors.spi.ConnectorJob;
import com.constellio.app.modules.es.model.connectors.ConnectorInstance;
import com.constellio.app.modules.es.model.connectors.ConnectorType;
import com.constellio.app.modules.es.services.ConnectorManager;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.sdk.tests.ConstellioTest;

public class ConnectorCrawlerAcceptTest extends ConstellioTest {

	private static boolean areConnectorsDoingJobs = true;

	private static List<String> connectorEvents;

	ConnectorInstance connector1, connector2;

	String testConnectorTypeId = "testConnectorTypeId";
	ConnectorManager connectorManager;
	RecordServices recordServices;
	ESSchemasRecordsServices es;
	ConnectorCrawler connectorCrawler;

	@Test
	public void whenCrawlingACollectionWithoutConnectorsThenNothingHappens()
			throws Exception {

		crawl();
		assertThat(connectorEvents).isEmpty();
	}

	@Test
	public void givenConnectorThatReturnsEmptyJobsThanItsTraversalDateSetCorrectly()
		throws Exception{
		LocalDateTime now = LocalDateTime.now();
		givenTimeIs(now);
		areConnectorsDoingJobs = false;
		ConnectorInstance<ConnectorInstance<?>> connectorInstance;
		connectorManager.createConnector(connectorInstance = newEnabledTestConnectorWithCode("connector1"));
		crawl();
		connectorInstance = es.getConnectorInstance(connectorInstance.getId());
		assertThat(connectorInstance.getLastTraversalOn()).isEqualTo(now);
	}

	@Test
	public void givenConnectorThatDoesNotReturnsEmptyJobsThanItsTraversalDateIsNull()
			throws Exception{
		LocalDateTime now = LocalDateTime.now();
		givenTimeIs(now);
		areConnectorsDoingJobs = true;
		ConnectorInstance<ConnectorInstance<?>> connectorInstance;
		connectorManager.createConnector(connectorInstance = newEnabledTestConnectorWithCode("connector1"));
		crawl();
		connectorInstance = es.getConnectorInstance(connectorInstance.getId());
		assertThat(connectorInstance.getLastTraversalOn()).isNull();
	}

	@Test
	public void whenAConnectorDoingNoJobsIsCreatedThenItIsStartedOnNextCrawl()
			throws Exception {

		areConnectorsDoingJobs = false;

		crawl();
		assertThat(connectorEvents).isEmpty();

		connectorManager.createConnector(newEnabledTestConnectorWithCode("connector1"));
		assertThat(connectorEvents).isEmpty();

		crawl();
		assertThat(connectorEvents).isEqualTo(asList(
				"connector1-initialize",
				"connector1-start"));

		crawl();
		assertThat(connectorEvents).isEmpty();

		connectorManager.createConnector(newEnabledTestConnectorWithCode("connector2"));
		crawl();
		assertThat(connectorEvents).isEqualTo(asList(
				"connector2-initialize",
				"connector2-start"));
	}

	@Test
	public void givenTwoDisabledConnectorsThenNothingHappensUntilItIsStarted()
			throws Exception {

		connector1 = connectorManager.createConnector(newDisabledTestConnectorWithCode("connector1"));
		connector2 = connectorManager.createConnector(newDisabledTestConnectorWithCode("connector2"));

		crawl();
		assertThat(connectorEvents).isEmpty();

		connectorManager.save(connector2.setEnabled(true));
		crawl();
		assertThat(connectorEvents).isEqualTo(asList(
				"connector2-initialize",
				"connector2-start",
				"connector2-getJobs",
				"connector2-execute-job1",
				"connector2-execute-job2",
				"connector2-afterJobs"));

		connectorManager.save(connector1.setEnabled(true));
		crawl();
		assertThat(connectorEvents).isEqualTo(asList(
				"connector1-initialize",
				"connector1-start",
				"connector1-getJobs",
				"connector1-execute-job1",
				"connector1-execute-job2",
				"connector1-afterJobs",
				"connector2-getJobs",
				"connector2-execute-job3",
				"connector2-execute-job4",
				"connector2-afterJobs"));

	}

	@Test
	public void givenTwoConnectorsWhenDisabledThenStopCrawling()
			throws Exception {

		areConnectorsDoingJobs = true;

		connector1 = connectorManager.createConnector(newEnabledTestConnectorWithCode("connector1"));
		connector2 = connectorManager.createConnector(newEnabledTestConnectorWithCode("connector2"));

		crawl();
		assertThat(connectorEvents).isEqualTo(asList(
				"connector1-initialize",
				"connector1-start",
				"connector2-initialize",
				"connector2-start",
				"connector1-getJobs",
				"connector1-execute-job1",
				"connector1-execute-job2",
				"connector1-afterJobs",
				"connector2-getJobs",
				"connector2-execute-job1",
				"connector2-execute-job2",
				"connector2-afterJobs"));

		connectorManager.save(connector2.setEnabled(false));

		crawl();
		assertThat(connectorEvents).isEqualTo(asList(
				"connector2-stop",
				"connector1-getJobs",
				"connector1-execute-job3",
				"connector1-execute-job4",
				"connector1-afterJobs"));

		connectorManager.save(connector1.setEnabled(false));

		crawl();
		assertThat(connectorEvents).isEqualTo(asList(
				"connector1-stop"));

	}

	@Test
	public void givenAConnectorAlreadyExistAndHasNoStartedTraversalWhenCrawlingForTheFirstTimeThenInitializeAndStartIt()
			throws Exception {
		areConnectorsDoingJobs = false;

		connector1 = connectorManager.createConnector(newEnabledTestConnectorWithCode("connector1"));
		crawl();
		assertThat(connectorEvents).isEqualTo(asList(
				"connector1-initialize",
				"connector1-start"));
		assertThat(es.getConnectorInstance(connector1.getId()).getTraversalCode()).isNotNull();

	}

	@Test
	public void givenOneConnectorWhenCrawlingThenReturnJobsAndExecuteThem()
			throws Exception {
		areConnectorsDoingJobs = true;

		connector1 = connectorManager.createConnector(newEnabledTestConnectorWithCode("connector1"));

		crawl();
		assertThat(connectorEvents).isEqualTo(asList(
				"connector1-initialize",
				"connector1-start",
				"connector1-getJobs",
				"connector1-execute-job1",
				"connector1-execute-job2",
				"connector1-afterJobs"));

		crawl();
		assertThat(connectorEvents).isEqualTo(asList(
				"connector1-getJobs",
				"connector1-execute-job3",
				"connector1-execute-job4",
				"connector1-afterJobs"));

	}

	@Test
	public void givenTwoConnectorsWhenCrawlingThenReturnJobsOfBothAndExecuteThem()
			throws Exception {
		areConnectorsDoingJobs = true;

		connector1 = connectorManager.createConnector(newEnabledTestConnectorWithCode("connector1"));
		connector2 = connectorManager.createConnector(newEnabledTestConnectorWithCode("connector2"));
		crawl();
		assertThat(connectorEvents).isEqualTo(asList(
				"connector1-initialize",
				"connector1-start",
				"connector2-initialize",
				"connector2-start",
				"connector1-getJobs",
				"connector1-execute-job1",
				"connector1-execute-job2",
				"connector1-afterJobs",
				"connector2-getJobs",
				"connector2-execute-job1",
				"connector2-execute-job2",
				"connector2-afterJobs"));

		crawl();
		assertThat(connectorEvents).isEqualTo(asList(
				"connector1-getJobs",
				"connector1-execute-job3",
				"connector1-execute-job4",
				"connector1-afterJobs",
				"connector2-getJobs",
				"connector2-execute-job3",
				"connector2-execute-job4",
				"connector2-afterJobs"));

	}

	@Test
	public void givenTwoConnectorsWhenRestartingCollectionOfOneThemRestartTraversalAndTheOtherContinue()
			throws Exception {
		areConnectorsDoingJobs = true;

		connector1 = connectorManager.createConnector(newEnabledTestConnectorWithCode("connector1"));
		connector2 = connectorManager.createConnector(newEnabledTestConnectorWithCode("connector2"));
		crawl();
		assertThat(connectorEvents).isEqualTo(asList(
				"connector1-initialize",
				"connector1-start",
				"connector2-initialize",
				"connector2-start",
				"connector1-getJobs",
				"connector1-execute-job1",
				"connector1-execute-job2",
				"connector1-afterJobs",
				"connector2-getJobs",
				"connector2-execute-job1",
				"connector2-execute-job2",
				"connector2-afterJobs"));

		connectorManager.restartConnectorTraversal(connector2.getId());

		crawl();
		assertThat(connectorEvents).isEqualTo(asList(
				"connector2-stop",
				"connector2-initialize",
				"connector2-start",
				"connector1-getJobs",
				"connector1-execute-job3",
				"connector1-execute-job4",
				"connector1-afterJobs",
				"connector2-getJobs",
				"connector2-execute-job1",
				"connector2-execute-job2",
				"connector2-afterJobs"));

	}

	@Test
	public void givenAConnectorAlreadyExistAndHasAStartedTraversalWhenCrawlingForTheFirstTimeThenInitializeAndResumeIt()
			throws Exception {
		areConnectorsDoingJobs = false;

		connector1 = connectorManager
				.createConnector(newEnabledTestConnectorWithCode("connector1").setTraversalCode("zeTraversal"));
		crawl();
		assertThat(connectorEvents).isEqualTo(asList(
				"connector1-initialize",
				"connector1-resume"));
		assertThat(es.getConnectorInstance(connector1.getId()).getTraversalCode()).isEqualTo("zeTraversal");
	}

	// ----------------------------------------------------------------------------------------------------------------

	private void crawl() {
		connectorEvents.clear();
		connectorCrawler.crawlAllConnectors();

	}

	private ConnectorInstance<ConnectorInstance<?>> newEnabledTestConnectorWithCode(String code) {
		MetadataSchemaTypes types = es.getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection);
		MetadataSchema schema = es.connectorInstance.schemaType().getSchema("test");

		ConnectorInstance connectorInstance = new ConnectorInstance(recordServices.newRecordWithSchema(schema), types);
		connectorInstance.setCode(code).setTitle(code);
		connectorInstance.setEnabled(true);
		connectorInstance.setConnectorType(testConnectorTypeId);
		return connectorInstance;
	}

	private ConnectorInstance<ConnectorInstance<?>> newDisabledTestConnectorWithCode(String code) {
		MetadataSchemaTypes types = es.getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection);
		MetadataSchema schema = es.connectorInstance.schemaType().getSchema("test");

		ConnectorInstance connectorInstance = new ConnectorInstance(recordServices.newRecordWithSchema(schema), types);
		connectorInstance.setCode(code).setTitle(code);
		connectorInstance.setEnabled(false);
		connectorInstance.setConnectorType(testConnectorTypeId);
		return connectorInstance;
	}

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(withZeCollection().withConstellioESModule());
		connectorEvents = new ArrayList<>();
		es = new ESSchemasRecordsServices(zeCollection, getAppLayerFactory());
		recordServices = es.getModelLayerFactory().newRecordServices();
		connectorManager = es.getConnectorManager();
		connectorManager.setCrawlerInParallel(false);
		connectorCrawler = es.getConnectorManager().getCrawler();
		connectorCrawler.timeWaitedWhenNoJobs = 0;
		givenDummyConnectorType();
		connectorEvents.clear();
	}

	private void givenDummyConnectorType()
			throws Exception {
		MetadataSchemasManager schemasManager = getModelLayerFactory().getMetadataSchemasManager();
		MetadataSchemaTypesBuilder typesBuilder = schemasManager.modify(zeCollection);
		typesBuilder.getSchemaType(ConnectorInstance.SCHEMA_TYPE).createCustomSchema("test");
		schemasManager.saveUpdateSchemaTypes(typesBuilder);

		ConnectorType type = es.newConnectorTypeWithId(testConnectorTypeId).setCode("test").setTitle("test")
				.setConnectorClassName(ConnectorTest.class.getName()).setLinkedSchema(ConnectorInstance.SCHEMA_TYPE + "_test");
		recordServices.add(type);
	}

	@After
	public void tearDown()
			throws Exception {
		connectorEvents = null;

	}

	public static class ConnectorTest extends Connector {

		String code;

		AtomicInteger atomicInteger = new AtomicInteger();

		@Override
		public List<ConnectorJob> getJobs() {
			if (!areConnectorsDoingJobs) {
				return new ArrayList<>();
			}

			ConnectorJob job1 = new ConnectorJob(this, "job" + atomicInteger.incrementAndGet()) {
				@Override
				public void execute(Connector connector) {
					connectorEvents.add(code + "-execute-" + jobName);
				}
			};
			ConnectorJob job2 = new ConnectorJob(this, "job" + atomicInteger.incrementAndGet()) {
				@Override
				public void execute(Connector connector) {
					connectorEvents.add(code + "-execute-" + jobName);
				}
			};
			connectorEvents.add(code + "-getJobs");
			return asList(job1, job2);
		}

		@Override
		protected void initialize(Record instance) {
			code = instance.get(Schemas.CODE);
			connectorEvents.add(code + "-initialize");
		}

		@Override
		public List<String> fetchTokens(String username) {
			return new ArrayList<>();
		}

		@Override
		public List<String> getConnectorDocumentTypes() {
			return new ArrayList<>();
		}

		@Override
		public void start() {
			connectorEvents.add(code + "-start");

		}

		@Override
		public void stop() {
			connectorEvents.add(code + "-stop");
		}

		@Override
		public void afterJobs(List<ConnectorJob> jobs) {
			connectorEvents.add(code + "-afterJobs");
		}

		@Override
		public void resume() {
			connectorEvents.add(code + "-resume");
		}

		@Override
		public List<String> getReportMetadatas(String reportMode) {
			return null;
		}

		@Override
		public String getMainConnectorDocumentType() {
			return null;
		}

		@Override
		public void onAllDocumentsDeleted() {

		}
	}
}
