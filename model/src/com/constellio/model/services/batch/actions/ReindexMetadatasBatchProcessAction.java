package com.constellio.model.services.batch.actions;

import java.util.ArrayList;
import java.util.List;

import com.constellio.model.entities.batchprocess.BatchProcessAction;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.TransactionRecordsReindexation;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.schemas.MetadataList;

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

	@Override
	public Transaction execute(List<Record> batch, MetadataSchemaTypes schemaTypes) {
		Transaction transaction = new Transaction();
		MetadataList reindexedMetadatas = schemaTypes.getMetadatas(reindexedMetadataCodes);
		transaction.getRecordUpdateOptions().forceReindexationOfMetadatas(new TransactionRecordsReindexation(reindexedMetadatas));
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
