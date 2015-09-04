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

import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.calculators.dependencies.ReferenceDependency;
import com.constellio.model.entities.schemas.MetadataValueType;

public class FolderCopyStatusCalculator2 implements MetadataValueCalculator<CopyType> {

	LocalDependency<CopyType> folderCopyTypeManualParam = LocalDependency.toAnEnum(Folder.COPY_STATUS_ENTERED);

	LocalDependency<String> folderUnitParam = LocalDependency.toAReference(Folder.ADMINISTRATIVE_UNIT);

	LocalDependency<List<String>> folderUnitAncestorsParam = LocalDependency.toAReference(Folder.ADMINISTRATIVE_UNIT_ANCESTORS)
			.whichIsMultivalue();

	ReferenceDependency<List<CopyRetentionRule>> ruleCopyRulesParam = ReferenceDependency.toAStructure(Folder.RETENTION_RULE,
			RetentionRule.COPY_RETENTION_RULES).whichIsMultivalue().whichIsRequired();

	ReferenceDependency<List<String>> ruleUnitsParam = ReferenceDependency.toAReference(Folder.RETENTION_RULE,
			RetentionRule.ADMINISTRATIVE_UNITS).whichIsMultivalue();

	ReferenceDependency<Boolean> ruleResponsibleUnitsParam = ReferenceDependency.toABoolean(Folder.RETENTION_RULE,
			RetentionRule.RESPONSIBLE_ADMINISTRATIVE_UNITS);

	@Override
	public CopyType calculate(CalculatorParameters parameters) {
		CopyType folderCopyTypeManual = parameters.get(folderCopyTypeManualParam);
		List<String> ruleUnits = parameters.get(ruleUnitsParam);
		List<String> folderUnitAncestors = parameters.get(folderUnitAncestorsParam);
		String folderUnit = parameters.get(folderUnitParam);
		Boolean ruleResponsibleUnits = parameters.get(ruleResponsibleUnitsParam);
		List<CopyRetentionRule> ruleCopyRules = parameters.get(ruleCopyRulesParam);

		boolean hasPrincipalCopyRule = false;
		for (CopyRetentionRule ruleCopyRule : ruleCopyRules) {
			if (ruleCopyRule.getCopyType() == CopyType.PRINCIPAL) {
				hasPrincipalCopyRule = true;
			}
		}

		if (!hasPrincipalCopyRule) {
			return CopyType.SECONDARY;
		}

		if (folderCopyTypeManual != null) {
			return folderCopyTypeManual;
		}

		if (Boolean.TRUE != ruleResponsibleUnits) {
			for (String ruleUnit : ruleUnits) {
				if (folderUnitAncestors.contains(ruleUnit) || ruleUnit.equals(folderUnit)) {
					return CopyType.PRINCIPAL;
				}
			}

			return CopyType.SECONDARY;
		}

		return null;
	}

	@Override
	public CopyType getDefaultValue() {
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
		return Arrays.asList(folderCopyTypeManualParam, folderUnitParam, folderUnitAncestorsParam, ruleUnitsParam,
				ruleResponsibleUnitsParam, ruleCopyRulesParam);
	}
}
