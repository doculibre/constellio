package com.constellio.app.modules.rm.model.calculators;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;

import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.calculators.dependencies.ReferenceDependency;
import com.constellio.model.entities.schemas.MetadataValueType;

public class AdministrativeUnitAncestorsCalculator implements MetadataValueCalculator<List<String>> {

	LocalDependency<String> parentParam = LocalDependency.toAReference(AdministrativeUnit.PARENT);
	ReferenceDependency<List<String>> parentAncestorsParam = ReferenceDependency
			.toAReference(AdministrativeUnit.PARENT, AdministrativeUnit.ANCESTORS).whichIsMultivalue();

	@Override
	public List<String> calculate(CalculatorParameters parameters) {
		String parent = parameters.get(parentParam);
		List<String> parentAncestors = parameters.get(parentAncestorsParam);

		List<String> ancestors = new ArrayList<>(parentAncestors);

		if (parent != null) {
			ancestors.add(parent);
		}

		return ancestors;
	}

	@Override
	public List<String> getDefaultValue() {
		return new ArrayList<>();
	}

	@Override
	public MetadataValueType getReturnType() {
		return MetadataValueType.REFERENCE;
	}

	@Override
	public boolean isMultiValue() {
		return true;
	}

	@Override
	public List<? extends Dependency> getDependencies() {
		return asList(parentParam, parentAncestorsParam);
	}
}
