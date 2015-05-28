/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
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
		case INACTIVATE_DEPOSITED:
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
