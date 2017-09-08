package com.constellio.model.services.migrations;

public class RecordMigrationsManagerRuntimeException extends RuntimeException {

	public RecordMigrationsManagerRuntimeException(String message) {
		super(message);
	}

	public RecordMigrationsManagerRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public RecordMigrationsManagerRuntimeException(Throwable cause) {
		super(cause);
	}

	public static class RecordMigrationsManagerRuntimeException_ScriptNotRegistered
			extends RecordMigrationsManagerRuntimeException {

		public RecordMigrationsManagerRuntimeException_ScriptNotRegistered(String script, String schemaType, String collection) {
			super("Script '" + script + "' is not registered for schemaType '" + schemaType + "' of collection '" + collection
					+ "'");
		}
	}
}
