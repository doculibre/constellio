package com.constellio.app.modules.rm.model.validators;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.model.enums.DisposalType;
import com.constellio.app.modules.rm.model.enums.RetentionRuleScope;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.modules.rm.wrappers.structures.RetentionRuleDocumentType;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.ConfigProvider;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.validation.RecordMetadataValidator;
import com.constellio.model.entities.schemas.validation.RecordValidator;
import com.constellio.model.frameworks.validation.ValidationErrors;

public class RetentionRuleValidator implements RecordValidator {

	public static final String MUST_SPECIFY_ADMINISTRATIVE_UNITS_XOR_RESPONSIBLES_FLAG = "mustSpecifyAdministrativeUnitsXORSetResponsibles";
	public static final String MUST_SPECIFY_ONE_SECONDARY_COPY_RETENTON_RULE = "mustSpecifyOneSecondaryRetentionRule";
	public static final String MUST_SPECIFY_AT_LEAST_ONE_PRINCIPAL_COPY_RETENTON_RULE = "mustSpecifyAtLeastOnePrincipalRetentionRule";
	public static final String MUST_NOT_SPECIFY_SECONDARY_DOCUMENT_COPY_RETENTON_RULE = "mustNotSpecifySecondaryDocumentRetentionRule";
	public static final String MUST_SPECIFY_AT_LEAST_ONE_PRINCIPAL_DOCUMENT_COPY_RETENTON_RULE = "mustSpecifyAtLeastOnePrincipalDocumentRetentionRule";
	public static final String COPY_RETENTION_RULE_FIELD_REQUIRED = "copyRetentionRuleFieldRequired";
	public static final String DOCUMENT_COPY_RETENTION_RULE_FIELD_REQUIRED = "documentCopyRetentionRuleFieldRequired";
	public static final String MISSING_DOCUMENT_TYPE_DISPOSAL = "missingDocumentTypeDisposal";
	public static final String DOCUMENT_RULE_MUST_HAVE_ONLY_DOCUMENT_COPY_RULES = "documentRuleMustHaveOnlyDocumentCopyRules";

	public static final String COPY_RETENTION_RULE_FIELD_REQUIRED_INDEX = "index";
	public static final String COPY_RETENTION_RULE_FIELD_REQUIRED_FIELD = "field";
	public static final String COPY_RETENTION_RULE_FIELD_REQUIRED_FIELD_COPY_TYPE = "copyType";
	public static final String DOCUMENT_COPY_RETENTION_RULE_FIELD_REQUIRED_FIELD_DOCUMENT_TYPE = "documentType";
	public static final String COPY_RETENTION_RULE_FIELD_REQUIRED_FIELD_MEDIUM_TYPE = "mediumType";
	public static final String COPY_RETENTION_RULE_FIELD_REQUIRED_FIELD_ACTIVE_PERIOD = "activePeriod";
	public static final String COPY_RETENTION_RULE_FIELD_REQUIRED_FIELD_SEMIACTIVE_PERIOD = "semiActivePeriod";
	public static final String COPY_RETENTION_RULE_FIELD_REQUIRED_FIELD_DISPOSAL = "disposal";

	public static final String PRINCIPAL_DEFAULT_COPY_RETENTION_RULE_IN_FOLDER_RULE = "principalDefaultCopyRetentionRuleInFolderRule";
	public static final String SECONDARY_DEFAULT_COPY_RETENTION_RULE_IN_FOLDER_RULE = "secondaryDefaultCopyRetentionRuleInFolderRule";

	public static final String PRINCIPAL_DEFAULT_COPY_RETENTION_RULE_REQUIRED_IN_DOCUMENT_RULE = "principalDefaultCopyRetentionRuleRequiredInDocumentRule";
	public static final String SECONDARY_DEFAULT_COPY_RETENTION_RULE_REQUIRED_IN_DOCUMENT_RULE = "secondaryDefaultCopyRetentionRuleRequiredInDocumentRule";

	public static final String DOCUMENT_TYPES_IN_DOCUMENT_RULE = "documentTypesInDocumentRule";

	//	public static final String PRINCIPAL_COPIES_MUST_HAVE_DIFFERENT_CONTENT_TYPES = "principalCopiesMustHaveDifferentContentTypes";
	//	public static final String PRINCIPAL_COPIES_MUST_HAVE_DIFFERENT_CONTENT_TYPES_DUPLICATES = "duplicates";

