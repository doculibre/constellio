package com.constellio.app.modules.rm.model.calculators;

import java.util.Arrays;
import java.util.List;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.ConfigDependency;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.calculators.dependencies.ReferenceDependency;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;

public class CategoryIsLinkableCalculator implements MetadataValueCalculator<Boolean> {

	// I guess dependencies should be parent and retentionRules

	LocalDependency<List<String>> rulesDependency = LocalDependency.toAReference(Category.RETENTION_RULES).whichIsMultivalue();
	LocalDependency<String> parentDependency = LocalDependency.toAReference(Category.PARENT);

	ReferenceDependency<List<Boolean>> rulesApprovedDependency = ReferenceDependency
			.toABoolean(Category.RETENTION_RULES, RetentionRule.APPROVED).whichIsMultivalue();
	ReferenceDependency<List<Boolean>> rulesInactiveDependency = ReferenceDependency
			.toABoolean(Category.RETENTION_RULES, Schemas.LOGICALLY_DELETED_STATUS.getLocalCode()).whichIsMultivalue();

	ConfigDependency<Boolean> configNotRoot = RMConfigs.LINKABLE_CATEGORY_MUST_NOT_BE_ROOT.dependency();
	ConfigDependency<Boolean> configRulesApproved = RMConfigs.LINKABLE_CATEGORY_MUST_HAVE_APPROVED_RULES.dependency();

	@Override
	public Boolean calculate(CalculatorParameters parameters) {
		boolean considerNotRoot = parameters.get(configNotRoot);
		boolean considerRulesApproved = parameters.get(configRulesApproved);

		String parentId = parameters.get(parentDependency);
		List<String> rulesIds = parameters.get(rulesDependency);
		List<Boolean> rulesApproved = parameters.get(rulesApprovedDependency);
		List<Boolean> rulesInactive = parameters.get(rulesInactiveDependency);

		if (rulesIds.isEmpty()) {
			return false;
		} else {
			boolean allRulesInactive = true;
			for (Boolean ruleIsInactive : rulesInactive) {
				if (ruleIsInactive == null || !ruleIsInactive) {
					allRulesInactive = false;
					break;
				}
			}
			if (allRulesInactive) {
				return false;
			}
		}

		if (considerNotRoot && parentId == null) {
			return false;
		}

		if (considerRulesApproved) {
			for (Boolean ruleIsApproved : rulesApproved) {
				if (ruleIsApproved != null && ruleIsApproved.booleanValue()) {
					return true;
				}
			}
			return false;
		}

		return true;
	}

	@Override
	public Boolean getDefaultValue() {
		return null;
	}

	@Override
	public MetadataValueType getReturnType() {
		return MetadataValueType.BOOLEAN;
	}

	@Override
	public boolean isMultiValue() {
		return false;
	}

	@Override
	public List<? extends Dependency> getDependencies() {
		return Arrays.asList(rulesDependency, parentDependency, rulesApprovedDependency, rulesInactiveDependency, configNotRoot,
				configRulesApproved);
	}
}
