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

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jsoup.nodes.Element;

import com.constellio.app.modules.es.connectors.http.ConnectorHttpUtils.FetchedDocumentContent;
import com.constellio.app.modules.es.connectors.spi.Connector;
import com.constellio.app.modules.es.connectors.spi.ConnectorJob;
import com.constellio.app.modules.es.model.connectors.ConnectorDocument;
import com.constellio.app.modules.es.model.connectors.http.ConnectorHttpDocument;
import com.constellio.app.modules.es.model.connectors.http.ConnectorHttpInstance;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.utils.BatchBuilderIterator;
import com.constellio.model.entities.records.ParsedContent;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.parser.FileParser;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

public class ConnectorHttp extends Connector {

	public static final String FIELD_MIMETYPE = "mimetype";

	private static final int DOCUMENTS_PER_JOBS = 5;
	private static final int JOBS_IN_PARALLEL = 5;

	ConnectorHttpInstance connectorInstance;

	@Override
	public void initialize(Record instanceRecord) {
		this.connectorInstance = es.wrapConnectorHttpInstance(instanceRecord);
	}

	@Override
	public List<String> fetchTokens(String username) {
		return new ArrayList<>();
	}

	@Override
	public List<String> getConnectorDocumentTypes() {
		return asList(ConnectorHttpDocument.SCHEMA_TYPE);
	}

	private ConnectorHttpDocument newUnfetchedURLDocument(String url) {
		return es.newConnectorHttpDocument(connectorInstance).setURL(url).setFetched(false);
	}

	public void start() {
		List<ConnectorDocument> documents = new ArrayList<>();
		for (String url : connectorInstance.getSeeds()) {
			documents.add(newUnfetchedURLDocument(url));
		}
		for (String url : connectorInstance.getOnDemands()) {
			documents.add(newUnfetchedURLDocument(url));
		}
		eventObserver.addUpdateEvents(documents);
	}

	public void resume() {
		List<ConnectorDocument> documents = new ArrayList<>();
		for (String url : connectorInstance.getOnDemands()) {
			documents.add(newUnfetchedURLDocument(url));
		}
		eventObserver.addUpdateEvents(documents);
	}

	@Override
	public synchronized List<ConnectorJob> getJobs() {

		LogicalSearchQuery query = es.connectorDocumentsToFetchQuery(connectorInstance);
		query.setNumberOfRows(JOBS_IN_PARALLEL * DOCUMENTS_PER_JOBS);

		List<ConnectorJob> jobs = new ArrayList<>();
		List<ConnectorHttpDocument> documentsToFetch = es.searchConnectorHttpDocuments(query);
		Iterator<List<ConnectorHttpDocument>> documentBatchsIterator = new BatchBuilderIterator<>(
				documentsToFetch.iterator(), DOCUMENTS_PER_JOBS);

		while (documentBatchsIterator.hasNext()) {
			jobs.add(new FetchJob(this, documentBatchsIterator.next()));
		}

		return jobs;
	}

	private class FetchJob extends ConnectorJob {

		private final List<ConnectorHttpDocument> documents;

		public FetchJob(Connector connector, List<ConnectorHttpDocument> documents) {
			super(connector, "fetch");
			this.documents = documents;
		}

		@Override
		public void execute(Connector connector) {
			User user = es.getModelLayerFactory().newUserServices()
					.getUserInCollection("admin", connectorInstance.getCollection());
			ContentManager contentManager = es.getContentManager();
			IOServices ioServices = es.getIOServices();
			for (ConnectorHttpDocument httpDocument : documents) {
				ensureNotStopped();
				setJobStep("Fetching " + httpDocument.getURL());

				InputStream inputStream = null;
				try {

					FetchedDocumentContent fetchedDocumentContent = ConnectorHttpUtils.fetch(httpDocument.getURL());

					inputStream = fetchedDocumentContent.newInputStream(ioServices);
					List<ConnectorDocument> savedDocuments = new ArrayList<>();

					FileParser fileParser = es.getModelLayerFactory().newFileParser();
					ParsedContent parsedContent = fileParser.parse(inputStream, fetchedDocumentContent.getContentLength());

					Set<String> urlsToFetch = new HashSet<>();
					for (Element element : fetchedDocumentContent.getDocument().getElementsByTag("a")) {
						String href = element.attr("href");
						if (href != null && !href.equals("#") && isNotBlank(href)) {
							String url = ConnectorHttpUtils.toAbsoluteHRef(httpDocument.getURL(), href);
							if (isFetched(url) && isNewUrl(url)) {
								urlsToFetch.add(url);
							}
						}
					}

					for (String urlToFetch : urlsToFetch) {
						logInfo("Adding URL to fetch : " + urlToFetch);
						savedDocuments.add(newUnfetchedURLDocument(urlToFetch));
					}

					// Until we manage security properly, all HttpDocuments will be public:
					httpDocument.setManualTokens(Record.PUBLIC_TOKEN);

					savedDocuments.add(httpDocument
							.setFetched(true)
							.setBaseURI(fetchedDocumentContent.baseUri())
							.setTitle(fetchedDocumentContent.getTitle())
							.setParsedContent(parsedContent.getParsedContent())
							.addProperty(FIELD_MIMETYPE, parsedContent.getMimeType()));

					eventObserver.addUpdateEvents(savedDocuments);

				} catch (Exception e) {
					logError(e);

				} finally {
					ioServices.closeQuietly(inputStream);
				}
			}
		}

		private boolean isNewUrl(String href) {
			boolean isNew = !es.getSearchServices().hasResults(
					from(es.connectorHttpDocument.schemaType()).where(es.connectorHttpDocument.url()).isEqualTo(href));

			//logInfo(href + (isNew ? " is a new URL" : " is not a new URL"));
			return isNew;
		}

		private boolean isFetched(String href) {
			//TODO Use white/black regex instead
			boolean fetched = false;
			for (String seed : connectorInstance.getSeeds()) {
				if (href.startsWith(seed)) {
					fetched = true;
				}
			}
			//TODO Use white/black regex instead

			fetched &= !href.contains("wp-content");

			//There is two http: in the url
			fetched &= href.indexOf("http", 1) == -1;

			//TODO
			fetched &= !href.contains("#") && !href.contains("javascript") && !href.contains("..") && !href.contains("@");

			if (!fetched) {
				//logInfo("URL '" + href + "' is excluded");
			}

			return fetched;
		}

	}

}
