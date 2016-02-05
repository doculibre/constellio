package com.constellio.app.modules.es.connectors.http.fetcher;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;

public abstract class URLFetchingServiceRuntimeException extends RuntimeException {

	public URLFetchingServiceRuntimeException(String message) {
		super(message);
	}

	public URLFetchingServiceRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public URLFetchingServiceRuntimeException(Throwable cause) {
		super(cause);
	}

	public abstract String getErrorCode();

	public abstract String getDescription();

	public static class URLFetchingServiceRuntimeException_HttpError extends URLFetchingServiceRuntimeException {

		private String url;

		private int errorCode;

		private String description;

		public URLFetchingServiceRuntimeException_HttpError(String url, FailingHttpStatusCodeException e) {
			super("Url '" + url + "' returned error code '" + e.getStatusCode() + "'", e);
			this.url = url;
			this.errorCode = e.getStatusCode();
			this.description = e.getStatusMessage();
		}

		public String getUrl() {
			return url;
		}

		public String getErrorCode() {
			return "" + errorCode;
		}

		public String getDescription() {
			return description;
		}
	}

	public static class URLFetchingServiceRuntimeException_MalformedUrl extends URLFetchingServiceRuntimeException {

		private String url;

		public URLFetchingServiceRuntimeException_MalformedUrl(String url, Exception e) {
			super("Url '" + url + "' is malformed", e);
			this.url = url;
		}

		public String getUrl() {
			return url;
		}

		@Override
		public String getErrorCode() {
			return "malformed url";
		}

		@Override
		public String getDescription() {
			return "";
		}
	}

	public static class URLFetchingServiceRuntimeException_IOException extends URLFetchingServiceRuntimeException {

		private String url;

		public URLFetchingServiceRuntimeException_IOException(String url, Exception e) {
			super("An io exception occured during fetching of url '" + url + "'", e);
			this.url = url;
		}

		public String getUrl() {
			return url;
		}

		@Override
		public String getErrorCode() {
			return "io exception";
		}

		@Override
		public String getDescription() {
			return "Cannot connect to server";
		}
	}

	public static class URLFetchingServiceRuntimeException_NoSuchAlgorithm extends URLFetchingServiceRuntimeException {

		private String url;

		public URLFetchingServiceRuntimeException_NoSuchAlgorithm(String url, Exception e) {
			super("No such algorithm for url '" + url + "'", e);
			this.url = url;
		}

		public String getUrl() {
			return url;
		}

		@Override
		public String getErrorCode() {
			return "no such algo";
		}

		@Override
		public String getDescription() {
			return "No such algorithm";
		}
	}

}
