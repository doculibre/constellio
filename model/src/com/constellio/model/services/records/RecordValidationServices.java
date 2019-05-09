package com.constellio.model.services.records;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.ConfigProvider;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.entities.schemas.validation.RecordMetadataValidator;
import com.constellio.model.entities.schemas.validation.RecordValidator;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.records.RecordServicesException.ValidationException;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.schemas.validators.AllowedReferencesValidator;
import com.constellio.model.services.schemas.validators.CyclicHierarchyValidator;
import com.constellio.model.services.schemas.validators.MaskedMetadataValidator;
import com.constellio.model.services.schemas.validators.MetadataChildOfValidator;
import com.constellio.model.services.schemas.validators.MetadataUniqueValidator;
import com.constellio.model.services.schemas.validators.MetadataUnmodifiableValidator;
import com.constellio.model.services.schemas.validators.MetadataValueTypeValidator;
import com.constellio.model.services.schemas.validators.RecordPermissionValidator;
import com.constellio.model.services.schemas.validators.ValueRequirementValidator;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.security.AuthorizationsServices;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RecordValidationServices {

	private final MetadataSchemasManager schemasManager;
	private final AuthorizationsServices authorizationServices;
	private final ConfigProvider configProvider;
	private final RecordProvider recordProvider;
	private SearchServices searchService;
	private final RecordAutomaticMetadataServices recordAutomaticMetadataServices;

	public RecordValidationServices(ConfigProvider configProvider, RecordProvider recordProvider,
									MetadataSchemasManager schemasManager,
									SearchServices searchService,
									AuthorizationsServices authorizationsServices,
									RecordAutomaticMetadataServices recordAutomaticMetadataServices) {
		this.configProvider = configProvider;
		this.recordProvider = recordProvider;
		this.schemasManager = schemasManager;
		this.searchService = searchService;
		this.authorizationServices = authorizationsServices;
		this.recordAutomaticMetadataServices = recordAutomaticMetadataServices;
	}

	public void validateMetadatas(Record record, RecordProvider recordProvider, Transaction transaction,
								  List<Metadata> metadatas, boolean afterCalculate)
			throws RecordServicesException.ValidationException {
		ValidationErrors validationErrors = validateMetadatasReturningErrors(record, recordProvider, transaction, metadatas, afterCalculate);
		if (!validationErrors.getValidationErrors().isEmpty()) {
			throw new RecordServicesException.ValidationException(record, validationErrors);
		}
	}

	public void validateCyclicReferences(Record record, RecordProvider recordProvider)
			throws RecordServicesException.ValidationException {

		MetadataSchemaTypes schemaTypes = schemasManager.getSchemaTypes(record.getCollection());
		MetadataSchema schema = schemaTypes.getSchema(record.getSchemaCode());
		List<Metadata> metadatas = getManualMetadatas(schema);
		validateCyclicReferences(record, recordProvider, schemaTypes, metadatas);
	}

	public void validateCyclicReferences(Record record, RecordProvider recordProvider, MetadataSchemaTypes schemaTypes,
										 List<Metadata> metadatas)
			throws RecordServicesException.ValidationException {
		ValidationErrors validationErrors = new ValidationErrors();
		new CyclicHierarchyValidator(schemaTypes, metadatas, recordProvider).validate(record, validationErrors);

		if (!validationErrors.getValidationErrors().isEmpty()) {
			throw new RecordServicesException.ValidationException(record, validationErrors);
		}
	}

	public void validateSchemaUsingCustomSchemaValidator(Record record, RecordProvider recordProvider,
														 Transaction transaction)
			throws RecordServicesException.ValidationException {
		this.validateUsingCustomSchemaValidators(record, recordProvider);
	}

	public void validateUsingCustomSchemaValidators(Record record, RecordProvider recordProvider)
			throws RecordServicesException.ValidationException {

		ValidationErrors validationErrors = validateUsingCustomSchemaValidatorsReturningErrors(record);
		if (!validationErrors.getValidationErrors().isEmpty()) {
			throw new RecordServicesException.ValidationException(record, validationErrors);
		}
	}

	boolean hasSecurityOnSchema(Record record) {
		String schemaTypeCode = new SchemaUtils().getSchemaTypeCode(record.getSchemaCode());
		MetadataSchemaType schemaType = schemasManager.getSchemaTypes(record.getCollection()).getSchemaType(schemaTypeCode);
		return schemaType.hasSecurity();
	}

	List<Metadata> getManualMetadatas(MetadataSchema schema) {
		List<Metadata> manualMetadatas = new ArrayList<>();
		for (Metadata metadata : schema.getMetadatas()) {
			if (metadata.getDataEntry().getType() == DataEntryType.MANUAL && !metadata.isSystemReserved()) {
				manualMetadatas.add(metadata);
			}
		}
		return manualMetadatas;
	}

	ValidationErrors validateMetadatasReturningErrors(Record record, RecordProvider recordProvider,
													  Transaction transaction, List<Metadata> metadatas,
													  boolean afterCalculate) {
		MetadataSchemaTypes schemaTypes = schemasManager.getSchemaTypes(record.getCollection());
		MetadataSchema schema = schemaTypes.getSchema(record.getSchemaCode());
		return validateMetadatasReturningErrors(record, recordProvider, schemaTypes, metadatas, transaction, afterCalculate);
	}

	ValidationErrors validateManualMetadatasReturningErrors(Record record, RecordProvider recordProvider,
															Transaction transaction) {
		MetadataSchemaTypes schemaTypes = schemasManager.getSchemaTypes(record.getCollection());
		MetadataSchema schema = schemaTypes.getSchema(record.getSchemaCode());
		List<Metadata> metadatas = getManualMetadatas(schema);
		return validateMetadatasReturningErrors(record, recordProvider, schemaTypes, metadatas, transaction, false);
	}

	ValidationErrors validateAutomaticMetadatasReturningErrors(Record record, RecordProvider recordProvider,
															   Transaction transaction) {
		MetadataSchemaTypes schemaTypes = schemasManager.getSchemaTypes(record.getCollection());
		MetadataSchema schema = schemaTypes.getSchema(record.getSchemaCode());
		List<Metadata> metadatas = schema.getAutomaticMetadatas();
		return validateMetadatasReturningErrors(record, recordProvider, schemaTypes, metadatas, transaction, true);
	}

	ValidationErrors validateMetadatasReturningErrors(Record record, RecordProvider recordProvider,
													  MetadataSchemaTypes schemaTypes, List<Metadata> metadatas,
													  Transaction transaction, boolean afterCalculate) {
		ValidationErrors validationErrors = new ValidationErrors();
		if (!transaction.isSkipReferenceValidation()) {
			new AllowedReferencesValidator(schemaTypes, metadatas, recordProvider,
					transaction.isSkippingReferenceToLogicallyDeletedValidation())
					.validate(record, validationErrors);
		}
		new MetadataValueTypeValidator(metadatas).validate(record, validationErrors);
		if (!transaction.isSkippingRequiredValuesValidation()) {
			boolean skipUSRMetadatas = transaction.getRecordUpdateOptions().isSkipUSRMetadatasRequirementValidations();
			new ValueRequirementValidator(metadatas, skipUSRMetadatas, recordAutomaticMetadataServices, afterCalculate)
					.validate(record, validationErrors);
		}
		new MetadataUnmodifiableValidator(metadatas).validate(record, validationErrors);
		if (transaction.getRecordUpdateOptions() == null || transaction.getRecordUpdateOptions().isUnicityValidationsEnabled()) {


			new MetadataUniqueValidator(metadatas, schemaTypes, searchService).validate(record, validationErrors);
		}
		new MetadataChildOfValidator(metadatas, schemaTypes).validate(record, validationErrors);
		if (transaction.getRecordUpdateOptions() == null || !transaction.getRecordUpdateOptions()
				.isSkipMaskedMetadataValidations()) {
			newMaskedMetadataValidator(metadatas).validate(record, validationErrors);
		}
		return validationErrors;
	}

	public MaskedMetadataValidator newMaskedMetadataValidator(List<Metadata> metadatas) {
		return new MaskedMetadataValidator(metadatas);
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	ValidationErrors validateUsingCustomSchemaValidatorsReturningErrors(Record record) {
		final ValidationErrors validationErrors = new ValidationErrors();
		String schemaCode = record.getSchemaCode();
		MetadataSchemaTypes types = schemasManager.getSchemaTypes(record.getCollection());
		MetadataSchema schema = types.getSchema(schemaCode);

		for (RecordValidator validator : schema.getValidators()) {
			callSchemaValidator(record, types, schema, validator, validationErrors);
		}
		for (final Metadata metadata : schema.getMetadatas()) {
			Set<RecordMetadataValidator<Object>> validators = (Set) metadata.getValidators();
			for (RecordMetadataValidator<Object> validator : validators) {
				callMetadataValidator(record, metadata, validator, validationErrors);
			}
		}
		return validationErrors;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	ValidationErrors validateUsingSecurityValidatorsReturningErrors(Record record, Transaction transaction) {
		final ValidationErrors validationErrors = new ValidationErrors();

		new RecordPermissionValidator(transaction, authorizationServices).validate(record, validationErrors);

		return validationErrors;
	}

	private void callMetadataValidator(Record record, final Metadata metadata,
									   RecordMetadataValidator<Object> validator,
									   final ValidationErrors validationErrors) {

		final Object value = record.get(metadata);
		callMetadataValidatorForValue(metadata, validator, validationErrors, value, record.getId());

	}

	private void callMetadataValidatorForValue(final Metadata metadata, RecordMetadataValidator<Object> validator,
											   final ValidationErrors validationErrors, final Object value,
											   final String recordId) {
		ValidationErrors validationErrorsWithFailedMetadataParameters = new ValidationErrors() {
			@Override
			public void add(Class<?> validatorClass, String code, Map<String, Object> parameters) {
				parameters.put("metadataCode", metadata.getCode());
				parameters.put("metadataValue", value.toString());
				parameters.put("record", recordId);
				validationErrors.add(validatorClass, code, parameters);
			}
		};
		validator.validate(metadata, value, configProvider, validationErrorsWithFailedMetadataParameters);
	}

	private void callSchemaValidator(Record record, MetadataSchemaTypes types, final MetadataSchema schema,
									 RecordValidator validator, final ValidationErrors validationErrors) {

		ValidationErrors validationErrorsWithExtraParams = new ValidationErrors() {
			@Override
			public void add(Class<?> validatorClass, String code, Map<String, Object> parameters) {
				parameters.put("schemaCode", schema.getCode());
				validationErrors.add(validatorClass, code, parameters);
			}
		};

		RecordValidatorParams params = new RecordValidatorParams(record, types, schema, validator,
				validationErrorsWithExtraParams,
				configProvider, recordProvider, searchService);

		validator.validate(params);
	}

	public void validateAccess(Record record, Transaction transaction)
			throws ValidationException {
		//Passe de l'ours temporaire!
		if (hasSecurityOnSchema(record) && !"workflowExecution".equals(record.getTypeCode())) {
			ValidationErrors validationErrors = validateUsingSecurityValidatorsReturningErrors(record, transaction);
			if (!validationErrors.getValidationErrors().isEmpty()) {
				throw new RecordServicesException.ValidationException(record, validationErrors);
			}
		}
	}
}
