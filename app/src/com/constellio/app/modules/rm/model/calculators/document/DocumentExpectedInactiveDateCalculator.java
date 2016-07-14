package com.constellio.app.modules.rm.model.calculators.document;

import static com.constellio.app.modules.rm.model.enums.DecommissioningDateBasedOn.OPEN_DATE;
import static com.constellio.app.modules.rm.model.enums.DisposalType.DEPOSIT;
import static com.constellio.app.modules.rm.model.enums.DisposalType.DESTRUCTION;
import static com.constellio.data.utils.LangUtils.max;
import static java.util.Arrays.asList;

import java.util.List;

import org.joda.time.LocalDate;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.calculators.CalculatorUtils;
import com.constellio.app.modules.rm.model.enums.DecommissioningDateBasedOn;
import com.constellio.app.modules.rm.model.enums.DisposalType;
import com.constellio.app.modules.rm.model.enums.FolderStatus;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.DynamicDependencyValues;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.ConfigDependency;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.calculators.dependencies.ReferenceDependency;
import com.constellio.model.entities.schemas.MetadataValueType;

public abstract class DocumentExpectedInactiveDateCalculator implements MetadataValueCalculator<LocalDate> {

	ReferenceDependency<LocalDate> folderOpenDateParam = ReferenceDependency
			.toADate(Document.FOLDER, Folder.OPENING_DATE);

	ReferenceDependency<LocalDate> folderCloseDateParam = ReferenceDependency
			.toADate(Document.FOLDER, Folder.CLOSING_DATE);

	ReferenceDependency<LocalDate> folderExpectedDestructionDateParam = ReferenceDependency
			.toADate(Document.FOLDER, Folder.EXPECTED_DESTRUCTION_DATE);
	ReferenceDependency<LocalDate> folderExpectedDepositDateParam = ReferenceDependency
			.toADate(Document.FOLDER, Folder.EXPECTED_DEPOSIT_DATE);

	LocalDependency<LocalDate> expectedTransferDateParam = LocalDependency.toADate(Document.FOLDER_EXPECTED_TRANSFER_DATE);
	LocalDependency<LocalDate> actualTransferDateParam = LocalDependency.toADate(Document.FOLDER_ACTUAL_TRANSFER_DATE);
	LocalDependency<LocalDate> actualDepositDateParam = LocalDependency.toADate(Document.FOLDER_ACTUAL_DEPOSIT_DATE);
	LocalDependency<LocalDate> actualDestructionDateParam = LocalDependency.toADate(Document.FOLDER_ACTUAL_DESTRUCTION_DATE);

	LocalDependency<CopyRetentionRule> copyParam = LocalDependency.toAStructure(Document.MAIN_COPY_RULE);

	LocalDependency<FolderStatus> archivisticTypeParam = LocalDependency.toAnEnum(Document.FOLDER_ARCHIVISTIC_STATUS);

	ConfigDependency<DecommissioningDateBasedOn> decommissioningDateBasedOnParam
			= RMConfigs.DECOMMISSIONING_DATE_BASED_ON.dependency();

	ConfigDependency<Integer> numberOfYearWhenInactiveVariableDelayParam =
			RMConfigs.CALCULATED_INACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_PERIOD.dependency();

	ConfigDependency<Integer> numberOfYearWhenSemiActiveVariableDelayParam =
			RMConfigs.CALCULATED_SEMIACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_PERIOD.dependency();

	ConfigDependency<Integer> requiredDaysBeforeYearEndParam = RMConfigs.REQUIRED_DAYS_BEFORE_YEAR_END_FOR_NOT_ADDING_A_YEAR
			.dependency();

	ConfigDependency<Boolean> documentRetentionRulesEnabledParam = RMConfigs.DOCUMENT_RETENTION_RULES.dependency();

	DocumentDecomDatesDynamicLocalDependency datesAndDateTimesParam = new DocumentDecomDatesDynamicLocalDependency();

	@Override
	public LocalDate calculate(CalculatorParameters parameters) {

		CalculatorInput input = new CalculatorInput(parameters);

		if (!input.documentRetentionRulesEnabled) {
			if (input.disposalType == DEPOSIT) {
				return input.folderExpectedDepositDate;
			} else {
				return input.folderExpectedDestructionDate;
			}
		} else if (input.actualDepositDate != null || input.actualDestructionDate != null || !input.isCalculated()) {
			return null;

		} else {
			LocalDate transferDate;
			if (input.actualTransferDate != null) {
				transferDate = input.ajustToFinancialYear(input.actualTransferDate);

			} else {
				transferDate = input.expectedTransferDate;
			}

			LocalDate baseDateFromSemiActiveDelay = input.getAjustedBaseDateFromSemiActiveDelay();

			if (!input.copy.isIgnoreActivePeriod()) {
				baseDateFromSemiActiveDelay = CalculatorUtils.calculateExpectedTransferDate(input.copy,
						baseDateFromSemiActiveDelay, input.numberOfYearWhenSemiActiveVariableDelay);
			}

			LocalDate baseDate = max(transferDate, baseDateFromSemiActiveDelay);

			return input.calculateInactiveBasedOn(baseDate);

		}
	}

