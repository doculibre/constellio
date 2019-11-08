package com.constellio.model.services.schemas.calculators;

import com.constellio.data.utils.LangUtils;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.services.records.RecordId;

import java.util.ArrayList;
import java.util.List;

public class AllAncestorsExceptPrincipalsCalculator extends AbstractAncestorCalculator {

	@Override
	public List<Integer> calculate(CalculatorParameters parameters) {

		List<Integer> secondaryTaxonomiesConceptIds = new ArrayList<>(getParentAllAncestorsExceptPrincipal(parameters));

		List<Integer> pathParts = getPathParts(parameters);
		List<Integer> attachedAncestors = getAttachedAncestors(parameters);
		List<Integer> principalPathNodes = parsePrincipalPathNodes(parameters);

		List<RecordId> stillAttachedPrincipalConceptIds = new ArrayList<>();
		LangUtils.findMatchesInSortedLists(pathParts, attachedAncestors,
				null,
				(Integer value) -> secondaryTaxonomiesConceptIds.add(value),
				null
		);
		secondaryTaxonomiesConceptIds.removeAll(principalPathNodes);

		return secondaryTaxonomiesConceptIds;
	}

	private List<Integer> getParentAllAncestorsExceptPrincipal(CalculatorParameters parameters) {
		return parameters.get(taxonomiesParam).getAllAncestorExceptPrincipal();
	}

}
