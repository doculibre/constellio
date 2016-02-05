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

public class FolderAppliedUniformSubdivisionCalculator implements MetadataValueCalculator<String> {

	ReferenceDependency<String> parentUniformSubdivisionParam = ReferenceDependency
			.toAReference(Folder.PARENT_FOLDER, Folder.UNIFORM_SUBDIVISION);
	LocalDependency<String> enteredUniformSubdivisionParam = LocalDependency.toAReference(Folder.UNIFORM_SUBDIVISION_ENTERED);

	@Override
	public String calculate(CalculatorParameters parameters) {
		String parentUniformSubdivision = parameters.get(parentUniformSubdivisionParam);
		String enteredUniformSubdivision = parameters.get(enteredUniformSubdivisionParam);

		if (parentUniformSubdivision != null) {
			return parentUniformSubdivision;
		} else {
			return enteredUniformSubdivision;
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
		return asList(parentUniformSubdivisionParam, enteredUniformSubdivisionParam);
	}
}
