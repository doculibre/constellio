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
import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.enums.FolderStatus;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.dependencies.ConfigDependency;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;

public abstract class AbstractFolderExpectedInactiveDatesCalculator extends AbstractFolderCopyRulesExpectedDatesCalculator {

	LocalDependency<FolderStatus> archivisticStatusParam = LocalDependency.toAnEnum(Folder.ARCHIVISTIC_STATUS);
	LocalDependency<LocalDate> decommissioningDateParam = LocalDependency.toADate(Folder.DECOMMISSIONING_DATE);

	LocalDependency<List<LocalDate>> copyRulesExpectedTransferDateParam = LocalDependency
			.toADate(Folder.COPY_RULES_EXPECTED_TRANSFER_DATES).whichIsMultivalue();

	ConfigDependency<Integer> configNumberOfYearWhenVariableDelayPeriod =
			RMConfigs.CALCULATED_INACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_RULE.dependency();

	@Override
	protected List<? extends Dependency> getCopyRuleDateCalculationDependencies() {
		return Arrays.asList(decommissioningDateParam, archivisticStatusParam,
				copyRulesExpectedTransferDateParam, configNumberOfYearWhenVariableDelayPeriod);
	}

	@Override
	protected LocalDate calculateForCopyRule(int index, CopyRetentionRule copyRule, CalculatorParameters parameters) {

		FolderStatus archivisticStatus = parameters.get(archivisticStatusParam);
		LocalDate decommissioningDate = parameters.get(decommissioningDateParam);
		List<LocalDate> copyRulesExpectedTransferDate = parameters.get(copyRulesExpectedTransferDateParam);
		int numberOfYearWhenVariableDelayPeriod = parameters.get(configNumberOfYearWhenVariableDelayPeriod);

		LocalDate baseDate;
		if (isReturningNullDate(copyRule)) {
			baseDate = null;
		} else if (archivisticStatus.isSemiActive()) {
			baseDate = decommissioningDate;
		} else {
			baseDate = copyRulesExpectedTransferDate.get(index);
		}

		if (baseDate == null) {
			return null;
		} else if (copyRule.getSemiActiveRetentionPeriod().isVariablePeriod()) {
			if (numberOfYearWhenVariableDelayPeriod == -1) {
				return null;
			} else {
				return baseDate.plusYears(numberOfYearWhenVariableDelayPeriod);
			}
		} else {
			return baseDate.plusYears(copyRule.getSemiActiveRetentionPeriod().getFixedPeriod());
		}

	}

	protected abstract boolean isReturningNullDate(CopyRetentionRule copyRule);
}
