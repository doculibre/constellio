package com.constellio.app.entities.modules;

import java.io.File;

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

		public MigrationResourcesProviderRuntimeException_NoBundle(File file) {
			super("No such properties bundle for migration. Expected location would be '" + file.getAbsolutePath() + "'");
		}
	}
}
