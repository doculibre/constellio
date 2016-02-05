package com.constellio.model.services.batch.actions;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.constellio.model.entities.batchprocess.BatchProcessAction;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.schemas.SchemaUtils;

public class ChangeValueOfMetadataBatchProcessAction implements BatchProcessAction {
	final Map<String, Object> metadataChangedValues;

	public ChangeValueOfMetadataBatchProcessAction(Map<String, Object> metadataChangedValues) {
		this.metadataChangedValues = metadataChangedValues;
	}

	@Override
	public Transaction execute(List<Record> batch, MetadataSchemaTypes schemaTypes) {
		SchemaUtils utils = new SchemaUtils();
		Transaction transaction = new Transaction().setSkippingRequiredValuesValidation(true);
		for (Record record : batch) {
			String schemaCode = record.getSchemaCode();
			for (Entry<String, Object> entry : metadataChangedValues.entrySet()) {
				String metadataCode = entry.getKey();
				if (metadataCode.startsWith(utils.getSchemaTypeCode(schemaCode))) {
					if (!metadataCode.startsWith(schemaCode + "_")) {
						metadataCode = schemaCode + "_" + utils.getLocalCodeFromMetadataCode(metadataCode);
					}
					record.set(schemaTypes.getMetadata(metadataCode), entry.getValue());
				}
			}
		}
		transaction.addUpdate(batch);
		return transaction;
	}

	@Override
	public Object[] getInstanceParameters() {
		return new Object[] { metadataChangedValues };
	}
}
