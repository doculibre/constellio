package com.constellio.app.modules.es.services.crawler;

public class ConnectorManagerRuntimeException extends RuntimeException {

	public ConnectorManagerRuntimeException(String message) {
		super(message);
	}

	public ConnectorManagerRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConnectorManagerRuntimeException(Throwable cause) {
		super(cause);
	}

	public static class ConnectorManagerRuntimeException_CrawlerCannotBeChangedAfterItIsStarted
			extends ConnectorManagerRuntimeException {

		public ConnectorManagerRuntimeException_CrawlerCannotBeChangedAfterItIsStarted() {
			super("Crawler cannot be changed after it is started");
		}
	}
}
