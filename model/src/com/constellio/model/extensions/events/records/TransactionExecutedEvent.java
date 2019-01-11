package com.constellio.model.extensions.events.records;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.schemas.MetadataList;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class TransactionExecutedEvent {

	Transaction transaction;


	List<Record> newRecords;
	List<Record> updatedRecords;
	Map<String, MetadataList> modifiedMetadatasOfModifiedRecords;

	public TransactionExecutedEvent(Transaction transaction, List<Record> newRecords, List<Record> modifiedRecords,
									Map<String, MetadataList> modifiedMetadatasOfModifiedRecords) {
		this.transaction = transaction;
		this.newRecords = Collections.unmodifiableList(newRecords);
		this.updatedRecords = Collections.unmodifiableList(modifiedRecords);
		this.modifiedMetadatasOfModifiedRecords = modifiedMetadatasOfModifiedRecords;
	}

	public List<Record> getNewRecords() {
		return newRecords;
	}

	public List<Record> getUpdatedRecords() {
		return updatedRecords;
	}

	public MetadataList getModifiedMetadataListOf(Record record) {
		return modifiedMetadatasOfModifiedRecords.get(record.getId());
	}

	public boolean isOnlySchemaType(String schemaType) {
		for (Record record : transaction.getRecords()) {
			if (!record.getTypeCode().equals(schemaType)) {
				return false;
			}
		}
		return !transaction.getRecords().isEmpty();
	}

	public Transaction getTransaction() {
		return transaction;
	}

	public boolean isNewRecordImport() {

		for (Record record : transaction.getModifiedRecords()) {
			if (!record.isSaved() && record.get(Schemas.LEGACY_ID) != null) {
				return true;
			}
		}

		return false;
	}
}
