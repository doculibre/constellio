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
package com.constellio.model.services.security;

@SuppressWarnings("serial")
public class AuthorizationsServicesRuntimeException extends RuntimeException {

	public AuthorizationsServicesRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public AuthorizationsServicesRuntimeException(String message) {
		super(message);
	}

	public static class CannotAddUpdateWithoutPrincipalsAndOrTargetRecords extends AuthorizationsServicesRuntimeException {

		public CannotAddUpdateWithoutPrincipalsAndOrTargetRecords() {
			super("Cannot add or update authorizations without set principals and/or target records");
		}
	}

	public static class NoSuchAuthorizationWithId extends AuthorizationsServicesRuntimeException {

		public NoSuchAuthorizationWithId(String id) {
			super("No such authorization with id '" + id + "'");
		}
	}

	public static class InvalidPrincipalsAndOrTargetRecordsIds extends AuthorizationsServicesRuntimeException {

		public InvalidPrincipalsAndOrTargetRecordsIds() {
			super("Invalid principals ids and/or target records ids");
		}
	}

	public static class CannotDetachConcept extends AuthorizationsServicesRuntimeException {

		public CannotDetachConcept(String concept) {
			super("Cannot detach concept : " + concept);
		}
	}

	public static class CannotAddAuhtorizationInNonPrincipalTaxonomy extends AuthorizationsServicesRuntimeException {

		public CannotAddAuhtorizationInNonPrincipalTaxonomy() {
			super("Cannot add auhtorization in a non principal taxonomy");
		}
	}

	public static class CannotSetTaxonomyAsPrincipal extends AuthorizationsServicesRuntimeException {

		public CannotSetTaxonomyAsPrincipal() {
			super("Cannot set taxonomy as principal");
		}
	}

	public static class CannotSetMultiValueInASchemaFromPrincipalTaxonomy extends AuthorizationsServicesRuntimeException {

		public CannotSetMultiValueInASchemaFromPrincipalTaxonomy() {
			super("Cannot set multi-value in a schema from principal taxonomy");
		}
	}

	public static class RecordIsNotAConceptOfPrincipalTaxonomy extends AuthorizationsServicesRuntimeException {

		public RecordIsNotAConceptOfPrincipalTaxonomy(String recordId, String principalTaxo) {
			super("Record \"" + recordId + "\" is not a concept of principal taxonomy\"" + principalTaxo + "\"");
		}
	}

	public static class RecordServicesErrorDuringOperation extends AuthorizationsServicesRuntimeException {

		public RecordServicesErrorDuringOperation(String operation, Throwable cause) {
			super("RecordServicesException on operation '" + operation + "'", cause);
		}
	}
}
