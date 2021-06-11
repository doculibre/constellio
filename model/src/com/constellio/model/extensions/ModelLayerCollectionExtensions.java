package com.constellio.model.extensions;

import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;
import com.constellio.data.frameworks.extensions.ExtensionUtils.BooleanCaller;
import com.constellio.data.frameworks.extensions.VaultBehaviorsList;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.RecordUpdateOptions;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.extensions.behaviors.BatchProcessingSpecialCaseExtension;
import com.constellio.model.extensions.behaviors.RecordExtension;
import com.constellio.model.extensions.behaviors.RecordExtension.IsRecordModifiableByParams;
import com.constellio.model.extensions.behaviors.RecordImportExtension;
import com.constellio.model.extensions.behaviors.SchemaExtension;
import com.constellio.model.extensions.behaviors.TaxonomyExtension;
import com.constellio.model.extensions.behaviors.ValueListsPageExtension;
import com.constellio.model.extensions.events.records.RecordCreationEvent;
import com.constellio.model.extensions.events.records.RecordInCreationBeforeSaveEvent;
import com.constellio.model.extensions.events.records.RecordInCreationBeforeValidationAndAutomaticValuesCalculationEvent;
import com.constellio.model.extensions.events.records.RecordInModificationBeforeSaveEvent;
import com.constellio.model.extensions.events.records.RecordInModificationBeforeValidationAndAutomaticValuesCalculationEvent;
import com.constellio.model.extensions.events.records.RecordLogicalDeletionEvent;
import com.constellio.model.extensions.events.records.RecordLogicalDeletionValidationEvent;
import com.constellio.model.extensions.events.records.RecordModificationEvent;
import com.constellio.model.extensions.events.records.RecordPhysicalDeletionEvent;
import com.constellio.model.extensions.events.records.RecordPhysicalDeletionValidationEvent;
import com.constellio.model.extensions.events.records.RecordReindexationEvent;
import com.constellio.model.extensions.events.records.RecordRestorationEvent;
import com.constellio.model.extensions.events.records.RecordSetCategoryEvent;
import com.constellio.model.extensions.events.records.TransactionExecutedEvent;
import com.constellio.model.extensions.events.records.TransactionExecutionBeforeSaveEvent;
import com.constellio.model.extensions.events.recordsImport.BuildParams;
import com.constellio.model.extensions.events.recordsImport.PrevalidationParams;
import com.constellio.model.extensions.events.recordsImport.ValidationParams;
import com.constellio.model.extensions.events.schemas.PreparePhysicalDeleteFromTrashParams;
import com.constellio.model.extensions.events.schemas.SchemaEvent;
import com.constellio.model.extensions.events.schemas.SearchFieldPopulatorParams;
import com.constellio.model.extensions.params.BatchProcessingSpecialCaseParams;
import com.constellio.model.extensions.params.GetCaptionForRecordParams;
import com.constellio.model.extensions.params.ValueListExtensionParams;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class ModelLayerCollectionExtensions {

	private static final Logger LOGGER = LoggerFactory.getLogger(ModelLayerCollectionExtensions.class);

	ModelLayerSystemExtensions systemExtensions;

	//------------ Extension points -----------

	public VaultBehaviorsList<RecordImportExtension> recordImportExtensions = new VaultBehaviorsList<>();

	public VaultBehaviorsList<RecordExtension> recordExtensions;

	public VaultBehaviorsList<SchemaExtension> schemaExtensions = new VaultBehaviorsList<>();

	public VaultBehaviorsList<BatchProcessingSpecialCaseExtension> batchProcessingSpecialCaseExtensions = new VaultBehaviorsList<>();

	public VaultBehaviorsList<ValueListsPageExtension> valueListsPageExtensions = new VaultBehaviorsList<>();

	public ModelLayerCollectionExtensions(ModelLayerSystemExtensions systemExtensions) {
		this.systemExtensions = systemExtensions;
		this.recordExtensions = new VaultBehaviorsList<>(systemExtensions.recordExtensions);
	}

	public VaultBehaviorsList<TaxonomyExtension> taxonomyExtensions = new VaultBehaviorsList<>();

	//----------------- Callers ---------------

	public void callSetRecordCategory(RecordSetCategoryEvent event) {
		for (RecordExtension extension : recordExtensions) {
			extension.setRecordCategory(event);
		}
	}

	public void callRecordImportBuild(String schemaType, BuildParams params) {
		for (RecordImportExtension extension : recordImportExtensions) {
			if (extension.getDecoratedSchemaType().equals(schemaType)) {
				extension.build(params);
			}
		}
	}

	public void callRecordImportValidate(String schemaType, ValidationParams params) {
		for (RecordImportExtension extension : recordImportExtensions) {
			if (extension.getDecoratedSchemaType().equals(schemaType)) {
				extension.validate(params);
			}
		}
	}

	public void callRecordImportPrevalidate(String schemaType, PrevalidationParams params) {
		for (RecordImportExtension extension : recordImportExtensions) {
			if (extension.getDecoratedSchemaType().equals(schemaType)) {
				extension.prevalidate(params);
			}
		}
	}

	public boolean isSkipValidation(String schemaType, ValidationParams params) {
		return recordImportExtensions.getBooleanValue(false, behavior -> {
			if (behavior.getDecoratedSchemaType().equals(schemaType)) {
				return behavior.skipValidation(params);
			}
			return ExtensionBooleanResult.NOT_APPLICABLE;
		});
	}

	public boolean isSkipPrevalidation(String schemaType, PrevalidationParams params) {
		return recordImportExtensions.getBooleanValue(false, behavior -> {
			if (behavior.getDecoratedSchemaType().equals(schemaType)) {
				return behavior.skipPrevalidation(params);
			}

			return ExtensionBooleanResult.NOT_APPLICABLE;
		});
	}

	public boolean isRecordTitleShouldBeCalculatedFromContent(Record record) {
		return schemaExtensions.getExtensions().stream()
				.allMatch(schemaExtension ->
						schemaExtension.isRecordTitleShouldBeCalculatedFromContent(record));
	}

	public void callTransactionExecutionBeforeSave(TransactionExecutionBeforeSaveEvent event,
												   RecordUpdateOptions options) {
		for (RecordExtension extension : recordExtensions) {
			try {
				extension.transactionExecutionBeforeSave(event);

			} catch (RuntimeException e) {
				if (options.isCatchExtensionsExceptions()) {
					LOGGER.warn("Exception while calling extension of class '" + extension.getClass().getName()
								+ "' on transaction ", e);
				} else {
					throw e;
				}
			}
		}
	}


	public void callRecordInCreationBeforeSave(RecordInCreationBeforeSaveEvent event, RecordUpdateOptions options) {
		for (RecordExtension extension : recordExtensions) {
			try {
				extension.recordInCreationBeforeSave(event);

			} catch (RuntimeException e) {
				handleException(e, event.getRecord().getId(), extension.getClass().getName(), options);
			}
		}
	}

	public void callRecordInModificationBeforeSave(RecordInModificationBeforeSaveEvent event,
												   RecordUpdateOptions options) {
		for (RecordExtension extension : recordExtensions) {
			try {
				extension.recordInModificationBeforeSave(event);
			} catch (RuntimeException e) {
				handleException(e, event.getRecord().getId(), extension.getClass().getName(), options);
			}
		}
	}

	public void callRecordInCreationBeforeValidationAndAutomaticValuesCalculation(
			RecordInCreationBeforeValidationAndAutomaticValuesCalculationEvent event, RecordUpdateOptions options) {

		for (RecordExtension extension : recordExtensions) {
			try {
				extension.recordInCreationBeforeValidationAndAutomaticValuesCalculation(event);

			} catch (RuntimeException e) {
				handleException(e, event.getRecord().getId(), extension.getClass().getName(), options);
			}
		}

	}

	public static void handleException(RuntimeException e, String recordId, String extensionClassname,
									   RecordUpdateOptions options) {
		if (options.isCatchExtensionsExceptions()) {
			if (recordId == null) {
				e.printStackTrace();
				LOGGER.warn("Exception while calling extension of class '" + extensionClassname + "'", e);
			} else {
				e.printStackTrace();
				LOGGER.warn("Exception while calling extension of class '" + extensionClassname + "' on record " + recordId, e);
			}
		} else {
			throw e;
		}
	}

	public void callRecordInModificationBeforeValidationAndAutomaticValuesCalculation(
			RecordInModificationBeforeValidationAndAutomaticValuesCalculationEvent event, RecordUpdateOptions options) {
		for (RecordExtension extension : recordExtensions) {
			try {
				extension.recordInModificationBeforeValidationAndAutomaticValuesCalculation(event);
			} catch (RuntimeException e) {
				handleException(e, event.getRecord().getId(), extension.getClass().getName(), options);
			}
		}
	}

	public void callRecordCreated(RecordCreationEvent event, RecordUpdateOptions options) {
		for (RecordExtension extension : recordExtensions) {
			try {
				extension.recordCreated(event);

			} catch (RuntimeException e) {
				handleException(e, event.getRecord().getId(), extension.getClass().getName(), options);
			}
		}
	}

	public void callRecordModified(RecordModificationEvent event, RecordUpdateOptions options) {
		for (RecordExtension extension : recordExtensions) {
			try {
				extension.recordModified(event);
			} catch (RuntimeException e) {
				handleException(e, event.getRecord().getId(), extension.getClass().getName(), options);
			}
		}
	}

	public void callTransactionExecuted(TransactionExecutedEvent event, RecordUpdateOptions options) {
		for (RecordExtension extension : recordExtensions) {
			try {
				extension.transactionExecuted(event);
			} catch (RuntimeException e) {
				handleException(e, null, extension.getClass().getName(), options);
			}
		}
	}

	public void callRecordLogicallyDeleted(RecordLogicalDeletionEvent event) {
		for (RecordExtension extension : recordExtensions) {
			extension.recordLogicallyDeleted(event);
		}
	}

	public void callRecordPhysicallyDeleted(RecordPhysicalDeletionEvent event) {
		for (RecordExtension extension : recordExtensions) {
			extension.recordPhysicallyDeleted(event);
		}
	}

	public void callRecordRestored(RecordRestorationEvent event) {
		for (RecordExtension extension : recordExtensions) {
			extension.recordRestored(event);
		}
	}

	public void callRecordReindexed(RecordReindexationEvent event) {
		for (RecordExtension extension : recordExtensions) {
			extension.recordReindexed(event);
		}
	}

	public boolean isModifyBlocked(Record record, User user) {
		boolean modifyBlocked = false;
		for (RecordExtension extension : recordExtensions) {
			modifyBlocked = extension.isModifyBlocked(record, user);
			if (modifyBlocked) {
				break;
			}
		}
		return modifyBlocked;
	}

	public ValidationErrors validateDeleteAuthorized(final Record record, final User user) {
		for (RecordExtension extension : recordExtensions.getExtensions()) {
			ValidationErrors validationErrors = extension.validateDeleteAuthorized(record, user);
			if (validationErrors != null && !validationErrors.isEmpty()) {
				return validationErrors;
			}
		}
		return new ValidationErrors();
	}

	public ValidationErrors validateLogicallyDeletable(final RecordLogicalDeletionValidationEvent event) {
		for (RecordExtension extension : recordExtensions.getExtensions()) {
			ValidationErrors validationErrors = extension.validateLogicallyDeletable(event);
			if (validationErrors != null && !validationErrors.isEmpty()) {
				return validationErrors;
			}
		}
		return new ValidationErrors();
	}

	public ValidationErrors validatePhysicallyDeletable(final RecordPhysicalDeletionValidationEvent event) {
		for (RecordExtension extension : recordExtensions.getExtensions()) {
			ValidationErrors validationErrors = extension.validatePhysicallyDeletable(event);
			if (validationErrors != null && !validationErrors.isEmpty()) {
				return validationErrors;
			}
		}
		return new ValidationErrors();
	}

	public boolean isPutInTrashBeforePhysicalDelete(final SchemaEvent event) {
		Boolean inTrashFirst = schemaExtensions.getBooleanValue(null, new BooleanCaller<SchemaExtension>() {
			@Override
			public ExtensionBooleanResult call(SchemaExtension behavior) {
				return behavior.isPutInTrashBeforePhysicalDelete(event);
			}
		});
		return (inTrashFirst == null) ? false : inTrashFirst;
	}

	public LogicalSearchCondition getPhysicallyDeletableQueryForSchemaType(final SchemaEvent event) {
		LogicalSearchCondition currentCondition = null;
		for (SchemaExtension extension : schemaExtensions) {
			LogicalSearchCondition newCondition = extension.getPhysicallyDeletableRecordsForSchemaType(event);
			if (newCondition != null) {
				if (currentCondition != null) {
					currentCondition = LogicalSearchQueryOperators.allConditions(currentCondition, newCondition);
				} else {
					currentCondition = newCondition;
				}
			}
		}
		return currentCondition;
	}

	public void preparePhysicalDeleteFromTrash(PreparePhysicalDeleteFromTrashParams params) {
		for (SchemaExtension extension : schemaExtensions) {
			extension.preparePhysicalDeleteFromTrash(params);
		}
	}

	@Deprecated
	//Use tokens instead
	public boolean isRecordModifiableBy(final Record record, final User user) {
		return recordExtensions.getBooleanValue(true, new BooleanCaller<RecordExtension>() {
			@Override
			public ExtensionBooleanResult call(RecordExtension behavior) {
				return behavior.isRecordModifiableBy(new IsRecordModifiableByParams(record, user));
			}
		});
	}

	public String getCaptionForRecord(GetCaptionForRecordParams params) {
		String caption = null;
		for (RecordExtension extension : recordExtensions) {
			caption = extension.getCaptionForRecord(params);
			if (caption != null) {
				break;
			}
		}

		if (caption == null) {
			return params.getRecord().getTitle();
		} else {
			return caption;
		}
	}

	public Collection<? extends String> getAllowedSystemReservedMetadatasForExcelReport(String schemaTypeCode) {
		Set<String> allowedMetadatas = new HashSet<>();
		for (SchemaExtension extension : schemaExtensions) {
			allowedMetadatas.addAll(extension.getAllowedSystemReservedMetadatasForExcelReport(schemaTypeCode));
		}

		return new ArrayList<>(allowedMetadatas);
	}

	public Object populateSearchField(final SearchFieldPopulatorParams params) {
		for (SchemaExtension extension : schemaExtensions) {
			Object populateResult = extension.populateSearchField(params);
			if (!ExtensionBooleanResult.NOT_APPLICABLE.equals(populateResult)) {
				params.setValue(populateResult);
			}
		}
		return params.getValue();
	}


	public java.util.Map<String, Object> batchProcessingSpecialCaseExtensions(
			BatchProcessingSpecialCaseParams batchProcessingSpecialCaseParams) {
		java.util.Map<String, Object> metadataChangeOnRecord = new HashMap<>();
		for (BatchProcessingSpecialCaseExtension batchProcessingSpecialCaseExtension : batchProcessingSpecialCaseExtensions) {
			metadataChangeOnRecord.putAll(batchProcessingSpecialCaseExtension.processSpecialCase(batchProcessingSpecialCaseParams));
		}

		return metadataChangeOnRecord;
	}

	public Metadata[] getSortMetadatas(Taxonomy taxonomy, boolean codeMetadataRequired) {
		Metadata[] sortMetadatas = new TaxonomyExtension().getSortMetadatas(taxonomy, codeMetadataRequired);

		for (TaxonomyExtension extension : taxonomyExtensions) {
			sortMetadatas = extension.getSortMetadatas(taxonomy, codeMetadataRequired);
		}
		return sortMetadatas;
	}

	public boolean isValueListManageable(String schemaCode) {
		return valueListsPageExtensions.getBooleanValue(true,
				behavior -> behavior.isManageable(new ValueListExtensionParams(schemaCode)));
	}
}