	public static final String MISSING_DOCUMENT_TYPE_DISPOSAL_INDEX = "index";

	@Override
	public void validate(Record record, MetadataSchemaTypes types, MetadataSchema schema,
			ConfigProvider configProvider, ValidationErrors validationErrors) {
		RetentionRule retentionRule = new RetentionRule(record, types);
		validate(retentionRule, schema, configProvider, validationErrors);
	}

	void validate(RetentionRule retentionRule, MetadataSchema schema, ConfigProvider configProvider,
			ValidationErrors validationErrors) {

		if (retentionRule.getScope() != RetentionRuleScope.DOCUMENTS) {
			validateDocumentTypes(retentionRule, validationErrors);
		} else {
			validateNoDocumentTypes(retentionRule, validationErrors);

		}
		validateAdministrativeUnits(retentionRule, schema, configProvider, validationErrors);
		validateCopyRetentionRules(retentionRule, schema, validationErrors, configProvider);
		validateDocumentCopyRetentionRules(retentionRule, schema, validationErrors, configProvider);
		validateDefaultDocumentCopyRetentionRules(retentionRule, schema, validationErrors);
	}

	private void validateDefaultDocumentCopyRetentionRules(RetentionRule retentionRule, MetadataSchema schema,
			ValidationErrors validationErrors) {
		if (retentionRule.getScope() == RetentionRuleScope.DOCUMENTS) {
			if (retentionRule.getPrincipalDefaultDocumentCopyRetentionRule() == null) {
				validationErrors.add(getClass(), PRINCIPAL_DEFAULT_COPY_RETENTION_RULE_REQUIRED_IN_DOCUMENT_RULE);
			}
			if (retentionRule.getSecondaryDefaultDocumentCopyRetentionRule() == null) {
				validationErrors.add(getClass(), SECONDARY_DEFAULT_COPY_RETENTION_RULE_REQUIRED_IN_DOCUMENT_RULE);
			}
		} else {
			if (retentionRule.getPrincipalDefaultDocumentCopyRetentionRule() != null) {
				validationErrors.add(getClass(), PRINCIPAL_DEFAULT_COPY_RETENTION_RULE_IN_FOLDER_RULE);
			}
			if (retentionRule.getSecondaryDefaultDocumentCopyRetentionRule() != null) {
				validationErrors.add(getClass(), SECONDARY_DEFAULT_COPY_RETENTION_RULE_IN_FOLDER_RULE);
			}
		}
	}

	private void validateNoDocumentTypes(RetentionRule retentionRule, ValidationErrors validationErrors) {
		if (!retentionRule.getDocumentTypesDetails().isEmpty()) {
			validationErrors.add(getClass(), DOCUMENT_TYPES_IN_DOCUMENT_RULE);
		}
	}

	private void validateDocumentTypes(RetentionRule retentionRule, ValidationErrors validationErrors) {

		List<RetentionRuleDocumentType> documentTypes = retentionRule.getDocumentTypesDetails();
		if (retentionRule.hasCopyRetentionRuleWithSortDispositionType()) {
			for (int i = 0; i < documentTypes.size(); i++) {
				RetentionRuleDocumentType documentType = documentTypes.get(i);

				if (documentType.getDisposalType() == null || documentType.getDisposalType() == DisposalType.SORT) {

					Map<String, String> parameters = new HashMap<>();
					parameters.put(MISSING_DOCUMENT_TYPE_DISPOSAL_INDEX, "" + i);
					validationErrors.add(getClass(), MISSING_DOCUMENT_TYPE_DISPOSAL, parameters);

				}
			}
		}

	}

