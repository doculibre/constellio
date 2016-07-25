package com.constellio.app.modules.rm.model.calculators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.constellio.app.modules.rm.model.enums.FolderStatus;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.schemas.MetadataValueType;

public class FolderStatusCalculator implements MetadataValueCalculator<FolderStatus> {
	LocalDependency<String> titleParam = LocalDependency.toAString(Folder.TITLE);
	@Override
	public FolderStatus calculate(CalculatorParameters parameters) {
		return FolderStatus.ACTIVE;
	}

	@Override
	public FolderStatus getDefaultValue() {
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
		return Arrays.asList(titleParam);
	}
}
