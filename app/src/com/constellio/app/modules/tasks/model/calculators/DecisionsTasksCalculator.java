package com.constellio.app.modules.tasks.model.calculators;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;

import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.structures.MapStringStringStructure;

public class DecisionsTasksCalculator implements MetadataValueCalculator<List<String>> {
	LocalDependency<MapStringStringStructure> decisionsParams = LocalDependency.toAStructure(Task.BETA_NEXT_TASKS_DECISIONS);

	@Override
	public List<String> calculate(CalculatorParameters parameters) {
		List<String> values = new ArrayList<>();
		MapStringStringStructure decisions = parameters.get(decisionsParams);

		if (decisions != null) {
			for (String value : decisions.values()) {
				if (value != null && !"NO_VALUE".equals(value)) {
					values.add(value);
				}
			}
		}

		return values;
	}

	@Override
	public List<String> getDefaultValue() {
		return null;
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
		return asList(decisionsParams);
	}
}
