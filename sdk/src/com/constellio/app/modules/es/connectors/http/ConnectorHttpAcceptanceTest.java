package com.constellio.app.modules.es.connectors.http;

import com.constellio.app.modules.es.connectors.http.utils.NtlmAuthenticationFilter;
import com.constellio.app.modules.es.connectors.http.utils.WebsitesUtils;
import com.constellio.app.modules.es.connectors.spi.ConnectorLogger;
import com.constellio.app.modules.es.connectors.spi.ConsoleConnectorLogger;
import com.constellio.app.modules.es.model.connectors.AuthenticationScheme;
import com.constellio.app.modules.es.model.connectors.http.ConnectorHttpDocument;
import com.constellio.app.modules.es.model.connectors.http.ConnectorHttpInstance;
import com.constellio.app.modules.es.model.connectors.structures.TraversalSchedule;
import com.constellio.app.modules.es.sdk.TestConnectorEventObserver;
import com.constellio.app.modules.es.services.ConnectorManager;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.app.modules.es.services.crawler.ConnectorCrawler;
import com.constellio.app.modules.es.services.crawler.DefaultConnectorEventObserver;
import com.constellio.app.modules.es.services.mapping.ConnectorField;
import com.constellio.app.modules.es.services.mapping.ConnectorMappingService;
import com.constellio.app.modules.es.services.mapping.TargetParams;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.services.records.RecordServices;
import com.constellio.sdk.tests.CommitCounter;
import com.constellio.sdk.tests.ConstellioTest;
import org.eclipse.jetty.server.Server;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static com.constellio.app.modules.es.sdk.TestConnectorEvent.ADD_EVENT;
import static com.constellio.app.modules.es.sdk.TestConnectorEvent.MODIFY_EVENT;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static com.constellio.model.entities.schemas.Schemas.IDENTIFIER;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.where;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.entry;
import static org.assertj.core.groups.Tuple.tuple;

public class ConnectorHttpAcceptanceTest extends ConstellioTest {
	Server server;

	String htmlMimetype = "text/html";
	String txtMimetype = "text/plain";
	String pdfMimetype = "application/pdf";

	LocalDateTime TIME1 = new LocalDateTime();
	LocalDateTime ONE_MINUTE_AFTER_TIME1 = TIME1.plusMinutes(1);
	LocalDateTime TWO_MINUTES_AFTER_TIME1 = TIME1.plusMinutes(2);
	LocalDateTime TWO_WEEKS_AFTER_TIME1 = TIME1.plusDays(14);
	LocalDateTime FOUR_WEEKS_AFTER_TIME1 = TIME1.plusDays(28);

	private static String WEBSITE = "http://localhost:4242/";

	ConnectorManager connectorManager;
	RecordServices recordServices;
	ESSchemasRecordsServices es;

	ConnectorHttpInstance connectorInstance;
	ConnectorLogger logger = new ConsoleConnectorLogger();
	private String zeMimetypeCode = "zeMimetype";
	private List<ConnectorHttpDocument> connectorDocuments;

	private TestConnectorEventObserver eventObserver;

	CommitCounter commitCounter;

	AtomicInteger counter;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(withZeCollection().withConstellioESModule().withAllTestUsers());

		es = new ESSchemasRecordsServices(zeCollection, getAppLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();
		connectorManager = es.getConnectorManager();
		eventObserver = new TestConnectorEventObserver(es, new DefaultConnectorEventObserver(es, logger, "crawlerObserver"));
		connectorManager.setCrawler(ConnectorCrawler.runningJobsSequentially(es, eventObserver).withoutSleeps());
		givenTimeIs(TIME1);
		commitCounter = new CommitCounter(getDataLayerFactory());
		counter = new AtomicInteger();
	}

	@Test
	public void whenModifyingSeedsAndInclusionsDuringExecutionThenApplied()
			throws Exception {

		givenTestWebsiteInState1();
		connectorInstance = connectorManager.createConnector(es.newConnectorHttpInstance().setCode("zeConnector")
				.setTitle("Ze connector").setEnabled(true).setSeeds("http://www.perdu.com"));

		connectorDocuments = tickAndGetAllDocuments();
		assertThat(connectorDocuments).extracting("URL", "fetched", "searchable", "level").containsOnly(
				tuple("http://www.perdu.com", true, true, 0)
		);

		recordServices.update(connectorInstance.setSeeds(WEBSITE + "index.html").setIncludePatterns(WEBSITE));

		connectorDocuments = tickAndGetAllDocuments();
		assertThat(connectorDocuments).extracting("URL", "fetched", "searchable", "level").containsOnly(
				tuple("http://www.perdu.com", true, true, 0),
				tuple(WEBSITE + "index.html", true, true, 0),
				tuple(WEBSITE + "singes.html", false, false, 1),
				tuple(WEBSITE + "girafe.html", false, false, 1),
				tuple(WEBSITE + "elephant.html", false, false, 1)
		);
	}

	@Test
	public void whenIndexingAPdfThenSaveFetchedDocument()
			throws Exception {

		givenTestWebsiteInState1();
		givenDataSet1Connector();

		// *
		// * ----------------- Fetch phase 1 --------------
		// *
		connectorDocuments = tickAndGetAllDocuments();
		assertThat(connectorDocuments).extracting("URL", "fetched", "fetchedDateTime", "searchable", "level").containsOnly(
				tuple(WEBSITE + "index.html", true, TIME1, true, 0),
				tuple(WEBSITE + "singes.html", false, null, false, 1),
				tuple(WEBSITE + "girafe.html", false, null, false, 1),
				tuple(WEBSITE + "elephant.html", false, null, false, 1)
		);
		assertThat(eventObserver.newEvents()).extracting("eventType", "url").containsOnly(
				tuple(ADD_EVENT, WEBSITE + "index.html"),
				tuple(MODIFY_EVENT, WEBSITE + "index.html"),
				tuple(ADD_EVENT, WEBSITE + "singes.html"),
				tuple(ADD_EVENT, WEBSITE + "girafe.html"),
				tuple(ADD_EVENT, WEBSITE + "elephant.html")
		);

		// *
		// * ----------------- Fetch phase 2 --------------
		// *
		givenTimeIs(ONE_MINUTE_AFTER_TIME1);
		connectorDocuments = tickAndGetAllDocuments();
		assertThat(connectorDocuments).extracting("URL", "fetched", "fetchedDateTime", "searchable", "level").containsOnly(
				tuple(WEBSITE + "index.html", true, TIME1, true, 0),
				tuple(WEBSITE + "singes.html", true, ONE_MINUTE_AFTER_TIME1, true, 1),
				tuple(WEBSITE + "girafe.html", true, ONE_MINUTE_AFTER_TIME1, true, 1),
				tuple(WEBSITE + "elephant.html", true, ONE_MINUTE_AFTER_TIME1, true, 1),
				tuple(WEBSITE + "licornes.html", false, null, false, 2),
				tuple(WEBSITE + "singes/gorille.html", false, null, false, 2),
				tuple(WEBSITE + "singes/macaque.html", false, null, false, 2)
		);
		assertThat(eventObserver.newEvents()).extracting("eventType", "url").containsOnly(
				tuple(MODIFY_EVENT, WEBSITE + "singes.html"),
				tuple(MODIFY_EVENT, WEBSITE + "girafe.html"),
				tuple(MODIFY_EVENT, WEBSITE + "elephant.html"),
				tuple(ADD_EVENT, WEBSITE + "licornes.html"),
				tuple(ADD_EVENT, WEBSITE + "singes/gorille.html"),
				tuple(ADD_EVENT, WEBSITE + "singes/macaque.html")
		);

		// *
		// * ---------------- Fetch phase 3 ---------------
		// *
		givenTimeIs(TWO_MINUTES_AFTER_TIME1);
		connectorDocuments = tickAndGetAllDocuments();
		assertThat(connectorDocuments).extracting("URL", "fetched", "fetchedDateTime", "searchable", "errorsCount").containsOnly(
				tuple(WEBSITE + "index.html", true, TIME1, true, 0),
				tuple(WEBSITE + "singes.html", true, ONE_MINUTE_AFTER_TIME1, true, 0),
				tuple(WEBSITE + "girafe.html", true, ONE_MINUTE_AFTER_TIME1, true, 0),
				tuple(WEBSITE + "elephant.html", true, ONE_MINUTE_AFTER_TIME1, true, 0),
				tuple(WEBSITE + "licornes.html", true, TWO_MINUTES_AFTER_TIME1, false, 1),
				tuple(WEBSITE + "singes/gorille.html", true, TWO_MINUTES_AFTER_TIME1, true, 0),
				tuple(WEBSITE + "singes/macaque.html", true, TWO_MINUTES_AFTER_TIME1, true, 0)
		);

		ConnectorHttpDocument licornes = es.getConnectorHttpDocumentByUrl(WEBSITE + "licornes.html");
		assertThat(licornes.getErrorCode()).isEqualTo("404");
		assertThat(licornes.getErrorMessage()).isEqualTo("Not Found");
		assertThat(licornes.getErrorStackTrace()).isNull();

		assertThat(eventObserver.newEvents()).extracting("eventType", "url").containsOnly(
				tuple(MODIFY_EVENT, WEBSITE + "licornes.html"),
				tuple(MODIFY_EVENT, WEBSITE + "singes/gorille.html"),
				tuple(MODIFY_EVENT, WEBSITE + "singes/macaque.html")
		);

		// *
		// * ---------------- Nothing to fetch ---------------
		// *
		connectorDocuments = tickAndGetAllDocuments();
		assertThat(eventObserver.newEvents()).extracting("eventType", "url").isEmpty();

		// *
		// * ---------------- Refetching everything two weeks later ---------------
		// *
		givenTimeIs(TWO_WEEKS_AFTER_TIME1);
		connectorDocuments = tickAndGetAllDocuments();

		connectorDocuments = tickAndGetAllDocuments();
		assertThat(connectorDocuments).extracting("URL", "fetched", "fetchedDateTime", "searchable", "errorsCount").containsOnly(
				tuple(WEBSITE + "index.html", true, TWO_WEEKS_AFTER_TIME1, true, 0),
				tuple(WEBSITE + "singes.html", true, TWO_WEEKS_AFTER_TIME1, true, 0),
				tuple(WEBSITE + "girafe.html", true, TWO_WEEKS_AFTER_TIME1, true, 0),
				tuple(WEBSITE + "elephant.html", true, TWO_WEEKS_AFTER_TIME1, true, 0),
				tuple(WEBSITE + "licornes.html", true, TWO_WEEKS_AFTER_TIME1, false, 2),
				tuple(WEBSITE + "singes/gorille.html", true, TWO_WEEKS_AFTER_TIME1, true, 0),
				tuple(WEBSITE + "singes/macaque.html", true, TWO_WEEKS_AFTER_TIME1, true, 0)
		);

		assertThat(eventObserver.newEvents()).extracting("eventType", "url").containsOnly(
				tuple(MODIFY_EVENT, WEBSITE + "index.html"),
				tuple(MODIFY_EVENT, WEBSITE + "singes.html"),
				tuple(MODIFY_EVENT, WEBSITE + "girafe.html"),
				tuple(MODIFY_EVENT, WEBSITE + "elephant.html"),
				tuple(MODIFY_EVENT, WEBSITE + "licornes.html"),
				tuple(MODIFY_EVENT, WEBSITE + "singes/gorille.html"),
				tuple(MODIFY_EVENT, WEBSITE + "singes/macaque.html")
		);

	}

