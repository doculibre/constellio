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

import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.RetentionPeriod;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.schemas.MetadataValueType;

public abstract class FolderRetentionPeriodCodeCalculator implements MetadataValueCalculator<String> {

	LocalDependency<CopyRetentionRule> mainCopyRuleParam = LocalDependency.toAStructure(Folder.MAIN_COPY_RULE);

	@Override
	public String calculate(CalculatorParameters parameters) {
		CopyRetentionRule mainCopyRule = parameters.get(mainCopyRuleParam);
		if (mainCopyRule == null) {
			return null;
		} else {
			RetentionPeriod period = getRetentionPeriod(mainCopyRule);
			if (period == null || !period.isVariablePeriod()) {
				return null;
			} else {
				return period.getVariablePeriodCode();
			}
		}
	}

	protected abstract RetentionPeriod getRetentionPeriod(CopyRetentionRule copyRetentionRule);

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
		return asList(mainCopyRuleParam);
	}

	public static class FolderActiveRetentionPeriodCodeCalculator extends FolderRetentionPeriodCodeCalculator
			implements MetadataValueCalculator<String> {

		@Override
		protected RetentionPeriod getRetentionPeriod(CopyRetentionRule copyRetentionRule) {
			return copyRetentionRule.getActiveRetentionPeriod();
		}
	}

	public static class FolderSemiActiveRetentionPeriodCodeCalculator extends FolderRetentionPeriodCodeCalculator
			implements MetadataValueCalculator<String> {

		@Override
		protected RetentionPeriod getRetentionPeriod(CopyRetentionRule copyRetentionRule) {
			return copyRetentionRule.getSemiActiveRetentionPeriod();
		}
	}
}


