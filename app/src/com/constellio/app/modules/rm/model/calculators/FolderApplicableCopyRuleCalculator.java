package com.constellio.app.modules.rm.model.calculators;

import static com.constellio.data.utils.LangUtils.isEqual;

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
	LocalDependency<String> enteredTypeParam = LocalDependency.toAReference(Folder.TYPE);
	ReferenceDependency<List<CopyRetentionRule>> ruleCopyRulesParam = ReferenceDependency.toAStructure(Folder.RETENTION_RULE,
			RetentionRule.COPY_RETENTION_RULES).whichIsMultivalue().whichIsRequired();

	@Override
	public List<CopyRetentionRule> calculate(CalculatorParameters parameters) {
		CalculatorInput input = new CalculatorInput(parameters);

		if (input.folderCopyType == CopyType.SECONDARY) {
			return input.rulesOfCopyType;
		} else {
			List<CopyRetentionRule> copies = input.rulesOfCopyType;
			copies = filterUsingFolderType(copies, input.enteredType);
			copies = filterUsingFolderMediumTypes(copies, input.folderMediumTypes);
			return copies;
		}
	}

	private List<CopyRetentionRule> filterUsingFolderType(List<CopyRetentionRule> copies, String enteredType) {
		List<CopyRetentionRule> matches = new ArrayList<>();
		List<CopyRetentionRule> applicableIfNoMatches = new ArrayList<>();

		for (CopyRetentionRule copy : copies) {
			if (isEqual(enteredType, copy.getTypeId())) {
				matches.add(copy);
			} else if (copy.getTypeId() == null) {
				applicableIfNoMatches.add(copy);
			}
		}

		if (matches.size() == 0) {
			return applicableIfNoMatches;
		} else {
			return matches;
		}
	}

	private List<CopyRetentionRule> filterUsingFolderMediumTypes(List<CopyRetentionRule> copies, List<String> mediumTypes) {

		List<CopyRetentionRule> matches = new ArrayList<>();

		for (CopyRetentionRule rulePrincipalCopyRule : copies) {
			loop2:
			for (String copyRuleMediumType : rulePrincipalCopyRule.getMediumTypeIds()) {
				if (mediumTypes.contains(copyRuleMediumType)) {
					matches.add(rulePrincipalCopyRule);
					break loop2;
				}
			}
		}

		if (matches.size() == 0) {
			return copies;
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
		return Arrays.asList(folderCopyTypeParam, folderMediumTypesParam, ruleCopyRulesParam, enteredTypeParam);
	}

	private class CalculatorInput {

		CopyType folderCopyType;
		List<String> folderMediumTypes;
		String enteredType;
		List<CopyRetentionRule> rulesOfCopyType;
		List<CopyRetentionRule> ruleCopyRules;

		public CalculatorInput(CalculatorParameters parameters) {
			folderCopyType = parameters.get(folderCopyTypeParam);
			folderMediumTypes = parameters.get(folderMediumTypesParam);
			ruleCopyRules = parameters.get(ruleCopyRulesParam);
			enteredType = parameters.get(enteredTypeParam);
			rulesOfCopyType = getCopyRulesWithType(ruleCopyRules, folderCopyType);
		}
	}
}
