package com.constellio.app.modules.es.connectors.smb;

import com.constellio.app.modules.es.model.connectors.ConnectorDocument;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbDocument;

public class ConnectorSmbRuntimeException extends RuntimeException {

	public ConnectorSmbRuntimeException(String message) {
		super(message);
	}

	public ConnectorSmbRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConnectorSmbRuntimeException(Throwable cause) {
		super(cause);
	}

	public static class ConnectorSmbRuntimeException_CannotDownloadSmbDocument extends ConnectorSmbRuntimeException {

		public ConnectorSmbRuntimeException_CannotDownloadSmbDocument(ConnectorSmbDocument document, Throwable cause) {
			super("Cannot download connector smb document '" + document.getURL() + "'", cause);
		}
	}

	public static class ConnectorSmbRuntimeException_CannotDelete extends ConnectorSmbRuntimeException {

		public ConnectorSmbRuntimeException_CannotDelete(ConnectorDocument document, Throwable cause) {
			super("Cannot delete connector document '" + document.getURL() + "'", cause);
		}
	}
}
