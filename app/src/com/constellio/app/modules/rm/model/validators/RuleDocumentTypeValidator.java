package com.constellio.app.modules.rm.model.validators;

import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.modules.rm.wrappers.RetentionRuleDocumentType;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.validation.RecordValidator;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.records.RecordProvider;
import com.constellio.model.services.records.RecordValidatorParams;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RuleDocumentTypeValidator implements RecordValidator {

	public static final String INVALID_RULE_COPY = "invalidRuleCopy";

	public static final String COPY = "copy";
	public static final String RULE_CODE = "ruleCode";
	public static final String COPY_IDS_IN_RULE = "copyIdsInRule";

	@Override
	public void validate(RecordValidatorParams params) {
		RetentionRuleDocumentType retentionRuleDocumentType =
				new RetentionRuleDocumentType(params.getValidatedRecord(), params.getTypes());
		validate(retentionRuleDocumentType, params.getRecordProvider(), params.getTypes(), params.getValidationErrors());
	}

	private void validate(RetentionRuleDocumentType retentionRuleDocumentType, RecordProvider recordProvider,
						  MetadataSchemaTypes types, ValidationErrors validationErrors) {
		if (StringUtils.isNotBlank(retentionRuleDocumentType.getRuleCopy())) {
			RetentionRule retentionRule = getRetentionRule(retentionRuleDocumentType.getRule(), recordProvider, types);
			if (retentionRule == null) {
				Map<String, Object> parameters = new HashMap<>();
				parameters.put(COPY, retentionRuleDocumentType);
				parameters.put(RULE_CODE, "");
				validationErrors.add(getClass(), INVALID_RULE_COPY, parameters);
			} else {
				List<CopyRetentionRule> copies = retentionRule.getCopyRetentionRules();
				List<String> copyIds = copies.stream().map(CopyRetentionRule::getId).collect(Collectors.toList());
				if (!copyIds.contains(retentionRuleDocumentType.getRuleCopy())) {
					Map<String, Object> parameters = new HashMap<>();
					parameters.put(COPY, retentionRuleDocumentType);
					parameters.put(RULE_CODE, retentionRule.getCode());
					parameters.put(COPY_IDS_IN_RULE, copyIds.toString());
					validationErrors.add(getClass(), INVALID_RULE_COPY, parameters);
				}
			}
		}
	}

	private RetentionRule getRetentionRule(String ruleId, RecordProvider recordProvider, MetadataSchemaTypes types) {
		if (StringUtils.isBlank(ruleId)) {
			return null;
		}

		Record ruleRecord = recordProvider.getRecord(ruleId);
		if (ruleRecord == null) {
			return null;
		}

		return new RetentionRule(ruleRecord, types);
	}
}
