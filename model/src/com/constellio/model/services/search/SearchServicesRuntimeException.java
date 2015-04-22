/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
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
