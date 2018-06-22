package com.constellio.model.services.batch.actions;

import static com.constellio.model.services.records.RecordUtils.changeSchemaTypeAccordingToTypeLinkedSchema;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.constellio.model.entities.batchprocess.BatchProcessAction;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.extensions.params.BatchProcessingSpecialCaseParams;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordProvider;
import com.constellio.model.services.schemas.SchemaUtils;
import com.sun.star.xforms.Model;

public class ChangeValueOfMetadataBatchProcessAction implements BatchProcessAction {
	final Map<String, Object> metadataChangedValues;
	private ModelLayerFactory modelLayerFactory;

	public ChangeValueOfMetadataBatchProcessAction(Map<String, Object> metadataChangedValues) {
		this.metadataChangedValues = metadataChangedValues;
	}

	@Override
	public Transaction execute(List<Record> batch, MetadataSchemaTypes schemaTypes, RecordProvider recordProvider, ModelLayerFactory modelLayerFactory) {
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
					modelLayerFactory.getExtensions().forCollection(schemaTypes.getCollection()).batchProcessingSpecialCaseExtensions(new BatchProcessingSpecialCaseParams(record, metadata));
					if (schemaTypes.isRecordTypeMetadata(metadata)) {
						changeSchemaTypeAccordingToTypeLinkedSchema(record, schemaTypes, recordProvider, metadata);
						schemaCode = record.getSchemaCode();
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
