package com.constellio.app.modules.rm.model.calculators.folder;

import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.schemas.MetadataValueType;

import java.util.Arrays;
import java.util.List;

public class FolderMainCopyRuleCodeCalculator implements MetadataValueCalculator<String> {

	LocalDependency<CopyRetentionRule> mainCopyRuleParam = LocalDependency.toAStructure(Folder.MAIN_COPY_RULE);

	@Override
	public String calculate(CalculatorParameters parameters) {
		CopyRetentionRule mainCopyRule = parameters.get(this.mainCopyRuleParam);

		if (mainCopyRule != null) {
			return mainCopyRule.getCode();
		}
		return null;
	}

	@Override
	public String getDefaultValue() {
		return null;
	}

	@Override
	public MetadataValueType getReturnType() {
		return MetadataValueType.STRING;
	}

	@Override
	public boolean isMultiValue() {
		return false;
	}

	@Override
	public List<? extends Dependency> getDependencies() {
		return Arrays.asList(mainCopyRuleParam);
	}
}
