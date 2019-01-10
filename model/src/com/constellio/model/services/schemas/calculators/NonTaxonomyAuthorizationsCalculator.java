package com.constellio.model.services.schemas.calculators;

import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.ReferenceListMetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NonTaxonomyAuthorizationsCalculator extends ReferenceListMetadataValueCalculator {

	@Override
	public List<String> calculate(CalculatorParameters parameters) {
		return new ArrayList<>();
	}


	@Override
	public List<? extends Dependency> getDependencies() {
		return Collections.emptyList();
	}
}
