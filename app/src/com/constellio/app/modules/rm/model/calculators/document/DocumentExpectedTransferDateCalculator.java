package com.constellio.app.modules.rm.model.calculators.document;

import static com.constellio.app.modules.rm.model.enums.DecommissioningDateBasedOn.OPEN_DATE;
import static com.constellio.app.modules.rm.model.enums.RetentionRuleScope.DOCUMENTS;
import static com.constellio.app.modules.rm.wrappers.RetentionRule.SCOPE;
import static com.constellio.data.utils.LangUtils.max;
import static java.util.Arrays.asList;

import java.util.List;

import org.joda.time.LocalDate;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.calculators.CalculatorUtils;
import com.constellio.app.modules.rm.model.enums.DecommissioningDateBasedOn;
import com.constellio.app.modules.rm.model.enums.RetentionRuleScope;
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

public class DocumentExpectedTransferDateCalculator implements MetadataValueCalculator<LocalDate> {

	ReferenceDependency<LocalDate> expectedTransferDateParam = ReferenceDependency
			.toADate(Document.FOLDER, Folder.EXPECTED_TRANSFER_DATE);

	ReferenceDependency<LocalDate> folderOpenDateParam = ReferenceDependency
			.toADate(Document.FOLDER, Folder.OPENING_DATE);

	ReferenceDependency<LocalDate> folderCloseDateParam = ReferenceDependency
			.toADate(Document.FOLDER, Folder.CLOSING_DATE);

	ReferenceDependency<LocalDate> folderExpectedTransferDateParam = ReferenceDependency
			.toADate(Document.FOLDER, Folder.EXPECTED_TRANSFER_DATE);

	LocalDependency<LocalDate> actualTransferDateParam = LocalDependency.toADate(Document.FOLDER_ACTUAL_TRANSFER_DATE);

	LocalDependency<CopyRetentionRule> copyParam = LocalDependency.toAStructure(Document.MAIN_COPY_RULE);

	ConfigDependency<DecommissioningDateBasedOn> decommissioningDateBasedOnParam
			= RMConfigs.DECOMMISSIONING_DATE_BASED_ON.dependency();

	ConfigDependency<Integer> numberOfYearWhenSemiActiveVariableDelayParam =
			RMConfigs.CALCULATED_SEMIACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_PERIOD.dependency();

	ConfigDependency<Integer> requiredDaysBeforeYearEndParam = RMConfigs.REQUIRED_DAYS_BEFORE_YEAR_END_FOR_NOT_ADDING_A_YEAR
			.dependency();

	ConfigDependency<Boolean> documentRetentionRulesEnabledParam = RMConfigs.DOCUMENT_RETENTION_RULES.dependency();

	ConfigDependency<Boolean> calculatedMetadatasBasedOnFirstTimerangePartParam = RMConfigs.CALCULATED_METADATAS_BASED_ON_FIRST_TIMERANGE_PART
			.dependency();

	DocumentDecomDatesDynamicLocalDependency datesAndDateTimesParam = new DocumentDecomDatesDynamicLocalDependency();

	ReferenceDependency<RetentionRuleScope> retentionRuleScopeParam = ReferenceDependency
			.toAnEnum(Document.FOLDER_RETENTION_RULE, SCOPE);
	LocalDependency<String> documentTypeParam = LocalDependency.toAReference(Document.TYPE);

	@Override
	public LocalDate calculate(CalculatorParameters parameters) {

		CalculatorInput input = new CalculatorInput(parameters);

		if (!input.documentRetentionRulesEnabled || (input.retentionRuleScope != DOCUMENTS && input.documentType == null)) {
			return input.folderExpectedTransferDate;
		} else if (input.actualTransferDate != null || input.copy == null) {
			return null;
		} else {
			LocalDate baseDateFromFolder = getBaseAjustedDate(input);
			LocalDate baseDateFromSemiActiveDelay = input.getAdjustedBaseDateFromActiveDelay(parameters.get(yearEndParam));
			LocalDate baseDate = max(baseDateFromFolder, baseDateFromSemiActiveDelay);
			return input.calculateSemiActiveBasedOn(baseDate);
		}
	}

