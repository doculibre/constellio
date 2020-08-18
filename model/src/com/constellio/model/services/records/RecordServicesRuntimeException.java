package com.constellio.model.services.records;

import com.constellio.data.dao.dto.records.TransactionDTO;
import com.constellio.data.dao.services.bigVault.RecordDaoException.OptimisticLocking;
import com.constellio.data.utils.LoggerUtils;
import com.constellio.model.entities.schemas.Metadata;

@SuppressWarnings("serial")
public class RecordServicesRuntimeException extends RuntimeException {

	public RecordServicesRuntimeException() {
	}

	public RecordServicesRuntimeException(String message) {
		super(message);
	}

	public RecordServicesRuntimeException(Throwable cause) {
		super(cause);
	}

	public RecordServicesRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public static class CannotAddRecord extends RecordServicesRuntimeException {

		public CannotAddRecord(String recordId, Exception e) {
			super("Cannot add record. Record id : " + recordId + ". Other changes may have been committed.", e);
		}

		public CannotAddRecord(String recordId) {
			this(recordId, null);
		}
	}

	public static class NoSuchRecordWithId extends RecordServicesRuntimeException {

		public NoSuchRecordWithId(String id, String dataStore, Exception e) {
			super("No such record with id : '" + id + "' in datastore '" + dataStore + "'", e);
		}
	}

	public static class SchemaTypeOfARecordHasReadOnlyLock extends RecordServicesRuntimeException {

		public SchemaTypeOfARecordHasReadOnlyLock(String schemaType, String id) {
			super("Schema type '" + schemaType + "' of record '" + id + "' has read only locked");
		}
	}

	public static class BrokenReference extends RecordServicesRuntimeException {

		public BrokenReference(String recordIdWithReference, String referencedId, Metadata metadata,
							   Exception e) {
			super("Record '" + recordIdWithReference + "' is referencing an inexistent record '" + referencedId
				  + "' of schema type '" + metadata.getReferencedSchemaTypeCode() + "' in metadata '" + metadata.getLocalCode()
				  + "'", e);
		}
	}

	public static class CannotSetIdsToReindexInEmptyTransaction extends RecordServicesRuntimeException {

		public CannotSetIdsToReindexInEmptyTransaction() {
			super("Cannot set ids to reindex in an empty transaction");
		}
	}

	public static class IdAlreadyExisting extends RecordServicesRuntimeException {

		public IdAlreadyExisting(String id) {
			super("The transaction is adding a record with id '" + id + "', which is already used for another record.");
		}
	}

	public static class NoSuchRecordWithMetadataValue extends RecordServicesRuntimeException {

		public NoSuchRecordWithMetadataValue(String metadataCode, String metadataValue) {
			super("No such record with value '" + metadataValue + "' for metadata '" + metadataCode + "'");
		}
	}

	public static class UserCannotReadDocument extends RecordServicesRuntimeException {

		public UserCannotReadDocument(String id, String username) {
			super("User " + username + " is not authorized to read document " + id + ".");
		}
	}

	public static class UnresolvableOptimsiticLockingCausingInfiniteLoops extends RecordServicesRuntimeException {

		public UnresolvableOptimsiticLockingCausingInfiniteLoops(TransactionDTO transaction, OptimisticLocking e) {
			super("Transaction is causing unresolvable optimistic locking (causing an infinite loop) : " +
				  LoggerUtils.toString(transaction), e);
		}
	}

	public static class RecordIsNotAPrincipalConcept extends RecordServicesRuntimeException {

		public RecordIsNotAPrincipalConcept(String id) {
			super("Record is not a principal concept : " + id);
		}
	}

	//	public static class NewReferenceToOtherLogicallyDeletedRecord extends RecordServicesRuntimeException {
	//
	//		String id;
	//
	//		public NewReferenceToOtherLogicallyDeletedRecord(String id, Exception e) {
	//			super("Record cannot have a new reference to a logically deleted record : " + id, e);
	//			this.id = id.startsWith("idx_act_") ? id.substring(8) : id;
	//		}
	//
	//		public String getId() {
	//			return id;
	//		}
	//	}

	public static class RecordServicesRuntimeException_CannotLogicallyDeleteRecord extends RecordServicesRuntimeException {

		public RecordServicesRuntimeException_CannotLogicallyDeleteRecord(String recordId, Exception e) {
			super("Cannot logically delete record '" + recordId + "'", e);
		}

		public RecordServicesRuntimeException_CannotLogicallyDeleteRecord(String message) {
			super(message);
		}
	}

	public static class RecordServicesRuntimeException_CannotDelayFlushingOfRecordsInCache
			extends RecordServicesRuntimeException {

		public RecordServicesRuntimeException_CannotDelayFlushingOfRecordsInCache(String type, String recordId) {
			super("Cannot delay the flushing of a record of type '" + type + "' that may be in a cache : '" + recordId + "'");
		}

	}

	public static class RecordServicesRuntimeException_CannotPhysicallyDeleteRecord extends RecordServicesRuntimeException {

		public RecordServicesRuntimeException_CannotPhysicallyDeleteRecord(String recordId, Exception e) {
			super("Cannot physically delete record '" + recordId + "'", e);
		}

		public RecordServicesRuntimeException_CannotPhysicallyDeleteRecord(String message) {
			super(message);
		}
	}

	public static class RecordServicesRuntimeException_CannotRestoreRecord extends RecordServicesRuntimeException {

		public RecordServicesRuntimeException_CannotRestoreRecord(String recordId, Exception e) {
			super("Cannot restore record '" + recordId + "'", e);
		}

		public RecordServicesRuntimeException_CannotRestoreRecord(String message) {
			super(message);
		}
	}

	public static class RecordServicesRuntimeException_TransactionWithMoreThan1000RecordsCannotHaveTryMergeOptimisticLockingResolution
			extends RecordServicesRuntimeException {

		public RecordServicesRuntimeException_TransactionWithMoreThan1000RecordsCannotHaveTryMergeOptimisticLockingResolution() {
			super("Transaction with more than 1000 records cannot have try merge optimistic locking resolution");
		}
	}

	public static class RecordServicesRuntimeException_TransactionHasMoreThan100000Records
			extends RecordServicesRuntimeException {

		public RecordServicesRuntimeException_TransactionHasMoreThan100000Records(int length) {
			super("Transaction has " + length + " records. Limit is 100000");
		}
	}

	public static class RecordServicesRuntimeException_RecordsFlushingFailed
			extends RecordServicesRuntimeException {

		public RecordServicesRuntimeException_RecordsFlushingFailed(Exception e) {
			super("Records flushing failed", e);
		}
	}

	public static class RecordServicesRuntimeException_CalculatorIsUsingAnForbiddenMetadata
			extends RecordServicesRuntimeException {

		public RecordServicesRuntimeException_CalculatorIsUsingAnForbiddenMetadata(String calculator, String metadata) {
			super("Calculator '" + calculator + "' is using a fobidden metadata : " + metadata);
		}
	}

	public static class RecordServicesRuntimeException_ExceptionWhileCalculating
			extends RecordServicesRuntimeException {

		public RecordServicesRuntimeException_ExceptionWhileCalculating(String recordId, Metadata metadata,
																		Exception e) {
			super("Exception while calculating metadata '" + metadata.getCode() + "' of record'" + recordId + "'. Verify that the record is valid.", e);
		}
	}
}
