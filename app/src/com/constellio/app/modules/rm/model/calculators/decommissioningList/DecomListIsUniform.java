package com.constellio.app.modules.rm.model.calculators.decommissioningList;

import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.model.entities.calculators.AbstractMetadataValueCalculator;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.schemas.MetadataValueType;

import java.util.List;

import static java.util.Arrays.asList;

public class DecomListIsUniform extends AbstractMetadataValueCalculator<Boolean> {

	LocalDependency<CopyRetentionRule> uniformCopyRuleParam = LocalDependency.toAStructure("uniformCopyRule");
	LocalDependency<CopyType> uniformCopyTypeParam = LocalDependency.toAnEnum("uniformCopyType");
	LocalDependency<String> uniformRuleParam = LocalDependency.toAReference("uniformRule");
	LocalDependency<String> uniformCategoryParam = LocalDependency.toAReference("uniformCategory");

	@Override
	public Boolean calculate(CalculatorParameters parameters) {
		CopyRetentionRule uniformCopyRule = parameters.get(uniformCopyRuleParam);
		CopyType uniformCopyType = parameters.get(uniformCopyTypeParam);
		String uniformRule = parameters.get(uniformRuleParam);
		String uniformCategory = parameters.get(uniformCategoryParam);

		if (uniformCopyRule != null
			&& uniformCopyType != null
			&& uniformRule != null
			&& uniformCategory != null) {
			return true;
		} else {
			return false;
		}
		//returne true si tous les parametres sont non-nulls, retourne false sinon
	}

	@Override
	public Boolean getDefaultValue() {
		return false;
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
		return asList(uniformCopyRuleParam, uniformCopyTypeParam, uniformRuleParam, uniformCategoryParam);
	}
}
