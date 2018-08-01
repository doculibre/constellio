package com.constellio.app.modules.rm.model.calculators;

import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.data.utils.LangUtils;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.StringListMetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;

import java.util.*;

import static com.constellio.model.services.schemas.builders.CommonMetadataBuilder.*;
import static java.util.Arrays.asList;

public class FolderTokensOfHierarchyCalculator extends StringListMetadataValueCalculator {

	LocalDependency<List<String>> tokensParam = LocalDependency.toAStringList(TOKENS);
	LocalDependency<List<String>> subFoldersParam = LocalDependency.toAStringList(Folder.SUB_FOLDERS_TOKENS);
	LocalDependency<List<String>> documentsParam = LocalDependency.toAStringList(Folder.DOCUMENTS_TOKENS);

	LocalDependency<Boolean> logicallyDeletedParam = LocalDependency.toABoolean(LOGICALLY_DELETED);
	LocalDependency<Boolean> visibleInTreesParam = LocalDependency.toABoolean(VISIBLE_IN_TREES);

	@Override
	public List<String> calculate(CalculatorParameters parameters) {
		Set<String> allTokens = new HashSet<>();

		List<String> tokens = parameters.get(tokensParam);
		List<String> subFoldersHierarchyTokens = parameters.get(subFoldersParam);
		List<String> documentsHierarchyTokens = parameters.get(documentsParam);

		Boolean logicallyDeleted = parameters.get(logicallyDeletedParam);
		Boolean visibleInTrees = parameters.get(visibleInTreesParam);
		String prefix = LangUtils.isTrueOrNull(visibleInTrees) ? "" : "z";

		if (LangUtils.isFalseOrNull(logicallyDeleted)) {
			if (tokens != null) {
				for (String token : tokens) {
					allTokens.add(prefix + token);
				}
			}
			if (subFoldersHierarchyTokens != null) {
				allTokens.addAll(subFoldersHierarchyTokens);
			}
			if (documentsHierarchyTokens != null) {
				allTokens.addAll(documentsHierarchyTokens);
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
