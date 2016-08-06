package com.constellio.app.modules.rm.model.calculators;

import static com.constellio.app.modules.rm.model.calculators.CalculatorUtils.calculateExpectedTransferDate;

import java.util.Arrays;
import java.util.List;

import org.joda.time.LocalDate;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.calculators.folder.FolderDecomDatesDynamicLocalDependency;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.DynamicDependencyValues;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.ConfigDependency;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;

public class FolderCopyRulesExpectedTransferDatesCalculator
		extends AbstractFolderCopyRulesExpectedDatesCalculator
		implements MetadataValueCalculator<List<LocalDate>> {

	LocalDependency<LocalDate> decommissioningDateParam = LocalDependency.toADate(Folder.DECOMMISSIONING_DATE);
	LocalDependency<LocalDate> actualTransferDateParam = LocalDependency.toADate(Folder.ACTUAL_TRANSFER_DATE);
	FolderDecomDatesDynamicLocalDependency datesAndDateTimesParam = new FolderDecomDatesDynamicLocalDependency();

	ConfigDependency<Integer> configNumberOfYearWhenVariableDelayPeriodParam =
			RMConfigs.CALCULATED_SEMIACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_PERIOD.dependency();

	@Override
	protected List<? extends Dependency> getCopyRuleDateCalculationDependencies() {
		return Arrays.asList(decommissioningDateParam, actualTransferDateParam, datesAndDateTimesParam,
				configNumberOfYearWhenVariableDelayPeriodParam);
	}

	@Override
	protected LocalDate calculateForCopyRule(int index, CopyRetentionRule copyRule, CalculatorParameters parameters) {

		CalculatorInput input = new CalculatorInput(parameters);

		if (input.actualTransferDate != null) {
			return null;
		} else {
			LocalDate date = getAdjustedDateUsedToCalculation(input, copyRule, parameters.get(configYearEndParam));
			return calculateExpectedTransferDate(copyRule, date, input.numberOfYearWhenVariableDelayPeriod);
		}
	}

	private LocalDate getAdjustedDateUsedToCalculation(CalculatorInput input, CopyRetentionRule copyRule, String yearEnd) {
		LocalDate activeDelayDate = input.getAdjustedBaseDateFromActiveDelay(copyRule, yearEnd);

		if (activeDelayDate != null && input.decommissioningDate != null) {
			return activeDelayDate;
		} else {
			return input.decommissioningDate;
		}

	}

	private class CalculatorInput extends AbstractFolderCopyRulesExpectedDatesCalculator_CalculatorInput {

		LocalDate decommissioningDate;
		LocalDate actualTransferDate;
		DynamicDependencyValues datesAndDateTimes;
		Integer numberOfYearWhenVariableDelayPeriod;

		public CalculatorInput(CalculatorParameters parameters) {
			super(parameters);
			this.decommissioningDate = parameters.get(decommissioningDateParam);
			this.actualTransferDate = parameters.get(actualTransferDateParam);
			this.datesAndDateTimes = parameters.get(datesAndDateTimesParam);
			this.numberOfYearWhenVariableDelayPeriod = parameters.get(configNumberOfYearWhenVariableDelayPeriodParam);
		}

		public LocalDate getAdjustedBaseDateFromActiveDelay(CopyRetentionRule copy, String yearEnd) {
			String metadata = copy.getActiveDateMetadata();

			LocalDate date = datesAndDateTimesParam.getDate(metadata, datesAndDateTimes, yearEnd);
			return date == null ? null : adjustToFinancialYear(date);
		}
	}
}