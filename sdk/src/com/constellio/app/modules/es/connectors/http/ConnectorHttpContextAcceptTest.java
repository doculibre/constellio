package com.constellio.app.modules.es.connectors.http;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.sdk.tests.ConstellioTest;

public class ConnectorHttpContextAcceptTest extends ConstellioTest {

	ConnectorHttpContextServices services;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(withZeCollection().withConstellioESModule());

		ESSchemasRecordsServices es = new ESSchemasRecordsServices(zeCollection, getAppLayerFactory());
		services = new ConnectorHttpContextServices(es);

	}

	@Test
	public void whenSaveAndLoadUrlListThenValuesConserved()
			throws Exception {

		ESSchemasRecordsServices es = new ESSchemasRecordsServices(zeCollection, getAppLayerFactory());

		ConnectorHttpContext context = services.createContext("zeConnector");
		context.markAsFetched("http://site1.com/a");
		context.markAsFetched("http://site1.com/b");
		context.markAsFetched("http://site1.com/c");
		context.addDocumentDigest("digest1", "id1");
		context.addDocumentDigest("digest2", "id2");
		context.addDocumentDigest("digest3", "id3");

		services.save(context);

		ConnectorHttpContext context2 = services.loadContext("zeConnector");
		assertThat(context2.fetchedUrls).containsOnly(
				"http://site1.com/a",
				"http://site1.com/b",
				"http://site1.com/c"
		);
		assertThat(context2.documentUrlsClassifiedByDigests).hasSize(3)
				.containsEntry("digest1", "id1")
				.containsEntry("digest3", "id3")
				.containsEntry("digest2", "id2");

		context.markAsNoMoreFetched("http://site1.com/b");
		context.markAsFetched("http://site1.com/d");
		context.removeDocumentDigest("digest1", "otherId");
		context.removeDocumentDigest("digest2", "id2");
		context.addDocumentDigest("digest4", "id4");

		services.save(context);

		ConnectorHttpContext context3 = services.loadContext("zeConnector");
		assertThat(context3.fetchedUrls).containsOnly(
				"http://site1.com/a",
				"http://site1.com/d",
				"http://site1.com/c"
		);
		assertThat(context3.documentUrlsClassifiedByDigests).hasSize(3)
				.containsEntry("digest1", "id1")
				.containsEntry("digest3", "id3")
				.containsEntry("digest4", "id4");
	}
}