	@Test
	public void whenIndexingAWebsiteThenSaveFetchedAndUnfetchedDocuments()
			throws Exception {

		givenTestWebsiteInState1();
		givenDataSet1Connector();

		// *
		// * ----------------- Fetch phase 1 --------------
		// *
		connectorDocuments = tickAndGetAllDocuments();
		assertThat(connectorDocuments).extracting("URL", "fetched", "fetchedDateTime", "searchable", "level").containsOnly(
				tuple(WEBSITE + "index.html", true, TIME1, true, 0),
				tuple(WEBSITE + "singes.html", false, null, false, 1),
				tuple(WEBSITE + "girafe.html", false, null, false, 1),
				tuple(WEBSITE + "elephant.html", false, null, false, 1)
		);
		assertThat(eventObserver.newEvents()).extracting("eventType", "url").containsOnly(
				tuple(ADD_EVENT, WEBSITE + "index.html"),
				tuple(MODIFY_EVENT, WEBSITE + "index.html"),
				tuple(ADD_EVENT, WEBSITE + "singes.html"),
				tuple(ADD_EVENT, WEBSITE + "girafe.html"),
				tuple(ADD_EVENT, WEBSITE + "elephant.html")
		);

		// *
		// * ----------------- Fetch phase 2 --------------
		// *
		givenTimeIs(ONE_MINUTE_AFTER_TIME1);
		connectorDocuments = tickAndGetAllDocuments();
		assertThat(connectorDocuments).extracting("URL", "fetched", "fetchedDateTime", "searchable", "level").containsOnly(
				tuple(WEBSITE + "index.html", true, TIME1, true, 0),
				tuple(WEBSITE + "singes.html", true, ONE_MINUTE_AFTER_TIME1, true, 1),
				tuple(WEBSITE + "girafe.html", true, ONE_MINUTE_AFTER_TIME1, true, 1),
				tuple(WEBSITE + "elephant.html", true, ONE_MINUTE_AFTER_TIME1, true, 1),
				tuple(WEBSITE + "licornes.html", false, null, false, 2),
				tuple(WEBSITE + "singes/gorille.html", false, null, false, 2),
				tuple(WEBSITE + "singes/macaque.html", false, null, false, 2)
		);
		assertThat(eventObserver.newEvents()).extracting("eventType", "url").containsOnly(
				tuple(MODIFY_EVENT, WEBSITE + "singes.html"),
				tuple(MODIFY_EVENT, WEBSITE + "girafe.html"),
				tuple(MODIFY_EVENT, WEBSITE + "elephant.html"),
				tuple(ADD_EVENT, WEBSITE + "licornes.html"),
				tuple(ADD_EVENT, WEBSITE + "singes/gorille.html"),
				tuple(ADD_EVENT, WEBSITE + "singes/macaque.html")
		);

		// *
		// * ---------------- Fetch phase 3 ---------------
		// *
		givenTimeIs(TWO_MINUTES_AFTER_TIME1);
		connectorDocuments = tickAndGetAllDocuments();
		assertThat(connectorDocuments).extracting("URL", "fetched", "fetchedDateTime", "searchable", "errorsCount").containsOnly(
				tuple(WEBSITE + "index.html", true, TIME1, true, 0),
				tuple(WEBSITE + "singes.html", true, ONE_MINUTE_AFTER_TIME1, true, 0),
				tuple(WEBSITE + "girafe.html", true, ONE_MINUTE_AFTER_TIME1, true, 0),
				tuple(WEBSITE + "elephant.html", true, ONE_MINUTE_AFTER_TIME1, true, 0),
				tuple(WEBSITE + "licornes.html", true, TWO_MINUTES_AFTER_TIME1, false, 1),
				tuple(WEBSITE + "singes/gorille.html", true, TWO_MINUTES_AFTER_TIME1, true, 0),
				tuple(WEBSITE + "singes/macaque.html", true, TWO_MINUTES_AFTER_TIME1, true, 0)
		);

		ConnectorHttpDocument licornes = es.getConnectorHttpDocumentByUrl(WEBSITE + "licornes.html");
		assertThat(licornes.getErrorCode()).isEqualTo("404");
		assertThat(licornes.getErrorMessage()).isEqualTo("Not Found");
		assertThat(licornes.getErrorStackTrace()).isNull();

		assertThat(eventObserver.newEvents()).extracting("eventType", "url").containsOnly(
				tuple(MODIFY_EVENT, WEBSITE + "licornes.html"),
				tuple(MODIFY_EVENT, WEBSITE + "singes/gorille.html"),
				tuple(MODIFY_EVENT, WEBSITE + "singes/macaque.html")
		);

		// *
		// * ---------------- Nothing to fetch ---------------
		// *
		connectorDocuments = tickAndGetAllDocuments();
		assertThat(eventObserver.newEvents()).extracting("eventType", "url").isEmpty();

		// *
		// * ---------------- Refetching everything two weeks later ---------------
		// *
		givenTimeIs(TWO_WEEKS_AFTER_TIME1);
		connectorDocuments = tickAndGetAllDocuments();

		connectorDocuments = tickAndGetAllDocuments();
		assertThat(connectorDocuments).extracting("URL", "fetched", "fetchedDateTime", "searchable", "errorsCount").containsOnly(
				tuple(WEBSITE + "index.html", true, TWO_WEEKS_AFTER_TIME1, true, 0),
				tuple(WEBSITE + "singes.html", true, TWO_WEEKS_AFTER_TIME1, true, 0),
				tuple(WEBSITE + "girafe.html", true, TWO_WEEKS_AFTER_TIME1, true, 0),
				tuple(WEBSITE + "elephant.html", true, TWO_WEEKS_AFTER_TIME1, true, 0),
				tuple(WEBSITE + "licornes.html", true, TWO_WEEKS_AFTER_TIME1, false, 2),
				tuple(WEBSITE + "singes/gorille.html", true, TWO_WEEKS_AFTER_TIME1, true, 0),
				tuple(WEBSITE + "singes/macaque.html", true, TWO_WEEKS_AFTER_TIME1, true, 0)
		);

		assertThat(eventObserver.newEvents()).extracting("eventType", "url").containsOnly(
				tuple(MODIFY_EVENT, WEBSITE + "index.html"),
				tuple(MODIFY_EVENT, WEBSITE + "singes.html"),
				tuple(MODIFY_EVENT, WEBSITE + "girafe.html"),
				tuple(MODIFY_EVENT, WEBSITE + "elephant.html"),
				tuple(MODIFY_EVENT, WEBSITE + "licornes.html"),
				tuple(MODIFY_EVENT, WEBSITE + "singes/gorille.html"),
				tuple(MODIFY_EVENT, WEBSITE + "singes/macaque.html")
		);

	}

