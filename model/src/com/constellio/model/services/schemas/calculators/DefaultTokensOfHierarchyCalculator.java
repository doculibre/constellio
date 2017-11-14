package com.constellio.model.services.schemas.calculators;

import static com.constellio.model.services.schemas.builders.CommonMetadataBuilder.TOKENS;

import java.util.Arrays;
import java.util.List;

import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.StringListMetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;

public class DefaultTokensOfHierarchyCalculator extends StringListMetadataValueCalculator {

	LocalDependency<List<String>> tokensParam = LocalDependency.toAStringList(TOKENS).whichIsRequired();

	@Override
	public List<String> calculate(CalculatorParameters parameters) {
		return parameters.get(tokensParam);

	}

	@Override
	public List<? extends Dependency> getDependencies() {
		return Arrays.asList(tokensParam);
	}
}
