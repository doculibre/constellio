package com.constellio.app.modules.es.connectors.http;

import com.constellio.app.modules.es.connectors.spi.ConnectorLogger;
import com.constellio.app.modules.es.connectors.spi.ConsoleConnectorLogger;
import com.constellio.app.modules.es.model.connectors.http.ConnectorHttpDocument;
import com.constellio.app.modules.es.model.connectors.http.ConnectorHttpInstance;
import com.constellio.app.modules.es.services.ConnectorManager;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.model.services.records.RecordServices;
import com.constellio.sdk.tests.ConstellioTest;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import static com.constellio.app.modules.es.connectors.caches.ConnectorDocumentURLCacheStatus.CURRENTLY_FETCHED;
import static com.constellio.app.modules.es.connectors.caches.ConnectorDocumentURLCacheStatus.FETCHED;
import static com.constellio.app.modules.es.connectors.caches.ConnectorDocumentURLCacheStatus.NOT_FETCHED;
import static org.assertj.core.api.Assertions.assertThat;

public class ConnectorHttpDocumentURLCacheAcceptanceTest extends ConstellioTest {
	ConnectorManager connectorManager;
	RecordServices recordServices;
	ESSchemasRecordsServices es;

	ConnectorHttpInstance connectorInstance;
	ConnectorLogger logger = new ConsoleConnectorLogger();

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(withZeCollection().withConstellioESModule().withAllTestUsers());

		es = new ESSchemasRecordsServices(zeCollection, getAppLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();
		connectorManager = es.getConnectorManager();

	}

	@Test
	public void givenMultipleJobsWhenLockingUrlForInsertionThenCannotBeAddedTwiceAndUnlockedOnSave()
			throws Exception {

		connectorInstance = connectorManager.createConnector(es.newConnectorHttpInstance().setCode("zeConnector")
				.setTitle("Ze connector").setEnabled(true).setSeeds("http://www.perdu.com"));

		ConnectorHttpDocumentURLCache cache = new ConnectorHttpDocumentURLCache(connectorInstance, getAppLayerFactory());
		cache.onConnectorStart();

		LocalDateTime currentTime = new LocalDateTime();
		givenTimeIs(currentTime);

		//Document does not exist
		assertThat(cache.exists("http://www.perdu.com")).isEqualTo(false);
		assertThat(cache.getEntry("http://www.perdu.com")).isNull();

		//Document is found, but we are not fetching it yet
		assertThat(cache.tryLockingDocumentForFetching("http://www.perdu.com")).isEqualTo(true);
		assertThat(cache.tryLockingDocumentForFetching("http://www.perdu.com")).isEqualTo(false);
		assertThat(cache.getEntry("http://www.perdu.com").getStatus()).isEqualTo(CURRENTLY_FETCHED);
		assertThat(cache.getEntry("http://www.perdu.com").getFetchingStartTime()).isEqualTo(currentTime);

		//Document is saved in "not fetched" state
		ConnectorHttpDocument doc = (es.newConnectorHttpDocument(connectorInstance).setFetched(false)
				.setUrl("http://www.perdu.com").setTraversalCode("zeTraversal"));
		recordServices.add(doc);
		assertThat(cache.getEntry("http://www.perdu.com").getStatus()).isEqualTo(NOT_FETCHED);
		assertThat(cache.getEntry("http://www.perdu.com").getFetchingStartTime()).isNull();

		//Relocking it, then saving it as fetched
		assertThat(cache.tryLockingDocumentForFetching("http://www.perdu.com")).isEqualTo(true);
		assertThat(cache.tryLockingDocumentForFetching("http://www.perdu.com")).isEqualTo(false);
		assertThat(cache.getEntry("http://www.perdu.com").getStatus()).isEqualTo(CURRENTLY_FETCHED);
		assertThat(cache.getEntry("http://www.perdu.com").getFetchingStartTime()).isEqualTo(currentTime);

		recordServices.add(doc.setFetched(true).setUrl("http://www.perdu.com").setTraversalCode("zeTraversal")
				.setDigest("digest1"));

		assertThat(cache.getEntry("http://www.perdu.com").getStatus()).isEqualTo(FETCHED);
		assertThat(cache.getEntry("http://www.perdu.com").getMetadata("digest")).isEqualTo("digest1");
		assertThat(cache.getEntry("http://www.perdu.com").getMetadata("copyOf")).isNull();
		assertThat(cache.getEntry("http://www.perdu.com").getFetchingStartTime()).isNull();

		//digest url map is only constructed when reading and inserting manually
		assertThat(cache.getDocumentUrlWithDigest("digest1")).isNull();

		cache.onConnectorStop();

		cache.onConnectorResume();
		cache.onConnectorGetJobsCalled();

		assertThat(cache.getEntry("http://www.perdu.com").getStatus()).isEqualTo(FETCHED);
		assertThat(cache.getEntry("http://www.perdu.com").getMetadata("digest")).isEqualTo("digest1");
		assertThat(cache.getEntry("http://www.perdu.com").getMetadata("copyOf")).isNull();
		assertThat(cache.getEntry("http://www.perdu.com").getFetchingStartTime()).isNull();

		//digest url map is only constructed when reading and inserting manually
		assertThat(cache.getDocumentUrlWithDigest("digest1")).isEqualTo("http://www.perdu.com");

	}
}