	private void validateCopyRetentionRules(RetentionRule retentionRule, MetadataSchema schema,
			ValidationErrors validationErrors, ConfigProvider configProvider) {

		List<CopyRetentionRule> copyRetentionRules = retentionRule.getCopyRetentionRules();

		boolean validIntegrity = true;
		for (int i = 0; i < copyRetentionRules.size(); i++) {
			validIntegrity &= validateCopyRuleIntegrity(copyRetentionRules.get(i), i, schema, validationErrors);
		}

		if (validIntegrity) {
			int principalCount = 0;
			int secondaryCount = 0;
			//
			//			List<String> duplicateContentTypes = new ArrayList<>();
			//			List<String> mediumTypeIds = new ArrayList<>();

			for (CopyRetentionRule copyRetentionRule : retentionRule.getCopyRetentionRules()) {
				if (copyRetentionRule.getCopyType() == CopyType.PRINCIPAL) {
					principalCount++;

					//					for (String mediumTypeId : copyRetentionRule.getMediumTypeIds()) {
					//						if (mediumTypeIds.contains(mediumTypeId)) {
					//							duplicateContentTypes.add(mediumTypeId);
					//						} else {
					//							mediumTypeIds.add(mediumTypeId);
					//						}
					//					}

				} else {
					secondaryCount++;
				}
			}

			boolean copyRulePrincipalRequired = configProvider.get(RMConfigs.COPY_RULE_PRINCIPAL_REQUIRED);

			if (retentionRule.getScope() != RetentionRuleScope.DOCUMENTS && principalCount == 0 && copyRulePrincipalRequired) {
				addCopyRetentionRuleError(MUST_SPECIFY_AT_LEAST_ONE_PRINCIPAL_COPY_RETENTON_RULE, schema, validationErrors);
			}

			if (retentionRule.getScope() == RetentionRuleScope.DOCUMENTS && (principalCount + secondaryCount) != 0) {
				addCopyRetentionRuleError(DOCUMENT_RULE_MUST_HAVE_ONLY_DOCUMENT_COPY_RULES, schema, validationErrors);
			}

			if (retentionRule.getScope() != RetentionRuleScope.DOCUMENTS && secondaryCount != 1) {
				addCopyRetentionRuleError(MUST_SPECIFY_ONE_SECONDARY_COPY_RETENTON_RULE, schema, validationErrors);
			}
			//			if (!duplicateContentTypes.isEmpty()) {
			//				addCopyRetentionRuleDuplicatedContentTypesError(duplicateContentTypes, schema, validationErrors);
			//			}

		}

	}

	private void validateDocumentCopyRetentionRules(RetentionRule retentionRule, MetadataSchema schema,
			ValidationErrors validationErrors, ConfigProvider configProvider) {

		List<CopyRetentionRule> copyRetentionRules = retentionRule.getDocumentCopyRetentionRules();

		boolean validIntegrity = true;
		for (int i = 0; i < copyRetentionRules.size(); i++) {
			validIntegrity &= validateDocumentCopyRuleIntegrity(copyRetentionRules.get(i), i, schema, validationErrors);
		}

		if (validIntegrity) {
			int principalCount = 0;
			int secondaryCount = 0;

			for (CopyRetentionRule copyRetentionRule : retentionRule.getDocumentCopyRetentionRules()) {
				if (copyRetentionRule.getCopyType() == CopyType.PRINCIPAL) {
					principalCount++;
				} else {
					secondaryCount++;
				}
			}

			if (retentionRule.getScope() == RetentionRuleScope.DOCUMENTS && principalCount == 0) {
				addCopyRetentionRuleError(MUST_SPECIFY_AT_LEAST_ONE_PRINCIPAL_DOCUMENT_COPY_RETENTON_RULE, schema,
						validationErrors);
			}

			if (secondaryCount != 0) {
				addCopyRetentionRuleError(MUST_NOT_SPECIFY_SECONDARY_DOCUMENT_COPY_RETENTON_RULE, schema, validationErrors);
			}
		}

	}

	private boolean validateCopyRuleIntegrity(CopyRetentionRule copyRetentionRule, int index, MetadataSchema schema,
			ValidationErrors validationErrors) {

		boolean valid = true;
		if (copyRetentionRule.getCopyType() == null) {
			valid = false;
			addCopyRetentionRuleIntegrityError(index, COPY_RETENTION_RULE_FIELD_REQUIRED_FIELD_COPY_TYPE, schema,
					validationErrors);
		}
		if (copyRetentionRule.getMediumTypeIds() == null || copyRetentionRule.getMediumTypeIds().isEmpty()) {
			valid = false;
			addCopyRetentionRuleIntegrityError(index, COPY_RETENTION_RULE_FIELD_REQUIRED_FIELD_MEDIUM_TYPE, schema,
					validationErrors);
		}

		if (copyRetentionRule.getActiveRetentionPeriod() == null) {
			valid = false;
			addCopyRetentionRuleIntegrityError(index, COPY_RETENTION_RULE_FIELD_REQUIRED_FIELD_ACTIVE_PERIOD, schema,
					validationErrors);
		}
		if (copyRetentionRule.getSemiActiveRetentionPeriod() == null) {
			valid = false;
			addCopyRetentionRuleIntegrityError(index, COPY_RETENTION_RULE_FIELD_REQUIRED_FIELD_SEMIACTIVE_PERIOD,
					schema, validationErrors);
		}
		if (copyRetentionRule.getInactiveDisposalType() == null) {
			valid = false;
			addCopyRetentionRuleIntegrityError(index, COPY_RETENTION_RULE_FIELD_REQUIRED_FIELD_DISPOSAL, schema,
					validationErrors);
		}

		return valid;
	}

