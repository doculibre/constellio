package com.constellio.app.modules.rm.model.calculators.folder;

import java.util.Arrays;
import java.util.List;

import org.joda.time.LocalDate;

import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.schemas.MetadataValueType;

public class FolderMainCopyRuleCalculator2 implements MetadataValueCalculator<CopyRetentionRule> {

	LocalDependency<String> enteredCopyRuleParam = LocalDependency.toAString(Folder.MAIN_COPY_RULE_ID_ENTERED);

	LocalDependency<List<CopyRetentionRule>> copyRulesParam = LocalDependency.toAStructure(Folder.APPLICABLE_COPY_RULES)
			.whichIsRequired();

	LocalDependency<List<LocalDate>> expectedDestructionDatesParam = LocalDependency
			.toADate(Folder.COPY_RULES_EXPECTED_DESTRUCTION_DATES).whichIsMultivalue().whichIsRequired();

	LocalDependency<List<LocalDate>> expectedDepositDatesParam = LocalDependency
			.toADate(Folder.COPY_RULES_EXPECTED_DEPOSIT_DATES).whichIsMultivalue().whichIsRequired();

	@Override
	public CopyRetentionRule calculate(CalculatorParameters parameters) {
		CalculatorInput input = new CalculatorInput(parameters);

		LocalDate smallestDate = null;
		CopyRetentionRule mainCopyRule = null;

		if (input.enteredCopyRule != null) {
			for (CopyRetentionRule copyRetentionRule : input.copyRules) {
				if (input.enteredCopyRule.equals(copyRetentionRule.getId())) {
					mainCopyRule = copyRetentionRule;
				}
			}
		}

		if (mainCopyRule == null) {

			for (int i = 0; i < input.copyRules.size(); i++) {
				LocalDate dateAtIndex = null;
				if (input.expectedDestructionDates.size() - 1 > i && input.expectedDestructionDates.get(i) != null) {
					dateAtIndex = input.expectedDestructionDates.get(i);
				} else if (input.expectedDepositDates.size() - 1 > i) {
					dateAtIndex = input.expectedDepositDates.get(i);
				}
				if (mainCopyRule == null || (dateAtIndex != null && dateAtIndex.isBefore(smallestDate))) {
					smallestDate = dateAtIndex;
					mainCopyRule = input.copyRules.get(i);
				}
			}
		}

		return mainCopyRule;
	}

	@Override
	public CopyRetentionRule getDefaultValue() {
		return null;
	}

	@Override
	public MetadataValueType getReturnType() {
		return MetadataValueType.STRUCTURE;
	}

	@Override
	public boolean isMultiValue() {
		return false;
	}

	@Override
	public List<? extends Dependency> getDependencies() {
		return Arrays.asList(copyRulesParam, enteredCopyRuleParam, expectedDestructionDatesParam, expectedDepositDatesParam);
	}

	class CalculatorInput {

		String enteredCopyRule;

		List<CopyRetentionRule> copyRules;

		List<LocalDate> expectedDestructionDates;

		List<LocalDate> expectedDepositDates;

		CalculatorInput(CalculatorParameters parameters) {
			enteredCopyRule = parameters.get(enteredCopyRuleParam);
			copyRules = parameters.get(copyRulesParam);
			expectedDestructionDates = parameters.get(expectedDestructionDatesParam);
			expectedDepositDates = parameters.get(expectedDepositDatesParam);
		}

	}
}
