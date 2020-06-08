package com.constellio.app.modules.rm.model.calculators.rule;

import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.services.schemas.calculators.AllReferencesCalculator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.constellio.app.modules.rm.wrappers.RetentionRule.COPY_RETENTION_RULES;

public class RuleMediumTypeReferencesCalculator extends AllReferencesCalculator {

	LocalDependency<List<CopyRetentionRule>> copyRetentionRulesParam = LocalDependency
			.toAStructure(COPY_RETENTION_RULES).whichIsMultivalue();

	@Override
	public List<String> calculate(CalculatorParameters parameters) {
		Set<String> returnedValues = new HashSet<>(super.calculate(parameters));

		for (CopyRetentionRule copyRetentionRule : parameters.get(copyRetentionRulesParam)) {
			returnedValues.addAll(copyRetentionRule.getMediumTypeIds());
		}

		List<String> returnedList = new ArrayList<>(returnedValues);
		Collections.sort(returnedList);

		return returnedList;
	}

	@Override
	public List<? extends Dependency> getDependencies() {
		ArrayList<Dependency> dependencies = new ArrayList<>();
		dependencies.addAll(super.getDependencies());
		dependencies.add(copyRetentionRulesParam);
		return dependencies;
	}
}
