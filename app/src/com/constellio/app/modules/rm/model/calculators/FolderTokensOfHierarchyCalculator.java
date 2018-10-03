package com.constellio.app.modules.rm.model.calculators;

import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.data.utils.LangUtils;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.StringListMetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.constellio.model.services.schemas.builders.CommonMetadataBuilder.LOGICALLY_DELETED;
import static com.constellio.model.services.schemas.builders.CommonMetadataBuilder.REMOVED_AUTHORIZATIONS;
import static com.constellio.model.services.schemas.builders.CommonMetadataBuilder.TOKENS;
import static com.constellio.model.services.schemas.builders.CommonMetadataBuilder.VISIBLE_IN_TREES;
import static java.util.Arrays.asList;

public class FolderTokensOfHierarchyCalculator extends StringListMetadataValueCalculator {

	LocalDependency<List<String>> tokensParam = LocalDependency.toAStringList(TOKENS);
	LocalDependency<List<String>> removedAuthorizationsParam = LocalDependency.toAStringList(REMOVED_AUTHORIZATIONS);
	LocalDependency<List<String>> subFoldersParam = LocalDependency.toAStringList(Folder.SUB_FOLDERS_TOKENS);
	LocalDependency<List<String>> documentsParam = LocalDependency.toAStringList(Folder.DOCUMENTS_TOKENS);

	LocalDependency<Boolean> logicallyDeletedParam = LocalDependency.toABoolean(LOGICALLY_DELETED);
	LocalDependency<Boolean> visibleInTreesParam = LocalDependency.toABoolean(VISIBLE_IN_TREES);

	@Override
	public List<String> calculate(CalculatorParameters parameters) {
		Set<String> allPositiveTokens = new HashSet<>();
		Set<String> allNegativeTokens = new HashSet<>();

		List<String> tokens = parameters.get(tokensParam);
		List<String> subFoldersHierarchyTokens = parameters.get(subFoldersParam);
		List<String> documentsHierarchyTokens = parameters.get(documentsParam);

		List<String> negativeTokensRemovedFurtherInHierarchy = new ArrayList<>();

		Boolean logicallyDeleted = parameters.get(logicallyDeletedParam);
		Boolean visibleInTrees = parameters.get(visibleInTreesParam);
		String prefix = LangUtils.isTrueOrNull(visibleInTrees) ? "" : "z";

		if (LangUtils.isFalseOrNull(logicallyDeleted)) {
			if (tokens != null) {
				for (String token : tokens) {
					if (token.startsWith("n")) {
						allNegativeTokens.add(prefix + token);

					} else {
						allPositiveTokens.add(prefix + token);
					}
				}
			}
			if (subFoldersHierarchyTokens != null) {
				for (String token : subFoldersHierarchyTokens) {
					if (token.startsWith("n")) {
						//discarded

					} else if (token.startsWith("-n")) {
						negativeTokensRemovedFurtherInHierarchy.add(token);
						allPositiveTokens.add(token.replace("-n", ""));
						allPositiveTokens.add(token.replace("-n", "").replace("_", "f_"));

					} else {
						allPositiveTokens.add(token);
					}
				}
			}
			if (documentsHierarchyTokens != null) {
				for (String token : documentsHierarchyTokens) {
					if (token.startsWith("n")) {
						//discarded

					} else if (token.startsWith("-n")) {
						negativeTokensRemovedFurtherInHierarchy.add(token);
						allPositiveTokens.add(token.replace("-n", ""));
						allPositiveTokens.add(token.replace("-n", "").replace("_", "d_"));

					} else {
						allPositiveTokens.add(token);
					}
				}
			}

		}

		Set<String> allTokens = new HashSet<>();

		allTokens.addAll(allPositiveTokens);

		for (String negativeToken : allNegativeTokens) {
			if (!negativeTokensRemovedFurtherInHierarchy.contains("-" + negativeToken)) {
				allTokens.add(negativeToken);
			}
		}

		List<String> tokensList = new ArrayList<>(allTokens);
		Collections.sort(tokensList);
		return tokensList;
	}

	@Override
	public List<? extends Dependency> getDependencies() {
		return asList(tokensParam, subFoldersParam, documentsParam, logicallyDeletedParam, visibleInTreesParam);
	}
}
