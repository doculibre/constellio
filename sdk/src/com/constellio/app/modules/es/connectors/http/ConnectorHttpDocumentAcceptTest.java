package com.constellio.app.modules.es.connectors.http;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.es.connectors.spi.ConnectorLogger;
import com.constellio.app.modules.es.connectors.spi.ConsoleConnectorLogger;
import com.constellio.app.modules.es.model.connectors.http.ConnectorHttpDocument;
import com.constellio.app.modules.es.model.connectors.http.ConnectorHttpInstance;
import com.constellio.app.modules.es.services.ConnectorManager;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.model.services.records.RecordServices;
import com.constellio.sdk.tests.ConstellioTest;

public class ConnectorHttpDocumentAcceptTest extends ConstellioTest {

	private LocalDateTime SHISH_O_CLOCK = new LocalDateTime();

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
	public void whenAddUpdatingConnectorHTTPDocumentThenAddUpdatedCorrectly()
			throws Exception {

		connectorInstance = connectorManager
				.createConnector(es.newConnectorHttpInstance().setCode("zeConnector").setEnabled(false)
						.setTitle("ze connector").setSeeds("http://perdu.com")
						.setDaysBeforeRefetching(42));

		ConnectorHttpDocument refDocument = es.newConnectorHttpDocument(connectorInstance);
		refDocument.setTitle("ref title");
		refDocument.setURL("http://www.perdu.com");
		refDocument.setTraversalCode("refTraversal");
		recordServices.add(refDocument);

		ConnectorHttpDocument document = es.newConnectorHttpDocument(connectorInstance);

		document.setTitle("ze title");
		document.setURL("http://www.perdu.com");
		document.setTraversalCode("zeTraversal");

		document.setLevel(2);
		document.setPriority(0.6);
		document.setOnDemand(true);
		document.setCopyOf(refDocument.getId());
		document.setOutlinks(asList("http://www.constellio/1", "http://www.constellio/2", "http://www.constellio/3"));
		document.setInlinks(asList("http://www.constellio/doc42", "http://www.constellio/doc666"));
		document.setCharset("UTF-8");
		document.setDigest("MD5");
		document.setContentType("TXT");
		document.setNeverFetch(true);
		document.setFetchedDateTime(SHISH_O_CLOCK);

		recordServices.add(document);
		document = es.getConnectorHttpDocument(document.getId());

		assertThat(document.getTitle()).isEqualTo("ze title");
		assertThat(document.getURL()).isEqualTo("http://www.perdu.com");
		assertThat(document.getTraversalCode()).isEqualTo("zeTraversal");
		assertThat(document.getLevel()).isEqualTo(2);
		assertThat(document.getFetchDelay()).isEqualTo(42);
		assertThat(document.getPriority()).isEqualTo(0.6);
		assertThat(document.getOnDemand()).isTrue();
		assertThat(document.getCopyOf()).isEqualTo(refDocument.getId());
		assertThat(document.getInlinks()).isEqualTo(asList("http://www.constellio/doc42", "http://www.constellio/doc666"));
		assertThat(document.getOutlinks())
				.isEqualTo(asList("http://www.constellio/1", "http://www.constellio/2", "http://www.constellio/3"));
		assertThat(document.getCharset()).isEqualTo("UTF-8");
		assertThat(document.getDigest()).isEqualTo("MD5");
		assertThat(document.getContentType()).isEqualTo("TXT");
		assertThat(document.getNeverFetch()).isTrue();
		assertThat(document.getFetchedDateTime()).isEqualTo(SHISH_O_CLOCK);
		assertThat(document.getNextFetch()).isEqualTo(SHISH_O_CLOCK.plusDays(42));

	}

}
