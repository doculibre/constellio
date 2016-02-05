package com.constellio.app.modules.rm.model.calculators.folder;

import static java.util.Arrays.asList;

import java.util.List;

import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.calculators.dependencies.ReferenceDependency;
import com.constellio.model.entities.schemas.MetadataValueType;

public class FolderAppliedFilingSpaceCalculator implements MetadataValueCalculator<String> {

	ReferenceDependency<String> parentFilingSpaceParam = ReferenceDependency
			.toAReference(Folder.PARENT_FOLDER, Folder.FILING_SPACE);
	LocalDependency<String> enteredFilingSpaceParam = LocalDependency.toAReference(Folder.FILING_SPACE_ENTERED);

	@Override
	public String calculate(CalculatorParameters parameters) {
		String parentFilingSpace = parameters.get(parentFilingSpaceParam);
		String enteredFilingSpace = parameters.get(enteredFilingSpaceParam);

		if (parentFilingSpace != null) {
			return parentFilingSpace;
		} else {
			return enteredFilingSpace;
		}

	}

	@Override
	public String getDefaultValue() {
		return null;
	}

	@Override
	public MetadataValueType getReturnType() {
		return MetadataValueType.REFERENCE;
	}

	@Override
	public boolean isMultiValue() {
		return false;
	}

	@Override
	public List<? extends Dependency> getDependencies() {
		return asList(parentFilingSpaceParam, enteredFilingSpaceParam);
	}
}
