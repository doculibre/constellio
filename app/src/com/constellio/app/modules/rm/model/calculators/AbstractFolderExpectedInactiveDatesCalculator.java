package com.constellio.app.modules.rm.model.calculators;

import static com.constellio.app.modules.rm.model.calculators.CalculatorUtils.calculateExpectedInactiveDate;
import static com.constellio.app.modules.rm.model.calculators.CalculatorUtils.calculateExpectedTransferDate;
import static com.constellio.app.modules.rm.model.enums.DisposalType.SORT;

import java.util.Arrays;
import java.util.List;

import org.joda.time.LocalDate;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.calculators.folder.FolderDecomDatesDynamicLocalDependency;
import com.constellio.app.modules.rm.model.enums.DisposalType;
import com.constellio.app.modules.rm.model.enums.FolderStatus;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.data.utils.LangUtils;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.DynamicDependencyValues;
import com.constellio.model.entities.calculators.dependencies.ConfigDependency;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;

public abstract class AbstractFolderExpectedInactiveDatesCalculator extends AbstractFolderCopyRulesExpectedDatesCalculator {

	LocalDependency<FolderStatus> archivisticStatusParam = LocalDependency.toAnEnum(Folder.ARCHIVISTIC_STATUS);
	LocalDependency<LocalDate> decommissioningDateParam = LocalDependency.toADate(Folder.DECOMMISSIONING_DATE);

	LocalDependency<List<LocalDate>> copyRulesExpectedTransferDateParam = LocalDependency
			.toADate(Folder.COPY_RULES_EXPECTED_TRANSFER_DATES).whichIsMultivalue();

	ConfigDependency<Integer> configSemiActiveNumberOfYearWhenVariableDelayPeriodParam =
			RMConfigs.CALCULATED_SEMIACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_PERIOD.dependency();
	ConfigDependency<Integer> configInactiveNumberOfYearWhenVariableDelayPeriodParam =
			RMConfigs.CALCULATED_INACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_PERIOD.dependency();

	ConfigDependency<String> configYearEndParam = RMConfigs.YEAR_END_DATE.dependency();

	FolderDecomDatesDynamicLocalDependency datesAndDateTimesParam = new FolderDecomDatesDynamicLocalDependency();

	@Override
	protected List<? extends Dependency> getCopyRuleDateCalculationDependencies() {
		return Arrays.asList(decommissioningDateParam, archivisticStatusParam, datesAndDateTimesParam,
				copyRulesExpectedTransferDateParam, configSemiActiveNumberOfYearWhenVariableDelayPeriodParam,
				configInactiveNumberOfYearWhenVariableDelayPeriodParam);
	}

	@Override
	protected LocalDate calculateForCopyRule(int index, CopyRetentionRule copyRule, CalculatorParameters parameters) {
		CalculatorInput input = new CalculatorInput(parameters);

		if (input.archivisticStatus.isInactive()) {
			return null;
		}

		DisposalType disposalType = getCalculatedDisposalType();

		LocalDate baseTransferDate;
		LocalDate expectedTransferDate = null;
		if (copyRule.getInactiveDisposalType() != SORT && copyRule.getInactiveDisposalType() != disposalType) {
			return null;

		} else if (input.archivisticStatus.isSemiActive()) {
			baseTransferDate = input.decommissioningDate;
			LocalDate dateSpecifiedInCopyRule = input
					.getAdjustedBaseDateFromSemiActiveDelay(copyRule, parameters.get(configYearEndParam));
			baseTransferDate = LangUtils.min(baseTransferDate, dateSpecifiedInCopyRule);

		} else {
			if (!input.copyRulesExpectedTransferDate.isEmpty()) {
				expectedTransferDate = input.copyRulesExpectedTransferDate.get(index);
			}

			LocalDate dateSpecifiedInCopyRule = input
					.getAdjustedBaseDateFromSemiActiveDelay(copyRule, parameters.get(configYearEndParam));
			if (dateSpecifiedInCopyRule != null && input.decommissioningDate != null) {
				baseTransferDate = dateSpecifiedInCopyRule;
			} else if (expectedTransferDate == null && input.inactiveNumberOfYearWhenVariableDelayPeriod != -1) {
				baseTransferDate = input.decommissioningDate;
			} else {
				baseTransferDate = expectedTransferDate;
			}

		}

		LocalDate calculatedInactiveDate = calculateExpectedInactiveDate(copyRule, baseTransferDate,
				input.inactiveNumberOfYearWhenVariableDelayPeriod);

		if (calculatedInactiveDate == null) {
			return null;

		} else if (input.archivisticStatus.isSemiActive()) {
			return LangUtils.max(calculatedInactiveDate, input.decommissioningDate);

		} else {
			return LangUtils.max(calculatedInactiveDate, expectedTransferDate);
		}

	}

	protected abstract DisposalType getCalculatedDisposalType();

	private class CalculatorInput extends AbstractFolderCopyRulesExpectedDatesCalculator_CalculatorInput {

		FolderStatus archivisticStatus;
		LocalDate decommissioningDate;

		List<LocalDate> copyRulesExpectedTransferDate;

		Integer semiActiveNumberOfYearWhenVariableDelayPeriod, inactiveNumberOfYearWhenVariableDelayPeriod;
		DynamicDependencyValues datesAndDateTimes;

		public CalculatorInput(CalculatorParameters parameters) {
			super(parameters);
			this.archivisticStatus = parameters.get(archivisticStatusParam);
			this.decommissioningDate = parameters.get(decommissioningDateParam);
			this.copyRulesExpectedTransferDate = parameters.get(copyRulesExpectedTransferDateParam);
			this.semiActiveNumberOfYearWhenVariableDelayPeriod = parameters
					.get(configSemiActiveNumberOfYearWhenVariableDelayPeriodParam);
			this.inactiveNumberOfYearWhenVariableDelayPeriod = parameters
					.get(configInactiveNumberOfYearWhenVariableDelayPeriodParam);
			this.datesAndDateTimes = parameters.get(datesAndDateTimesParam);
		}

		public LocalDate getAdjustedBaseDateFromSemiActiveDelay(CopyRetentionRule copy, String yearEnd) {
			String semiActiveMetadata = copy.getSemiActiveDateMetadata();

			if (semiActiveMetadata != null && semiActiveMetadata.equals(copy.getActiveDateMetadata())) {
				return null;

			} else {
				LocalDate date = datesAndDateTimesParam.getDate(semiActiveMetadata, datesAndDateTimes, yearEnd);
				if (date == null) {
					return null;
				} else {

					if (!copy.isIgnoreActivePeriod()) {
						date = calculateExpectedTransferDate(copy, date, semiActiveNumberOfYearWhenVariableDelayPeriod);
					}
					date = adjustToFinancialYear(date);
					return date;
				}
			}
		}
	}
}
