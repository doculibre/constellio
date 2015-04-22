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

import org.joda.time.LocalDate;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.model.enums.DecommissioningDateBasedOn;
import com.constellio.app.modules.rm.model.enums.FolderStatus;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.ConfigDependency;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.schemas.MetadataValueType;

public class FolderDecommissioningDateCalculator implements MetadataValueCalculator<LocalDate> {

	LocalDependency<LocalDate> openingDateParam = LocalDependency.toADate(Folder.OPENING_DATE);
	LocalDependency<LocalDate> closingDateParam = LocalDependency.toADate(Folder.CLOSING_DATE);
	LocalDependency<LocalDate> actualTransferDateParam = LocalDependency.toADate(Folder.ACTUAL_TRANSFER_DATE);
	LocalDependency<FolderStatus> folderStatusParam = LocalDependency.toAnEnum(Folder.ARCHIVISTIC_STATUS);
	ConfigDependency<DecommissioningDateBasedOn> decommissioningDateBasedOnParam =
			RMConfigs.DECOMMISSIONING_DATE_BASED_ON.dependency();

	ConfigDependency<Integer> configRequiredDaysBeforeYearEndParam =
			RMConfigs.REQUIRED_DAYS_BEFORE_YEAR_END_FOR_NOT_ADDING_A_WEEK.dependency();
	ConfigDependency<String> configYearEndParam = RMConfigs.YEAR_END_DATE.dependency();

	@Override
	public LocalDate calculate(CalculatorParameters parameters) {
		LocalDate openingDate = parameters.get(openingDateParam);
		LocalDate closingDate = parameters.get(closingDateParam);
		LocalDate actualTransferDate = parameters.get(actualTransferDateParam);
		FolderStatus folderStatus = parameters.get(folderStatusParam);
		DecommissioningDateBasedOn basedOn = parameters.get(decommissioningDateBasedOnParam);

		String yearEnd = parameters.get(configYearEndParam);
		int requiredDaysBeforeYearEnd = parameters.get(configRequiredDaysBeforeYearEndParam);

		if (actualTransferDate != null && folderStatus.isActiveOrSemiActive()) {
			return CalculatorUtils.toNextEndOfYearDateIfNotAlready(actualTransferDate, yearEnd, requiredDaysBeforeYearEnd);

		} else if (DecommissioningDateBasedOn.OPEN_DATE == basedOn) {
			return CalculatorUtils.toNextEndOfYearDateIfNotAlready(openingDate, yearEnd, requiredDaysBeforeYearEnd);

		} else {
			return CalculatorUtils.toNextEndOfYearDateIfNotAlready(closingDate, yearEnd, requiredDaysBeforeYearEnd);
		}

	}

	@Override
	public LocalDate getDefaultValue() {
		return null;
	}

	@Override
	public MetadataValueType getReturnType() {
		return MetadataValueType.DATE;
	}

	@Override
	public boolean isMultiValue() {
		return false;
	}

	@Override
	public List<? extends Dependency> getDependencies() {
		return Arrays
				.asList(openingDateParam, closingDateParam, actualTransferDateParam, folderStatusParam,
						decommissioningDateBasedOnParam, configRequiredDaysBeforeYearEndParam, configYearEndParam);
	}
}