package com.constellio.model.services.schemas.testimpl.problems;

import com.constellio.model.entities.calculators.AbstractMetadataValueCalculator;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.schemas.MetadataValueType;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractTestMetadataValueCalculator extends AbstractMetadataValueCalculator<String> {

	@Override
	public String calculate(CalculatorParameters parameters) {
		return null;
	}

	@Override
	public String getDefaultValue() {
		return null;
	}

	@Override
	public MetadataValueType getReturnType() {
		return MetadataValueType.STRING;
	}

	@Override
	public List<Dependency> getDependencies() {
		List<Dependency> dependencies = new ArrayList<Dependency>();
		dependencies.add(LocalDependency.toAString("codeSchema_default_dependence").whichIsRequired());
		return dependencies;
	}

}
