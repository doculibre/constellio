package com.constellio.app.modules.rm.model.calculators;

import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.enums.FolderStatus;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import org.joda.time.LocalDate;

import java.util.Arrays;
import java.util.List;

import static com.constellio.app.modules.rm.model.calculators.CalculatorUtils.calculateExpectedTransferDate;

public class FolderCopyRulesExpectedTransferDatesCalculator2
		extends AbstractFolderCopyRulesExpectedDatesCalculator
		implements MetadataValueCalculator<List<LocalDate>> {

	LocalDependency<LocalDate> actualTransferDateParam = LocalDependency.toADate(Folder.ACTUAL_TRANSFER_DATE);
	LocalDependency<FolderStatus> statusParam = LocalDependency.toAnEnum(Folder.ARCHIVISTIC_STATUS);

	@Override
	protected List<? extends Dependency> getCopyRuleDateCalculationDependencies() {
		return Arrays.asList(actualTransferDateParam, datesAndDateTimesParam,
				configNumberOfYearWhenVariableDelayPeriodParam, statusParam,
				calculatedMetadatasBasedOnFirstTimerangePartParam);
	}

	@Override
	protected LocalDate calculateForCopyRule(int index, CopyRetentionRule copyRule, CalculatorParameters parameters) {

		CalculatorInput input = new CalculatorInput(parameters);

		if (input.actualTransferDate != null || FolderStatus.ACTIVE != parameters.get(statusParam)) {
			return null;
		} else {
			LocalDate date = getAdjustedDateUsedToCalculation(input, copyRule, parameters.get(configYearEndParam));
			return calculateExpectedTransferDate(copyRule, date, input.numberOfYearWhenVariableDelayPeriod);
		}
	}

	protected class CalculatorInput extends AbstractFolderCopyRulesExpectedDatesCalculator_CalculatorInput {

		public CalculatorInput(CalculatorParameters parameters) {
			super(parameters);
		}
	}
}
