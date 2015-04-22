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

import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.calculators.dependencies.ReferenceDependency;
import com.constellio.model.entities.schemas.MetadataValueType;

public class FolderAppliedRetentionRuleCalculator implements MetadataValueCalculator<String> {

	ReferenceDependency<String> parentRetentionRuleParam = ReferenceDependency
			.toAReference(Folder.PARENT_FOLDER, Folder.RETENTION_RULE);
	LocalDependency<String> enteredRetentionRuleParam = LocalDependency.toAReference(Folder.RETENTION_RULE_ENTERED);

	@Override
	public String calculate(CalculatorParameters parameters) {
		String parentRetentionRule = parameters.get(parentRetentionRuleParam);
		String enteredRetentionRule = parameters.get(enteredRetentionRuleParam);

		if (parentRetentionRule != null) {
			return parentRetentionRule;
		} else {
			return enteredRetentionRule;
		}

	}

	@Override
	public String getDefaultValue() {
		return null;
	}

	@Override
	public MetadataValueType getReturnType() {
		return MetadataValueType.REFERENCE;
	}

	@Override
	public boolean isMultiValue() {
		return false;
	}

	@Override
	public List<? extends Dependency> getDependencies() {
		return asList(parentRetentionRuleParam, enteredRetentionRuleParam);
	}
}
