package com.constellio.app.modules.rm.model.calculators.folder;

import static java.util.Arrays.asList;

import java.util.List;

import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.calculators.dependencies.ReferenceDependency;
import com.constellio.model.entities.schemas.MetadataValueType;

public class FolderAppliedRetentionRuleCalculator implements MetadataValueCalculator<String> {

	ReferenceDependency<String> parentRetentionRuleParam = ReferenceDependency
			.toAReference(Folder.PARENT_FOLDER, Folder.RETENTION_RULE);
	LocalDependency<String> enteredRetentionRuleParam = LocalDependency.toAReference(Folder.RETENTION_RULE_ENTERED);

	@Override
	public String calculate(CalculatorParameters parameters) {
		String parentRetentionRule = parameters.get(parentRetentionRuleParam);
		String enteredRetentionRule = parameters.get(enteredRetentionRuleParam);

		if (parentRetentionRule != null) {
			return parentRetentionRule;
		} else {
			return enteredRetentionRule;
		}

	}

	@Override
	public String getDefaultValue() {
		return null;
	}

	@Override
	public MetadataValueType getReturnType() {
		return MetadataValueType.REFERENCE;
	}

	@Override
	public boolean isMultiValue() {
		return false;
	}

	@Override
	public List<? extends Dependency> getDependencies() {
		return asList(parentRetentionRuleParam, enteredRetentionRuleParam);
	}
}
