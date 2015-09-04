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
package com.constellio.app.modules.es.model;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.es.connectors.spi.Connector;
import com.constellio.app.modules.es.connectors.spi.ConnectorJob;
import com.constellio.app.modules.es.connectors.spi.ConnectorLogger;
import com.constellio.app.modules.es.connectors.spi.ConsoleConnectorLogger;
import com.constellio.app.modules.es.model.connectors.ConnectorInstance;
import com.constellio.app.modules.es.model.connectors.ConnectorType;
import com.constellio.app.modules.es.model.connectors.http.ConnectorHttpInstance;
import com.constellio.app.modules.es.services.ConnectorManager;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.sdk.tests.ConstellioTest;

public class ConnectorRecordsAcceptanceTest extends ConstellioTest {

	LocalDateTime shishOClock = new LocalDateTime();

	String testConnectorTypeId = "testConnectorTypeId";
	ConnectorManager connectorManager;
	RecordServices recordServices;
	ESSchemasRecordsServices es;

	ConnectorHttpInstance connectorInstance, connectorInstance2;
	ConnectorLogger logger = new ConsoleConnectorLogger();

	@Before
	public void setUp()
			throws Exception {

		givenCollection(zeCollection).withConstellioESModule().withAllTestUsers();

		es = new ESSchemasRecordsServices(zeCollection, getAppLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();
		connectorManager = es.getConnectorManager();

	}

	@Test
	public void givenConnectorWithDocumentsThenOnlyDocumentsWithFetchedStatusAreInDocumentsCountAndLastDocuments()
			throws Exception {

		connectorInstance = connectorManager.createConnector(es.newConnectorHttpInstance().setCode("zeConnector")
				.setEnabled(false).setSeeds(asList("http://constellio.com")));

		connectorInstance2 = connectorManager.createConnector(es.newConnectorHttpInstance().setCode("zeConnector2")
				.setEnabled(false).setSeeds(asList("http://constellio.com")));

		Transaction transaction = new Transaction();

		transaction.add(connectorInstance.setTraversalCode("t2"));
		transaction.add(connectorInstance2.setTraversalCode("t2"));
		transaction.add(es.newConnectorHttpDocumentWithId("olderTraversalRecord", connectorInstance))
				.setURL("http://constellio.com/document1").setTitle("Titre1").setFetched(true)
				.setModifiedOn(shishOClock).setTraversalCode("t1");

		transaction.add(es.newConnectorHttpDocumentWithId("otherConnectorRecord", connectorInstance2))
				.setModifiedOn(shishOClock.plusSeconds(1)).setURL("http://constellio.com/document2").setTitle("Titre2")
				.setFetched(true).setTraversalCode("t2");

		transaction.add(es.newConnectorHttpDocumentWithId("record1", connectorInstance))
				.setModifiedOn(shishOClock.plusSeconds(2)).setURL("http://constellio.com/document1").setTitle("Titre1")
				.setFetched(true).setTraversalCode("t2");

		transaction.add(es.newConnectorHttpDocumentWithId("record2", connectorInstance))
				.setModifiedOn(shishOClock.plusSeconds(3)).setURL("http://constellio.com/document2").setTitle("Titre2")
				.setFetched(true).setTraversalCode("t2");

		transaction.add(es.newConnectorHttpDocumentWithId("record3", connectorInstance))
				.setModifiedOn(shishOClock.plusSeconds(4)).setURL("http://constellio.com/document3").setTitle("Titre3")
				.setFetched(false).setTraversalCode("t2");

		transaction.add(es.newConnectorHttpDocumentWithId("record4", connectorInstance))
				.setModifiedOn(shishOClock.plusSeconds(5)).setURL("http://constellio.com/document4").setTitle("Titre4")
				.setFetched(true).setTraversalCode("t2");

		transaction.add(es.newConnectorHttpDocumentWithId("record5", connectorInstance))
				.setModifiedOn(shishOClock.plusSeconds(6)).setURL("http://constellio.com/document5").setTitle("Titre5")
				.setFetched(false).setTraversalCode("t2");
		recordServices.execute(transaction);

		assertThat(connectorManager.getLastFetchedDocuments(connectorInstance.getId(), 1)).extracting("id")
				.isEqualTo(asList("record4"));

		assertThat(connectorManager.getLastFetchedDocuments(connectorInstance.getId(), 10)).extracting("id")
				.isEqualTo(asList("record4", "record2", "record1"));

		assertThat(connectorManager.getFetchedDocumentsCount(connectorInstance.getId())).isEqualTo(3);
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

	public static class ConnectorTest extends Connector {

		String code;

		AtomicInteger atomicInteger = new AtomicInteger();

		@Override
		public List<ConnectorJob> getJobs() {
			return new ArrayList<>();

		}

		@Override
		protected void initialize(Record instance) {
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

		}

		@Override
		public void resume() {
		}
	}
}
