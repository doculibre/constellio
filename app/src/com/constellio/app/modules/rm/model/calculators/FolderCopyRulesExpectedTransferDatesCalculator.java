package com.constellio.app.modules.rm.model.calculators;

import static com.constellio.app.modules.rm.model.calculators.CalculatorUtils.calculateExpectedTransferDate;

import java.util.Arrays;
import java.util.List;

import org.joda.time.LocalDate;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.ConfigDependency;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;

public class FolderCopyRulesExpectedTransferDatesCalculator
		extends AbstractFolderCopyRulesExpectedDatesCalculator
		implements MetadataValueCalculator<List<LocalDate>> {

	LocalDependency<LocalDate> decommissioningDateParam = LocalDependency.toADate(Folder.DECOMMISSIONING_DATE);
	LocalDependency<LocalDate> actualTransferDateParam = LocalDependency.toADate(Folder.ACTUAL_TRANSFER_DATE);

	ConfigDependency<Integer> configNumberOfYearWhenVariableDelayPeriod =
			RMConfigs.CALCULATED_SEMIACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_PERIOD.dependency();

	@Override
	protected List<? extends Dependency> getCopyRuleDateCalculationDependencies() {
		return Arrays.asList(decommissioningDateParam, actualTransferDateParam,
				configNumberOfYearWhenVariableDelayPeriod);
	}

	@Override
	protected LocalDate calculateForCopyRule(int index, CopyRetentionRule copyRule, CalculatorParameters parameters) {

		LocalDate decommissioningDate = parameters.get(decommissioningDateParam);
		LocalDate actualTransferDate = parameters.get(actualTransferDateParam);

		int numberOfYearWhenVariableDelay = parameters.get(configNumberOfYearWhenVariableDelayPeriod);

		if (actualTransferDate != null) {
			return null;
		} else {
			return calculateExpectedTransferDate(copyRule, decommissioningDate, numberOfYearWhenVariableDelay);
		}
	}

}