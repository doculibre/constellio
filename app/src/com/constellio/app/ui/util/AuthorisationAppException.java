package com.constellio.app.ui.util;

import com.constellio.model.entities.records.Record;
import com.constellio.model.services.records.RecordUtils;
import com.constellio.model.services.security.AuthorizationsServicesRuntimeException;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;


public class AuthorisationAppException
		extends RuntimeException {

	public AuthorisationAppException(String message, Throwable cause) {
		super(message, cause);
	}

	public AuthorisationAppException(String message) {
		super(message);
	}

	public static class CannotAddUpdateWithoutPrincipalsAndOrTargetRecords extends AuthorizationsServicesRuntimeException {

		public CannotAddUpdateWithoutPrincipalsAndOrTargetRecords() {
			super($("Cannot add or update authorizations without set principals and/or target records"));
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

	public static class CannotSetMultiValueInASchemaFromPrincipalTaxonomy extends AuthorizationsServicesRuntimeException {

		public CannotSetMultiValueInASchemaFromPrincipalTaxonomy() {
			super($("AuthorizationError.CannotSetMultiValueInASchemaFromPrincipalTaxonomy"));
		}
	}

	public static class RecordIsNotAConceptOfPrincipalTaxonomy extends AuthorizationsServicesRuntimeException {

		public RecordIsNotAConceptOfPrincipalTaxonomy(String recordId, String principalTaxo) {
			super($("AuthorizationError.RecordIsNotAConceptOfPrincipalTaxonomy", recordId, principalTaxo));
		}
	}

	public static class AuthServices_RecordServicesException extends AuthorizationsServicesRuntimeException {

		public AuthServices_RecordServicesException(Throwable cause) {
			super("Record service exception", cause);
		}
	}

	public static class StartDateGreaterThanEndDate extends AuthorizationsServicesRuntimeException {

		public StartDateGreaterThanEndDate(LocalDate startDate, LocalDate endDate) {
			super($("AuthorizationError.StartDateGreaterThanEndDate", startDate.toString(), endDate.toString()));
		}
	}

	public static class EndDateLessThanCurrentDate extends AuthorizationsServicesRuntimeException {

		public EndDateLessThanCurrentDate(String endDate) {
			super($("AuthorizationError.EndDateLessThanCurrentDate", endDate));
		}
	}
}
