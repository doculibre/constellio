package com.constellio.model.entities.calculators.dependencies;

public class SpecialDependencies {

	public static final SpecialDependency<HierarchyDependencyValue> HIERARCHY = new SpecialDependency<>("hierarchy");
	public static final SpecialDependency<String> IDENTIFIER = new SpecialDependency<>("identifier");
	public static final SpecialDependency<String> PRINCIPAL_TAXONOMY_CODE = new SpecialDependency<>("principalTaxonomyCode");

}
