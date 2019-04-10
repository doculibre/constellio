package com.constellio.app.modules.rm.model.calculators;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.calculators.folder.FolderDecomDatesDynamicLocalDependency;
import com.constellio.app.modules.rm.model.enums.DecommissioningDateBasedOn;
import com.constellio.app.modules.rm.model.enums.FolderStatus;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.calculators.AbstractMetadataValueCalculator;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.DynamicDependencyValues;
import com.constellio.model.entities.calculators.dependencies.ConfigDependency;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.schemas.MetadataValueType;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.modules.rm.model.calculators.CalculatorUtils.calculateExpectedTransferDate;
import static com.constellio.app.modules.rm.model.calculators.CalculatorUtils.toNextEndOfYearDateIfNotAlready;

public abstract class AbstractFolderCopyRulesExpectedDatesCalculator extends AbstractMetadataValueCalculator<List<LocalDate>> {
	protected LocalDependency<List<CopyRetentionRule>> applicableCopyRulesParam = LocalDependency
			.toAStructure(Folder.APPLICABLE_COPY_RULES).whichIsMultivalue();
	protected LocalDependency<LocalDate> openingDateParam = LocalDependency.toADate(Folder.OPENING_DATE);
	protected LocalDependency<LocalDate> closingDateParam = LocalDependency.toADate(Folder.CLOSING_DATE);
	protected LocalDependency<LocalDate> actualTransferDateParam = LocalDependency.toADate(Folder.ACTUAL_TRANSFER_DATE);
	protected LocalDependency<List<LocalDate>> reactivationDatesParam = LocalDependency.toADate(Folder.REACTIVATION_DATES)
			.whichIsMultivalue();
	protected LocalDependency<LocalDate> reactivationDecommissioningDateParam = LocalDependency
			.toADate(Folder.REACTIVATION_DECOMMISSIONING_DATE);
	protected LocalDependency<FolderStatus> folderStatusParam = LocalDependency.toAnEnum(Folder.ARCHIVISTIC_STATUS);

	protected FolderDecomDatesDynamicLocalDependency datesAndDateTimesParam = new FolderDecomDatesDynamicLocalDependency();

	protected ConfigDependency<Integer> configRequiredDaysBeforeYearEndParam =
			RMConfigs.REQUIRED_DAYS_BEFORE_YEAR_END_FOR_NOT_ADDING_A_YEAR.dependency();
	protected ConfigDependency<String> configYearEndParam = RMConfigs.YEAR_END_DATE.dependency();
	protected ConfigDependency<Boolean> configAddYearIfCalculationDateIsEndOfYearParam =
			RMConfigs.ADD_YEAR_IF_CALULATION_DATE_IS_END_IF_YEAR.dependency();
	protected ConfigDependency<Boolean> calculatedMetadatasBasedOnFirstTimerangePartParam =
			RMConfigs.CALCULATED_METADATAS_BASED_ON_FIRST_TIMERANGE_PART.dependency();
	protected ConfigDependency<DecommissioningDateBasedOn> decommissioningDateBasedOnParam =
			RMConfigs.DECOMMISSIONING_DATE_BASED_ON.dependency();
	protected ConfigDependency<Boolean> depositAndDestructionDatesBasedOnActualTransferDateParam =
			RMConfigs.DEPOSIT_AND_DESTRUCTION_DATES_BASED_ON_ACTUAL_TRANSFER_DATE.dependency();
	protected ConfigDependency<Integer> configNumberOfYearWhenVariableDelayPeriodParam =
			RMConfigs.CALCULATED_SEMIACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_PERIOD.dependency();

