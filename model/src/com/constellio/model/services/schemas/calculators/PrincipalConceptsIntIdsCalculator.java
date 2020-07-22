package com.constellio.model.services.schemas.calculators;

public class PrincipalConceptsIntIdsCalculator extends AbstractAncestorCalculator {

	//	@Override
	//	public List<Integer> calculate(CalculatorParameters parameters) {
	//		HierarchyDependencyValue dependencyValue = parameters.get(taxonomiesParam);
	//
	//		Set<Integer> ids = new HashSet<>();
	//		ids.addAll(dependencyValue.getPrincipalConceptsIntIds());
	//		ids.addAll(dependencyValue.getPrincipalConceptsIntIdsFromParent());
	//
	//		return LangUtils.toSortedList(ids);
	//
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
	//	}


}
