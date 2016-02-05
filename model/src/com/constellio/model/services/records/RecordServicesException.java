package com.constellio.model.services.records;

import com.constellio.data.dao.dto.records.TransactionDTO;
import com.constellio.data.dao.services.bigVault.RecordDaoException;
import com.constellio.model.entities.records.Record;
import com.constellio.model.frameworks.validation.ValidationErrors;

@SuppressWarnings("serial")
public class RecordServicesException extends Exception {

	public RecordServicesException(String message, Throwable cause) {
		super(message, cause);
	}

	public RecordServicesException(String message) {
		super(message);
	}

	public RecordServicesException(Throwable cause) {
		super(cause);
	}

	public static class PreviouslySavedContentCouldNotBeCommitted extends RecordServicesException {

		public PreviouslySavedContentCouldNotBeCommitted(Throwable cause) {
			super(cause);
		}

	}

	public static class OptimisticLocking extends RecordServicesException {

		final String id;

		final Long version;

		final TransactionDTO transactionDTO;

		public OptimisticLocking(TransactionDTO transactionDTO, RecordDaoException.OptimisticLocking e) {
			super(getMessage(e.getId(), e.getVersion()), e);
			this.transactionDTO = transactionDTO;
			this.id = e.getId();
			this.version = e.getVersion();
		}

		public OptimisticLocking(String id, TransactionDTO transactionDTO, Throwable cause) {
			super(getMessage(id, null), cause);
			this.transactionDTO = transactionDTO;
			this.id = id;
			this.version = null;
		}

		private static String getMessage(String id, Long version) {
			return "Optimistic locking while saving record with id '" + id + "' in version '" + version + "'";
		}

		public String getId() {
			return id;
		}

		public TransactionDTO getTransactionDTO() {
			return transactionDTO;
		}

		public Long getVersion() {
			return version;
		}
	}

	public static class UnresolvableOptimisticLockingConflict extends RecordServicesException {

		public UnresolvableOptimisticLockingConflict(Exception e) {
			super(e);
		}
	}

	public static class ValidationException extends RecordServicesException {

		private final ValidationErrors errors;

		public ValidationException(Record record, ValidationErrors errors) {
			super(newMessage(record, errors));
			this.errors = errors;
		}

		private static String newMessage(Record record, ValidationErrors errors) {
			StringBuilder sb = new StringBuilder();
			sb.append("Validation of record '");
			sb.append(record.getId());
			sb.append("' of type '");
			sb.append(record.getSchemaCode());
			sb.append("' failed. : \n\nValidation errors :\n");
			sb.append(errors.toMultilineErrorsSummaryString());
			sb.append("\n\nStack trace :");
			return sb.toString();
		}

		public ValidationErrors getErrors() {
			return errors;
		}

	}

}
