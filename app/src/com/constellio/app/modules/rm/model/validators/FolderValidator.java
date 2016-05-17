package com.constellio.app.modules.rm.model.validators;

import java.util.HashMap;
import java.util.Map;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.model.entities.schemas.validation.RecordValidator;
import com.constellio.model.services.records.RecordValidatorParams;

public class FolderValidator implements RecordValidator {

	public static final String FOLDER_CATEGORY_MUST_BE_RELATED_TO_ITS_RULE = "folderCategoryMustBeRelatedToItsRule";
	public static final String RULE_CODE = "ruleCode";
	public static final String CATEGORY_CODE = "categoryCode";

	@Override
	public void validate(RecordValidatorParams params) {
		Folder folder = new Folder(params.getValidatedRecord(), params.getTypes());
		validate(folder, params);
	}

	private void validate(Folder folder, RecordValidatorParams params) {
		if (params.getConfigProvider().get(RMConfigs.ENFORCE_CATEGORY_AND_RULE_RELATIONSHIP_IN_FOLDER)) {
			RetentionRule retentionRule = RetentionRule.wrap(params.getRecord(folder.getRetentionRule()), params.getTypes());
			Category category = Category.wrap(params.getRecord(folder.getCategory()), params.getTypes());
			if (!category.getRententionRules().contains(retentionRule.getId())) {
				Map<String, Object> parameters = new HashMap<>();
				parameters.put(RULE_CODE, retentionRule.getCode());
				parameters.put(CATEGORY_CODE, category.getCode());

				params.getValidationErrors().add(FolderValidator.class, FOLDER_CATEGORY_MUST_BE_RELATED_TO_ITS_RULE, parameters);
			}
		}
	}

}
