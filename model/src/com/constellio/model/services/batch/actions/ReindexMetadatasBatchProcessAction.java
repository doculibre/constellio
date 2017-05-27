package com.constellio.model.services.batch.actions;

import java.util.ArrayList;
import java.util.List;

import com.constellio.model.entities.batchprocess.BatchProcessAction;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.TransactionRecordsReindexation;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.records.RecordProvider;

public class ReindexMetadatasBatchProcessAction implements BatchProcessAction {

	final List<String> reindexedMetadataCodes;

	public ReindexMetadatasBatchProcessAction(List<String> reindexedMetadataCodes) {
		this.reindexedMetadataCodes = reindexedMetadataCodes;
	}

	public static ReindexMetadatasBatchProcessAction forMetadatas(List<Metadata> metadataToReindex) {
		List<String> codes = new ArrayList<>();
		for (Metadata metadata : metadataToReindex) {
			codes.add(metadata.getCode());
		}
		return new ReindexMetadatasBatchProcessAction(codes);
	}

	public static ReindexMetadatasBatchProcessAction allMetadatas() {
		return new ReindexMetadatasBatchProcessAction(null);
	}

	@Override
	public Transaction execute(List<Record> batch, MetadataSchemaTypes schemaTypes, RecordProvider recordProvider) {
		Transaction transaction = new Transaction();
		if (reindexedMetadataCodes == null) {
			transaction.getRecordUpdateOptions().setForcedReindexationOfMetadatas(TransactionRecordsReindexation.ALL());
		} else {
			transaction.getRecordUpdateOptions().setForcedReindexationOfMetadatas(
					new TransactionRecordsReindexation(schemaTypes.getMetadatas(reindexedMetadataCodes)));
		}
		transaction.setSkippingReferenceToLogicallyDeletedValidation(true);
		transaction.setSkippingRequiredValuesValidation(true);
		transaction.addUpdate(batch);
		return transaction;
	}

	@Override
	public Object[] getInstanceParameters() {
		return new Object[] { reindexedMetadataCodes };
	}
}
