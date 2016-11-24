package com.constellio.app.modules.rm.model.calculators.category;

import java.util.List;

import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.schemas.MetadataValueType;

public class CategoryDefaultRetentionRuleCalculator implements MetadataValueCalculator {
	@Override
	public Object calculate(CalculatorParameters parameters) {
		return null;
	}

	@Override
	public Object getDefaultValue() {
		return null;
	}

	@Override
	public MetadataValueType getReturnType() {
		return null;
	}

	@Override
	public boolean isMultiValue() {
		return false;
	}

	@Override
	public List<? extends Dependency> getDependencies() {
		return null;
	}
}
