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
package com.constellio.app.modules.es.connectors.http;

import static com.constellio.app.modules.es.sdk.ESTestUtils.assertThatEventsObservedBy;
import static com.constellio.app.modules.es.sdk.TestConnectorEvent.addEvent;
import static com.constellio.app.modules.es.sdk.TestConnectorEvent.modifyEvent;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.where;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.joda.time.Duration.standardMinutes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.es.connectors.spi.ConnectorLogger;
import com.constellio.app.modules.es.connectors.spi.ConsoleConnectorLogger;
import com.constellio.app.modules.es.model.connectors.http.ConnectorHttpDocument;
import com.constellio.app.modules.es.model.connectors.http.ConnectorHttpInstance;
import com.constellio.app.modules.es.sdk.TestConnectorEventObserver;
import com.constellio.app.modules.es.services.ConnectorManager;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.app.modules.es.services.crawler.ConnectorCrawler;
import com.constellio.app.modules.es.services.crawler.DefaultConnectorEventObserver;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.structures.MapStringListStringStructure;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.MetadataSchemasManagerException.OptimistickLocking;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.SlowTest;

public class ConnectorHttpAcceptanceTest extends ConstellioTest {

	ConnectorManager connectorManager;
	RecordServices recordServices;
	ESSchemasRecordsServices es;

	ConnectorHttpInstance connectorInstance;
	ConnectorLogger logger = new ConsoleConnectorLogger();
	private String zeMimetypeCode = "zeMimetype";

	private TestConnectorEventObserver eventObserver;

