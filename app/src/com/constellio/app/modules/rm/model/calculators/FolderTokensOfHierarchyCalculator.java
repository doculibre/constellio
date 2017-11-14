package com.constellio.app.modules.rm.model.calculators;

import static com.constellio.model.services.schemas.builders.CommonMetadataBuilder.MANUAL_TOKENS;
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.StringListMetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;

public class FolderTokensOfHierarchyCalculator extends StringListMetadataValueCalculator {

	LocalDependency<List<String>> manualTokensParam = LocalDependency.toAStringList(MANUAL_TOKENS);
	LocalDependency<List<String>> subFoldersParam = LocalDependency.toAStringList(Folder.SUB_FOLDERS_TOKENS);
	LocalDependency<List<String>> documentsParam = LocalDependency.toAStringList(Folder.DOCUMENTS_TOKENS);

	@Override
	public List<String> calculate(CalculatorParameters parameters) {
		Set<String> tokens = new HashSet<>();

		List<String> manualTokens = parameters.get(manualTokensParam);
		List<String> subFoldersHierarchyTokens = parameters.get(subFoldersParam);
		List<String> documentsHierarchyTokens = parameters.get(documentsParam);

		if (manualTokens != null) {
			tokens.addAll(manualTokens);
		}
		if (subFoldersHierarchyTokens != null) {
			tokens.addAll(subFoldersHierarchyTokens);
		}
		if (documentsHierarchyTokens != null) {
			tokens.addAll(documentsHierarchyTokens);
		}

		List<String> tokensList = new ArrayList<>(tokens);
		Collections.sort(tokensList);
		return tokensList;
	}

	@Override
	public List<? extends Dependency> getDependencies() {
		return asList(manualTokensParam, subFoldersParam, documentsParam);
	}
}