	@Test
	public void whenOnDemandWebsitesAreSpecifiedThenPriorizedAndRemovedFromOnDemandList()
			throws Exception {

		givenTestWebsiteInState1();
		givenDataSet1Connector();
		recordServices.update(connectorInstance.setOnDemands(
				WEBSITE + "singes/gorille.html\n" +
				WEBSITE + "girafe.html"));
		// *
		// * ----------------- Fetch phase 1 with two on demand url--------------
		// *
		connectorDocuments = tickAndGetAllDocuments();
		assertThat(connectorDocuments).extracting("URL", "fetched", "fetchedDateTime").containsOnly(
				tuple(WEBSITE + "index.html", true, TIME1),
				tuple(WEBSITE + "singes/gorille.html", true, TIME1),
				tuple(WEBSITE + "girafe.html", true, TIME1),
				tuple(WEBSITE + "singes.html", false, null),
				tuple(WEBSITE + "licornes.html", false, null),
				tuple(WEBSITE + "elephant.html", false, null),
				tuple(WEBSITE + "singes/macaque.html", false, null)
		);
		assertThat(eventObserver.newEvents()).extracting("eventType", "url").containsOnly(
				tuple(ADD_EVENT, WEBSITE + "index.html"),
				tuple(MODIFY_EVENT, WEBSITE + "index.html"),
				tuple(ADD_EVENT, WEBSITE + "singes/gorille.html"),
				tuple(ADD_EVENT, WEBSITE + "girafe.html"),
				tuple(ADD_EVENT, WEBSITE + "singes.html"),
				tuple(ADD_EVENT, WEBSITE + "licornes.html"),
				tuple(ADD_EVENT, WEBSITE + "elephant.html"),
				tuple(ADD_EVENT, WEBSITE + "singes/macaque.html")
		);
		recordServices.refresh(connectorInstance);
		assertThat(connectorInstance.getOnDemands()).isNull();

		// *
		// * ----------------- Fetch phase 2 with two on demand (an already fetched and a new one) --------------
		// *
		recordServices.update(connectorInstance.setOnDemands(
				WEBSITE + "singes/gorille.html\n" +
				WEBSITE + "yeti.html"));
		givenTimeIs(ONE_MINUTE_AFTER_TIME1);
		connectorDocuments = tickAndGetAllDocuments();
		assertThat(connectorDocuments).extracting("URL", "fetched", "fetchedDateTime").containsOnly(
				tuple(WEBSITE + "index.html", true, TIME1),
				tuple(WEBSITE + "singes.html", true, ONE_MINUTE_AFTER_TIME1),
				tuple(WEBSITE + "girafe.html", true, TIME1),
				tuple(WEBSITE + "elephant.html", true, ONE_MINUTE_AFTER_TIME1),
				tuple(WEBSITE + "yeti.html", true, ONE_MINUTE_AFTER_TIME1),
				tuple(WEBSITE + "licornes.html", true, ONE_MINUTE_AFTER_TIME1),
				tuple(WEBSITE + "singes/gorille.html", true, ONE_MINUTE_AFTER_TIME1),
				tuple(WEBSITE + "singes/macaque.html", true, ONE_MINUTE_AFTER_TIME1)
		);
		assertThat(eventObserver.newEvents()).extracting("eventType", "url").containsOnly(
				tuple(MODIFY_EVENT, WEBSITE + "singes.html"),
				tuple(MODIFY_EVENT, WEBSITE + "elephant.html"),
				tuple(MODIFY_EVENT, WEBSITE + "licornes.html"),
				tuple(MODIFY_EVENT, WEBSITE + "singes/macaque.html"),
				tuple(MODIFY_EVENT, WEBSITE + "singes/gorille.html"),
				tuple(ADD_EVENT, WEBSITE + "yeti.html")
		);
		recordServices.refresh(connectorInstance);
		assertThat(connectorInstance.getOnDemands()).isNull();

	}

	@Test
	public void givenConnectorIsStoppedThenResumeCorrectly()
			throws Exception {

		givenTestWebsiteInState1();
		givenDataSet1Connector();

		// *
		// * ----------------- Fetch phase 1 --------------
		// *
		connectorDocuments = tickAndGetAllDocuments();
		assertThat(connectorDocuments).extracting("URL", "fetched", "fetchedDateTime").containsOnly(
				tuple(WEBSITE + "index.html", true, TIME1),
				tuple(WEBSITE + "singes.html", false, null),
				tuple(WEBSITE + "girafe.html", false, null),
				tuple(WEBSITE + "elephant.html", false, null)
		);
		assertThat(eventObserver.newEvents()).extracting("eventType", "url").containsOnly(
				tuple(ADD_EVENT, WEBSITE + "index.html"),
				tuple(MODIFY_EVENT, WEBSITE + "index.html"),
				tuple(ADD_EVENT, WEBSITE + "singes.html"),
				tuple(ADD_EVENT, WEBSITE + "girafe.html"),
				tuple(ADD_EVENT, WEBSITE + "elephant.html")
		);

		// *
		// * ----------------- Connector is disabled - nothing is fetched --------------
		// *
		recordServices.update(connectorInstance.setEnabled(false));
		connectorDocuments = tickAndGetAllDocuments();

		assertThat(connectorDocuments).extracting("URL", "fetched", "fetchedDateTime").containsOnly(
				tuple(WEBSITE + "index.html", true, TIME1),
				tuple(WEBSITE + "singes.html", false, null),
				tuple(WEBSITE + "girafe.html", false, null),
				tuple(WEBSITE + "elephant.html", false, null)
		);

		assertThat(eventObserver.newEvents()).extracting("eventType", "url").isEmpty();

		// *
		// * ----------------- Fetch phase 2 --------------
		// *
		recordServices.update(connectorInstance.setEnabled(true));
		givenTimeIs(ONE_MINUTE_AFTER_TIME1);
		connectorDocuments = tickAndGetAllDocuments();
		assertThat(connectorDocuments).extracting("URL", "fetched", "fetchedDateTime").containsOnly(
				tuple(WEBSITE + "index.html", true, TIME1),
				tuple(WEBSITE + "singes.html", true, ONE_MINUTE_AFTER_TIME1),
				tuple(WEBSITE + "girafe.html", true, ONE_MINUTE_AFTER_TIME1),
				tuple(WEBSITE + "elephant.html", true, ONE_MINUTE_AFTER_TIME1),
				tuple(WEBSITE + "licornes.html", false, null),
				tuple(WEBSITE + "singes/gorille.html", false, null),
				tuple(WEBSITE + "singes/macaque.html", false, null)
		);
		assertThat(eventObserver.newEvents()).extracting("eventType", "url").containsOnly(
				tuple(MODIFY_EVENT, WEBSITE + "singes.html"),
				tuple(MODIFY_EVENT, WEBSITE + "girafe.html"),
				tuple(MODIFY_EVENT, WEBSITE + "elephant.html"),
				tuple(ADD_EVENT, WEBSITE + "licornes.html"),
				tuple(ADD_EVENT, WEBSITE + "singes/gorille.html"),
				tuple(ADD_EVENT, WEBSITE + "singes/macaque.html")
		);

		// *
		// * ---------------- Fetch phase 3 ---------------
		// *
		givenTimeIs(TWO_MINUTES_AFTER_TIME1);
		connectorDocuments = tickAndGetAllDocuments();
		assertThat(connectorDocuments).extracting("URL", "fetched", "fetchedDateTime").containsOnly(
				tuple(WEBSITE + "index.html", true, TIME1),
				tuple(WEBSITE + "singes.html", true, ONE_MINUTE_AFTER_TIME1),
				tuple(WEBSITE + "girafe.html", true, ONE_MINUTE_AFTER_TIME1),
				tuple(WEBSITE + "elephant.html", true, ONE_MINUTE_AFTER_TIME1),
				tuple(WEBSITE + "licornes.html", true, TWO_MINUTES_AFTER_TIME1),
				tuple(WEBSITE + "singes/gorille.html", true, TWO_MINUTES_AFTER_TIME1),
				tuple(WEBSITE + "singes/macaque.html", true, TWO_MINUTES_AFTER_TIME1)
		);

		ConnectorHttpDocument licornes = es.getConnectorHttpDocumentByUrl(WEBSITE + "licornes.html");
		assertThat(licornes.getErrorCode()).isEqualTo("404");
		assertThat(licornes.getErrorMessage()).isEqualTo("Not Found");
		assertThat(licornes.getErrorStackTrace()).isNull();

		assertThat(eventObserver.newEvents()).extracting("eventType", "url").containsOnly(
				tuple(MODIFY_EVENT, WEBSITE + "licornes.html"),
				tuple(MODIFY_EVENT, WEBSITE + "singes/gorille.html"),
				tuple(MODIFY_EVENT, WEBSITE + "singes/macaque.html")
		);
	}

