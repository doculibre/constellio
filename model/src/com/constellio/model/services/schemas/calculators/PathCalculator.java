package com.constellio.model.services.schemas.calculators;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.calculators.dependencies.SpecialDependencies;
import com.constellio.model.entities.calculators.dependencies.SpecialDependency;
import com.constellio.model.entities.schemas.MetadataValueType;

public class PathCalculator implements MetadataValueCalculator<List<String>> {

	LocalDependency<List<String>> parentPathDependency = LocalDependency.toAStringList("parentpath");
	SpecialDependency<String> idDependency = SpecialDependencies.IDENTIFIER;

	@Override
	public List<String> calculate(CalculatorParameters parameters) {
		List<String> calculatedValue = new ArrayList<>();

		String id = parameters.get(idDependency);
		List<String> parentPathsValue = parameters.get(parentPathDependency);
		if (parentPathsValue != null && !parentPathsValue.isEmpty()) {
			for (String parentPath : parentPathsValue) {
				calculatedValue.add(parentPath + "/" + id);
			}
		} else {
			calculatedValue.add("/" + id);
		}
		return calculatedValue;
	}

	@Override
	public List<String> getDefaultValue() {
		return null;
	}

	@Override
	public MetadataValueType getReturnType() {
		return STRING;
	}

	@Override
	public boolean isMultiValue() {
		return true;
	}

	@Override
	public List<? extends Dependency> getDependencies() {
		return Arrays.asList(parentPathDependency, idDependency);
	}
}
