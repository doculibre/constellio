package com.constellio.model.services.taxonomies;

public class TaxonomiesSearchServicesRuntimeException extends RuntimeException {

	public TaxonomiesSearchServicesRuntimeException(String message) {
		super(message);
	}

	public TaxonomiesSearchServicesRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public TaxonomiesSearchServicesRuntimeException(Throwable cause) {
		super(cause);
	}

	public static class TaxonomiesSearchServicesRuntimeException_CannotFilterNonPrincipalConceptWithWriteOrDeleteAccess
			extends TaxonomiesSearchServicesRuntimeException {

		public TaxonomiesSearchServicesRuntimeException_CannotFilterNonPrincipalConceptWithWriteOrDeleteAccess() {
			super("Cannot filter non principal concept with write or delete access");
		}
	}
}
