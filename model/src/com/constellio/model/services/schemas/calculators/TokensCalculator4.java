package com.constellio.model.services.schemas.calculators;

import com.constellio.data.utils.KeyListMap;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.*;
import com.constellio.model.entities.enums.GroupAuthorizationsInheritance;
import com.constellio.model.entities.records.wrappers.SolrAuthorizationDetails;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.schemas.builders.CommonMetadataBuilder;

import java.util.*;
import java.util.Map.Entry;

import static com.constellio.model.services.migrations.ConstellioEIMConfigs.GROUP_AUTHORIZATIONS_INHERITANCE;
import static com.constellio.model.services.schemas.builders.CommonMetadataBuilder.*;

public class TokensCalculator4 implements MetadataValueCalculator<List<String>> {

	LocalDependency<List<String>> manualTokensParam = LocalDependency.toAStringList(MANUAL_TOKENS);

	LocalDependency<Boolean> logicallyDeletedParam = LocalDependency.toABoolean(LOGICALLY_DELETED);

	LocalDependency<Boolean> visibleInTreesParam = LocalDependency.toABoolean(VISIBLE_IN_TREES);

	ReferenceDependency<SortedMap<String, List<String>>> authorizationsRolesParam = ReferenceDependency.toAString(
			CommonMetadataBuilder.NON_TAXONOMY_AUTHORIZATIONS, SolrAuthorizationDetails.ROLES).whichIsMultivalue()
			.whichAreReferencedMultiValueGroupedByReference();

	SpecialDependency<AllPrincipalsAuthsDependencyValue> allPrincipalsAuthsParam = SpecialDependencies.ALL_PRINCIPALS;

	ConfigDependency<GroupAuthorizationsInheritance> groupAuthorizationsInheritanceParam = GROUP_AUTHORIZATIONS_INHERITANCE
			.dependency();

	@Override
	public List<String> calculate(CalculatorParameters parameters) {
		Set<String> tokens = new HashSet<>();

		List<String> manualTokens = parameters.get(manualTokensParam);
		AllPrincipalsAuthsDependencyValue principalsAuthorizations = parameters.get(allPrincipalsAuthsParam);

		SortedMap<String, List<String>> authorizationsRoles = parameters.get(authorizationsRolesParam);

		KeyListMap<String, String> principalsTokens = principalsAuthorizations.getPrincipalIdsWithAnyAuthorization(
				authorizationsRoles, parameters.get(groupAuthorizationsInheritanceParam));

		String typeSmallCode = parameters.getSchemaType().getSmallCode();
		if (typeSmallCode == null) {
			typeSmallCode = parameters.getSchemaType().getCode();
		}
		for (Entry<String, List<String>> entry : principalsTokens.getMapEntries()) {
			for (String access : entry.getValue()) {
				if (Role.READ.equals(access)) {
					tokens.add("r_" + entry.getKey());
					tokens.add("r" + typeSmallCode + "_" + entry.getKey());

				} else if (Role.WRITE.equals(access)) {
					tokens.add("r_" + entry.getKey());
					tokens.add("w_" + entry.getKey());//TODO Check to remove this token
					tokens.add("r" + typeSmallCode + "_" + entry.getKey());//TODO Check to remove this token
					tokens.add("w" + typeSmallCode + "_" + entry.getKey());

				} else if (Role.DELETE.equals(access)) {
					tokens.add("r_" + entry.getKey());
					tokens.add("r" + typeSmallCode + "_" + entry.getKey());

				} else {
					tokens.add(access + "_" + entry.getKey());
				}
			}
		}

		tokens.addAll(manualTokens);

		List<String> tokensList = new ArrayList<>(tokens);
		Collections.sort(tokensList);
		return tokensList;
	}

	@Override
	public List<String> getDefaultValue() {
		return Collections.emptyList();
	}

	@Override
	public MetadataValueType getReturnType() {
		return MetadataValueType.STRING;
	}

	@Override
	public boolean isMultiValue() {
		return true;
	}

	@Override
	public List<? extends Dependency> getDependencies() {
		return Arrays.asList(allPrincipalsAuthsParam, authorizationsRolesParam, manualTokensParam, logicallyDeletedParam,
				visibleInTreesParam, groupAuthorizationsInheritanceParam);
	}
}
