package com.constellio.app.entities.modules;

public class MigrationResourcesProviderRuntimeException extends RuntimeException {
	public MigrationResourcesProviderRuntimeException(String message) {
		super(message);
	}

	public MigrationResourcesProviderRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public MigrationResourcesProviderRuntimeException(Throwable cause) {
		super(cause);
	}

	public static class MigrationResourcesProviderRuntimeException_NoBundle extends MigrationResourcesProviderRuntimeException {

		public MigrationResourcesProviderRuntimeException_NoBundle(String version, String module) {
			super("No such properties bundle for migration " + version + " of module " + module);
		}
	}
}