	@Override
	public List<LocalDate> calculate(CalculatorParameters parameters) {

		List<CopyRetentionRule> applicableCopyRules = parameters.get(applicableCopyRulesParam);
		String yearEnd = parameters.get(configYearEndParam);
		int requiredDaysBeforeYearEnd = parameters.get(configRequiredDaysBeforeYearEndParam);
		boolean addYearIfCalculationDateIsEndOfYearParam = parameters.get(configAddYearIfCalculationDateIsEndOfYearParam);
		List<LocalDate> result = new ArrayList<>();
		for (int i = 0; i < applicableCopyRules.size(); i++) {
			CopyRetentionRule applicableCopyRule = applicableCopyRules.get(i);
			LocalDate copyRuleCalculedDate = calculateForCopyRule(i, applicableCopyRule, parameters);
			if (copyRuleCalculedDate == null) {
				result.add(null);
			} else if (CalculatorUtils.isEndOfYear(copyRuleCalculedDate, yearEnd)) {
				result.add(copyRuleCalculedDate);
			} else {
				result.add(CalculatorUtils.toNextEndOfYearDate(copyRuleCalculedDate, yearEnd, requiredDaysBeforeYearEnd,
						addYearIfCalculationDateIsEndOfYearParam));
			}
		}

		return result;
	}

	@Override
	public List<LocalDate> getDefaultValue() {
		return new ArrayList<>();
	}

	@Override
	public MetadataValueType getReturnType() {
		return MetadataValueType.DATE;
	}

	@Override
	public boolean isMultiValue() {
		return true;
	}

	@Override
	public List<? extends Dependency> getDependencies() {

		List<Dependency> dependencies = new ArrayList<>();
		dependencies.add(applicableCopyRulesParam);
		dependencies.add(openingDateParam);
		dependencies.add(closingDateParam);
		dependencies.add(reactivationDatesParam);
		dependencies.add(reactivationDecommissioningDateParam);
		dependencies.add(folderStatusParam);
		dependencies.add(actualTransferDateParam);

		dependencies.add(configRequiredDaysBeforeYearEndParam);
		dependencies.add(configYearEndParam);
		dependencies.add(configAddYearIfCalculationDateIsEndOfYearParam);
		dependencies.add(calculatedMetadatasBasedOnFirstTimerangePartParam);
		dependencies.add(decommissioningDateBasedOnParam);
		dependencies.add(depositAndDestructionDatesBasedOnActualTransferDateParam);

		dependencies.addAll(getCopyRuleDateCalculationDependencies());

		return dependencies;
	}

	protected abstract List<? extends Dependency> getCopyRuleDateCalculationDependencies();

	protected abstract LocalDate calculateForCopyRule(int index, CopyRetentionRule copyRule,
													  CalculatorParameters parameters);

	protected LocalDate getAdjustedDateUsedToCalculation(
			AbstractFolderCopyRulesExpectedDatesCalculator_CalculatorInput input,
			CopyRetentionRule copyRule, String yearEnd) {
		return getAdjustedDateUsedToCalculation(input, copyRule, yearEnd, false);
	}

	protected LocalDate getAdjustedDateUsedToCalculation(
			AbstractFolderCopyRulesExpectedDatesCalculator_CalculatorInput input,
			CopyRetentionRule copyRule, String yearEnd, boolean ignoreActualTransfertDate) {
		LocalDate activeDelayDate = input.getAdjustedBaseDateFromActiveDelay(copyRule, yearEnd);

		if (activeDelayDate != null) {
			return activeDelayDate;
		} else {
			return calculateDecommissioningDate(copyRule, input, ignoreActualTransfertDate);
		}
	}

	protected LocalDate calculateDecommissioningDate(CopyRetentionRule copyRule,
													 AbstractFolderCopyRulesExpectedDatesCalculator_CalculatorInput input) {
		return calculateDecommissioningDate(copyRule, input, false);
	}

