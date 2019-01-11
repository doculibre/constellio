package com.constellio.model.entities.calculators.dependencies;

import com.constellio.model.entities.security.SecurityModel;

public class SpecialDependencies {

	public static final SpecialDependency<HierarchyDependencyValue> HIERARCHY = new SpecialDependency<>("hierarchy");
	public static final SpecialDependency<AllAuthorizationsTargettingRecordDependencyValue> AURHORIZATIONS_TARGETTING_RECORD =
			new SpecialDependency<>("authorizationsTargettingRecord");

	public static final SpecialDependency<String> IDENTIFIER = new SpecialDependency<>("identifier");
	public static final SpecialDependency<String> PRINCIPAL_TAXONOMY_CODE = new SpecialDependency<>("principalTaxonomyCode");
	public static final SpecialDependency<SecurityModel> SECURITY_MODEL = new SpecialDependency<>("securityModel");

}
