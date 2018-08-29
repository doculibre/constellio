package com.constellio.model.services.schemas.calculators;

import com.constellio.data.utils.LangUtils;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.StringListMetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.constellio.model.services.schemas.builders.CommonMetadataBuilder.LOGICALLY_DELETED;
import static com.constellio.model.services.schemas.builders.CommonMetadataBuilder.TOKENS;
import static com.constellio.model.services.schemas.builders.CommonMetadataBuilder.VISIBLE_IN_TREES;

public class DefaultTokensOfHierarchyCalculator extends StringListMetadataValueCalculator {

	LocalDependency<Boolean> logicallyDeletedParam = LocalDependency.toABoolean(LOGICALLY_DELETED);
	LocalDependency<Boolean> visibleInTreesParam = LocalDependency.toABoolean(VISIBLE_IN_TREES);
	LocalDependency<List<String>> tokensParam = LocalDependency.toAStringList(TOKENS).whichIsRequired();

	@Override
	public List<String> calculate(CalculatorParameters parameters) {
		Set<String> allTokens = new HashSet<>();

		List<String> tokens = parameters.get(tokensParam);

		Boolean logicallyDeleted = parameters.get(logicallyDeletedParam);
		Boolean visibleInTrees = parameters.get(visibleInTreesParam);
		String prefix = LangUtils.isTrueOrNull(visibleInTrees) ? "" : "z";

		if (tokens != null && LangUtils.isFalseOrNull(logicallyDeleted)) {
			for (String token : tokens) {
				allTokens.add(prefix + token);
			}
		}

		List<String> tokensList = new ArrayList<>(allTokens);
		Collections.sort(tokensList);
		return tokensList;

	}

	@Override
	public List<? extends Dependency> getDependencies() {
		return Arrays.asList(tokensParam, logicallyDeletedParam, visibleInTreesParam);
	}
}
