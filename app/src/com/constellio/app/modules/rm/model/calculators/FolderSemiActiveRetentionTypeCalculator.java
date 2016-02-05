package com.constellio.app.modules.rm.model.calculators;

import java.util.Arrays;
import java.util.List;

import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.enums.RetentionType;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.schemas.MetadataValueType;

public class FolderSemiActiveRetentionTypeCalculator implements MetadataValueCalculator<RetentionType> {

	LocalDependency<CopyRetentionRule> mainCopyRuleParam = LocalDependency.toAStructure(Folder.MAIN_COPY_RULE)
			.whichIsRequired();

	@Override
	public RetentionType calculate(CalculatorParameters parameters) {
		return parameters.get(mainCopyRuleParam).getSemiActiveRetentionPeriod().getRetentionType();
	}

	@Override
	public RetentionType getDefaultValue() {
		return null;
	}

	@Override
	public MetadataValueType getReturnType() {
		return MetadataValueType.ENUM;
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
