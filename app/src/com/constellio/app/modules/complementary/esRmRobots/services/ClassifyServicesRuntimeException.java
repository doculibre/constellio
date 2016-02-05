package com.constellio.app.modules.complementary.esRmRobots.services;

import com.constellio.app.modules.es.model.connectors.ConnectorDocument;

public class ClassifyServicesRuntimeException extends RuntimeException {

	public ClassifyServicesRuntimeException(String message) {
		super(message);
	}

	public ClassifyServicesRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public ClassifyServicesRuntimeException(Throwable cause) {
		super(cause);
	}

	public static class ClassifyServicesRuntimeException_CannotClassifyAsFolder extends ClassifyServicesRuntimeException {
		public ClassifyServicesRuntimeException_CannotClassifyAsFolder(ConnectorDocument connectorDocument, Throwable cause) {
			super("Cannot classify " + connectorDocument.getClass().getSimpleName() + " '" + connectorDocument.getId() + "-"
					+ connectorDocument.getTitle() + "'", cause);
		}
	}

	public static class ClassifyServicesRuntimeException_CannotClassifyAsDocument extends ClassifyServicesRuntimeException {
		public ClassifyServicesRuntimeException_CannotClassifyAsDocument(ConnectorDocument connectorDocument, Throwable cause) {
			super("Cannot classify " + connectorDocument.getClass().getSimpleName() + " '" + connectorDocument.getId() + "-"
					+ connectorDocument.getTitle() + "'", cause);
		}
	}
}
