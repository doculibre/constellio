package com.constellio.model.entities.records;

import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.search.query.logical.condition.SchemaFilters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ConditionnedActionExecutorInBatchBuilder {

	String actionName = null;
	int batchSize = 1000;
	ModelLayerFactory modelLayerFactory;
	RecordUpdateOptions options;
	LogicalSearchCondition condition;

	public ConditionnedActionExecutorInBatchBuilder(ModelLayerFactory modelLayerFactory,
													LogicalSearchCondition condition) {
		this.modelLayerFactory = modelLayerFactory;
		this.condition = condition;
		this.options = new RecordUpdateOptions()
				.setSkipMaskedMetadataValidations(true).setUnicityValidationsEnabled(false)
				.setSkippingReferenceToLogicallyDeletedValidation(true).setSkipUSRMetadatasRequirementValidations(true)
				.setCatchExtensionsExceptions(true).setCatchExtensionsValidationsErrors(true).setCatchBrokenReferenceErrors(true)
				.setOverwriteModificationDateAndUser(false);
	}

	public RecordUpdateOptions getOptions() {
		return options;
	}

	public ConditionnedActionExecutorInBatchBuilder setOptions(RecordUpdateOptions options) {
		this.options = options;
		return this;
	}

	public int getBatchSize() {
		return batchSize;
	}

	public ConditionnedActionExecutorInBatchBuilder setBatchSize(int batchSize) {
		this.batchSize = batchSize;
		return this;
	}

	public String getActionName() {
		return actionName;
	}

	public ConditionnedActionExecutorInBatchBuilder setActionName(String actionName) {
		this.actionName = actionName;
		return this;
	}

	public void settingMetadatasWithoutImpactHandling(final Map<String, Object> values) {
		settingMetadatas(values, false);
	}

	public void settingMetadatasWithImpactHandling(final Map<String, Object> values) {
		settingMetadatas(values, true);
	}

	public void settingMetadatas(final Map<String, Object> values, final boolean handlingImapcts) {
		if (actionName == null) {
			List<String> metadataCodes = new ArrayList<>(values.keySet());

			Collections.sort(metadataCodes);
			actionName = "Modifying metadatas " + metadataCodes;

			if (condition != null) {
				if (condition.getFilters() instanceof SchemaFilters) {
					SchemaFilters filters = (SchemaFilters) condition.getFilters();
					if (filters.getSchemaTypeFilter() != null) {
						actionName += actionName + " on some records of schema type " + filters.getSchemaTypeFilter().getCode();
					} else if (filters.getSchemaFilter() != null) {
						actionName += actionName + " on some records of schema " + filters.getSchemaFilter().getCode();
					}
				} else {
					actionName += actionName + " on some records of collection " + condition.getCollection();
				}
			}

		}

		final MetadataSchemaTypes types = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(condition.getCollection());
		final RecordServices recordServices = modelLayerFactory.newRecordServices();
		try {
			new ActionExecutorInBatch(modelLayerFactory.newSearchServices(), actionName, batchSize) {

				@Override
				public void doActionOnBatch(List<Record> records)
						throws Exception {

					Transaction transaction = new Transaction();

					transaction.setOptions(options);

					for (Record record : records) {
						MetadataSchema schema = types.getSchemaOf(record);
						for (Map.Entry<String, Object> replacedMetadata : values.entrySet()) {
							Metadata metadata = schema.get(replacedMetadata.getKey());
							record.set(metadata, replacedMetadata.getValue());
						}

						transaction.add(record);
					}

					if (handlingImapcts) {
						recordServices.execute(transaction);
					} else {
						recordServices.executeWithoutImpactHandling(transaction);
					}
				}
			}.execute(condition);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	public interface RecordScript {
		void modifyRecord(Record record);
	}

	public void modifyingRecordsWithImpactHandling(RecordScript script) {
		modifyingRecords(script, true);
	}

	public void modifyingRecordsWithoutImpactHandling(RecordScript script) {
		modifyingRecords(script, false);
	}

	public void modifyingRecords(final RecordScript script, final boolean handlingImapcts) {
		if (actionName == null) {
			actionName = "Modifying ";

			if (condition != null) {
				if (condition.getFilters() instanceof SchemaFilters) {
					SchemaFilters filters = (SchemaFilters) condition.getFilters();
					if (filters.getSchemaTypeFilter() != null) {
						actionName += actionName + " some records of schema type " + filters.getSchemaTypeFilter().getCode();
					} else if (filters.getSchemaFilter() != null) {
						actionName += actionName + " some records of schema " + filters.getSchemaFilter().getCode();
					}
				} else {
					actionName += actionName + " some records of collection " + condition.getCollection();
				}
			}

		}

		final MetadataSchemaTypes types = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(condition.getCollection());
		final RecordServices recordServices = modelLayerFactory.newRecordServices();
		try {
			new ActionExecutorInBatch(modelLayerFactory.newSearchServices(), actionName, batchSize) {

				@Override
				public void doActionOnBatch(List<Record> records)
						throws Exception {

					Transaction transaction = new Transaction();

					transaction.setOptions(options);

					for (Record record : records) {
						script.modifyRecord(record);
						transaction.add(record);
					}

					if (handlingImapcts) {
						recordServices.execute(transaction);
					} else {
						recordServices.executeWithoutImpactHandling(transaction);
					}
				}
			}.execute(condition);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}
}
