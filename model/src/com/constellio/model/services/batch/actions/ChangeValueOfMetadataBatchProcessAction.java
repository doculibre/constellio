package com.constellio.model.services.batch.actions;

import static com.constellio.model.entities.schemas.MetadataValueType.REFERENCE;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.constellio.model.entities.batchprocess.BatchProcessAction;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.records.RecordProvider;
import com.constellio.model.services.schemas.SchemaUtils;

public class ChangeValueOfMetadataBatchProcessAction implements BatchProcessAction {
	final Map<String, Object> metadataChangedValues;

	public ChangeValueOfMetadataBatchProcessAction(Map<String, Object> metadataChangedValues) {
		this.metadataChangedValues = metadataChangedValues;
	}

	@Override
	public Transaction execute(List<Record> batch, MetadataSchemaTypes schemaTypes, RecordProvider recordProvider) {
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

					Metadata metadata = schemaTypes.getMetadata(metadataCode);

					record.set(metadata, entry.getValue());
					if (metadata.getCode().equals("type") || metadata.getType() == REFERENCE) {
						MetadataSchema referencedSchema = schemaTypes.getSchemaType(metadata
								.getAllowedReferences().getTypeWithAllowedSchemas()).getDefaultSchema();
						if (referencedSchema.hasMetadataWithCode("linkedSchema")) {
							String schemaTypeCode = new SchemaUtils().getSchemaTypeCode(record.getSchemaCode());
							String typeId = (String) entry.getValue();
							String customSchema = null;
							if (typeId != null) {

								Record typeRecord = recordProvider.getRecord(typeId);
								customSchema = typeRecord.get(referencedSchema.get("linkedSchema"));
							}

							String newSchemaCode = schemaTypeCode + "_" + (customSchema == null ? "default" : customSchema);
							if (!record.getSchemaCode().equals(newSchemaCode)) {
								MetadataSchema currentSchema = schemaTypes.getSchema(record.getSchemaCode());
								MetadataSchema newSchema = schemaTypes.getSchema(newSchemaCode);
								record.changeSchema(currentSchema, newSchema);
							}
						}

					}
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
