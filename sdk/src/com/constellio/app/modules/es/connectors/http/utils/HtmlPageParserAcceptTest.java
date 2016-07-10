package com.constellio.app.modules.es.connectors.http.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.jetty.server.Server;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.es.connectors.http.ConnectorHttpDocumentFetchException;
import com.constellio.app.modules.es.connectors.http.fetcher.HttpURLFetchingService;
import com.constellio.app.modules.es.connectors.http.fetcher.UrlAcceptor;
import com.constellio.app.modules.es.connectors.http.utils.HtmlPageParser.HtmlPageParserResults;
import com.constellio.data.utils.hashing.HashingService;
import com.constellio.model.services.parser.FileParser;
import com.constellio.sdk.tests.ConstellioTest;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class HtmlPageParserAcceptTest extends ConstellioTest {

	private static String WEBSITE = "http://localhost:4242/";

	Server server;
	HttpURLFetchingService fetchingService = new HttpURLFetchingService(1000);
	UrlAcceptor acceptAllExceptElephant = new UrlAcceptor() {

		@Override
		public boolean isAccepted(String normalizedUrl) {
			return !normalizedUrl.contains("elephant");
		}
	};
	UrlAcceptor acceptAll = new UrlAcceptor() {

		@Override
		public boolean isAccepted(String normalizedUrl) {
			return true;
		}
	};
	FileParser fileParser;
	HashingService hashingService;

	HtmlPageParser parser;

	@Before
	public void setUp()
			throws Exception {
		fileParser = getModelLayerFactory().newFileParser();
		hashingService = getModelLayerFactory().getIOServicesFactory().newHashingService(false);
		parser = new HtmlPageParser(acceptAll, fileParser, hashingService);
		server = WebsitesUtils.startWebsiteInState1();
	}

	@After
	public void tearDown()
			throws Exception {
		if (server != null) {
			server.stop();
		}

	}

	@Test
	public void whenParsingHtmlPageThenExtractUrlsTitleAndParsedContent()
			throws Exception {

		HtmlPageParserResults results = parse(WEBSITE + "singes.html");
		assertThat(results.getTitle()).isEqualTo("La famille des singes");
		assertThat(results.getParsedContent()).contains("espèces").contains("animaux").contains("éléphants");
		assertThat(results.getLinkedUrls()).containsOnly(
				WEBSITE + "singes/macaque.html",
				WEBSITE + "singes/gorille.html",
				WEBSITE + "girafe.html",
				WEBSITE + "elephant.html"
		);

	}

	@Test
	public void givenAnUrlIsNotAcceptedThenNotReturnedInUrlList()
			throws Exception {

		parser = new HtmlPageParser(acceptAllExceptElephant, fileParser, hashingService);

		HtmlPageParserResults results = parse(WEBSITE + "singes.html");
		assertThat(results.getTitle()).isEqualTo("La famille des singes");
		assertThat(results.getParsedContent()).contains("espèces").contains("animaux").contains("éléphants");
		assertThat(results.getLinkedUrls()).containsOnly(
				WEBSITE + "singes/macaque.html",
				WEBSITE + "singes/gorille.html",
				WEBSITE + "girafe.html"
		);

	}

	private HtmlPageParserResults parse(String url)
			throws ConnectorHttpDocumentFetchException {
		HtmlPage page = (HtmlPage) fetchingService.fetch(url);
		return parser.parse(url, page);
	}
}
