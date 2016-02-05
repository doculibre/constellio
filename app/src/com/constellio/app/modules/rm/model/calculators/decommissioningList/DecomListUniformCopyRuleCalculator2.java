package com.constellio.app.modules.rm.model.calculators.decommissioningList;

import static java.util.Arrays.asList;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.ReferenceDependency;
import com.constellio.model.entities.schemas.MetadataValueType;

public class DecomListUniformCopyRuleCalculator2 implements MetadataValueCalculator<CopyRetentionRule> {

	ReferenceDependency<List<CopyRetentionRule>> copyRulesParam = ReferenceDependency.toAStructure(DecommissioningList.FOLDERS,
			Folder.MAIN_COPY_RULE).whichIsMultivalue();

	ReferenceDependency<List<CopyRetentionRule>> documentsCopyRulesParam = ReferenceDependency
			.toAStructure(DecommissioningList.DOCUMENTS,
					Document.MAIN_COPY_RULE).whichIsMultivalue();

	@Override
	public CopyRetentionRule calculate(CalculatorParameters parameters) {

		List<CopyRetentionRule> copyRules = parameters.get(copyRulesParam);
		List<CopyRetentionRule> documentsCopyRules = parameters.get(documentsCopyRulesParam);
		Set<CopyRetentionRule> copyRulesWithoutDuplicates = new HashSet<>(copyRules);
		copyRulesWithoutDuplicates.addAll(documentsCopyRules);
		return copyRulesWithoutDuplicates.size() == 1 ? copyRulesWithoutDuplicates.iterator().next() : null;
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
		return asList(copyRulesParam, documentsCopyRulesParam);
	}
}