	@Test
	public void givenWebSiteIsModifiedThenUpdatedCorrectly()
			throws Exception {

		givenTestWebsiteInState1();
		givenDataSet1Connector();

		fullyFetchWebsite();
		verifyWebsiteInVersion1IsCorrectlyFetched();

		// *
		// * ---------------- Refetching everything two weeks later ---------------
		// *

		givenTestWebsiteInState2();
		givenTimeIs(TWO_WEEKS_AFTER_TIME1);

		connectorDocuments = tickAndGetAllDocuments();
		assertThat(connectorDocuments).extracting("URL", "fetched", "fetchedDateTime").containsOnly(
				tuple(WEBSITE + "index.html", true, TWO_WEEKS_AFTER_TIME1),
				tuple(WEBSITE + "singes.html", true, TWO_WEEKS_AFTER_TIME1),
				tuple(WEBSITE + "singes-wiki.pdf", false, null),
				tuple(WEBSITE + "singes.txt", false, null),
				tuple(WEBSITE + "girafe.html", true, TWO_WEEKS_AFTER_TIME1),
				tuple(WEBSITE + "paresseux.html", false, null),
				tuple(WEBSITE + "elephant.html", true, TWO_WEEKS_AFTER_TIME1),
				tuple(WEBSITE + "licornes.html", true, TWO_WEEKS_AFTER_TIME1),
				tuple(WEBSITE + "singes/gorille.html", true, TWO_WEEKS_AFTER_TIME1),
				tuple(WEBSITE + "singes/macaque.html", true, TWO_WEEKS_AFTER_TIME1)
		);

		assertThat(eventObserver.newEvents()).extracting("eventType", "url").containsOnly(
				tuple(MODIFY_EVENT, WEBSITE + "index.html"),
				tuple(MODIFY_EVENT, WEBSITE + "singes.html"),
				tuple(ADD_EVENT, WEBSITE + "paresseux.html"),
				tuple(ADD_EVENT, WEBSITE + "singes-wiki.pdf"),
				tuple(ADD_EVENT, WEBSITE + "singes.txt"),
				tuple(MODIFY_EVENT, WEBSITE + "elephant.html"),
				tuple(MODIFY_EVENT, WEBSITE + "licornes.html"),
				tuple(MODIFY_EVENT, WEBSITE + "girafe.html"),
				tuple(MODIFY_EVENT, WEBSITE + "singes/gorille.html"),
				tuple(MODIFY_EVENT, WEBSITE + "singes/macaque.html")
		);

		connectorDocuments = tickAndGetAllDocuments();
		assertThat(connectorDocuments).extracting("URL", "fetched", "fetchedDateTime").containsOnly(
				tuple(WEBSITE + "index.html", true, TWO_WEEKS_AFTER_TIME1),
				tuple(WEBSITE + "singes.html", true, TWO_WEEKS_AFTER_TIME1),
				tuple(WEBSITE + "girafe.html", true, TWO_WEEKS_AFTER_TIME1),
				tuple(WEBSITE + "singes-wiki.pdf", true, TWO_WEEKS_AFTER_TIME1),
				tuple(WEBSITE + "singes.txt", true, TWO_WEEKS_AFTER_TIME1),
				tuple(WEBSITE + "paresseux.html", true, TWO_WEEKS_AFTER_TIME1),
				tuple(WEBSITE + "elephant.html", true, TWO_WEEKS_AFTER_TIME1),
				tuple(WEBSITE + "licornes.html", true, TWO_WEEKS_AFTER_TIME1),
				tuple(WEBSITE + "singes/gorille.html", true, TWO_WEEKS_AFTER_TIME1),
				tuple(WEBSITE + "singes/macaque.html", true, TWO_WEEKS_AFTER_TIME1)
		);

		assertThat(eventObserver.newEvents()).extracting("eventType", "url").containsOnly(
				tuple(MODIFY_EVENT, WEBSITE + "paresseux.html"),
				tuple(MODIFY_EVENT, WEBSITE + "singes-wiki.pdf"),
				tuple(MODIFY_EVENT, WEBSITE + "singes.txt")
		);

		assertThat(es.getConnectorHttpDocumentByUrl(WEBSITE + "licornes.html")).isNotNull();
		assertThat(es.getConnectorHttpDocumentByUrl(WEBSITE + "girafe.html")).isNotNull();
		assertThat(es.getConnectorHttpDocumentByUrl(WEBSITE + "singes.html").getParsedContent()).contains("sympathique");
		assertThat(es.getConnectorHttpDocumentByUrl(WEBSITE + "singes-wiki.pdf").getParsedContent()).contains("Simiiformes");
		assertThat(es.getConnectorHttpDocumentByUrl(WEBSITE + "singes.txt").getParsedContent()).contains("Linux");
	}

	//@Test
	public void whenCrawlingThenNoCommitsInSolr()
			throws Exception {

		givenTestWebsiteInState1();
		givenDataSet1Connector();
		commitCounter.reset();
		fullyFetchWebsite();
		assertThat(commitCounter.newCommitsCall()).hasSize(0);

		givenTestWebsiteInState2();
		givenTimeIs(TWO_WEEKS_AFTER_TIME1);
		fullyFetchWebsite();
		assertThat(commitCounter.newCommitsCall()).hasSize(0);
	}

	@Test
	public void givenWebSitePagesAreNotAccessibleThenErrorCodesButStillSearchable()

			throws Exception {

		givenTimeIs(TIME1);
		givenTestWebsiteInState2();
		givenDataSet1Connector();

		connectorDocuments = tickAndGetAllDocuments();
		assertThat(connectorDocuments).extracting("URL", "fetched", "searchable", "errorCode", "errorsCount").containsOnly(
				tuple(WEBSITE + "index.html", true, true, null, 0),
				tuple(WEBSITE + "singes.html", false, false, null, 0),
				tuple(WEBSITE + "paresseux.html", false, false, null, 0),
				tuple(WEBSITE + "elephant.html", false, false, null, 0)
		);

		connectorDocuments = tickAndGetAllDocuments();
		assertThat(connectorDocuments).extracting("URL", "fetched", "searchable", "errorCode", "errorsCount").containsOnly(
				tuple(WEBSITE + "index.html", true, true, null, 0),
				tuple(WEBSITE + "singes.html", true, true, null, 0),
				tuple(WEBSITE + "singes-wiki.pdf", false, false, null, 0),
				tuple(WEBSITE + "singes.txt", false, false, null, 0),
				tuple(WEBSITE + "paresseux.html", true, true, null, 0),
				tuple(WEBSITE + "licornes.html", false, false, null, 0),
				tuple(WEBSITE + "elephant.html", true, true, null, 0),
				tuple(WEBSITE + "singes/gorille.html", false, false, null, 0),
				tuple(WEBSITE + "singes/macaque.html", false, false, null, 0)
		);

		stopWebsiteServer();
		connectorDocuments = tickAndGetAllDocuments();
		assertThat(connectorDocuments).extracting("URL", "fetched", "searchable", "errorCode", "errorsCount").containsOnly(
				tuple(WEBSITE + "index.html", true, true, null, 0),
				tuple(WEBSITE + "singes.html", true, true, null, 0),
				tuple(WEBSITE + "singes-wiki.pdf", true, false, "io exception", 1),
				tuple(WEBSITE + "singes.txt", true, false, "io exception", 1),
				tuple(WEBSITE + "paresseux.html", true, true, null, 0),
				tuple(WEBSITE + "licornes.html", true, false, "io exception", 1),
				tuple(WEBSITE + "elephant.html", true, true, null, 0),
				tuple(WEBSITE + "singes/gorille.html", true, false, "io exception", 1),
				tuple(WEBSITE + "singes/macaque.html", true, false, "io exception", 1)
		);

		givenTimeIs(TWO_WEEKS_AFTER_TIME1);
		givenTestWebsiteInState2();

		connectorDocuments = fullyFetchWebsite();
		assertThat(connectorDocuments).extracting("URL", "fetched", "searchable", "errorCode", "errorsCount").containsOnly(
				tuple(WEBSITE + "index.html", true, true, null, 0),
				tuple(WEBSITE + "singes.html", true, true, null, 0),
				tuple(WEBSITE + "singes-wiki.pdf", true, true, null, 0),
				tuple(WEBSITE + "singes.txt", true, true, null, 0),
				tuple(WEBSITE + "paresseux.html", true, true, null, 0),
				tuple(WEBSITE + "licornes.html", true, false, "404", 1),
				tuple(WEBSITE + "elephant.html", true, true, null, 0),
				tuple(WEBSITE + "singes/gorille.html", true, true, null, 0),
				tuple(WEBSITE + "singes/macaque.html", true, true, null, 0)
		);

		stopWebsiteServer();
		givenTimeIs(FOUR_WEEKS_AFTER_TIME1);
		connectorDocuments = fullyFetchWebsite();

		assertThat(connectorDocuments).extracting("URL", "fetched", "searchable", "errorCode", "errorsCount").containsOnly(
				tuple(WEBSITE + "index.html", true, true, "io exception", 1),
				tuple(WEBSITE + "singes.html", true, true, "io exception", 1),
				tuple(WEBSITE + "singes-wiki.pdf", true, true, "io exception", 1),
				tuple(WEBSITE + "singes.txt", true, true, "io exception", 1),
				tuple(WEBSITE + "paresseux.html", true, true, "io exception", 1),
				tuple(WEBSITE + "licornes.html", true, false, "io exception", 1),
				tuple(WEBSITE + "elephant.html", true, true, "io exception", 1),
				tuple(WEBSITE + "singes/gorille.html", true, true, "io exception", 1),
				tuple(WEBSITE + "singes/macaque.html", true, true, "io exception", 1)
		);
	}

	private void verifyWebsiteInVersion1IsCorrectlyFetched() {

		assertThat(connectorDocuments).extracting("URL", "fetched").containsOnly(
				tuple(WEBSITE + "index.html", true),
				tuple(WEBSITE + "singes.html", true),
				tuple(WEBSITE + "girafe.html", true),
				tuple(WEBSITE + "elephant.html", true),
				tuple(WEBSITE + "licornes.html", true),
				tuple(WEBSITE + "singes/gorille.html", true),
				tuple(WEBSITE + "singes/macaque.html", true)
		);

		//		assertThat(connectorDocuments).extracting("URL", "fetched", "fetchedDateTime").containsOnly(
		//				tuple(WEBSITE + "index.html", true, TIME1),
		//				tuple(WEBSITE + "singes.html", true, ONE_MINUTE_AFTER_TIME1),
		//				tuple(WEBSITE + "girafe.html", true, ONE_MINUTE_AFTER_TIME1),
		//				tuple(WEBSITE + "elephant.html", true, ONE_MINUTE_AFTER_TIME1),
		//				tuple(WEBSITE + "licornes.html", true, TWO_MINUTES_AFTER_TIME1),
		//				tuple(WEBSITE + "singes/gorille.html", true, TWO_MINUTES_AFTER_TIME1),
		//				tuple(WEBSITE + "singes/macaque.html", true, TWO_MINUTES_AFTER_TIME1)
		//		);
		assertThat(es.getConnectorHttpDocumentByUrl(WEBSITE + "licornes.html")).isNotNull();
	}

