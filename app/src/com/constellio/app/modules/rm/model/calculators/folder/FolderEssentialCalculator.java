package com.constellio.app.modules.rm.model.calculators.folder;

import static java.util.Arrays.asList;

import java.util.List;

import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.calculators.dependencies.ReferenceDependency;
import com.constellio.model.entities.schemas.MetadataValueType;

public class FolderEssentialCalculator implements MetadataValueCalculator<Boolean> {

	LocalDependency<CopyRetentionRule> mainCopyRuleParam = LocalDependency.toAStructure(Folder.MAIN_COPY_RULE).whichIsRequired();
	ReferenceDependency<Boolean> retentionRuleEssentialParam = ReferenceDependency.toABoolean(Folder.RETENTION_RULE,
			RetentionRule.ESSENTIAL_DOCUMENTS);

	@Override
	public Boolean calculate(CalculatorParameters parameters) {

		CopyRetentionRule mainCopyRule = parameters.get(mainCopyRuleParam);
		Boolean retentionRuleEssential = parameters.get(retentionRuleEssentialParam);
		return mainCopyRule.isEssential() || Boolean.TRUE.equals(retentionRuleEssential);
	}

	@Override
	public Boolean getDefaultValue() {
		return false;
	}

	@Override
	public MetadataValueType getReturnType() {
		return MetadataValueType.BOOLEAN;
	}

	@Override
	public boolean isMultiValue() {
		return false;
	}

	@Override
	public List<? extends Dependency> getDependencies() {
		return asList(mainCopyRuleParam, retentionRuleEssentialParam);
	}

}
