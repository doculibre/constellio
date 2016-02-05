package com.constellio.app.services.schemas.bulkImport;

import java.util.List;

public class RecordsImportServicesRuntimeException extends RuntimeException {

	public RecordsImportServicesRuntimeException(Throwable t) {
		super(t);
	}

	public RecordsImportServicesRuntimeException(String message) {
		super(message);
	}

	public RecordsImportServicesRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public static class RecordsImportServicesRuntimeException_CyclicDependency extends RecordsImportServicesRuntimeException {

		private List<String> cyclicDependentIds;

		public RecordsImportServicesRuntimeException_CyclicDependency(String schemaType, List<String> cyclicDependentIds) {
			super("Cyclic dependency detected in schemaType '" + schemaType + "' with legacy ids " + cyclicDependentIds);
			this.cyclicDependentIds = cyclicDependentIds;
		}

		public List<String> getCyclicDependentIds() {
			return cyclicDependentIds;
		}
	}
}
