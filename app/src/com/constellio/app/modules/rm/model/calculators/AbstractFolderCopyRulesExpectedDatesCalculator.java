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

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.ConfigDependency;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.schemas.MetadataValueType;

public abstract class AbstractFolderCopyRulesExpectedDatesCalculator implements MetadataValueCalculator<List<LocalDate>> {
	LocalDependency<List<CopyRetentionRule>> applicableCopyRulesParam = LocalDependency
			.toAStructure(Folder.APPLICABLE_COPY_RULES).whichIsMultivalue();

	ConfigDependency<Integer> configRequiredDaysBeforeYearEndParam =
			RMConfigs.REQUIRED_DAYS_BEFORE_YEAR_END_FOR_NOT_ADDING_A_WEEK.dependency();

	ConfigDependency<String> configYearEndParam = RMConfigs.YEAR_END_DATE.dependency();

	@Override
	public List<LocalDate> calculate(CalculatorParameters parameters) {

		List<CopyRetentionRule> applicableCopyRules = parameters.get(applicableCopyRulesParam);
		String yearEnd = parameters.get(configYearEndParam);
		int requiredDaysBeforeYearEnd = parameters.get(configRequiredDaysBeforeYearEndParam);

		List<LocalDate> result = new ArrayList<>();
		for (int i = 0; i < applicableCopyRules.size(); i++) {
			CopyRetentionRule applicableCopyRule = applicableCopyRules.get(i);
			LocalDate copyRuleCalculedDate = calculateForCopyRule(i, applicableCopyRule, parameters);
			if (copyRuleCalculedDate == null) {
				result.add(null);
			} else if (CalculatorUtils.isEndOfYear(copyRuleCalculedDate, yearEnd)) {
				result.add(copyRuleCalculedDate);
			} else {
				result.add(CalculatorUtils.toNextEndOfYearDate(copyRuleCalculedDate, yearEnd, requiredDaysBeforeYearEnd));
			}
		}

		return result;
	}

	@Override
	public List<LocalDate> getDefaultValue() {
		return new ArrayList<>();
	}

	@Override
	public MetadataValueType getReturnType() {
		return MetadataValueType.DATE;
	}

	@Override
	public boolean isMultiValue() {
		return true;
	}

	@Override
	public List<? extends Dependency> getDependencies() {

		List<Dependency> dependencies = new ArrayList<>();
		dependencies.add(applicableCopyRulesParam);
		dependencies.add(configRequiredDaysBeforeYearEndParam);
		dependencies.add(configYearEndParam);
		dependencies.addAll(getCopyRuleDateCalculationDependencies());

		return dependencies;
	}

	protected abstract List<? extends Dependency> getCopyRuleDateCalculationDependencies();

	protected abstract LocalDate calculateForCopyRule(int index, CopyRetentionRule copyRule, CalculatorParameters parameters);
}