package com.constellio.app.modules.rm.model.calculators;

import com.constellio.app.modules.rm.wrappers.RMMessage;
import com.constellio.model.entities.calculators.AbstractMetadataValueCalculator;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;

import java.util.Collections;
import java.util.List;

public class MessageHasLinkedDocumentCalculator extends AbstractMetadataValueCalculator<Boolean> {
	private LocalDependency<List<String>> linkedDocumentsDependency = LocalDependency.toAReferenceList(RMMessage.LINKED_DOCUMENTS);

	@Override
	public Boolean calculate(CalculatorParameters parameters) {
		List<String> linkedDocuments = parameters.get(linkedDocumentsDependency);

		return !linkedDocuments.isEmpty();
	}

	@Override
	public List<? extends Dependency> getDependencies() {
		return Collections.singletonList(linkedDocumentsDependency);
	}
}
