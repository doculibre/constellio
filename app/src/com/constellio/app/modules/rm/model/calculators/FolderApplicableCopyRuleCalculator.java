package com.constellio.app.modules.rm.model.calculators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.calculators.dependencies.ReferenceDependency;
import com.constellio.model.entities.schemas.MetadataValueType;

public class FolderApplicableCopyRuleCalculator implements MetadataValueCalculator<List<CopyRetentionRule>> {

	LocalDependency<CopyType> folderCopyTypeParam = LocalDependency.toAnEnum(Folder.COPY_STATUS).whichIsRequired();
	LocalDependency<List<String>> folderMediumTypesParam = LocalDependency.toAReference(Folder.MEDIUM_TYPES).whichIsMultivalue();
	ReferenceDependency<List<CopyRetentionRule>> ruleCopyRulesParam = ReferenceDependency.toAStructure(Folder.RETENTION_RULE,
			RetentionRule.COPY_RETENTION_RULES).whichIsMultivalue().whichIsRequired();

	@Override
	public List<CopyRetentionRule> calculate(CalculatorParameters parameters) {
		CopyType folderCopyType = parameters.get(folderCopyTypeParam);
		List<String> folderMediumTypes = parameters.get(folderMediumTypesParam);
		List<CopyRetentionRule> ruleCopyRules = parameters.get(ruleCopyRulesParam);

		List<CopyRetentionRule> rulesOfType = getCopyRulesWithType(ruleCopyRules, folderCopyType);
		if (folderCopyType == CopyType.SECONDARY) {
			return rulesOfType;
		} else {
			return getPrincipalCopyRule(rulesOfType, folderMediumTypes);
		}

	}

	private List<CopyRetentionRule> getPrincipalCopyRule(List<CopyRetentionRule> rulePrincipalCopyRules,
			List<String> folderMediumTypes) {

		List<CopyRetentionRule> matches = new ArrayList<>();

		for (CopyRetentionRule rulePrincipalCopyRule : rulePrincipalCopyRules) {

			loop2:
			for (String copyRuleMediumType : rulePrincipalCopyRule.getMediumTypeIds()) {
				if (folderMediumTypes.contains(copyRuleMediumType)) {
					matches.add(rulePrincipalCopyRule);
					break loop2;
				}
			}
		}

		if (matches.size() == 0) {
			return rulePrincipalCopyRules;
		} else {
			return matches;
		}
	}

	private List<CopyRetentionRule> getCopyRulesWithType(List<CopyRetentionRule> ruleCopyRules, CopyType copyType) {
		List<CopyRetentionRule> rulesWithCopyType = new ArrayList<>();
		for (CopyRetentionRule copyRule : ruleCopyRules) {
			if (copyRule.getCopyType() == copyType) {
				rulesWithCopyType.add(copyRule);
			}
		}
		return rulesWithCopyType;
	}

	@Override
	public List<CopyRetentionRule> getDefaultValue() {
		return new ArrayList<>();
	}

	@Override
	public MetadataValueType getReturnType() {
		return MetadataValueType.STRUCTURE;
	}

	@Override
	public boolean isMultiValue() {
		return true;
	}

	@Override
	public List<? extends Dependency> getDependencies() {
		return Arrays.asList(folderCopyTypeParam, folderMediumTypesParam, ruleCopyRulesParam);
	}
}