	ConfigDependency<String> yearEndParam =
			RMConfigs.YEAR_END_DATE.dependency();

	private LocalDate getBaseAjustedDate(CalculatorInput input) {
		if (input.preferedDecommissioningDateBasedOn == OPEN_DATE || input.folderCloseDate == null) {
			return input.ajustToFinancialYear(input.folderOpenDate);
		} else {
			return input.folderCloseDate;
		}
	}

	@Override
	public LocalDate getDefaultValue() {
		return null;
	}

	@Override
	public MetadataValueType getReturnType() {
		return MetadataValueType.DATE;
	}

	@Override
	public boolean isMultiValue() {
		return false;
	}

	@Override
	public List<? extends Dependency> getDependencies() {
		return asList(folderOpenDateParam, folderCloseDateParam, actualTransferDateParam, copyParam,
				decommissioningDateBasedOnParam, numberOfYearWhenInactiveVariableDelayParam, yearEndParam,
				requiredDaysBeforeYearEndParam, folderExpectedDestructionDateParam, folderExpectedDepositDateParam,
				expectedTransferDateParam, documentRetentionRulesEnabledParam, archivisticTypeParam, actualDepositDateParam,
				actualDestructionDateParam, numberOfYearWhenSemiActiveVariableDelayParam,
				numberOfYearWhenInactiveVariableDelayParam, datesAndDateTimesParam);
	}

	private class CalculatorInput {

		LocalDate folderOpenDate;
		LocalDate folderCloseDate;
		LocalDate folderExpectedDestructionDate;
		LocalDate folderExpectedDepositDate;
		LocalDate expectedTransferDate;
		DecommissioningDateBasedOn preferedDecommissioningDateBasedOn;
		CopyRetentionRule copy;
		LocalDate actualTransferDate;
		LocalDate actualDepositDate;
		LocalDate actualDestructionDate;
		int numberOfYearWhenInactiveVariableDelay;
		int numberOfYearWhenSemiActiveVariableDelay;
		String yearEnd;
		int requiredDaysBeforeYearEnd;
		boolean documentRetentionRulesEnabled;
		DisposalType disposalType;
		FolderStatus archivisticType;
		DynamicDependencyValues datesAndDateTimes;

		public CalculatorInput(CalculatorParameters parameters) {
			this.expectedTransferDate = parameters.get(expectedTransferDateParam);
			this.folderOpenDate = parameters.get(folderOpenDateParam);
			this.folderCloseDate = parameters.get(folderCloseDateParam);
			this.preferedDecommissioningDateBasedOn = parameters.get(decommissioningDateBasedOnParam);
			this.copy = parameters.get(copyParam);
			this.actualTransferDate = parameters.get(actualTransferDateParam);
			this.numberOfYearWhenSemiActiveVariableDelay = parameters.get(numberOfYearWhenSemiActiveVariableDelayParam);
			this.numberOfYearWhenInactiveVariableDelay = parameters.get(numberOfYearWhenInactiveVariableDelayParam);
			this.yearEnd = parameters.get(yearEndParam);
			this.requiredDaysBeforeYearEnd = parameters.get(requiredDaysBeforeYearEndParam);
			this.folderExpectedDestructionDate = parameters.get(folderExpectedDestructionDateParam);
			this.folderExpectedDepositDate = parameters.get(folderExpectedDepositDateParam);
			this.documentRetentionRulesEnabled = parameters.get(documentRetentionRulesEnabledParam);
			this.archivisticType = parameters.get(archivisticTypeParam);
			this.disposalType = getCalculatedDisposalType();
			this.actualDepositDate = parameters.get(actualDepositDateParam);
			this.actualDestructionDate = parameters.get(actualDestructionDateParam);
			this.datesAndDateTimes = parameters.get(datesAndDateTimesParam);
		}

		LocalDate calculateInactiveBasedOn(LocalDate baseDate) {
			return CalculatorUtils.calculateExpectedInactiveDate(copy, baseDate, numberOfYearWhenInactiveVariableDelay);
		}

		LocalDate calculateSemiActiveBasedOn(LocalDate baseDate) {
			return CalculatorUtils.calculateExpectedTransferDate(copy, baseDate, numberOfYearWhenSemiActiveVariableDelay);
		}

		LocalDate ajustToFinancialYear(LocalDate date) {
			return CalculatorUtils.toNextEndOfYearDateIfNotAlready(date, yearEnd, requiredDaysBeforeYearEnd);
		}

		public boolean isCalculated() {
			if (disposalType == DEPOSIT) {
				return copy != null && copy.getInactiveDisposalType() != DESTRUCTION;
			} else {
				return copy != null && copy.getInactiveDisposalType() != DEPOSIT;
			}
		}

		public LocalDate getAjustedBaseDateFromSemiActiveDelay() {
			String metadata = copy.getSemiActiveDateMetadata();

			LocalDate date = datesAndDateTimesParam.getDate(metadata, datesAndDateTimes);
			if (date == null) {
				return null;
			} else {
				return ajustToFinancialYear(date);
			}
		}
	}

	protected abstract DisposalType getCalculatedDisposalType();
}
