package com.constellio.app.modules.rm.model.calculators;

import static com.constellio.app.modules.rm.model.calculators.CalculatorUtils.calculateExpectedInactiveDate;
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

	ConfigDependency<Integer> configNumberOfYearWhenVariableDelayPeriodParam =
			RMConfigs.CALCULATED_INACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_PERIOD.dependency();
	FolderDecomDatesDynamicLocalDependency datesAndDateTimesParam = new FolderDecomDatesDynamicLocalDependency();

	@Override
	protected List<? extends Dependency> getCopyRuleDateCalculationDependencies() {
		return Arrays.asList(decommissioningDateParam, archivisticStatusParam, datesAndDateTimesParam,
				copyRulesExpectedTransferDateParam, configNumberOfYearWhenVariableDelayPeriodParam);
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
			LocalDate dateSpecifiedInCopyRule = input.getAjustedBaseDateFromSemiActiveDelay(copyRule);
			baseTransferDate = LangUtils.min(baseTransferDate, dateSpecifiedInCopyRule);

		} else {
			if (!input.copyRulesExpectedTransferDate.isEmpty()) {
				expectedTransferDate = input.copyRulesExpectedTransferDate.get(index);
			}

			LocalDate dateSpecifiedInCopyRule = input.getAjustedBaseDateFromSemiActiveDelay(copyRule);
			if (dateSpecifiedInCopyRule != null && input.decommissioningDate != null) {
				baseTransferDate = dateSpecifiedInCopyRule;
			} else {
				baseTransferDate = expectedTransferDate;
			}

		}

		LocalDate calculatedInactiveDate = calculateExpectedInactiveDate(copyRule, baseTransferDate,
				input.numberOfYearWhenVariableDelayPeriod);

		if (input.archivisticStatus.isSemiActive()) {
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

		Integer numberOfYearWhenVariableDelayPeriod;
		DynamicDependencyValues datesAndDateTimes;

		public CalculatorInput(CalculatorParameters parameters) {
			super(parameters);
			this.archivisticStatus = parameters.get(archivisticStatusParam);
			this.decommissioningDate = parameters.get(decommissioningDateParam);
			this.copyRulesExpectedTransferDate = parameters.get(copyRulesExpectedTransferDateParam);
			this.numberOfYearWhenVariableDelayPeriod = parameters.get(configNumberOfYearWhenVariableDelayPeriodParam);
			this.datesAndDateTimes = parameters.get(datesAndDateTimesParam);
		}

		public LocalDate getAjustedBaseDateFromSemiActiveDelay(CopyRetentionRule copy) {
			String semiActiveMetadata = copy.getSemiActiveDateMetadata();

			if (semiActiveMetadata != null && semiActiveMetadata.equals(copy.getActiveDateMetadata())) {
				return null;

			} else {
				LocalDate date = datesAndDateTimesParam.getDate(semiActiveMetadata, datesAndDateTimes);
				return date == null ? null : ajustToFinancialYear(date);
			}
		}
	}
}
