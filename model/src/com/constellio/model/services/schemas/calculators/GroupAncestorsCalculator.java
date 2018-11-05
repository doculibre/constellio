package com.constellio.model.services.schemas.calculators;

import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.ReferenceDependency;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.schemas.MetadataValueType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GroupAncestorsCalculator implements MetadataValueCalculator<List<String>> {

	ReferenceDependency<List<String>> parentAncestorsDependency = ReferenceDependency
			.toAString(Group.PARENT, Group.ANCESTORS).whichIsMultivalue();

	@Override
	public List<String> calculate(CalculatorParameters parameters) {
		List<String> ancestors = new ArrayList<>();
		List<String> parentAncestors = parameters.get(parentAncestorsDependency);

		ancestors.add(parameters.getId());
		ancestors.addAll(parentAncestors);

		return ancestors;
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
		return Arrays.asList(parentAncestorsDependency);
	}
}
