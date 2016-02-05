package com.constellio.app.modules.rm.model.calculators.rule;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;

import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.modules.rm.wrappers.structures.RetentionRuleDocumentType;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.schemas.MetadataValueType;

public class RuleDocumentTypesCalculator implements MetadataValueCalculator<List<String>> {

	LocalDependency<List<RetentionRuleDocumentType>> documentsTypesDetailsParam = LocalDependency
			.toAStructure(RetentionRule.DOCUMENT_TYPES_DETAILS).whichIsMultivalue();

	@Override
	public List<String> calculate(CalculatorParameters parameters) {
		List<String> ids = new ArrayList<>();
		List<RetentionRuleDocumentType> details = parameters.get(documentsTypesDetailsParam);

		for (RetentionRuleDocumentType detail : details) {
			ids.add(detail.getDocumentTypeId());
		}

		return ids;
	}

	@Override
	public List<String> getDefaultValue() {
		return new ArrayList<>();
	}

	@Override
	public MetadataValueType getReturnType() {
		return MetadataValueType.REFERENCE;
	}

	@Override
	public boolean isMultiValue() {
		return true;
	}

	@Override
	public List<? extends Dependency> getDependencies() {
		return asList(documentsTypesDetailsParam);
	}
}
