package com.constellio.model.services.schemas.calculators;

import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.HierarchyDependencyValue;
import com.constellio.model.entities.calculators.dependencies.SpecialDependencies;
import com.constellio.model.entities.calculators.dependencies.SpecialDependency;
import com.constellio.model.entities.schemas.MetadataValueType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class InheritedAuthorizationsCalculator implements MetadataValueCalculator<List<String>> {

	SpecialDependency<HierarchyDependencyValue> inheritedAuthorizationsParam = SpecialDependencies.HIERARCHY;

	@Override
	public List<String> calculate(CalculatorParameters parameters) {
		return new ArrayList<>();
	}

	@Override
	public List<String> getDefaultValue() {
		return Collections.emptyList();
	}

	@Override
	public MetadataValueType getReturnType() {
		return MetadataValueType.STRING;
	}

	@Override
	public boolean isMultiValue() {
		return true;
	}

	@Override
	public List<? extends Dependency> getDependencies() {
		return Arrays.asList(inheritedAuthorizationsParam);
	}
}
