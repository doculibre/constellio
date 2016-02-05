package com.constellio.app.modules.rm.model.calculators.category;

import static java.util.Arrays.asList;

import java.util.List;

import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;

public class CategoryLevelCalculator implements MetadataValueCalculator<Double> {

	LocalDependency<List<String>> pathParam = LocalDependency.toAString(Schemas.PATH.getLocalCode()).whichIsMultivalue();

	@Override
	public Double calculate(CalculatorParameters parameters) {
		List<String> paths = parameters.get(pathParam);
		if (paths.isEmpty()) {
			return 0.0;
		} else {
			return (double) paths.get(0).split("/").length - 3;
		}
	}

	@Override
	public Double getDefaultValue() {
		return null;
	}

	@Override
	public MetadataValueType getReturnType() {
		return MetadataValueType.NUMBER;
	}

	@Override
	public boolean isMultiValue() {
		return false;
	}

	@Override
	public List<? extends Dependency> getDependencies() {
		return asList(pathParam);
	}
}
