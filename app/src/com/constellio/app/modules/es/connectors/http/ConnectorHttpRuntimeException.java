package com.constellio.app.modules.es.connectors.http;

public class ConnectorHttpRuntimeException extends RuntimeException {

	public ConnectorHttpRuntimeException(String message) {
		super(message);
	}

	public ConnectorHttpRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConnectorHttpRuntimeException(Throwable cause) {
		super(cause);
	}

	public static class ConnectorHttpRuntimeException_CannotGetAbsoluteHref extends ConnectorHttpRuntimeException {
		public ConnectorHttpRuntimeException_CannotGetAbsoluteHref(String currentUrl, String href) {
			super("Cannot get absolute url using currentUrl '" + currentUrl + "' and relative href '" + href + "'");
		}
	}
}
