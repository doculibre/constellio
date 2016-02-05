package com.constellio.app.modules.es.connectors;

import com.constellio.app.modules.es.model.connectors.ConnectorDocument;
import com.constellio.app.modules.rm.wrappers.Document;

public class ConnectorServicesRuntimeException extends RuntimeException {

	public ConnectorServicesRuntimeException(String message) {
		super(message);
	}

	public ConnectorServicesRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConnectorServicesRuntimeException(Throwable cause) {
		super(cause);
	}

	public static class ConnectorServicesRuntimeException_CannotDownloadDocument
			extends ConnectorServicesRuntimeException {

		public ConnectorServicesRuntimeException_CannotDownloadDocument(ConnectorDocument<?> document,
				Throwable cause) {
			super("Cannot download connector document '" + document.getURL() + "'", cause);
		}
	}

	public static class ConnectorServicesRuntimeException_CannotDelete extends ConnectorServicesRuntimeException {

		public ConnectorServicesRuntimeException_CannotDelete(ConnectorDocument<?> document, Throwable cause) {
			super("Cannot delete connector document '" + document.getURL() + "'", cause);
		}
	}
	
	public static class ConnectorServicesRuntimeException_CannotUpload extends ConnectorServicesRuntimeException {

		public ConnectorServicesRuntimeException_CannotUpload(Document document, Throwable cause, String destinationUrl) {
			super("Cannot upload connector document '" + document.getTitle() + "' @"+destinationUrl, cause);
		}
	}
}