	private void verifyWebsiteInVersion1IsNotCorrectlyFetched() {
		assertThat(connectorDocuments).extracting("URL", "fetched", "fetchedDateTime").containsOnly(
				tuple(WEBSITE + "index.html", true, TIME1)
		);
		assertThat(es.getConnectorHttpDocumentByUrl(WEBSITE + "licornes.html")).isNull();
	}

	private void verifyWebsiteInVersion5IsCorrectlyFetched() {

		assertThat(connectorDocuments).extracting("URL", "fetched").containsOnly(
				tuple(WEBSITE + "index.html", true),
				tuple(WEBSITE + "girafe.png", true),
				tuple(WEBSITE + "girafe_corrupt.png", true),
				tuple(WEBSITE + "empty.html", false)
		);
	}

	private List<ConnectorHttpDocument> fullyFetchWebsite() {
		// *
		// * ----------------- Fetch phase 1 --------------
		// *

		boolean newEvents = true;
		while (newEvents) {

			connectorDocuments = tickAndGetAllDocuments();
			newEvents = !eventObserver.newEvents().isEmpty();
			givenTimeIs(TimeProvider.getLocalDateTime().plusMinutes(1));
		}
		return connectorDocuments;
	}

	@Test
	public void whenIndexingAWebSiteWithDuplicatedPagesThenDoNot()
			throws Exception {

		givenTestWebsiteInState1();
		givenDataSet1Connector();

		// *
		// * ----------------- Fully fetching website in version 1 --------------
		// *

		fullyFetchWebsite();
		verifyWebsiteInVersion1IsCorrectlyFetched();

		ConnectorHttpDocumentURLCache cache = (ConnectorHttpDocumentURLCache)
				getModelLayerFactory().getCachesManager().getCollectionCache(zeCollection, connectorInstance.getId());

		assertThat(cache.getCache().keySet()).containsOnly(
				WEBSITE + "index.html",
				WEBSITE + "singes.html",
				WEBSITE + "girafe.html",
				WEBSITE + "elephant.html",
				WEBSITE + "licornes.html",
				WEBSITE + "singes/gorille.html",
				WEBSITE + "singes/macaque.html"
		);


		assertThat(cache.documentUrlsClassifiedByDigests).containsOnly(
				entry(INDEX_DIGEST, WEBSITE + "index.html"),
				entry(SINGES_DIGEST, WEBSITE + "singes.html"),
				entry(GIRAFE_DIGEST, WEBSITE + "girafe.html"),
				entry(ELEPHANT_DIGEST, WEBSITE + "elephant.html"),
				entry(SINGES_GORILLE_DIGEST, WEBSITE + "singes/gorille.html"),
				entry(SINGES_MACAQUE_DIGEST, WEBSITE + "singes/macaque.html")
		);

		// *
		// * ----------------- Fully fetching website in version 3 (with duplicates) --------------
		// *

		givenTimeIs(TWO_WEEKS_AFTER_TIME1);
		givenTestWebsiteInState3WithDuplucates();
		fullyFetchWebsite();

		assertThat(cache.getCache().keySet()).containsOnly(
				WEBSITE + "index.html",
				WEBSITE + "singes.html",
				WEBSITE + "singes.txt",
				WEBSITE + "singes-wiki.pdf",
				WEBSITE + "paresseux.html",
				WEBSITE + "girafe.html",
				WEBSITE + "elephant.html",
				WEBSITE + "licornes.html",
				WEBSITE + "singes/gorille.html",
				WEBSITE + "singes/dk.html",
				WEBSITE + "singes/macaque.html",
				WEBSITE + "copy/index.html",
				WEBSITE + "copy/singes.html",
				WEBSITE + "copy/singes.txt",
				WEBSITE + "copy/singes-wiki.pdf",
				WEBSITE + "copy/paresseux.html",
				WEBSITE + "copy/elephant.html",
				WEBSITE + "copy/licornes.html",
				WEBSITE + "copy/singes/gorille.html",
				WEBSITE + "copy/singes/dk.html",
				WEBSITE + "copy/singes/macaque.html"
		);

		//
		assertThat(cache.documentUrlsClassifiedByDigests).containsOnly(
				entry(INDEX_DIGEST_V3, WEBSITE + "index.html"),
				entry(INDEX_COPY_DIGEST_V3, WEBSITE + "copy/index.html"),
				entry(SINGES_DIGEST_V3, WEBSITE + "singes.html"),
				entry(PARESSEUX_DIGEST_V3, WEBSITE + "paresseux.html"),
				entry(ELEPHANT_DIGEST_V3, WEBSITE + "elephant.html"),
				entry(SINGES_GORILLE_AND_DK_DIGEST_V3, WEBSITE + "singes/gorille.html"),
				entry(SINGES_MACAQUE_DIGEST_V3, WEBSITE + "singes/macaque.html"),
				entry(SINGES_PDF_DIGEST_V3, WEBSITE + "singes-wiki.pdf"),
				entry(SINGES_TEXT_DIGEST_V3, WEBSITE + "singes.txt"),
				entry(GIRAFE_DIGEST, WEBSITE + "girafe.html")
		);

		ConnectorHttpDocument singe = es.getConnectorHttpDocumentByUrl(WEBSITE + "singes.html");
		assertThat(singe.isSearchable()).isTrue();
		assertThat(singe.getParsedContent()).contains("gorille");
		assertThat(singe.getCopyOf()).isNull();
		assertThat(singe.getDigest()).contains(SINGES_DIGEST_V3);
		//		assertThat(singe.getOutlinks()).containsOnly(
		//				WEBSITE + "elephant.html",
		//				WEBSITE + "paresseux.html",
		//				WEBSITE + "singes.txt",
		//				WEBSITE + "singes-wiki.pdf",
		//				WEBSITE + "singes/gorille.html",
		//				WEBSITE + "singes/macaque.html"
		//		);

		ConnectorHttpDocument singeCopy = es.getConnectorHttpDocumentByUrl(WEBSITE + "copy/singes.html");
		assertThat(singeCopy.isSearchable()).isFalse();
		assertThat(singeCopy.getParsedContent()).isNull();
		assertThat(singeCopy.getCopyOf()).isEqualTo(singe.getURL());
		assertThat(singeCopy.getDigest()).contains(SINGES_DIGEST_V3);
		//		assertThat(singeCopy.getOutlinks()).containsOnly(
		//				WEBSITE + "copy/elephant.html",
		//				WEBSITE + "copy/paresseux.html",
		//				WEBSITE + "copy/singes.txt",
		//				WEBSITE + "copy/singes-wiki.pdf",
		//				WEBSITE + "copy/singes/gorille.html",
		//				WEBSITE + "copy/singes/macaque.html"
		//		);

		ConnectorHttpDocument gorille = es.getConnectorHttpDocumentByUrl(WEBSITE + "singes/gorille.html");
		assertThat(gorille.isSearchable()).isTrue();
		assertThat(gorille.getParsedContent()).contains("gros");
		assertThat(gorille.getCopyOf()).isNull();
		assertThat(gorille.getDigest()).contains(SINGES_GORILLE_AND_DK_DIGEST_V3);
		//		assertThat(gorille.getOutlinks()).containsOnly(
		//				WEBSITE + "singes.html",
		//				WEBSITE + "singes/dk.html",
		//				WEBSITE + "singes/macaque.html"
		//		);

		ConnectorHttpDocument dk = es.getConnectorHttpDocumentByUrl(WEBSITE + "singes/dk.html");
		assertThat(dk.isSearchable()).isFalse();
		assertThat(dk.getParsedContent()).isNull();
		assertThat(dk.getCopyOf()).isEqualTo(gorille.getURL());
		assertThat(dk.getDigest()).contains(SINGES_GORILLE_AND_DK_DIGEST_V3);
		//		assertThat(dk.getOutlinks()).containsOnly(
		//				WEBSITE + "singes.html",
		//				WEBSITE + "singes/gorille.html",
		//				WEBSITE + "singes/macaque.html"
		//		);

		ConnectorHttpDocument singeTxt = es.getConnectorHttpDocumentByUrl(WEBSITE + "singes.txt");
		assertThat(singeTxt.isSearchable()).isTrue();
		assertThat(singeTxt.getParsedContent()).contains("consultant");
		assertThat(singeTxt.getCopyOf()).isNull();
		assertThat(singeTxt.getDigest()).contains(SINGES_TEXT_DIGEST_V3);
		assertThat(singeTxt.getOutlinks()).isEmpty();

		ConnectorHttpDocument singeTxtCopy = es.getConnectorHttpDocumentByUrl(WEBSITE + "copy/singes.txt");
		assertThat(singeTxtCopy.isSearchable()).isFalse();
		assertThat(singeTxtCopy.getParsedContent()).isNull();
		assertThat(singeTxtCopy.getCopyOf()).isEqualTo(singeTxt.getURL());
		assertThat(singeTxtCopy.getDigest()).contains(SINGES_TEXT_DIGEST_V3);
		assertThat(singeTxtCopy.getOutlinks()).isEmpty();

		ConnectorHttpDocument singePdf = es.getConnectorHttpDocumentByUrl(WEBSITE + "singes-wiki.pdf");
		assertThat(singePdf.isSearchable()).isTrue();
		assertThat(singePdf.getParsedContent()).contains("Wikimedia");
		assertThat(singePdf.getCopyOf()).isNull();
		assertThat(singePdf.getDigest()).contains(SINGES_PDF_DIGEST_V3);
		assertThat(singePdf.getOutlinks()).isEmpty();

		ConnectorHttpDocument singePdfCopy = es.getConnectorHttpDocumentByUrl(WEBSITE + "copy/singes-wiki.pdf");
		assertThat(singePdfCopy.isSearchable()).isFalse();
		assertThat(singePdfCopy.getParsedContent()).isNull();
		assertThat(singePdfCopy.getCopyOf()).isEqualTo(singePdf.getURL());
		assertThat(singePdfCopy.getDigest()).contains(SINGES_PDF_DIGEST_V3);
		assertThat(singePdfCopy.getOutlinks()).isEmpty();

		// *
		// * ----------------- Fully fetching website in version 4 (some duplicated pages were modified) --------------
		// *

		givenTimeIs(FOUR_WEEKS_AFTER_TIME1);
		givenTestWebsiteInState4WithDuplicatesModified();
		fullyFetchWebsite();

		assertThat(cache.getCache().keySet()).containsOnly(
				WEBSITE + "index.html",
				WEBSITE + "singes.html",
				WEBSITE + "singes.txt",
				WEBSITE + "singes-wiki.pdf",
				WEBSITE + "paresseux.html",
				WEBSITE + "girafe.html",
				WEBSITE + "elephant.html",
				WEBSITE + "licornes.html",
				WEBSITE + "singes/gorille.html",
				WEBSITE + "singes/dk.html",
				WEBSITE + "singes/macaque.html",
				WEBSITE + "copy/index.html",
				WEBSITE + "copy/singes.html",
				WEBSITE + "copy/singes.txt",
				WEBSITE + "copy/singes-wiki.pdf",
				WEBSITE + "copy/paresseux.html",
				WEBSITE + "copy/elephant.html",
				WEBSITE + "copy/licornes.html",
				WEBSITE + "copy/singes/gorille.html",
				WEBSITE + "copy/singes/dk.html",
				WEBSITE + "copy/singes/macaque.html"
		);

		assertThat(cache.documentUrlsClassifiedByDigests).containsOnly(
				entry(INDEX_DIGEST_V3, WEBSITE + "index.html"),
				entry(INDEX_COPY_DIGEST_V3, WEBSITE + "copy/index.html"),
				entry(SINGES_DIGEST_V3, WEBSITE + "singes.html"),
				entry(PARESSEUX_DIGEST_V3, WEBSITE + "paresseux.html"),
				entry(ELEPHANT_DIGEST_V3, WEBSITE + "elephant.html"),
				entry(SINGES_GORILLE_DIGEST_V4, WEBSITE + "singes/gorille.html"),
				entry(SINGES_DK_DIGEST_V4, WEBSITE + "singes/dk.html"),
				entry(SINGES_MACAQUE_DIGEST_V3, WEBSITE + "singes/macaque.html"),
				entry(SINGES_PDF_DIGEST_V3, WEBSITE + "singes-wiki.pdf"),
				entry(SINGES_TEXT_DIGEST_V4, WEBSITE + "singes.txt"),
				entry(SINGES_TEXT_COPY_DIGEST_V4, WEBSITE + "copy/singes.txt"),
				entry(GIRAFE_DIGEST, WEBSITE + "girafe.html")
		);

		singe = es.getConnectorHttpDocumentByUrl(WEBSITE + "singes.html");
		assertThat(singe.isSearchable()).isTrue();
		assertThat(singe.getParsedContent()).contains("gorille");
		assertThat(singe.getCopyOf()).isNull();
		assertThat(singe.getDigest()).contains(SINGES_DIGEST_V3);
		//		assertThat(singe.getOutlinks()).containsOnly(
		//				WEBSITE + "elephant.html",
		//				WEBSITE + "paresseux.html",
		//				WEBSITE + "singes.txt",
		//				WEBSITE + "singes-wiki.pdf",
		//				WEBSITE + "singes/gorille.html",
		//				WEBSITE + "singes/macaque.html"
		//		);

		singeCopy = es.getConnectorHttpDocumentByUrl(WEBSITE + "copy/singes.html");
		assertThat(singeCopy.isSearchable()).isFalse();
		assertThat(singeCopy.getParsedContent()).isNull();
		assertThat(singeCopy.getCopyOf()).isEqualTo(singe.getURL());
		assertThat(singeCopy.getDigest()).contains(SINGES_DIGEST_V3);
		//		assertThat(singeCopy.getOutlinks()).containsOnly(
		//				WEBSITE + "copy/elephant.html",
		//				WEBSITE + "copy/paresseux.html",
		//				WEBSITE + "copy/singes.txt",
		//				WEBSITE + "copy/singes-wiki.pdf",
		//				WEBSITE + "copy/singes/gorille.html",
		//				WEBSITE + "copy/singes/macaque.html"
		//		);

		gorille = es.getConnectorHttpDocumentByUrl(WEBSITE + "singes/gorille.html");
		assertThat(gorille.isSearchable()).isTrue();
		assertThat(gorille.getParsedContent()).contains("gros");
		assertThat(gorille.getCopyOf()).isNull();
		assertThat(gorille.getDigest()).contains(SINGES_GORILLE_DIGEST_V4);
		//		assertThat(gorille.getOutlinks()).containsOnly(
		//				WEBSITE + "singes.html",
		//				WEBSITE + "singes/dk.html",
		//				WEBSITE + "singes/macaque.html"
		//		);

		dk = es.getConnectorHttpDocumentByUrl(WEBSITE + "singes/dk.html");
		assertThat(dk.isSearchable()).isTrue();
		assertThat(dk.getParsedContent()).contains("gros").contains("DK");
		assertThat(dk.getCopyOf()).isNull();
		assertThat(dk.getDigest()).contains(SINGES_DK_DIGEST_V4);
		//		assertThat(dk.getOutlinks()).containsOnly(
		//				WEBSITE + "singes.html",
		//				WEBSITE + "singes/gorille.html",
		//				WEBSITE + "singes/macaque.html"
		//		);

		singeTxt = es.getConnectorHttpDocumentByUrl(WEBSITE + "singes.txt");
		assertThat(singeTxt.isSearchable()).isTrue();
		assertThat(singeTxt.getParsedContent()).contains("consultant").contains("$");
		assertThat(singeTxt.getCopyOf()).isNull();
		assertThat(singeTxt.getDigest()).contains(SINGES_TEXT_DIGEST_V4);
		assertThat(singeTxt.getOutlinks()).isEmpty();

		singeTxtCopy = es.getConnectorHttpDocumentByUrl(WEBSITE + "copy/singes.txt");
		assertThat(singeTxtCopy.isSearchable()).isTrue();
		assertThat(singeTxtCopy.getParsedContent()).contains("consultant");
		assertThat(singeTxtCopy.getCopyOf()).isNull();
		assertThat(singeTxtCopy.getDigest()).contains(SINGES_TEXT_COPY_DIGEST_V4);
		assertThat(singeTxtCopy.getOutlinks()).isEmpty();

		singePdf = es.getConnectorHttpDocumentByUrl(WEBSITE + "singes-wiki.pdf");
		assertThat(singePdf.isSearchable()).isTrue();
		assertThat(singePdf.getParsedContent()).contains("Wikimedia");
		assertThat(singePdf.getCopyOf()).isNull();
		assertThat(singePdf.getDigest()).contains(SINGES_PDF_DIGEST_V3);
		assertThat(singePdf.getOutlinks()).isEmpty();

		singePdfCopy = es.getConnectorHttpDocumentByUrl(WEBSITE + "copy/singes-wiki.pdf");
		assertThat(singePdfCopy.isSearchable()).isFalse();
		assertThat(singePdfCopy.getParsedContent()).isNull();
		assertThat(singePdfCopy.getCopyOf()).isEqualTo(singePdf.getURL());
		assertThat(singePdfCopy.getDigest()).contains(SINGES_PDF_DIGEST_V3);
		assertThat(singePdfCopy.getOutlinks()).isEmpty();

	}