	protected LocalDate calculateDecommissioningDate(CopyRetentionRule copyRule,
													 AbstractFolderCopyRulesExpectedDatesCalculator_CalculatorInput input,
													 boolean ignoreActualTranferDate) {
		if (!ignoreActualTranferDate && input.actualTransferDate != null && input.folderStatus.isActiveOrSemiActive()) {
			if (Boolean.FALSE.equals(input.depositAndDestructionDatesBasedOnActualTransferDate)) {
				LocalDate date = getAdjustedDateUsedToCalculation(input, copyRule, input.yearEnd, true);
				LocalDate expectedDate = calculateExpectedTransferDate(copyRule, date, input.numberOfYearWhenVariableDelayPeriod);
				return toNextEndOfYearDateIfNotAlready(expectedDate, input.yearEnd, input.requiredDaysBeforeYearEnd);
			}
			return toNextEndOfYearDateIfNotAlready(input.actualTransferDate, input.yearEnd, input.requiredDaysBeforeYearEnd);
		} else if (input.reactivationDates != null && !input.reactivationDates.isEmpty()) {
			if (input.reactivationDecommissioningDate == null) {
				return toNextEndOfYearDateIfNotAlready(input.reactivationDates.get(0), input.yearEnd, input.requiredDaysBeforeYearEnd);
			} else {
				return toNextEndOfYearDateIfNotAlready(input.reactivationDecommissioningDate, input.yearEnd, input.requiredDaysBeforeYearEnd);
			}

		} else if (DecommissioningDateBasedOn.OPEN_DATE == input.basedOn) {
			return toNextEndOfYearDateIfNotAlready(input.openingDate, input.yearEnd, input.requiredDaysBeforeYearEnd);

		} else {
			return CalculatorUtils.toNextEndOfYearDateIfNotAlready(input.closingDate, input.yearEnd, input.requiredDaysBeforeYearEnd);
		}
	}

	protected class AbstractFolderCopyRulesExpectedDatesCalculator_CalculatorInput {

		protected List<CopyRetentionRule> applicableCopyRules;
		protected Integer requiredDaysBeforeYearEnd;
		protected String yearEnd;
		protected DynamicDependencyValues datesAndDateTimes;
		protected boolean calculatedMetadatasBasedOnFirstTimerangePart;
		protected LocalDate openingDate;
		protected LocalDate closingDate;
		protected List<LocalDate> reactivationDates;
		protected LocalDate reactivationDecommissioningDate;
		protected LocalDate actualTransferDate;
		protected FolderStatus folderStatus;
		protected DecommissioningDateBasedOn basedOn;
		protected Boolean depositAndDestructionDatesBasedOnActualTransferDate;
		protected Integer numberOfYearWhenVariableDelayPeriod;

		public AbstractFolderCopyRulesExpectedDatesCalculator_CalculatorInput(CalculatorParameters parameters) {
			applicableCopyRules = parameters.get(applicableCopyRulesParam);
			requiredDaysBeforeYearEnd = parameters.get(configRequiredDaysBeforeYearEndParam);
			yearEnd = parameters.get(configYearEndParam);
			datesAndDateTimes = parameters.get(datesAndDateTimesParam);
			calculatedMetadatasBasedOnFirstTimerangePart =
					parameters.get(calculatedMetadatasBasedOnFirstTimerangePartParam);
			openingDate = parameters.get(openingDateParam);
			closingDate = parameters.get(closingDateParam);
			reactivationDates = parameters.get(reactivationDatesParam);
			reactivationDecommissioningDate = parameters.get(reactivationDecommissioningDateParam);
			actualTransferDate = parameters.get(actualTransferDateParam);
			folderStatus = parameters.get(folderStatusParam);
			basedOn = parameters.get(decommissioningDateBasedOnParam);
			depositAndDestructionDatesBasedOnActualTransferDate =
					parameters.get(depositAndDestructionDatesBasedOnActualTransferDateParam);
			numberOfYearWhenVariableDelayPeriod = parameters.get(configNumberOfYearWhenVariableDelayPeriodParam);
		}

		LocalDate adjustToFinancialYear(LocalDate date) {
			return CalculatorUtils.toNextEndOfYearDateIfNotAlready(date, yearEnd, requiredDaysBeforeYearEnd);
		}

		LocalDate getAdjustedBaseDateFromActiveDelay(CopyRetentionRule copy, String yearEnd) {
			String metadata = copy.getActiveDateMetadata();

			LocalDate date = datesAndDateTimesParam
					.getDate(metadata, datesAndDateTimes, yearEnd, calculatedMetadatasBasedOnFirstTimerangePart);
			return date == null ? null : adjustToFinancialYear(date);
		}


	}
}