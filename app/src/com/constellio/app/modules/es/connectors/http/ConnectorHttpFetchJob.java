package com.constellio.app.modules.es.connectors.http;

import static java.util.Arrays.asList;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import com.constellio.app.modules.es.connectors.http.ConnectorHttpDocumentFetchException.ConnectorHttpDocumentFetchException_CannotDownloadDocument;
import com.constellio.app.modules.es.connectors.http.ConnectorHttpDocumentFetchException.ConnectorHttpDocumentFetchException_CannotParseDocument;
import com.constellio.app.modules.es.connectors.http.ConnectorHttpDocumentFetchException.ConnectorHttpDocumentFetchException_DocumentHasNoParsedContent;
import com.constellio.app.modules.es.connectors.http.fetcher.ConnectorUrlAcceptor;
import com.constellio.app.modules.es.connectors.http.fetcher.HttpURLFetchingService;
import com.constellio.app.modules.es.connectors.http.fetcher.URLFetchingServiceRuntimeException;
import com.constellio.app.modules.es.connectors.http.fetcher.UrlAcceptor;
import com.constellio.app.modules.es.connectors.http.utils.HtmlPageParser;
import com.constellio.app.modules.es.connectors.http.utils.HtmlPageParser.HtmlPageParserResults;
import com.constellio.app.modules.es.connectors.spi.Connector;
import com.constellio.app.modules.es.connectors.spi.ConnectorJob;
import com.constellio.app.modules.es.connectors.spi.ConnectorLogger;
import com.constellio.app.modules.es.model.connectors.ConnectorDocument;
import com.constellio.app.modules.es.model.connectors.ConnectorDocumentStatus;
import com.constellio.app.modules.es.model.connectors.http.ConnectorHttpDocument;
import com.constellio.app.modules.es.model.connectors.http.ConnectorHttpInstance;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.data.utils.TimeProvider;
import com.constellio.data.utils.hashing.HashingService;
import com.constellio.data.utils.hashing.HashingServiceException;
import com.constellio.model.entities.records.ParsedContent;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.parser.FileParser;
import com.constellio.model.services.parser.FileParserException;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

class ConnectorHttpFetchJob extends ConnectorJob {

	private final ConnectorHttp connectorHttp;

	private final ConnectorHttpContext context;

	private final List<ConnectorHttpDocument> documents;

	private final HtmlPageParser pageParser;

	private final FileParser fileParser;

	private final HashingService hashingService;

	private final ConnectorLogger connectorLogger;

	private final ESSchemasRecordsServices es;

	private final int maxLevel;

	public ConnectorHttpFetchJob(ConnectorHttp connector, ConnectorHttpInstance instance, List<ConnectorHttpDocument> documents,
			ConnectorHttpContext context, ConnectorLogger connectorLogger) {
		super(connector, "fetch");
		this.connectorHttp = connector;
		this.context = context;
		this.documents = documents;
		this.connectorLogger = connectorLogger;
		this.es = connectorHttp.getEs();
		this.maxLevel = instance.getMaxLevel();
		UrlAcceptor urlAcceptor = new ConnectorUrlAcceptor(instance);
		fileParser = connectorHttp.getEs().getModelLayerFactory().newFileParser();
		hashingService = connectorHttp.getEs().getModelLayerFactory().getIOServicesFactory().newHashingService(false);
		this.pageParser = new HtmlPageParser(urlAcceptor, fileParser, hashingService);
	}

	@Override
	public void execute(Connector connector) {
		//FIXME Same instance of connector ?
		try (HttpURLFetchingService fetchingService = connectorHttp.newFetchingService()) {
			for (ConnectorHttpDocument httpDocument : documents) {
				Page page = null;
				//FIXME
				long beforeFetch = new Date().getTime();
				try {
					page = fetchingService.fetch(httpDocument.getURL());
					long afterFetch = new Date().getTime();
					httpDocument.setDownloadTime((double) afterFetch - beforeFetch);

				} catch (URLFetchingServiceRuntimeException e) {
					long afterFetch = new Date().getTime();
					httpDocument.setDownloadTime((double) afterFetch - beforeFetch);
					handleFetchException(httpDocument, e);
				}

				if (page != null) {
					try {
						parse(httpDocument, page);
					} catch (ConnectorHttpDocumentFetchException e) {
						connectorLogger.error(e);
					} catch (Throwable t) {
						connectorLogger.errorUnexpected(t);
					}
				}
			}
		}
	}

	private void handleFetchException(ConnectorHttpDocument httpDocument, URLFetchingServiceRuntimeException e) {

		httpDocument.setFetched(true);
		httpDocument.setStatus(ConnectorDocumentStatus.ERROR);
		if (!e.getErrorCode().equals(httpDocument.getErrorCode())) {
			httpDocument.resetErrorsCount();
		}
		httpDocument.incrementErrorsCount();
		httpDocument.setErrorCode(e.getErrorCode());
		httpDocument.setErrorMessage(e.getDescription());
		httpDocument.setFetchedDateTime(TimeProvider.getLocalDateTime());
		List<ConnectorDocument> documents = asList((ConnectorDocument) httpDocument);
		if (httpDocument.getErrorsCount() >= 3) {
			connectorHttp.getEventObserver().deleteEvents(httpDocument);
		} else {
			connectorHttp.getEventObserver().push(documents);
		}
	}

	private void parse(ConnectorHttpDocument httpDocument, Page page)
			throws ConnectorHttpDocumentFetchException {
		httpDocument.setFetched(true)
				.setStatus(ConnectorDocumentStatus.OK)
				.setFetchedDateTime(TimeProvider.getLocalDateTime());
		if (page instanceof HtmlPage) {
			parseHtml(httpDocument, (HtmlPage) page);

		} else {
			parseBinary(httpDocument, page);
		}
	}