	private boolean validateDocumentCopyRuleIntegrity(CopyRetentionRule copyRetentionRule, int index, MetadataSchema schema,
			ValidationErrors validationErrors) {

		boolean valid = validateCopyRuleIntegrity(copyRetentionRule, index, schema, validationErrors);

		if (copyRetentionRule.getDocumentTypeId() == null) {
			valid = false;
			addCopyRetentionRuleIntegrityError(index, DOCUMENT_COPY_RETENTION_RULE_FIELD_REQUIRED_FIELD_DOCUMENT_TYPE, schema,
					validationErrors);
		}

		return valid;
	}

	private void addCopyRetentionRuleIntegrityError(int index, String field, MetadataSchema schema,
			ValidationErrors validationErrors) {
		Map<String, String> parameters = new HashMap<>();
		parameters.put(COPY_RETENTION_RULE_FIELD_REQUIRED_FIELD, field);
		parameters.put(COPY_RETENTION_RULE_FIELD_REQUIRED_INDEX, "" + index);
		parameters.put(RecordMetadataValidator.METADATA_CODE, RetentionRule.COPY_RETENTION_RULES);
		parameters.put(RecordMetadataValidator.METADATA_LABEL, schema.getMetadata(RetentionRule.COPY_RETENTION_RULES).getLabel());
		validationErrors.add(getClass(), COPY_RETENTION_RULE_FIELD_REQUIRED, parameters);

	}

	//	private void addCopyRetentionRuleDuplicatedContentTypesError(List<String> duplicatedContentTypes, MetadataSchema schema,
	//			ValidationErrors validationErrors) {
	//		Map<String, String> parameters = new HashMap<>();
	//		parameters.put(PRINCIPAL_COPIES_MUST_HAVE_DIFFERENT_CONTENT_TYPES_DUPLICATES, StringUtils
	//				.join(duplicatedContentTypes, ","));
	//		parameters.put(RecordMetadataValidator.METADATA_CODE, RetentionRule.COPY_RETENTION_RULES);
	//		parameters.put(RecordMetadataValidator.METADATA_LABEL, schema.getMetadata(RetentionRule.COPY_RETENTION_RULES).getLabel());
	//		validationErrors.add(getClass(), PRINCIPAL_COPIES_MUST_HAVE_DIFFERENT_CONTENT_TYPES, parameters);
	//
	//	}Les a

	private void addCopyRetentionRuleError(String code, MetadataSchema schema, ValidationErrors validationErrors) {
		Map<String, String> parameters = new HashMap<>();
		parameters.put(RecordMetadataValidator.METADATA_CODE, RetentionRule.COPY_RETENTION_RULES);
		parameters.put(RecordMetadataValidator.METADATA_LABEL, schema.getMetadata(RetentionRule.COPY_RETENTION_RULES).getLabel());
		validationErrors.add(getClass(), code, parameters);

	}

	private void validateAdministrativeUnits(RetentionRule retentionRule, MetadataSchema schema,
			ConfigProvider configProvider, ValidationErrors validationErrors) {

		List<String> administrativeUnits = retentionRule.getAdministrativeUnits();
		boolean responsibleAdministrativeUnits = retentionRule.isResponsibleAdministrativeUnits();

		if (administrativeUnits.isEmpty() && !responsibleAdministrativeUnits) {
			validationErrors.add(getClass(), MUST_SPECIFY_ADMINISTRATIVE_UNITS_XOR_RESPONSIBLES_FLAG);

		} else {
			if (!administrativeUnits.isEmpty() && responsibleAdministrativeUnits) {
				Object openHolder = configProvider.get(RMConfigs.OPEN_HOLDER);
				if (openHolder == null || !(Boolean) openHolder) {
					validationErrors.add(getClass(), MUST_SPECIFY_ADMINISTRATIVE_UNITS_XOR_RESPONSIBLES_FLAG);
				}
			}
		}
	}

}