	@Test
	public void givenADocumentHasErrorCode404ForAThirdTimeWhenFetchingThenDeleted()
			throws Exception {

		givenTimeIs(TIME1);
		givenDataSet1Connector();
		givenTestWebsiteInState1();
		fullyFetchWebsite();
		assertThat(es.getConnectorHttpDocumentByUrl(WEBSITE + "licornes.html").getErrorsCount()).isEqualTo(1);

		givenTimeIs(TIME1.plusDays(14));
		fullyFetchWebsite();
		assertThat(es.getConnectorHttpDocumentByUrl(WEBSITE + "licornes.html").getErrorsCount()).isEqualTo(2);

		givenTimeIs(TIME1.plusDays(28));
		fullyFetchWebsite();
		//DELETED!
		assertThat(es.getConnectorHttpDocumentByUrl(WEBSITE + "licornes.html")).isNull();
	}

	@Test
	public void whenIndexingAWebsiteWhenDocumentLevelExceedMaximumThenNotAdded()
			throws Exception {

		givenTestWebsiteInState1();
		givenDataSet1Connector();

		recordServices.update(connectorInstance.setMaxLevel(1));
		String cacheName = "ConnectorDocumentURLCache-" + zeCollection + "-" + connectorInstance.getId();

		// *
		// * ----------------- Fetch phase 1 --------------
		// *
		connectorDocuments = tickAndGetAllDocuments();
		assertThat(connectorDocuments).extracting("URL", "fetched", "fetchedDateTime", "searchable", "level").containsOnly(
				tuple(WEBSITE + "index.html", true, TIME1, true, 0),
				tuple(WEBSITE + "singes.html", false, null, false, 1),
				tuple(WEBSITE + "girafe.html", false, null, false, 1),
				tuple(WEBSITE + "elephant.html", false, null, false, 1)
		);
		assertThat(eventObserver.newEvents()).extracting("eventType", "url").containsOnly(
				tuple(ADD_EVENT, WEBSITE + "index.html"),
				tuple(MODIFY_EVENT, WEBSITE + "index.html"),
				tuple(ADD_EVENT, WEBSITE + "singes.html"),
				tuple(ADD_EVENT, WEBSITE + "girafe.html"),
				tuple(ADD_EVENT, WEBSITE + "elephant.html")
		);

		// *
		// * ----------------- Fetch phase 2 --------------
		// *
		givenTimeIs(ONE_MINUTE_AFTER_TIME1);
		connectorDocuments = tickAndGetAllDocuments();
		assertThat(connectorDocuments).extracting("URL", "fetched", "fetchedDateTime", "searchable", "level").containsOnly(
				tuple(WEBSITE + "index.html", true, TIME1, true, 0),
				tuple(WEBSITE + "singes.html", true, ONE_MINUTE_AFTER_TIME1, true, 1),
				tuple(WEBSITE + "girafe.html", true, ONE_MINUTE_AFTER_TIME1, true, 1),
				tuple(WEBSITE + "elephant.html", true, ONE_MINUTE_AFTER_TIME1, true, 1)
		);
		assertThat(eventObserver.newEvents()).extracting("eventType", "url").containsOnly(
				tuple(MODIFY_EVENT, WEBSITE + "singes.html"),
				tuple(MODIFY_EVENT, WEBSITE + "girafe.html"),
				tuple(MODIFY_EVENT, WEBSITE + "elephant.html")
		);

		fullyFetchWebsite();
		assertThat(connectorDocuments).extracting("URL", "fetched", "fetchedDateTime", "searchable", "level").containsOnly(
				tuple(WEBSITE + "index.html", true, TIME1, true, 0),
				tuple(WEBSITE + "singes.html", true, ONE_MINUTE_AFTER_TIME1, true, 1),
				tuple(WEBSITE + "girafe.html", true, ONE_MINUTE_AFTER_TIME1, true, 1),
				tuple(WEBSITE + "elephant.html", true, ONE_MINUTE_AFTER_TIME1, true, 1)
		);
		assertThat(getDataLayerFactory().getLocalCacheManager().getCache(cacheName).keySet()).containsOnly(
				WEBSITE + "index.html",
				WEBSITE + "singes.html",
				WEBSITE + "elephant.html",
				WEBSITE + "girafe.html"
		);

		recordServices.update(connectorInstance.setMaxLevel(2));

		givenTimeIs(TWO_WEEKS_AFTER_TIME1);
		fullyFetchWebsite();
		assertThat(connectorDocuments).extracting("URL", "fetched", "searchable", "level").containsOnly(
				tuple(WEBSITE + "index.html", true, true, 0),
				tuple(WEBSITE + "singes.html", true, true, 1),
				tuple(WEBSITE + "girafe.html", true, true, 1),
				tuple(WEBSITE + "elephant.html", true, true, 1),
				tuple(WEBSITE + "licornes.html", true, false, 2),
				tuple(WEBSITE + "singes/gorille.html", true, true, 2),
				tuple(WEBSITE + "singes/macaque.html", true, true, 2)
		);

		assertThat(getDataLayerFactory().getLocalCacheManager().getCache(cacheName).keySet()).containsOnly(
				WEBSITE + "index.html",
				WEBSITE + "singes.html",
				WEBSITE + "elephant.html",
				WEBSITE + "girafe.html",
				WEBSITE + "licornes.html",
				WEBSITE + "singes/gorille.html",
				WEBSITE + "singes/macaque.html"
		);
	}

