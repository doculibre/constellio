/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.modules.rm.model.validators;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.model.enums.DisposalType;
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
	public static final String COPY_RETENTION_RULE_FIELD_REQUIRED = "copyRetentionRuleFieldRequired";
	public static final String MISSING_DOCUMENT_TYPE_DISPOSAL = "missingDocumentTypeDisposal";

	public static final String COPY_RETENTION_RULE_FIELD_REQUIRED_INDEX = "index";
	public static final String COPY_RETENTION_RULE_FIELD_REQUIRED_FIELD = "field";
	public static final String COPY_RETENTION_RULE_FIELD_REQUIRED_FIELD_COPY_TYPE = "copyType";
	public static final String COPY_RETENTION_RULE_FIELD_REQUIRED_FIELD_MEDIUM_TYPE = "mediumType";
	public static final String COPY_RETENTION_RULE_FIELD_REQUIRED_FIELD_ACTIVE_PERIOD = "activePeriod";
	public static final String COPY_RETENTION_RULE_FIELD_REQUIRED_FIELD_SEMIACTIVE_PERIOD = "semiActivePeriod";
	public static final String COPY_RETENTION_RULE_FIELD_REQUIRED_FIELD_DISPOSAL = "disposal";

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

		validateAdministrativeUnits(retentionRule, schema, validationErrors);
		validateCopyRetentionRules(retentionRule, schema, validationErrors, configProvider);
		validateDocumentTypes(retentionRule, validationErrors);
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
			validIntegrity &= validateIntegrity(copyRetentionRules.get(i), i, schema, validationErrors);
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

			if (principalCount == 0 && copyRulePrincipalRequired) {
				addCopyRetentionRuleError(MUST_SPECIFY_AT_LEAST_ONE_PRINCIPAL_COPY_RETENTON_RULE, schema, validationErrors);
			}

			if (secondaryCount != 1) {
				addCopyRetentionRuleError(MUST_SPECIFY_ONE_SECONDARY_COPY_RETENTON_RULE, schema, validationErrors);
			}
			//			if (!duplicateContentTypes.isEmpty()) {
			//				addCopyRetentionRuleDuplicatedContentTypesError(duplicateContentTypes, schema, validationErrors);
			//			}

		}

	}

	private boolean validateIntegrity(CopyRetentionRule copyRetentionRule, int index, MetadataSchema schema,
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
			ValidationErrors validationErrors) {

		List<String> administrativeUnits = retentionRule.getAdministrativeUnits();
		boolean responsibleAdministrativeUnits = retentionRule.isResponsibleAdministrativeUnits();

		if (administrativeUnits.isEmpty() && !responsibleAdministrativeUnits) {
			validationErrors.add(getClass(), MUST_SPECIFY_ADMINISTRATIVE_UNITS_XOR_RESPONSIBLES_FLAG);

		} else if (!administrativeUnits.isEmpty() && responsibleAdministrativeUnits) {
			validationErrors.add(getClass(), MUST_SPECIFY_ADMINISTRATIVE_UNITS_XOR_RESPONSIBLES_FLAG);

		}

	}

}
