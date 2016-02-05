package com.constellio.app.modules.rm.model.calculators;

import java.util.Arrays;
import java.util.List;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.model.enums.FolderStatus;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.ConfigDependency;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.schemas.MetadataValueType;

public class FolderTreeVisibilityCalculator implements MetadataValueCalculator<Boolean> {
	LocalDependency<FolderStatus> folderStatus = LocalDependency.toAnEnum(Folder.ARCHIVISTIC_STATUS);
	ConfigDependency<Boolean> displaySemiActive = RMConfigs.DISPLAY_SEMI_ACTIVE_RECORDS_IN_TREES.dependency();
	ConfigDependency<Boolean> displayDeposited = RMConfigs.DISPLAY_DEPOSITED_RECORDS_IN_TREES.dependency();
	ConfigDependency<Boolean> displayDestroyed = RMConfigs.DISPLAY_DESTROYED_RECORDS_IN_TREES.dependency();

	@Override
	public Boolean calculate(CalculatorParameters parameters) {
		switch (parameters.get(folderStatus)) {
		case SEMI_ACTIVE:
			return parameters.get(displaySemiActive);
		case INACTIVE_DEPOSITED:
			return parameters.get(displayDeposited);
		case INACTIVE_DESTROYED:
			return parameters.get(displayDestroyed);
		}
		return true;
	}

	@Override
	public Boolean getDefaultValue() {
		return null;
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
		return Arrays.asList(folderStatus, displaySemiActive, displayDeposited, displayDestroyed);
	}
}
