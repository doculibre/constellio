package com.constellio.model.services.schemas.impacts;

import com.constellio.model.entities.batchprocess.BatchProcessAction;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.TransactionRecordsReindexation;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordProvider;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

public class SchemaTypeAlterationBatchProcessAction implements BatchProcessAction {

	List<String> reindexedMetadataForSearch = new ArrayList<>();
	List<String> convertedToSingleValue = new ArrayList<>();
	List<String> convertedToMultiValue = new ArrayList<>();

	public SchemaTypeAlterationBatchProcessAction(List<String> reindexedMetadataForSearch,
												  List<String> convertedToSingleValue,
												  List<String> convertedToMultiValue) {
		this.convertedToSingleValue = convertedToSingleValue;
		this.convertedToMultiValue = convertedToMultiValue;
		this.reindexedMetadataForSearch = reindexedMetadataForSearch;
	}

	@Override
	public Transaction execute(List<Record> batch, User user, MetadataSchemaTypes schemaTypes,
							   RecordProvider recordProvider, ModelLayerFactory modelLayerFactory) {
		Transaction transaction = new Transaction();
		transaction.getRecordUpdateOptions().setForcedReindexationOfMetadatas(TransactionRecordsReindexation.ALL());
		transaction.getRecordUpdateOptions().setFullRewrite(true);

		for (Record record : batch) {
			MetadataSchema schema = schemaTypes.getSchemaOf(record);
			for (Metadata metadata : schema.getMetadatas()) {
				if (convertedToMultiValue.contains(metadata.getLocalCode())) {
					Metadata singleValueMetadata = Schemas.dummySingleValueMetadata(metadata);
					Object value = record.get(singleValueMetadata);
					if (value != null) {
						record.removeAllFieldsStartingWith(metadata.getLocalCode() + "_");
						record.set(metadata, asList(value));
					}

				} else if (convertedToSingleValue.contains(metadata.getLocalCode())) {
					Metadata multiValueMetadata = Schemas.dummyMultiValueMetadata(metadata);
					List<Object> values = record.getList(multiValueMetadata);
					if (!values.isEmpty()) {
						record.removeAllFieldsStartingWith(metadata.getLocalCode() + "_");
						record.set(metadata, values.get(0));

					}
				}
				if (reindexedMetadataForSearch.contains(metadata.getLocalCode())) {
					record.markAsModified(metadata);

				}
			}

			transaction.add(record);
		}

		return transaction;
	}

	@Override
	public Object[] getInstanceParameters() {
		return new Object[]{reindexedMetadataForSearch, convertedToSingleValue, convertedToMultiValue};
	}
}
