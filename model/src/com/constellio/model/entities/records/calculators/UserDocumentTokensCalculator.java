package com.constellio.model.entities.records.calculators;

import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.StringListMetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.records.wrappers.UserDocument;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;

public class UserDocumentTokensCalculator extends StringListMetadataValueCalculator {

	LocalDependency<String> userDependency = LocalDependency.toAReference(UserDocument.USER);

	@Override
	public List<String> calculate(CalculatorParameters parameters) {
		String user = parameters.get(userDependency);

		Set<String> returnedTokens = new HashSet<>();

		if (user != null) {
			returnedTokens.add("r_" + user);
		}

		List<String> returnedTokensList = new ArrayList<>(returnedTokens);
		Collections.sort(returnedTokensList);
		return returnedTokensList;
	}

	@Override
	public List<? extends Dependency> getDependencies() {
		return asList(userDependency);
	}
}
