package com.constellio.app.modules.tasks.model.calculators;

import static java.util.Arrays.asList;

import java.util.List;

import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.ReferenceDependency;
import com.constellio.model.entities.calculators.dependencies.SpecialDependencies;
import com.constellio.model.entities.calculators.dependencies.SpecialDependency;
import com.constellio.model.entities.schemas.MetadataValueType;

/**
 * *** USED BY BETA_WORKFLOW BETA ***
 */
public class WorkflowTaskSortCalculator implements MetadataValueCalculator<Double> {
	SpecialDependency<String> identifierParam = SpecialDependencies.IDENTIFIER;
	ReferenceDependency<List<Double>> childrenIndexesParam = ReferenceDependency
			.toANumber(Task.BETA_NEXT_TASKS, Task.BETA_WORKFLOW_TASK_SORT).whichIsMultivalue();

	@Override
	public Double calculate(CalculatorParameters parameters) {
		String identifier = parameters.get(identifierParam);
		List<Double> childrenIndexes = parameters.get(childrenIndexesParam);

		double max = 0;

		if (childrenIndexes != null) {
			for (Double value : childrenIndexes) {
				if (value != null && max < value) {
					max = value;
				}
			}
		}

		return max == 0 ? (double) identifier.hashCode() * 1000 : (max + 1);

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
		return asList(identifierParam, childrenIndexesParam);
	}
}
