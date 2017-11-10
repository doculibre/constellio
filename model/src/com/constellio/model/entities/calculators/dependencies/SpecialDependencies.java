package com.constellio.model.entities.calculators.dependencies;

public class SpecialDependencies {

	public static final SpecialDependency<HierarchyDependencyValue> HIERARCHY = new SpecialDependency<>("hierarchy");
	public static final SpecialDependency<AllPrincipalsAuthsDependencyValue> ALL_PRINCIPALS =
			new SpecialDependency<>("allPrincipals");
	public static final SpecialDependency<AllAuthorizationsTargettingRecordDependencyValue> AURHORIZATIONS_TARGETTING_RECORD =
			new SpecialDependency<>("authorizationsTargettingRecord");

	public static final SpecialDependency<String> IDENTIFIER = new SpecialDependency<>("identifier");
	public static final SpecialDependency<String> PRINCIPAL_TAXONOMY_CODE = new SpecialDependency<>("principalTaxonomyCode");

}