	@Test
	public void givenAppropriateTimeForScheduleThenConnectorCurrentlyRunning()
			throws Exception {
		connectorInstance = es.newConnectorHttpInstanceWithId("zeConnector").setCode("zeConnector")
				.setTitle("Ze connector").setEnabled(false).setSeeds("http://constellio.com");
		LocalDateTime shishOClock = new LocalDateTime().withDayOfWeek(DateTimeConstants.WEDNESDAY).withHourOfDay(12)
				.withMinuteOfHour(50);
		givenTimeIs(shishOClock);
		TraversalSchedule schedule1 = new TraversalSchedule(DateTimeConstants.WEDNESDAY, "11:40", "13:30");
		connectorInstance.setTraversalSchedule(asList(schedule1));
		assertThat(connectorInstance.isCurrentlyRunning()).isTrue();
	}

	@Test
	public void givenFullDailyScheduleThenConnectorCurrentlyRunning()
			throws Exception {
		connectorInstance = es.newConnectorHttpInstanceWithId("zeConnector").setCode("zeConnector")
				.setTitle("Ze connector").setEnabled(false).setSeeds("http://constellio.com");
		LocalDateTime shishOClock = new LocalDateTime().withDayOfWeek(DateTimeConstants.WEDNESDAY).withHourOfDay(12)
				.withMinuteOfHour(50);
		givenTimeIs(shishOClock);
		TraversalSchedule schedule1 = new TraversalSchedule(DateTimeConstants.WEDNESDAY, "00:00", "00:00");
		connectorInstance.setTraversalSchedule(asList(schedule1));
		assertThat(connectorInstance.isCurrentlyRunning()).isTrue();
	}

	@Test
	public void givenTimeAfterScheduleThenConnectorNotCurrentlyRunning()
			throws Exception {
		connectorInstance = es.newConnectorHttpInstanceWithId("zeConnector").setCode("zeConnector")
				.setTitle("Ze connector").setEnabled(false).setSeeds("http://constellio.com");
		LocalDateTime shishOClock = new LocalDateTime().withDayOfWeek(DateTimeConstants.WEDNESDAY).withHourOfDay(14)
				.withMinuteOfHour(10);
		givenTimeIs(shishOClock);
		TraversalSchedule schedule1 = new TraversalSchedule(DateTimeConstants.WEDNESDAY, "11:40", "13:30");
		connectorInstance.setTraversalSchedule(asList(schedule1));
		assertThat(connectorInstance.isCurrentlyRunning()).isFalse();
	}

	@Test
	public void givenTimeBeforeScheduleThenConnectorNotCurrentlyRunning()
			throws Exception {
		connectorInstance = es.newConnectorHttpInstanceWithId("zeConnector").setCode("zeConnector")
				.setTitle("Ze connector").setEnabled(false).setSeeds("http://constellio.com");
		LocalDateTime shishOClock = new LocalDateTime().withDayOfWeek(DateTimeConstants.WEDNESDAY).withHourOfDay(11)
				.withMinuteOfHour(10);
		givenTimeIs(shishOClock);
		TraversalSchedule schedule1 = new TraversalSchedule(DateTimeConstants.WEDNESDAY, "11:40", "13:30");
		connectorInstance.setTraversalSchedule(asList(schedule1));
		assertThat(connectorInstance.isCurrentlyRunning()).isFalse();
	}

	@Test
	public void givenNoScheduleThenConnectorCurrentlyRunning()
			throws Exception {
		connectorInstance = es.newConnectorHttpInstanceWithId("zeConnector").setCode("zeConnector")
				.setTitle("Ze connector").setEnabled(false).setSeeds("http://constellio.com");
		LocalDateTime shishOClock = new LocalDateTime().withDayOfWeek(DateTimeConstants.WEDNESDAY).withHourOfDay(11)
				.withMinuteOfHour(10);
		givenTimeIs(shishOClock);
		connectorInstance.setTraversalSchedule(new ArrayList<TraversalSchedule>());
		assertThat(connectorInstance.isCurrentlyRunning()).isTrue();
	}

	@Test
	public void givenNullScheduleThenConnectorCurrentlyRunning()
			throws Exception {
		connectorInstance = es.newConnectorHttpInstanceWithId("zeConnector").setCode("zeConnector")
				.setTitle("Ze connector").setEnabled(false).setSeeds("http://constellio.com");
		LocalDateTime shishOClock = new LocalDateTime().withDayOfWeek(DateTimeConstants.WEDNESDAY).withHourOfDay(11)
				.withMinuteOfHour(10);
		givenTimeIs(shishOClock);
		assertThat(connectorInstance.isCurrentlyRunning()).isTrue();
	}

	@Test
	public void givenWebSiteIsNtlmWhenAuthenticationThenFetchCorrectly()
			throws Exception {

		givenTestWebsiteInState1Ntlm();
		givenDataSet1ConnectorWithNtlmAuthentication();

		fullyFetchWebsite();
		verifyWebsiteInVersion1IsCorrectlyFetched();
	}

	@Test
	public void givenWebSiteIsNtlmWhenNoAuthenticationThenFetchFails()
			throws Exception {

		givenTestWebsiteInState1Ntlm();
		givenDataSet1Connector();

		fullyFetchWebsite();
		verifyWebsiteInVersion1IsNotCorrectlyFetched();
	}

	@Test
	public void givenInvalidAndEmptyContentThenStopsFetching()
			throws Exception {
		givenTestWebsiteInState5();
		givenDataSet1Connector();

		fullyFetchWebsite();
		verifyWebsiteInVersion5IsCorrectlyFetched();

		//assertThat(fullyFetchWebsite()).isEmpty();
	}

