package com.constellio.model.services.schemas.calculators;

import com.constellio.data.utils.LangUtils;
import com.constellio.model.entities.calculators.CalculatorParameters;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AllAncestorsExceptPrincipalsCalculator extends AbstractAncestorCalculator {

	@Override
	public List<Integer> calculate(CalculatorParameters parameters) {

		Set<Integer> secondaryTaxonomiesConceptIds = new HashSet<>(getParentAllAncestorsExceptPrincipal(parameters));

		List<Integer> pathParts = getPathParts(parameters);
		List<Integer> attachedAncestors = getAttachedAncestors(parameters);
		List<Integer> principalPathNodes = parsePrincipalPathNodes(parameters);

		LangUtils.findMatchesInSortedLists(pathParts, attachedAncestors,
				null,
				(Integer value) -> secondaryTaxonomiesConceptIds.add(value),
				null
		);
		secondaryTaxonomiesConceptIds.removeAll(principalPathNodes);

		List<Integer> secondaryTaxonomiesConceptIdsList = new ArrayList<>(secondaryTaxonomiesConceptIds);
		secondaryTaxonomiesConceptIdsList.sort(null);
		return secondaryTaxonomiesConceptIdsList;
	}

	private List<Integer> getParentAllAncestorsExceptPrincipal(CalculatorParameters parameters) {
		return parameters.get(taxonomiesParam).getAllAncestorExceptPrincipal();
	}

}
