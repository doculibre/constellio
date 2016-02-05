package com.constellio.app.modules.rm.model.calculators.document;

import static java.util.Arrays.asList;

import java.util.List;

import org.joda.time.LocalDate;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.ConfigDependency;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.calculators.dependencies.ReferenceDependency;
import com.constellio.model.entities.schemas.MetadataValueType;

public class DocumentActualTransferDateCalculator implements MetadataValueCalculator<LocalDate> {

	ConfigDependency<Boolean> documentRetentionRulesEnabledParam = RMConfigs.DOCUMENT_RETENTION_RULES.dependency();

	LocalDependency<LocalDate> actualTransferDateEnteredParam = LocalDependency
			.toADate(Document.ACTUAL_TRANSFER_DATE_ENTERED);

	ReferenceDependency<LocalDate> folderActualTransferDateParam = ReferenceDependency
			.toADate(Document.FOLDER, Folder.ACTUAL_TRANSFER_DATE);

	@Override
	public LocalDate calculate(CalculatorParameters parameters) {
		CalculatorInput input = new CalculatorInput(parameters);
		if (input.documentRetentionRulesEnabled && input.actualTransferDateEntered != null) {
			return input.actualTransferDateEntered;
		} else {
			return input.folderActualTransferDate;
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
		return asList(folderActualTransferDateParam, actualTransferDateEnteredParam, documentRetentionRulesEnabledParam);
	}

	class CalculatorInput {
		Boolean documentRetentionRulesEnabled;

		LocalDate folderActualTransferDate;

		LocalDate actualTransferDateEntered;

		CalculatorInput(CalculatorParameters parameters) {
			documentRetentionRulesEnabled = parameters.get(documentRetentionRulesEnabledParam);
			actualTransferDateEntered = parameters.get(actualTransferDateEnteredParam);
			folderActualTransferDate = parameters.get(folderActualTransferDateParam);
		}

	}
}
