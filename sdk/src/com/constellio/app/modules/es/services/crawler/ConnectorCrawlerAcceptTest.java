/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.modules.es.services.crawler;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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
				"connector2-execute-job2"));

		connectorManager.save(connector1.setEnabled(true));
		crawl();
		assertThat(connectorEvents).isEqualTo(asList(
				"connector1-initialize",
				"connector1-start",
				"connector1-getJobs",
				"connector1-execute-job1",
				"connector1-execute-job2",
				"connector2-getJobs",
				"connector2-execute-job3",
				"connector2-execute-job4"));

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
				"connector2-getJobs",
				"connector2-execute-job1",
				"connector2-execute-job2"));

		connectorManager.save(connector2.setEnabled(false));

		crawl();
		assertThat(connectorEvents).isEqualTo(asList(
				"connector1-getJobs",
				"connector1-execute-job3",
				"connector1-execute-job4"));

		connectorManager.save(connector1.setEnabled(false));

		crawl();
		assertThat(connectorEvents).isEmpty();

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
				"connector1-execute-job2"));

		crawl();
		assertThat(connectorEvents).isEqualTo(asList(
				"connector1-getJobs",
				"connector1-execute-job3",
				"connector1-execute-job4"));

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
				"connector2-getJobs",
				"connector2-execute-job1",
				"connector2-execute-job2"));

		crawl();
		assertThat(connectorEvents).isEqualTo(asList(
				"connector1-getJobs",
				"connector1-execute-job3",
				"connector1-execute-job4",
				"connector2-getJobs",
				"connector2-execute-job3",
				"connector2-execute-job4"));

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
				"connector2-getJobs",
				"connector2-execute-job1",
				"connector2-execute-job2"));

		connectorManager.restartConnectorTraversal(connector2.getId());

		crawl();
		assertThat(connectorEvents).isEqualTo(asList(
				"connector2-initialize",
				"connector2-start",
				"connector1-getJobs",
				"connector1-execute-job3",
				"connector1-execute-job4",
				"connector2-getJobs",
				"connector2-execute-job1",
				"connector2-execute-job2"));

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
		connectorInstance.setConnectorType(testConnectorTypeId);
		return connectorInstance;
	}

	@Before
	public void setUp()
			throws Exception {

		connectorEvents = new ArrayList<>();
		givenCollection(zeCollection).withConstellioESModule();
		es = new ESSchemasRecordsServices(zeCollection, getAppLayerFactory());
		recordServices = es.getModelLayerFactory().newRecordServices();
		connectorManager = es.getConnectorManager();
		connectorManager.setCrawlerInParallel(false);
		connectorCrawler = es.getConnectorManager().getCrawler();
		givenDummyConnectorType();
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
		public void resume() {
			connectorEvents.add(code + "-resume");
		}
	}
}
