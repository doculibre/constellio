package com.constellio.app.modules.es.services;

import com.constellio.app.modules.es.connectors.http.utils.WebsitesUtils;
import com.constellio.app.modules.es.connectors.spi.ConnectorLogger;
import com.constellio.app.modules.es.connectors.spi.ConsoleConnectorLogger;
import com.constellio.app.modules.es.model.connectors.http.ConnectorHttpDocument;
import com.constellio.app.modules.es.model.connectors.http.ConnectorHttpInstance;
import com.constellio.app.modules.es.sdk.TestConnectorEventObserver;
import com.constellio.app.modules.es.services.crawler.ConnectorCrawler;
import com.constellio.app.modules.es.services.crawler.DefaultConnectorEventObserver;
import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.services.contents.ContentDao;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.SlowTest;
import org.eclipse.jetty.server.Server;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.constellio.model.entities.schemas.Schemas.IDENTIFIER;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.where;
import static org.assertj.core.api.Assertions.assertThat;

@SlowTest
public class ConnectorDeleteServiceAcceptanceTest extends ConstellioTest {
	private static String WEBSITE = "http://localhost:4242/";

	Server server;
	ConnectorManager connectorManager;
	RecordServices recordServices;
	ESSchemasRecordsServices es;

	ConnectorHttpInstance connectorInstance;
	ConnectorLogger logger = new ConsoleConnectorLogger();
	private String zeMimetypeCode = "zeMimetype";
	private List<ConnectorHttpDocument> connectorDocuments;
	private ConfigManager configManager;
	private ContentDao contentDao;

	private TestConnectorEventObserver eventObserver;

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(withZeCollection().withConstellioESModule().withAllTestUsers());

		es = new ESSchemasRecordsServices(zeCollection, getAppLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();
		connectorManager = es.getConnectorManager();
		eventObserver = new TestConnectorEventObserver(es, new DefaultConnectorEventObserver(es, logger, "crawlerObserver"));
		connectorManager.setCrawler(ConnectorCrawler.runningJobsSequentially(es, eventObserver).withoutSleeps());
		configManager = getDataLayerFactory().getConfigManager();
		this.contentDao = getDataLayerFactory().getContentsDao();
	}

	@Test
	public void whenIndexingAWebsiteAndDeletingConnectorDocumentsThenAllDocumentsDeleted()
			throws Exception {

		givenTestWebsiteInState1();
		givenDataSet1Connector();

		connectorDocuments = tickAndGetAllDocuments();
		assertThat(connectorDocuments).isNotEmpty();

		connectorManager.totallyDeleteConnectorRecordsSkippingValidation(getDataLayerFactory().newRecordDao(), connectorInstance);

		assertThat(connectorDocuments()).isEmpty();
	}

	@Test
	public void whenIndexingAWebsiteAndDeletingConnectorThenAllDocumentsSchemasAndConfigsDeleted()
			throws Exception {

		givenTestWebsiteInState1();
		givenDataSet1Connector();

		connectorDocuments = tickAndGetAllDocuments();
		assertThat(connectorDocuments).isNotEmpty();

		new ConnectorDeleteService(zeCollection, getAppLayerFactory()).deleteConnector(connectorInstance);

		assertThat(connectorDocuments()).isEmpty();
		MetadataSchemaTypes types = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection);
		assertThat(recordDoesNotExist(connectorInstance.getId())).isTrue();
	}

	private boolean recordDoesNotExist(String id) {
		return !getModelLayerFactory().newSearchServices()
				.hasResults(fromAllSchemasIn(zeCollection).where(Schemas.IDENTIFIER).isEqualTo(id));
	}

	private void givenTestWebsiteInState1() {
		if (server != null) {
			try {
				server.stop();
				server.join();
			} catch (Exception e) {
				throw new RuntimeException(e);

			}
		}
		server = WebsitesUtils.startWebsiteInState1();
	}

	private void givenDataSet1Connector() {
		connectorInstance = connectorManager.createConnector(es.newConnectorHttpInstance().setCode("zeConnector")
				.setTitle("Ze connector").setEnabled(true).setSeeds(WEBSITE + "index.html"));
	}

	private List<ConnectorHttpDocument> tickAndGetAllDocuments() {
		connectorManager.getCrawler().crawlNTimes(1);
		return connectorDocuments();
	}

	private List<ConnectorHttpDocument> connectorDocuments() {
		return es.searchConnectorHttpDocuments(where(IDENTIFIER).isNotNull());
	}

	@After
	public void tearDown()
			throws Exception {
		eventObserver.close();

		if (server != null) {
			stopWebsiteServer();
		}
	}

	private void stopWebsiteServer() {
		try {
			server.stop();
			server.join();
			server = null;
		} catch (Exception e) {

		}
	}
}