	@Test
	public void whenFetchingThenValidMimetypes()
			throws Exception {

		givenTestWebsiteInState1();
		givenDataSet1Connector();

		fullyFetchWebsite();
		verifyWebsiteInVersion1IsCorrectlyFetched();

		// *
		// * ---------------- Refetching everything two weeks later ---------------
		// *

		givenTestWebsiteInState2();
		givenTimeIs(TWO_WEEKS_AFTER_TIME1);

		tickAndGetAllDocuments();
		tickAndGetAllDocuments();
		tickAndGetAllDocuments();
		tickAndGetAllDocuments();

		ConnectorHttpDocument girafe = es.getConnectorHttpDocumentByUrl(WEBSITE + "girafe.html");
		ConnectorHttpDocument singesWikiPdf = es.getConnectorHttpDocumentByUrl(WEBSITE + "singes-wiki.pdf");
		ConnectorHttpDocument singesTxt = es.getConnectorHttpDocumentByUrl(WEBSITE + "singes.txt");

		assertThat(girafe.getMimetype()).isEqualTo(htmlMimetype);
		assertThat(girafe.getTitle()).isEqualTo("girafe.html");
		assertThat(singesWikiPdf.getMimetype()).isNotNull().isEqualTo(pdfMimetype);
		assertThat(singesWikiPdf.getTitle()).isEqualTo("Singe — Wikipédia");
		assertThat(singesTxt.getMimetype()).isNotNull().isEqualTo(txtMimetype);
		assertThat(singesTxt.getTitle()).isEqualTo("singes.txt");

	}

	@Test
	public void givenMappedPropertiesWhenFetchingThenPersisted()
			throws Exception {

		givenTestWebsiteInState1();
		givenDataSet1Connector();

		givenTimeIs(TIME1);
		String schemaType = ConnectorHttpDocument.SCHEMA_TYPE;
		ConnectorMappingService connectorMappingService = new ConnectorMappingService(es);
		Metadata language = es.connectorHttpDocument.language();
		Metadata encoding = connectorMappingService.createTargetMetadata(
				connectorInstance, schemaType, new TargetParams("encoding", "Encoding", STRING));
		Metadata lastModification = connectorMappingService.createTargetMetadata(
				connectorInstance, schemaType, new TargetParams("lastModification", "Last modification", STRING));

		List<ConnectorField> fields = connectorMappingService.getConnectorFields(connectorInstance, schemaType);
		System.out.println(fields);

		Map<String, List<String>> mapping = new HashMap<>();
		mapping.put(encoding.getLocalCode(), asList("connectorHttpDocument:charset"));
		mapping.put(lastModification.getLocalCode(), asList("connectorHttpDocument:lastModification"));

		connectorMappingService.setMapping(connectorInstance, schemaType, mapping);

		fullyFetchWebsite();
		verifyWebsiteInVersion1IsCorrectlyFetched();

		// *
		// * ---------------- Refetching everything two weeks later ---------------
		// *

		givenTestWebsiteInState2();
		givenTimeIs(TWO_WEEKS_AFTER_TIME1);

		tickAndGetAllDocuments();
		tickAndGetAllDocuments();
		tickAndGetAllDocuments();
		tickAndGetAllDocuments();

		ConnectorHttpDocument singe = es.getConnectorHttpDocumentByUrl(WEBSITE + "singes.html");
		ConnectorHttpDocument singesWikiPdf = es.getConnectorHttpDocumentByUrl(WEBSITE + "singes-wiki.pdf");
		ConnectorHttpDocument singesTxt = es.getConnectorHttpDocumentByUrl(WEBSITE + "singes.txt");

		assertThat(singe.get(language)).isEqualTo("fr");
		assertThat(singe.getList(encoding)).containsOnly("ISO-8859-1");
		//assertThat(girafe.getList(lastModification)).containsOnly(TIME1);

		assertThat(singesWikiPdf.get(language)).isEqualTo("fr");
		assertThat(singesWikiPdf.getList(encoding)).containsOnly("ISO-8859-1");
		//		assertThat(singesWikiPdf.getList(lastModification)).containsOnly(TIME1);

		assertThat(singesTxt.get(language)).isEqualTo("fr");
		assertThat(singesTxt.getList(encoding)).containsOnly("ISO-8859-1");
		//		assertThat(singesTxt.getList(lastModification)).containsOnly(TIME1);

	}

	// ---------------------------------------------------------------

	private static final String INDEX_DIGEST = "4LzGtIzrofT/hg/zAdNk6/j/nu0=";
	private static final String SINGES_DIGEST = "IzCp+N6dMvQYwncpnrs1I8CHrLI=";
	private static final String GIRAFE_DIGEST = "nIKV/aqR9UyagkJ6Up8x9U0TmyI=";
	private static final String ELEPHANT_DIGEST = "blmPbJZ/q3AZop1vZyGRlH9E8q0=";
	private static final String SINGES_GORILLE_DIGEST = "sarDCy+pYUan0wQXdCNg6+mTxF4=";
	private static final String SINGES_MACAQUE_DIGEST = "QaPeyDvwWrjOUg+VGHaRhcJlWxo=";

	private static final String INDEX_DIGEST_V3 = "nAXXzobf+NXcRaR7v9lZtxvOi98=";
	private static final String INDEX_COPY_DIGEST_V3 = "kU2TpATqzOn92/M9mzRSQgFlciU=";
	private static final String SINGES_DIGEST_V3 = "oUMEgsH3SyjhuGkFxBO7LMLV4Dc=";
	private static final String SINGES_PDF_DIGEST_V3 = "3vWp9E/WFuhkbRIOJ+f7IRVlRrY=";
	private static final String SINGES_TEXT_DIGEST_V3 = "qBpsYTysp2dnSNEh2cRhRJSh/3M=";
	private static final String PARESSEUX_DIGEST_V3 = "psR2DVR15uhXRFYCpXyUMfQxjxQ=";
	private static final String ELEPHANT_DIGEST_V3 = "6gezA9JGdk6ZUQXSwokz2sP0HhQ=";
	private static final String SINGES_GORILLE_AND_DK_DIGEST_V3 = "sarDCy+pYUan0wQXdCNg6+mTxF4=";
	private static final String SINGES_MACAQUE_DIGEST_V3 = "QaPeyDvwWrjOUg+VGHaRhcJlWxo=";

	private static final String SINGES_GORILLE_DIGEST_V4 = "sarDCy+pYUan0wQXdCNg6+mTxF4=";
	private static final String SINGES_DK_DIGEST_V4 = "GstMbuBCbxrM6ltGb/QCyfI13so=";

	private static final String SINGES_TEXT_DIGEST_V4 = "rV1iSsaGrJgCyrMM0GgfH5zMBHk=";
	private static final String SINGES_TEXT_COPY_DIGEST_V4 = "qBpsYTysp2dnSNEh2cRhRJSh/3M=";

	private String idOf(String url) {
		return es.getConnectorHttpDocumentByUrl(url).getId();
	}

	private ConnectorHttpContext loadContext() {
		return new ConnectorHttpContextServices(es).loadContext(connectorInstance.getId());
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

	private void givenTestWebsiteInState2() {
		if (server != null) {
			try {
				server.stop();
				server.join();
			} catch (Exception e) {
				throw new RuntimeException(e);

			}
		}

		server = WebsitesUtils.startWebsiteInState2();
	}

	private void givenTestWebsiteInState3WithDuplucates() {
		if (server != null) {
			try {
				server.stop();
				server.join();
			} catch (Exception e) {
				throw new RuntimeException(e);

			}
		}

		server = WebsitesUtils.startWebsiteInState3WithDuplicates();
	}

	private void givenTestWebsiteInState4WithDuplicatesModified() {
		if (server != null) {
			try {
				server.stop();
				server.join();
			} catch (Exception e) {
				throw new RuntimeException(e);

			}
		}

		server = WebsitesUtils.startWebsiteInState4WithDuplicatesModified();
	}

	private void givenTestWebsiteInState1Ntlm() {
		if (server != null) {
			try {
				server.stop();
				server.join();
			} catch (Exception e) {
				throw new RuntimeException(e);

			}
		}
		server = WebsitesUtils.startWebsiteInState1Ntlm();
	}

	private void givenTestWebsiteInState5() {
		if (server != null) {
			try {
				server.stop();
				server.join();
			} catch (Exception e) {
				throw new RuntimeException(e);

			}
		}
		server = WebsitesUtils.startWebsiteInState5();
	}

	private List<ConnectorHttpDocument> tickAndGetAllDocuments() {
		connectorManager.getCrawler().crawlNTimes(1);
		return connectorDocuments();
	}

	private List<ConnectorHttpDocument> connectorDocuments() {
		return es.searchConnectorHttpDocuments(where(IDENTIFIER).isNotNull());
	}

	private void givenDataSet1Connector() {
		connectorInstance = connectorManager.createConnector(es.newConnectorHttpInstance().setCode("zeConnector")
				.setTitle("Ze connector").setEnabled(true).setSeeds(WEBSITE + "index.html").setIncludePatterns(WEBSITE));
	}

	private void givenDataSet1ConnectorWithNtlmAuthentication() {
		connectorInstance = connectorManager.createConnector(es.newConnectorHttpInstance().setCode("zeConnector")
				.setTitle("Ze connector").setEnabled(true).setSeeds(WEBSITE + "index.html").setIncludePatterns(WEBSITE)
				.setAuthenticationScheme(AuthenticationScheme.NTLM)
				.setUsername(NtlmAuthenticationFilter.USER).setPassword("password").setDomain(NtlmAuthenticationFilter.DOMAIN));
	}

	private void stopWebsiteServer() {
		try {
			server.stop();
			server.join();
			server = null;
		} catch (Exception e) {

		}
	}

	@After
	public void tearDown()
			throws Exception {
		eventObserver.close();

		if (server != null) {
			stopWebsiteServer();
		}
	}
}
