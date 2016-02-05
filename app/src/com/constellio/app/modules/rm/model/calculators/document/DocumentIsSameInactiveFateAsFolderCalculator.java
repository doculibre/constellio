package com.constellio.app.modules.rm.model.calculators.document;

import static com.constellio.data.utils.LangUtils.areNullableEqual;
import static java.util.Arrays.asList;

import java.util.List;

import org.joda.time.LocalDate;

import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.calculators.dependencies.ReferenceDependency;
import com.constellio.model.entities.schemas.MetadataValueType;

public class DocumentIsSameInactiveFateAsFolderCalculator implements MetadataValueCalculator<Boolean> {

	LocalDependency<LocalDate> documentExpectedDepositDateParam = LocalDependency.toADate(
			Document.FOLDER_EXPECTED_DEPOSIT_DATE);
	ReferenceDependency<LocalDate> folderExpectedDepositDateParam = ReferenceDependency.toADate(
			Document.FOLDER, Folder.EXPECTED_DEPOSIT_DATE);

	LocalDependency<LocalDate> documentExpectedDestructionDateParam = LocalDependency.toADate(
			Document.FOLDER_EXPECTED_DESTRUCTION_DATE);
	ReferenceDependency<LocalDate> folderExpectedDestructionDateParam = ReferenceDependency.toADate(
			Document.FOLDER, Folder.EXPECTED_DESTRUCTION_DATE);

	@Override
	public Boolean calculate(CalculatorParameters parameters) {
		CalculatorInput input = new CalculatorInput(parameters);
		return input.isSameExpectedDepositDate() && input.isSameExpectedDestructionDate();
	}

	@Override
	public Boolean getDefaultValue() {
		return false;
	}

	@Override
	public MetadataValueType getReturnType() {
		return MetadataValueType.BOOLEAN;
	}

	@Override
	public boolean isMultiValue() {
		return false;
	}

	@Override
	public List<? extends Dependency> getDependencies() {
		return asList(documentExpectedDepositDateParam, folderExpectedDepositDateParam,
				documentExpectedDestructionDateParam, folderExpectedDestructionDateParam);
	}

	private class CalculatorInput {
		LocalDate documentExpectedDepositDate;
		LocalDate folderExpectedDepositDate;
		LocalDate documentExpectedDestructionDate;
		LocalDate folderExpectedDestructionDate;

		private CalculatorInput(CalculatorParameters parameters) {
			documentExpectedDepositDate = parameters.get(documentExpectedDepositDateParam);
			folderExpectedDepositDate = parameters.get(folderExpectedDepositDateParam);
			documentExpectedDestructionDate = parameters.get(documentExpectedDestructionDateParam);
			folderExpectedDestructionDate = parameters.get(folderExpectedDestructionDateParam);
		}

		boolean isSameExpectedDepositDate() {
			return areNullableEqual(documentExpectedDepositDate, folderExpectedDepositDate);
		}

		boolean isSameExpectedDestructionDate() {
			return areNullableEqual(documentExpectedDestructionDate, folderExpectedDestructionDate);
		}

	}
}
