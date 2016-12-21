package com.constellio.model.services.security;

import java.util.ArrayList;
import java.util.List;

import com.constellio.model.entities.records.Record;
import com.constellio.model.services.records.RecordUtils;

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

	public static class NoSuchAuthorizationWithIdOnRecord extends AuthorizationsServicesRuntimeException {

		public NoSuchAuthorizationWithIdOnRecord(String id, Record record) {
			super("No such authorization with id '" + id + "' on record '" + record.getIdTitle() + "'");
		}
	}

	public static class InvalidTargetRecordsIds extends AuthorizationsServicesRuntimeException {

		public InvalidTargetRecordsIds(List<Record> records, List<String> recordIds) {
			super(buildMessage(records, recordIds));
		}

		private static String buildMessage(List<Record> records, List<String> recordIds) {
			List<String> notfoundIds = new ArrayList<>(recordIds);
			notfoundIds.removeAll(new RecordUtils().toIdList(records));

			return "Invalid target records ids : " + notfoundIds;
		}
	}

	public static class InvalidPrincipalsIds extends AuthorizationsServicesRuntimeException {

		public InvalidPrincipalsIds(List<Record> records, List<String> recordIds) {
			super(buildMessage(records, recordIds));
		}

		private static String buildMessage(List<Record> records, List<String> recordIds) {
			List<String> notfoundIds = new ArrayList<>(recordIds);
			notfoundIds.removeAll(new RecordUtils().toIdList(records));

			return "Invalid principals records ids : " + notfoundIds;
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