	private void parseBinary(ConnectorHttpDocument httpDocument, Page page)
			throws ConnectorHttpDocumentFetchException {

		try {
			InputStream inputStream = null;
			try {
				try {
					inputStream = page.getWebResponse().getContentAsStream();
				} catch (IOException e) {
					//TODO Test!
					throw new ConnectorHttpDocumentFetchException_CannotDownloadDocument(httpDocument.getURL(), e);
				}
				ParsedContent parsedContent = fileParser.parse(inputStream, true);
				if (parsedContent.getParsedContent().isEmpty()) {
					//TODO Test!
					throw new ConnectorHttpDocumentFetchException_DocumentHasNoParsedContent(httpDocument.getURL());
				} else {
					httpDocument.addStringProperty("lastModified", page.getWebResponse().getResponseHeaderValue("Last-Modified"));
					httpDocument.addStringProperty("charset", page.getWebResponse().getContentCharset());
					httpDocument.addStringProperty("language", parsedContent.getLanguage());
					httpDocument.setParsedContent(parsedContent.getParsedContent());

					httpDocument.setTitle(extractFilename(httpDocument.getURL()));
					httpDocument.setDigest(hashingService.getHashFromString(parsedContent.getParsedContent()));
					httpDocument.setMimetype(parsedContent.getMimetypeWithoutCharset());
				}
			} catch (FileParserException e) {
				//TODO Test!
				throw new ConnectorHttpDocumentFetchException_CannotParseDocument(httpDocument.getURL(), e);

			} catch (HashingServiceException e) {
				throw new ImpossibleRuntimeException(e);

			} finally {
				IOUtils.closeQuietly(inputStream);
			}

			httpDocument.setErrorCode(null)
					.setErrorMessage(null)
					.setErrorStackTrace(null)
					.resetErrorsCount()
					.setManualTokens(Record.PUBLIC_TOKEN);

		} catch (Exception e) {
			httpDocument.setErrorCode(ConnectorHttpFetchJob.class.getSimpleName() + ".parseBinary()")
					.setErrorMessage(ExceptionUtils.getMessage(e))
					.setErrorStackTrace(ExceptionUtils.getFullStackTrace(e))
					.incrementErrorsCount();
		}

		saveDocumentDigestAndDetectCopy(httpDocument);
		connectorHttp.getEventObserver().push(asList((ConnectorDocument) httpDocument));
	}

	private void parseHtml(ConnectorHttpDocument httpDocument, HtmlPage page)
			throws ConnectorHttpDocumentFetchException {
		HtmlPageParserResults results = pageParser.parse(httpDocument.getURL(), (HtmlPage) page);

		List<ConnectorDocument> savedDocuments = new ArrayList<>();
		List<String> urls = new ArrayList<>(results.getLinkedUrls());
		int linksLevel = httpDocument.getLevel() + 1;
		if (linksLevel <= maxLevel)
			for (String url : urls) {
				if (context.isNewUrl(url)) {
					context.markAsFetched(url);

					savedDocuments.add(connectorHttp.newUnfetchedURLDocument(url, linksLevel));
				}
			}

		ensureNotStopped();
		setJobStep("Fetching " + httpDocument.getURL());

		String title = results.getTitle() == null ? extractFilename(httpDocument.getURL()) : results.getTitle();

		httpDocument.setManualTokens(Record.PUBLIC_TOKEN);
		savedDocuments.add(httpDocument
				.setTitle(title)
				.setErrorCode(null)
				.setErrorMessage(null)
				.setErrorStackTrace(null)
				.resetErrorsCount()
				.setParsedContent(results.getParsedContent())
				.setDigest(results.getDigest())
				//.setOutlinks(urls)
				.setMimetype(results.getMimetype())
				.addStringProperty("lastModified", page.getWebResponse().getResponseHeaderValue("Last-Modified"))
				.addStringProperty("charset", page.getWebResponse().getContentCharset())
				.addStringProperty("language", results.getLanguage()));

		saveDocumentDigestAndDetectCopy(httpDocument);
		connectorHttp.getEventObserver().push(savedDocuments);
	}

	private String extractFilename(String url) {
		int lastSlash = url.lastIndexOf("/");
		if (lastSlash == -1) {
			return url;
		} else {
			return url.substring(lastSlash + 1);
		}
	}

	private void saveDocumentDigestAndDetectCopy(ConnectorHttpDocument httpDocument) {
		Record record = httpDocument.getWrappedRecord();

		httpDocument.setSearchable(true);
		httpDocument.setCopyOf(null);
		String originalDigest = null;
		if (record.isSaved()) {
			originalDigest = record.getCopyOfOriginalRecord().get(es.connectorHttpDocument.digest());
		}
		if (originalDigest != null && !originalDigest.equals(httpDocument.getDigest())) {
			context.removeDocumentDigest(originalDigest, httpDocument.getURL());
		}

		if (httpDocument.getDigest() != null) {
			String documentUrlWithDigest = context.getDocumentUrlWithDigest(httpDocument.getDigest());
			if (documentUrlWithDigest != null && !httpDocument.getURL().equals(documentUrlWithDigest)) {
				httpDocument.setParsedContent(null);
				httpDocument.setCopyOf(documentUrlWithDigest);
				httpDocument.setSearchable(false);
			} else {
				context.addDocumentDigest(httpDocument.getDigest(), httpDocument.getURL());
			}
		}

	}

}
