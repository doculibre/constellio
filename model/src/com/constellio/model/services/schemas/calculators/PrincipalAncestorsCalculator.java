package com.constellio.model.services.schemas.calculators;

import com.constellio.data.utils.LangUtils;
import com.constellio.model.entities.calculators.CalculatorParameters;

import java.util.List;

public class PrincipalAncestorsCalculator extends AbstractAncestorCalculator {

	@Override
	public List<Integer> calculate(CalculatorParameters parameters) {
		List<Integer> principalPathNodes = parsePrincipalPathNodes(parameters);
		List<Integer> pathParts = getPathParts(parameters);

		return LangUtils.findMatchesInSortedLists(principalPathNodes, pathParts);

	}

}
