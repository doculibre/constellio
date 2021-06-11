package com.constellio.model.services.schemas;

import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.ReferenceDependency;
import com.constellio.model.entities.schemas.entries.AdvancedSequenceCalculator;

import java.text.DecimalFormat;
import java.util.List;

import static org.apache.ignite.internal.util.lang.GridFunc.asList;

public class ReferencedMetadataBasedTestSequenceCalculator implements AdvancedSequenceCalculator {

	ReferenceDependency<String> REFERENCED_STRING_METADATA_DEPENDENCY = ReferenceDependency
			.toAString("zeSchemaType_default_referenceFromAnotherSchemaToZeSchema", "anotherSchemaType_default_stringMetadata");

	ReferenceDependency<String> REFERENCED_TITLE_DEPENDENCY = ReferenceDependency
			.toAString("zeSchemaType_default_referenceFromAnotherSchemaToZeSchema", "anotherSchemaType_default_title");

	@Override
	public List<? extends Dependency> getDependencies() {
		return asList(REFERENCED_STRING_METADATA_DEPENDENCY, REFERENCED_TITLE_DEPENDENCY);
	}

	@Override
	public String computeSequenceTableId(CalculatorParameters parameters) {
		return parameters.get(REFERENCED_STRING_METADATA_DEPENDENCY);
	}

	@Override
	public String computeSequenceTableValue(CalculatorParameters parameters, int sequenceValue) {
		String title = parameters.get(REFERENCED_TITLE_DEPENDENCY);
		return title + "-" + new DecimalFormat("####").format(sequenceValue);
	}
}