	@Before
	public void setUp()
			throws Exception {

		givenCollection(zeCollection).withConstellioRMModule().withConstellioESModule().withAllTestUsers();
		//		prepareSystem(
		//				withZeCollection().withConstellioESModule().withAllTestUsers()
		//		);

		es = new ESSchemasRecordsServices(zeCollection, getAppLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();
		connectorManager = es.getConnectorManager();

		eventObserver = new TestConnectorEventObserver(es, new DefaultConnectorEventObserver(es, logger, "crawlerObserver"));

	}

	@After
	public void tearDown()
			throws Exception {
		eventObserver.close();

	}

	@Test
	public void givenConnectorWithSeedsWhenFetchingThenFetchPagesStartingFromSeeds()
			throws Exception {

		connectorInstance = connectorManager.createConnector(es.newConnectorHttpInstance().setCode("zeConnector").setEnabled(true)
				.setSeeds(asList("http://perdu.com")));

		addMetadataToDocumentSchema(connectorInstance.getDocumentsCustomSchemaCode());

		connectorInstance.setPropertiesMapping(createPropertiesMappingForZeMimetype());
		recordServices.update(connectorInstance.getWrappedRecord());

		ConnectorCrawler.runningJobsSequentially(es, eventObserver).crawlUntilRecordsFound(
				from(es.connectorHttpDocument.schemaType())
						.where(es.connectorHttpDocument.url()).isEqualTo("http://perdu.com")
						.andWhere(es.connectorHttpDocument.fetched()).isTrue());

		assertThatEventsObservedBy(eventObserver).comparingRecordsUsing(es.connectorHttpDocument.url(),
				es.connectorHttpDocument.title()).contains(
				addEvent(es.newConnectorHttpDocument(connectorInstance).setTitle(null).setURL("http://perdu.com")),
				modifyEvent(es.newConnectorHttpDocument(connectorInstance).setTitle("Vous Etes Perdu ?")
						.setURL("http://perdu.com"))
		);

		LogicalSearchQuery query = new LogicalSearchQuery(es.fromConnectorHttpDocumentWhereConnectorIs(connectorInstance)
				.andWhere(es.connectorHttpDocument.title()).isNotNull());

		List<ConnectorHttpDocument> connectorHttpDocuments = es.searchConnectorHttpDocuments(query);
		assertThat(connectorHttpDocuments).hasSize(1);
		ConnectorHttpDocument connectorHttpDocument = connectorHttpDocuments.get(0);
		assertThat(connectorHttpDocument.getParsedContent())
				.contains("Perdu sur l'Internet")
				.doesNotContain("<h1>").doesNotContain("<H1>");
		assertThat(connectorHttpDocument.getList(zeMimetypeCode)).containsOnly("text/plain; charset=UTF-8");
	}

	@Test
	@SlowTest
	public void whenRunningUntilTheConnectorHasFinishedThenFetchAllPages()
			throws Exception {

		connectorInstance = connectorManager.createConnector(es.newConnectorHttpInstance().setCode("zeConnector").setEnabled(true)
				.setSeeds(asList("http://constellio.com")));

		ConnectorCrawler.runningJobsSequentially(es, eventObserver)
				.crawlUntilNoMoreRecordsFoundWithinTimeoutForSchemaTypes(standardMinutes(5), ConnectorHttpDocument.SCHEMA_TYPE);

		//assertThat(es.searchConnectorHttpDocuments(where(Schemas.TITLE).isNotNull()));
		assertThat(es.searchConnectorHttpDocuments(where(Schemas.TITLE).isNull())).isEmpty();
		assertThat(titlesOf(es.searchConnectorHttpDocuments(where(Schemas.TITLE).isNotNull())))
				.contains(
						"Constellio - plateforme EIM",
						"Comment contribuer - Constellio",
						"Appliance - Constellio",
						"Webinaire Constellio",
						"Recherche Entreprise Constellio"
				);
		assertThat(es.searchConnectorHttpDocuments(where(Schemas.TITLE).isNotNull()).size()).isGreaterThan(90);
	}

	private List<String> titlesOf(List<ConnectorHttpDocument> connectorHttpDocuments) {
		List<String> titles = new ArrayList<>();

		for (ConnectorHttpDocument document : connectorHttpDocuments) {
			titles.add(document.getTitle().trim().replace(" ", ""));
		}

		return titles;
	}

	private void addMetadataToDocumentSchema(String documentSchemaCode)
			throws OptimistickLocking {
		MetadataSchemasManager schemasManager = getModelLayerFactory().getMetadataSchemasManager();
		MetadataSchemaTypesBuilder schemaTypesBuilder = schemasManager.modify(zeCollection);
		MetadataSchemaBuilder schemaBuilder = schemaTypesBuilder.getSchemaType(ConnectorHttpDocument.SCHEMA_TYPE)
				.getSchema(documentSchemaCode);

		schemaBuilder.createUndeletable(zeMimetypeCode).setType(MetadataValueType.STRING).setMultivalue(true);

		schemasManager.saveUpdateSchemaTypes(schemaTypesBuilder);
	}

	private MapStringListStringStructure createPropertiesMappingForZeMimetype() {
		MapStringListStringStructure propertiesMapping = new MapStringListStringStructure();
		propertiesMapping.put(zeMimetypeCode, Arrays.asList(ConnectorHttp.FIELD_MIMETYPE));
		return propertiesMapping;
	}

	//
	//	@Test
	//	public void startConnectorWithInit() {
	//		Map<String, String[]> config = new HashMap<>();
	//		config.put(ConnectorHttp.SEEDS, new String[] { "http://constellio.com" });
	//
	//		ConnectorHttp connectorHttp = new ConnectorHttp(store, observer, logger, config);
	//		connectorHttp.init();
	//		List<ConnectorJob> jobs = connectorHttp.getJobs();
	//		assertThat(jobs).isNotEmpty();
	//	}
	//
	//	@Test
	//	public void givenConnectorWhenCrawlingThenCreateDocuments()
	//			throws Exception {
	//
	//		ConnectorInstance connectorInstance = es.newHTTPConnectorInstance().setSeeds(asList("http://constellio.com"));
	//		recordServices.add(connectorInstance);
	//
	//		Map<String, String[]> config = new HashMap<>();
	//		config.put(ConnectorHttp.SEEDS, new String[] { "http://constellio.com" });
	//		ConnectorHttp connectorHttp = new ConnectorHttp(store, observer, logger, config);
	//		connectorHttp.init();
	//
	//		List<ConnectorJob> jobs = connectorHttp.getJobs();
	//		connectorManager.crawl(jobs);
	//
	//		ArgumentCaptor<ConnectorDocument> argument = ArgumentCaptor.forClass(ConnectorDocument.class);
	//		verify(observer).addUpdateEvent(argument.capture());
	//		ConnectorDocument document = argument.getValue();
	//		System.out.println("New document added: " + document.getTitle());
	//		assertThat(document.getTitle()).contains("Constellio");
	//	}
	//
	//	@Test
	//	public void crawlOndemand()
	//			throws Exception {
	//		Map<String, String[]> config = new HashMap<>();
	//		config.put(ConnectorHttp.SEEDS, new String[] { "http://constellio.com" });
	//		config.put(ConnectorHttp.ON_DEMANDS, new String[] { "http://apache.org" });
	//		ConnectorHttp connectorHttp = new ConnectorHttp(store, observer, logger, config);
	//		connectorHttp.init();
	//
	//		List<ConnectorJob> jobs = connectorHttp.getJobs();
	//		connectorManager.crawl(jobs);
	//
	//		ArgumentCaptor<ConnectorDocument> argument = ArgumentCaptor.forClass(ConnectorDocument.class);
	//		verify(observer).addUpdateEvent(argument.capture());
	//		ConnectorDocument document = argument.getValue();
	//		System.out.println("New document added: " + document.getTitle());
	//		assertThat(document.getTitle()).contains("Constellio");
	//	}
	//
	//	@Test
	//	public void stopRestartConnector()
	//			throws Exception {
	//
	//	}

}
