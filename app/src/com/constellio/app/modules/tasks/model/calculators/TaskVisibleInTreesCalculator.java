package com.constellio.app.modules.tasks.model.calculators;

import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.SpecialDependencies;
import com.constellio.model.entities.calculators.dependencies.SpecialDependency;
import com.constellio.model.entities.schemas.MetadataValueType;

import java.util.Collections;
import java.util.List;

public class TaskVisibleInTreesCalculator implements MetadataValueCalculator<Boolean> {

	SpecialDependency<String> identifier = SpecialDependencies.IDENTIFIER;

	private static boolean OH_HELL_NO = false;

	@Override
	public Boolean calculate(CalculatorParameters parameters) {
		return OH_HELL_NO;
	}

	@Override
	public Boolean getDefaultValue() {
		return OH_HELL_NO;
	}

	@Override
	public MetadataValueType getReturnType() {
		return MetadataValueType.BOOLEAN;
	}

	@Override
	public boolean isMultiValue() {
		return false;
	}

	@Override
	public List<? extends Dependency> getDependencies() {
		return Collections.singletonList(identifier);
	}
}
