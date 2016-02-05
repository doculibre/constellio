package com.constellio.model.services.search;

public class SearchServicesRuntimeException extends RuntimeException {

	public SearchServicesRuntimeException(String message) {
		super(message);
	}

	public static class TooManyRecordsInSingleSearchResult extends SearchServicesRuntimeException {

		public TooManyRecordsInSingleSearchResult(String query) {
			super("Too many records in single search result. Query: " + query);
		}
	}

	public static class TooManyElementsInCriterion extends SearchServicesRuntimeException {

		public TooManyElementsInCriterion(int size) {
			super("Too many elementsin isIn/isNotIn criterion Max 1000. Was " + size);
		}
	}

	public static class CannotUseIsInForEmptyList extends SearchServicesRuntimeException {

		public CannotUseIsInForEmptyList() {
			super("Cannot verify if an element is in an empty list");
		}
	}

	public static class SearchServicesRuntimeException_CollectionIsRequired extends SearchServicesRuntimeException {

		public SearchServicesRuntimeException_CollectionIsRequired() {
			super("Collection is required");
		}
	}

}
