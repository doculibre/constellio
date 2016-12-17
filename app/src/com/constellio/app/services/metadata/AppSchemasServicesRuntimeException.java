package com.constellio.app.services.metadata;

public class AppSchemasServicesRuntimeException extends RuntimeException {

	public AppSchemasServicesRuntimeException(String message) {
		super(message);
	}

	public AppSchemasServicesRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public AppSchemasServicesRuntimeException(Throwable cause) {
		super(cause);
	}

	public static class AppSchemasServicesRuntimeException_CannotChangeCodeFromOrToDefault
			extends AppSchemasServicesRuntimeException {

		public AppSchemasServicesRuntimeException_CannotChangeCodeFromOrToDefault() {
			super("Cannot change code from or to 'default'");
		}
	}

	public static class AppSchemasServicesRuntimeException_CannotChangeCodeToOtherSchemaType
			extends AppSchemasServicesRuntimeException {

		public AppSchemasServicesRuntimeException_CannotChangeCodeToOtherSchemaType() {
			super("Cannot change code to other schema type");
		}
	}

	public static class AppSchemasServicesRuntimeException_CannotDeleteSchema
			extends AppSchemasServicesRuntimeException {

		public AppSchemasServicesRuntimeException_CannotDeleteSchema(String schema) {
			super("Cannot delete schema '" + schema + "'");
		}
	}
}
