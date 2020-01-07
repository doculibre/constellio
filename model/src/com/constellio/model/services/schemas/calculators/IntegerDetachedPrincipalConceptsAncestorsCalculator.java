package com.constellio.model.services.schemas.calculators;

import com.constellio.model.entities.calculators.CalculatorParameters;

import java.util.List;

public class IntegerDetachedPrincipalConceptsAncestorsCalculator extends AbstractAncestorCalculator {

	@Override
	public List<Integer> calculate(CalculatorParameters parameters) {
		return (List<Integer>) calculateAttachedAndDetached(parameters).get("detached");
	}

}
