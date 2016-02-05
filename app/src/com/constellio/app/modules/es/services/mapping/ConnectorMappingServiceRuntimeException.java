package com.constellio.app.modules.es.services.mapping;

public class ConnectorMappingServiceRuntimeException extends RuntimeException {

	public ConnectorMappingServiceRuntimeException(String message) {
		super(message);
	}

	public ConnectorMappingServiceRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConnectorMappingServiceRuntimeException(Throwable cause) {
		super(cause);
	}

	public static class ConnectorMappingServiceRuntimeException_InvalidArgument
			extends ConnectorMappingServiceRuntimeException {

		public ConnectorMappingServiceRuntimeException_InvalidArgument() {
			super("Invalid required argument");
		}
	}

	public static class ConnectorMappingServiceRuntimeException_MetadataAlreadyExist
			extends ConnectorMappingServiceRuntimeException {

		public ConnectorMappingServiceRuntimeException_MetadataAlreadyExist(String code) {
			super("A metadata with the code '" + code + "' already exist");
		}
	}
}
