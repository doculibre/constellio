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

import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.schemas.MetadataValueType;

public class FolderMainCopyRuleCalculator implements MetadataValueCalculator<CopyRetentionRule> {

	LocalDependency<List<CopyRetentionRule>> copyRulesParam = LocalDependency.toAStructure(Folder.APPLICABLE_COPY_RULES)
			.whichIsRequired();

	LocalDependency<List<LocalDate>> expectedDestructionDatesParam = LocalDependency
			.toADate(Folder.COPY_RULES_EXPECTED_DESTRUCTION_DATES).whichIsMultivalue().whichIsRequired();

	LocalDependency<List<LocalDate>> expectedDepositDatesParam = LocalDependency
			.toADate(Folder.COPY_RULES_EXPECTED_DEPOSIT_DATES).whichIsMultivalue().whichIsRequired();

	@Override
	public CopyRetentionRule calculate(CalculatorParameters parameters) {

		List<CopyRetentionRule> copyRules = parameters.get(copyRulesParam);
		List<LocalDate> expectedDestructionDates = parameters.get(expectedDestructionDatesParam);
		List<LocalDate> expectedDepositDates = parameters.get(expectedDepositDatesParam);

		LocalDate smallestDate = null;
		CopyRetentionRule mainCopyRule = null;

		for (int i = 0; i < copyRules.size(); i++) {
			LocalDate dateAtIndex = null;
			if (expectedDestructionDates.get(i) != null) {
				dateAtIndex = expectedDestructionDates.get(i);
			} else {
				dateAtIndex = expectedDepositDates.get(i);
			}
			if (mainCopyRule == null || (dateAtIndex != null && dateAtIndex.isBefore(smallestDate))) {
				smallestDate = dateAtIndex;
				mainCopyRule = copyRules.get(i);
			}
		}

		return mainCopyRule;
	}

	@Override
	public CopyRetentionRule getDefaultValue() {
		return null;
	}

	@Override
	public MetadataValueType getReturnType() {
		return MetadataValueType.STRUCTURE;
	}

	@Override
	public boolean isMultiValue() {
		return false;
	}

	@Override
	public List<? extends Dependency> getDependencies() {
		return Arrays.asList(copyRulesParam, expectedDestructionDatesParam, expectedDepositDatesParam);
	}
}
