package com.constellio.app.modules.rm.model.calculators.document;

import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.schemas.MetadataValueType;

import java.util.Arrays;
import java.util.List;

public class DocumentHiddenStatusCalculator implements MetadataValueCalculator<Boolean> {

	LocalDependency<Boolean> isModelDependency = LocalDependency.toABoolean(Folder.IS_MODEL).whichIsRequired();

	@Override
	public Boolean calculate(CalculatorParameters parameters) {
		return Boolean.TRUE.equals(parameters.get(isModelDependency)) ? true : null;
	}

	@Override
	public Boolean getDefaultValue() {
		return null;
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
		return Arrays.asList(isModelDependency);
	}
}
