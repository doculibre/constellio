package com.constellio.app.modules.rm.model.calculators.document;

import static com.constellio.app.modules.rm.wrappers.Document.ACTUAL_DEPOSIT_DATE_ENTERED;
import static com.constellio.app.modules.rm.wrappers.Document.ACTUAL_DESTRUCTION_DATE_ENTERED;
import static com.constellio.app.modules.rm.wrappers.Document.FOLDER;
import static com.constellio.app.modules.rm.wrappers.Folder.ACTUAL_DEPOSIT_DATE;
import static java.util.Arrays.asList;

import java.util.List;

import org.joda.time.LocalDate;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.ConfigDependency;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.calculators.dependencies.ReferenceDependency;
import com.constellio.model.entities.schemas.MetadataValueType;

public class DocumentActualDepositDateCalculator implements MetadataValueCalculator<LocalDate> {

	ConfigDependency<Boolean> documentRetentionRulesEnabledParam = RMConfigs.DOCUMENT_RETENTION_RULES.dependency();

	LocalDependency<LocalDate> actualDestructionDateEnteredParam = LocalDependency.toADate(ACTUAL_DESTRUCTION_DATE_ENTERED);

	LocalDependency<LocalDate> actualDepositDateEnteredParam = LocalDependency.toADate(ACTUAL_DEPOSIT_DATE_ENTERED);

	ReferenceDependency<LocalDate> folderActualDepositDateParam = ReferenceDependency.toADate(FOLDER, ACTUAL_DEPOSIT_DATE);

	@Override
	public LocalDate calculate(CalculatorParameters parameters) {
		CalculatorInput input = new CalculatorInput(parameters);
		if (input.documentRetentionRulesEnabled && (input.actualDepositDateEntered != null
				|| input.actualDestructionDateEntered != null)) {
			return input.actualDepositDateEntered;
		} else {
			return input.folderActualDepositDate;
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
		return asList(folderActualDepositDateParam, documentRetentionRulesEnabledParam, actualDepositDateEnteredParam,
				actualDestructionDateEnteredParam);
	}

	class CalculatorInput {
		Boolean documentRetentionRulesEnabled;

		LocalDate actualDepositDateEntered;
		LocalDate actualDestructionDateEntered;

		LocalDate folderActualDepositDate;

		CalculatorInput(CalculatorParameters parameters) {
			documentRetentionRulesEnabled = parameters.get(documentRetentionRulesEnabledParam);
			actualDepositDateEntered = parameters.get(actualDepositDateEnteredParam);
			actualDestructionDateEntered = parameters.get(actualDestructionDateEnteredParam);
			folderActualDepositDate = parameters.get(folderActualDepositDateParam);
		}

	}
}
