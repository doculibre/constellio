package com.constellio.model.services.schemas.calculators;

import com.constellio.data.utils.LangUtils;
import com.constellio.model.entities.calculators.CalculatorParameters;

import java.util.ArrayList;
import java.util.List;

public class IntegerAttachedPrincipalConceptsAncestorsCalculator extends AbstractAncestorCalculator {

	@Override
	public List<Integer> calculate(CalculatorParameters parameters) {
		List<Integer> pathParts = getPathParts(parameters);
		List<Integer> attachedAncestors = getAttachedAncestors(parameters);

		List<Integer> stillAttachedPrincipalConceptIds = new ArrayList<>();

		LangUtils.findMatchesInSortedLists(pathParts, attachedAncestors,
				(Integer value) -> stillAttachedPrincipalConceptIds.add(value),
				null,
				null
		);

		return stillAttachedPrincipalConceptIds;
	}

}
