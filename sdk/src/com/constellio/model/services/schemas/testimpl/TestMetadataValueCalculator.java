package com.constellio.model.services.schemas.testimpl;

import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.schemas.MetadataValueType;

import java.util.ArrayList;
import java.util.List;

public class TestMetadataValueCalculator implements MetadataValueCalculator<String> {

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

	@Override
	public boolean isMultiValue() {
		return false;
	}
}
