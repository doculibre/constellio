package com.constellio.model.services.schemas.calculators;

import com.constellio.data.utils.LangUtils;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.dependencies.HierarchyDependencyValue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SecondaryConceptsIntIdsCalculator extends AbstractAncestorCalculator {

	@Override
	public List<Integer> calculate(CalculatorParameters parameters) {

		HierarchyDependencyValue dependencyValue = parameters.get(taxonomiesParam);

		Set<Integer> ids = new HashSet<>();
		ids.addAll(dependencyValue.getSecondaryConceptsIntIds());
		ids.addAll(dependencyValue.getSecondaryConceptsIntIdsFromParent());

		return LangUtils.toSortedList(ids);

		//		Set<Integer> secondaryTaxonomiesConceptIds = new HashSet<>(getParentAllAncestorsExceptPrincipal(parameters));
		//
		//		List<Integer> pathParts = getPathParts(parameters);
		//		List<Integer> attachedAncestors = getAttachedAncestors(parameters);
		//		List<Integer> principalPathNodes = parsePrincipalPathNodes(parameters);
		//
		//		LangUtils.findMatchesInSortedLists(pathParts, attachedAncestors,
		//				null,
		//				(Integer value) -> secondaryTaxonomiesConceptIds.add(value),
		//				null
		//		);
		//		secondaryTaxonomiesConceptIds.removeAll(principalPathNodes);
		//
		//		List<Integer> secondaryTaxonomiesConceptIdsList = new ArrayList<>(secondaryTaxonomiesConceptIds);
		//		secondaryTaxonomiesConceptIdsList.sort(null);
		//		return secondaryTaxonomiesConceptIdsList;
	}


}
