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
package com.constellio.app.modules.rm.model.calculators.folder;

import static java.util.Arrays.asList;

import java.util.List;

import com.constellio.app.modules.rm.model.enums.FolderMediaType;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.type.MediumType;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.ReferenceDependency;
import com.constellio.model.entities.schemas.MetadataValueType;

public class FolderMediaTypesCalculator implements MetadataValueCalculator<FolderMediaType> {

	ReferenceDependency<List<Boolean>> mediumTypesAnalogStatusParam = ReferenceDependency
			.toABoolean(Folder.MEDIUM_TYPES, MediumType.ANALOGICAL).whichIsMultivalue();

	@Override
	public FolderMediaType calculate(CalculatorParameters parameters) {
		List<Boolean> mediumTypesAnalogStatus = parameters.get(mediumTypesAnalogStatusParam);

		boolean hasAnalog = false;
		boolean hasElectronic = false;

		for (Boolean mediumTypeAnalogStatus : mediumTypesAnalogStatus) {
			if (mediumTypeAnalogStatus) {
				hasAnalog = true;
			} else {
				hasElectronic = true;
			}
		}

		if (hasAnalog && hasElectronic) {
			return FolderMediaType.HYBRID;

		} else if (hasAnalog) {
			return FolderMediaType.ANALOG;

		} else if (hasElectronic) {
			return FolderMediaType.ELECTRONIC;

		} else {
			return FolderMediaType.UNKNOWN;
		}

	}

	@Override
	public FolderMediaType getDefaultValue() {
		return FolderMediaType.UNKNOWN;
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
		return asList(mediumTypesAnalogStatusParam);
	}
}
