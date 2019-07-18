package com.constellio.app.modules.rm.model.calculators;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.model.enums.RetentionRuleScope;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.model.entities.calculators.AbstractMetadataValueCalculator;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.dependencies.ConfigDependency;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.calculators.dependencies.ReferenceDependency;
import com.constellio.model.entities.schemas.MetadataValueType;

import java.util.Arrays;
import java.util.List;

public class FolderCopyStatusCalculator3 extends AbstractMetadataValueCalculator<CopyType> {

	LocalDependency<CopyType> folderCopyTypeManualParam = LocalDependency.toAnEnum(Folder.COPY_STATUS_ENTERED);

	LocalDependency<String> folderUnitParam = LocalDependency.toAReference(Folder.ADMINISTRATIVE_UNIT);

	ReferenceDependency<RetentionRuleScope> ruleScopeParam = ReferenceDependency
			.toAnEnum(Folder.RETENTION_RULE, RetentionRule.SCOPE);

	LocalDependency<List<String>> folderUnitAncestorsParam = LocalDependency.toAReference(Folder.ADMINISTRATIVE_UNIT_ANCESTORS)
			.whichIsMultivalue();

	ReferenceDependency<List<CopyRetentionRule>> ruleCopyRulesParam = ReferenceDependency.toAStructure(Folder.RETENTION_RULE,
			RetentionRule.COPY_RETENTION_RULES).whichIsMultivalue();

	ReferenceDependency<List<String>> ruleUnitsParam = ReferenceDependency.toAReference(Folder.RETENTION_RULE,
			RetentionRule.ADMINISTRATIVE_UNITS).whichIsMultivalue();

	ReferenceDependency<Boolean> ruleResponsibleUnitsParam = ReferenceDependency.toABoolean(Folder.RETENTION_RULE,
			RetentionRule.RESPONSIBLE_ADMINISTRATIVE_UNITS);

	ReferenceDependency<CopyType> parentCopyTypeParam = ReferenceDependency.toAnEnum(Folder.PARENT_FOLDER, Folder.COPY_STATUS);

	ConfigDependency<Boolean> openHolderParam = RMConfigs.OPEN_HOLDER.dependency();


	@Override
	public CopyType calculate(CalculatorParameters parameters) {
		CopyType folderCopyTypeManual = parameters.get(folderCopyTypeManualParam);
		List<String> ruleUnits = parameters.get(ruleUnitsParam);
		List<String> folderUnitAncestors = parameters.get(folderUnitAncestorsParam);
		String folderUnit = parameters.get(folderUnitParam);
		Boolean ruleResponsibleUnits = parameters.get(ruleResponsibleUnitsParam);
		boolean documentRule = RetentionRuleScope.DOCUMENTS == parameters.get(ruleScopeParam);
		List<CopyRetentionRule> ruleCopyRules = parameters.get(ruleCopyRulesParam);
		Boolean openHolder = parameters.get(openHolderParam);

		if (ruleCopyRules.isEmpty() && !documentRule) {
			return null;
		}

		CopyType parentCopyType = parameters.get(parentCopyTypeParam);
		if (parentCopyType != null) {
			return parentCopyType;
		}

		boolean hasPrincipalCopyRule = false;
		for (CopyRetentionRule ruleCopyRule : ruleCopyRules) {
			if (ruleCopyRule.getCopyType() == CopyType.PRINCIPAL) {
				hasPrincipalCopyRule = true;
			}
		}

		if (!hasPrincipalCopyRule && !documentRule) {
			return CopyType.SECONDARY;
		}

		if (folderCopyTypeManual != null) {
			return folderCopyTypeManual;
		}

		if (!Boolean.TRUE.equals(ruleResponsibleUnits)) {
			for (String ruleUnit : ruleUnits) {
				if (folderUnitAncestors.contains(ruleUnit) || ruleUnit.equals(folderUnit)) {
					return CopyType.PRINCIPAL;
				}
			}

			return CopyType.SECONDARY;
		}

		if (openHolder && Boolean.TRUE.equals(ruleResponsibleUnits) && ruleUnits != null
			&& ruleUnits.contains(folderUnit)) {
			return CopyType.PRINCIPAL;
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
				ruleResponsibleUnitsParam, ruleCopyRulesParam, parentCopyTypeParam, ruleScopeParam, openHolderParam);
	}
}