	ConfigDependency<String> yearEndParam =
			RMConfigs.YEAR_END_DATE.dependency();

	private LocalDate getBaseAjustedDate(CalculatorInput input) {
		if (input.preferedDecommissioningDateBasedOn == OPEN_DATE || input.folderCloseDate == null) {
			return input.adjustToFinancialYear(input.folderOpenDate);
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
		return asList(expectedTransferDateParam, folderOpenDateParam, folderCloseDateParam, actualTransferDateParam, copyParam,
				decommissioningDateBasedOnParam, numberOfYearWhenSemiActiveVariableDelayParam, yearEndParam,
				requiredDaysBeforeYearEndParam, documentRetentionRulesEnabledParam, folderExpectedTransferDateParam,
				datesAndDateTimesParam, calculatedMetadatasBasedOnFirstTimerangePartParam, retentionRuleScopeParam,
				documentTypeParam);
	}

	private class CalculatorInput {

		LocalDate expectedTransferDate;
		LocalDate folderOpenDate;
		LocalDate folderCloseDate;
		DecommissioningDateBasedOn preferedDecommissioningDateBasedOn;
		CopyRetentionRule copy;
		LocalDate actualTransferDate;
		int numberOfYearWhenSemiActiveVariableDelay;
		String yearEnd;
		int requiredDaysBeforeYearEnd;
		boolean documentRetentionRulesEnabled;
		LocalDate folderExpectedTransferDate;
		DynamicDependencyValues datesAndDateTimes;
		boolean calculatedMetadatasBasedOnFirstTimerangePart;
		RetentionRuleScope retentionRuleScope;
		String documentType;

		public CalculatorInput(CalculatorParameters parameters) {
			this.expectedTransferDate = parameters.get(expectedTransferDateParam);
			this.folderOpenDate = parameters.get(folderOpenDateParam);
			this.folderCloseDate = parameters.get(folderCloseDateParam);
			this.preferedDecommissioningDateBasedOn = parameters.get(decommissioningDateBasedOnParam);
			this.copy = parameters.get(copyParam);
			this.actualTransferDate = parameters.get(actualTransferDateParam);
			this.numberOfYearWhenSemiActiveVariableDelay = parameters.get(numberOfYearWhenSemiActiveVariableDelayParam);
			this.yearEnd = parameters.get(yearEndParam);
			this.requiredDaysBeforeYearEnd = parameters.get(requiredDaysBeforeYearEndParam);
			this.documentRetentionRulesEnabled = parameters.get(documentRetentionRulesEnabledParam);
			this.folderExpectedTransferDate = parameters.get(folderExpectedTransferDateParam);
			this.datesAndDateTimes = parameters.get(datesAndDateTimesParam);
			this.calculatedMetadatasBasedOnFirstTimerangePart = parameters.get(calculatedMetadatasBasedOnFirstTimerangePartParam);
			this.retentionRuleScope = parameters.get(retentionRuleScopeParam);
			this.documentType = parameters.get(documentTypeParam);
		}

		LocalDate calculateSemiActiveBasedOn(LocalDate baseDate) {
			return CalculatorUtils.calculateExpectedTransferDate(copy, baseDate, numberOfYearWhenSemiActiveVariableDelay);
		}

		LocalDate adjustToFinancialYear(LocalDate date) {
			return CalculatorUtils.toNextEndOfYearDateIfNotAlready(date, yearEnd, requiredDaysBeforeYearEnd);
		}

		public LocalDate getAdjustedBaseDateFromActiveDelay(String yearEnd) {
			String metadata = copy.getActiveDateMetadata();

			LocalDate date = datesAndDateTimesParam
					.getDate(metadata, datesAndDateTimes, yearEnd, calculatedMetadatasBasedOnFirstTimerangePart);
			return date == null ? null : adjustToFinancialYear(date);
		}
	}
}
