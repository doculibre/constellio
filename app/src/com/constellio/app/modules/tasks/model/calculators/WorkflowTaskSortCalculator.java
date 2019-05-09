package com.constellio.app.modules.tasks.model.calculators;

import com.constellio.model.entities.calculators.AbstractMetadataValueCalculator;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.SpecialDependencies;
import com.constellio.model.entities.calculators.dependencies.SpecialDependency;
import com.constellio.model.entities.schemas.MetadataValueType;

import java.util.List;

import static java.util.Arrays.asList;

/**
 * *** USED BY BETA_WORKFLOW BETA ***
 */
public class WorkflowTaskSortCalculator extends AbstractMetadataValueCalculator<Double> {
	SpecialDependency<String> identifierParam = SpecialDependencies.IDENTIFIER;

	@Override
	public Double calculate(CalculatorParameters parameters) {
		String identifier = parameters.get(identifierParam);
		return 0.0;
	}

	@Override
	public Double getDefaultValue() {
		return 0.0;
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
		return asList(identifierParam);
	}
}
