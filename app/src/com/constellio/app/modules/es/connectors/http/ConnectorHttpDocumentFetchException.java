package com.constellio.app.modules.es.connectors.http;

import java.util.HashMap;
import java.util.Map;

import com.constellio.app.modules.es.connectors.spi.LoggedException;

public class ConnectorHttpDocumentFetchException extends Exception implements LoggedException {

	private String url;

	public ConnectorHttpDocumentFetchException(String url, String message) {
		super(message);
		this.url = url;
	}

	public ConnectorHttpDocumentFetchException(String url, String message, Throwable cause) {
		super(message, cause);
		this.url = url;
	}

	public ConnectorHttpDocumentFetchException(String url, Throwable cause) {
		super(cause);
		this.url = url;
	}

	@Override
	public Map<String, String> getParameters() {
		Map<String, String> parameters = new HashMap<>();
		parameters.put("url", url);
		return parameters;
	}

	public static class ConnectorHttpDocumentFetchException_DocumentHasNoParsedContent
			extends ConnectorHttpDocumentFetchException {

		public ConnectorHttpDocumentFetchException_DocumentHasNoParsedContent(String url) {
			super(url, "Document at url '" + url + "' has no parsed content");
		}
	}

	public static class ConnectorHttpDocumentFetchException_CannotParseDocument
			extends ConnectorHttpDocumentFetchException {

		public ConnectorHttpDocumentFetchException_CannotParseDocument(String url, Throwable t) {
			super(url, "Cannot parse document at url '" + url + "'", t);
		}
	}

	public static class ConnectorHttpDocumentFetchException_CannotDownloadDocument
			extends ConnectorHttpDocumentFetchException {

		public ConnectorHttpDocumentFetchException_CannotDownloadDocument(String url, Throwable t) {
			super(url, "Cannot download document at url '" + url + "'", t);
		}
	}
}
