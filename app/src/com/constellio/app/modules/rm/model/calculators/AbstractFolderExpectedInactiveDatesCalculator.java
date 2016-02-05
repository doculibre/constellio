package com.constellio.app.modules.rm.model.calculators;

import static com.constellio.app.modules.rm.model.enums.DisposalType.SORT;

import java.util.Arrays;
import java.util.List;

import org.joda.time.LocalDate;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.enums.DisposalType;
import com.constellio.app.modules.rm.model.enums.FolderStatus;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.dependencies.ConfigDependency;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;

public abstract class AbstractFolderExpectedInactiveDatesCalculator extends AbstractFolderCopyRulesExpectedDatesCalculator {

	LocalDependency<FolderStatus> archivisticStatusParam = LocalDependency.toAnEnum(Folder.ARCHIVISTIC_STATUS);
	LocalDependency<LocalDate> decommissioningDateParam = LocalDependency.toADate(Folder.DECOMMISSIONING_DATE);

	LocalDependency<List<LocalDate>> copyRulesExpectedTransferDateParam = LocalDependency
			.toADate(Folder.COPY_RULES_EXPECTED_TRANSFER_DATES).whichIsMultivalue();

	ConfigDependency<Integer> configNumberOfYearWhenVariableDelayPeriod =
			RMConfigs.CALCULATED_INACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_PERIOD.dependency();

	@Override
	protected List<? extends Dependency> getCopyRuleDateCalculationDependencies() {
		return Arrays.asList(decommissioningDateParam, archivisticStatusParam,
				copyRulesExpectedTransferDateParam, configNumberOfYearWhenVariableDelayPeriod);
	}

	@Override
	protected LocalDate calculateForCopyRule(int index, CopyRetentionRule copyRule, CalculatorParameters parameters) {

		FolderStatus archivisticStatus = parameters.get(archivisticStatusParam);
		LocalDate decommissioningDate = parameters.get(decommissioningDateParam);
		List<LocalDate> copyRulesExpectedTransferDate = parameters.get(copyRulesExpectedTransferDateParam);
		int numberOfYearWhenVariableDelayPeriod = parameters.get(configNumberOfYearWhenVariableDelayPeriod);
		DisposalType disposalType = getCalculatedDisposalType();

		LocalDate baseTransferDate;
		if (copyRule.getInactiveDisposalType() != SORT && copyRule.getInactiveDisposalType() != disposalType) {
			baseTransferDate = null;
		} else if (archivisticStatus.isSemiActive()) {
			baseTransferDate = decommissioningDate;

		} else if (copyRulesExpectedTransferDate.isEmpty() && index == 0) {
			baseTransferDate = null;
		} else {
			baseTransferDate = copyRulesExpectedTransferDate.get(index);
		}

		return CalculatorUtils.calculateExpectedInactiveDate(copyRule, baseTransferDate, numberOfYearWhenVariableDelayPeriod);

	}

	protected abstract DisposalType getCalculatedDisposalType();
}
