package com.constellio.model.services.taxonomies;

public class CacheBasedTaxonomyVisitingServicesException extends Exception {

	public CacheBasedTaxonomyVisitingServicesException(String message) {
		super(message);
	}

	public CacheBasedTaxonomyVisitingServicesException(String message, Throwable cause) {
		super(message, cause);
	}

	public CacheBasedTaxonomyVisitingServicesException(Throwable cause) {
		super(cause);
	}

	public static class CacheBasedTaxonomyVisitingServicesException_NotAvailableCacheNotLoaded
			extends CacheBasedTaxonomyVisitingServicesException {

		public CacheBasedTaxonomyVisitingServicesException_NotAvailableCacheNotLoaded() {
			super("Service is not available during loading of caches");
		}
	}

}
