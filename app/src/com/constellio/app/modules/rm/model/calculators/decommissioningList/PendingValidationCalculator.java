package com.constellio.app.modules.rm.model.calculators.decommissioningList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.structures.DecomListValidation;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.schemas.MetadataValueType;

public class PendingValidationCalculator implements MetadataValueCalculator<List<String>> {
	LocalDependency<List<DecomListValidation>> validations = LocalDependency.toAStructure(DecommissioningList.VALIDATIONS)
			.whichIsMultivalue();

	@Override
	public List<String> calculate(CalculatorParameters parameters) {
		List<String> result = new ArrayList<>();
		for (DecomListValidation validation : parameters.get(validations)) {
			if (!validation.isValidated()) {
				result.add(validation.getUserId());
			}
		}
		return result;
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
		return Arrays.asList(validations);
	}
}
