package com.constellio.model.services.schemas.calculators;

import static com.constellio.model.entities.schemas.MetadataValueType.REFERENCE;
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.DynamicDependencyValues;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.DynamicLocalDependency;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;

public class AllReferencesCalculator implements MetadataValueCalculator<List<String>> {

	DynamicLocalDependency dependency = new DynamicLocalDependency() {
		@Override
		public boolean isDependentOf(Metadata metadata) {
			return metadata.getType() == REFERENCE;
		}
	};

	@Override
	public List<String> calculate(CalculatorParameters parameters) {
		DynamicDependencyValues values = parameters.get(dependency);
		Set<String> returnedValues = new HashSet<>();
		for (Metadata metadata : values.getAvailableMetadatas()) {
			Object o = values.getValue(metadata);
			if (o != null) {
				if (metadata.isMultivalue()) {
					returnedValues.addAll((List) o);
				} else {
					returnedValues.add((String) o);
				}
			}
		}

		return new ArrayList<>(returnedValues);
	}

	@Override
	public List<String> getDefaultValue() {
		return new ArrayList<>();
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
		return asList(dependency);
	}
}