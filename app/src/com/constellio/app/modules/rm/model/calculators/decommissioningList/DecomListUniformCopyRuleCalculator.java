package com.constellio.app.modules.rm.model.calculators.decommissioningList;

import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.calculators.AbstractMetadataValueCalculator;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.ReferenceDependency;
import com.constellio.model.entities.schemas.MetadataValueType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;

public class DecomListUniformCopyRuleCalculator extends AbstractMetadataValueCalculator<CopyRetentionRule> {

	ReferenceDependency<List<CopyRetentionRule>> copyRulesParam = ReferenceDependency.toAStructure("folders",
			Folder.MAIN_COPY_RULE).whichIsMultivalue();

	@Override
	public CopyRetentionRule calculate(CalculatorParameters parameters) {

		List<CopyRetentionRule> copyRules = parameters.get(copyRulesParam);
		Set<CopyRetentionRule> copyRulesWithoutDuplicates = new HashSet<>(copyRules);
		return copyRulesWithoutDuplicates.size() == 1 ? copyRules.get(0) : null;
	}

	@Override
	public CopyRetentionRule getDefaultValue() {
		return null;
	}

	@Override
	public MetadataValueType getReturnType() {
		return MetadataValueType.STRUCTURE;
	}

	@Override
	public boolean isMultiValue() {
		return false;
	}

	@Override
	public List<? extends Dependency> getDependencies() {
		return asList(copyRulesParam);
	}
}
