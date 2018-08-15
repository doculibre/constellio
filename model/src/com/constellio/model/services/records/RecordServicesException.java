package com.constellio.model.services.records;

import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.dto.records.RecordDeltaDTO;
import com.constellio.data.dao.dto.records.TransactionDTO;
import com.constellio.data.dao.services.bigVault.RecordDaoException;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.frameworks.validation.ValidationErrors;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
			super(getMessage(e.getId(), e.getVersion(), transactionDTO), e);
			this.transactionDTO = transactionDTO;
			this.id = e.getId();
			this.version = e.getVersion();
		}

		public OptimisticLocking(String id, TransactionDTO transactionDTO, Throwable cause) {
			super(getMessage(id, null, transactionDTO), cause);
			this.transactionDTO = transactionDTO;
			this.id = id;
			this.version = null;
		}

		private static String getMessage(String id, Long version, TransactionDTO transactionDTO) {
			return "Optimistic locking while saving record with id '" + id + "' in version '" + version + "'";
			//					+ "\n" + getAdvancedMessage(id, transactionDTO);
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

		private static String getAdvancedMessage(String id, TransactionDTO transactionDTO) {
			StringBuilder stringBuilder = new StringBuilder();
			List<RecordDTO> newRecords = transactionDTO.getNewRecords();
			List<RecordDTO> deletedRecords = transactionDTO.getDeletedRecords();
			List<RecordDeltaDTO> modifiedRecords = transactionDTO.getModifiedRecords();
			for (RecordDTO record : newRecords) {
				if (id.equals(record.getId())) {
					Map<String, Object> fields = record.getFields();
					Iterator<Map.Entry<String, Object>> fieldIterator = fields.entrySet().iterator();
					while (fieldIterator.hasNext()) {
						Map.Entry<String, Object> next = fieldIterator.next();
						try {
							if (next != null && next.getKey() != null && next.getValue() != null) {
								stringBuilder.append("Version " + record.getVersion() + " in new records with metadata: " + next.getKey() + " and value: " + next.getValue().toString() + "\n");
							}
						} catch (Exception e) {
						}
					}
					stringBuilder.append("\n");
				}
			}
			stringBuilder.append("\n");

			for (RecordDTO record : deletedRecords) {
				if (id.equals(record.getId())) {
					Map<String, Object> fields = record.getFields();
					Iterator<Map.Entry<String, Object>> fieldIterator = fields.entrySet().iterator();
					while (fieldIterator.hasNext()) {
						Map.Entry<String, Object> next = fieldIterator.next();
						try {
							if (next != null && next.getKey() != null && next.getValue() != null) {
								stringBuilder.append("Version " + record.getVersion() + " in deleted records with metadata: " + next.getKey() + " and value: " + next.getValue().toString() + "\n");
							}
						} catch (Exception e) {
						}
					}
					stringBuilder.append("\n");
				}
			}
			stringBuilder.append("\n");

			for (RecordDeltaDTO record : modifiedRecords) {
				if (id.equals(record.getId())) {
					Map<String, Object> initialFields = record.getInitialFields();
					Map<String, Object> modifiedFields = record.getModifiedFields();
					Iterator<Map.Entry<String, Object>> initialFieldIterator = initialFields.entrySet().iterator();
					Iterator<Map.Entry<String, Object>> modifiedFieldIterator = initialFields.entrySet().iterator();
					Set<String> modifiedFieldsAlreadyFound = new HashSet<>();
					while (initialFieldIterator.hasNext()) {
						Map.Entry<String, Object> next = initialFieldIterator.next();
						try {
							if (next != null && next.getKey() != null && next.getValue() != null) {
								modifiedFieldsAlreadyFound.add(next.getKey());
								Object modifiedValue = modifiedFields.get(next.getKey());
								if (modifiedValue != null) {
									stringBuilder.append("From version " + record.getFromVersion() + " in modified records with metadata: " + next.getKey() + " and old value: " + next.getValue().toString() +
														 " and new value: " + modifiedValue.toString() + "\n");
								} else {
									stringBuilder.append("From version " + record.getFromVersion() + " in modified records with metadata: " + next.getKey() + " and old value: " + next.getValue().toString() + "\n");
								}
							}
						} catch (Exception e) {
						}
					}

					while (modifiedFieldIterator.hasNext()) {
						Map.Entry<String, Object> next = modifiedFieldIterator.next();
						try {
							if (next != null && next.getKey() != null && next.getValue() != null && !modifiedFieldsAlreadyFound.contains(next.getKey())) {
								modifiedFieldsAlreadyFound.add(next.getKey());
								stringBuilder.append("From version " + record.getFromVersion() + " in modified records with metadata: " + next.getKey() + " and new value: " + next.getValue().toString() + "\n");
							}
						} catch (Exception e) {
						}
					}
					stringBuilder.append("\n");
				}
			}
			stringBuilder.append("\n");

			return stringBuilder.toString();
		}
	}

	public static class UnresolvableOptimisticLockingConflict extends RecordServicesException {

		public UnresolvableOptimisticLockingConflict(Exception e) {
			super("Unresolvable optimistic locking", e);
		}

		public UnresolvableOptimisticLockingConflict(String id) {
			super("Unresolvable optimistic locking caused by id " + id);
		}
	}

	public static class ValidationException extends RecordServicesException {

		private final ValidationErrors errors;

		public ValidationException(Transaction transaction, ValidationErrors errors) {
			super(newMessage(transaction, errors));
			this.errors = errors;
		}

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

		private static String newMessage(Transaction transaction, ValidationErrors errors) {
			StringBuilder sb = new StringBuilder();
			sb.append("Validation of transaction failed. : \n\nValidation errors :\n");
			sb.append(errors.toMultilineErrorsSummaryString());
			sb.append("\n\nStack trace :");
			return sb.toString();
		}

		public ValidationErrors getErrors() {
			return errors;
		}

	}

}
