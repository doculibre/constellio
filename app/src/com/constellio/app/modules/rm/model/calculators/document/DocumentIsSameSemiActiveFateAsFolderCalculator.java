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

public class DocumentIsSameSemiActiveFateAsFolderCalculator implements MetadataValueCalculator<Boolean> {

	LocalDependency<LocalDate> documentExpectedTransferDateParam =
			LocalDependency.toADate(Document.FOLDER_EXPECTED_TRANSFER_DATE);
	ReferenceDependency<LocalDate> folderExpectedTransferDateParam = ReferenceDependency.toADate(
			Document.FOLDER, Folder.EXPECTED_TRANSFER_DATE);

	@Override
	public Boolean calculate(CalculatorParameters parameters) {
		CalculatorInput input = new CalculatorInput(parameters);
		return input.isSameSemiActiveDate();
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
		return asList(documentExpectedTransferDateParam, folderExpectedTransferDateParam);
	}

	private class CalculatorInput {
		LocalDate documentExpectedTransferDate;
		LocalDate folderExpectedTransferDate;

		private CalculatorInput(CalculatorParameters parameters) {
			documentExpectedTransferDate = parameters.get(documentExpectedTransferDateParam);
			folderExpectedTransferDate = parameters.get(folderExpectedTransferDateParam);
		}

		boolean isSameSemiActiveDate() {
			return areNullableEqual(documentExpectedTransferDate, folderExpectedTransferDate);
		}
	}
}
