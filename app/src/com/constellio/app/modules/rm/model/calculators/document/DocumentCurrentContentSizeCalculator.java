package com.constellio.app.modules.rm.model.calculators.document;

import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.schemas.MetadataValueType;

import java.util.List;

import static java.util.Collections.singletonList;

public class DocumentCurrentContentSizeCalculator implements MetadataValueCalculator<Long> {
	LocalDependency<Content> content = LocalDependency.toAContent(Document.CONTENT);

	@Override
	public Long calculate(CalculatorParameters parameters) {
		Content documentContent = parameters.get(content);
		if (documentContent == null) {
			return 0L;
		}
		return documentContent.getCurrentVersion().getLength();
	}

	@Override
	public Long getDefaultValue() {
		return 0L;
	}

	@Override
	public MetadataValueType getReturnType() {
		return MetadataValueType.NUMBER;
	}

	@Override
	public boolean isMultiValue() {
		return false;
	}

	@Override
	public List<? extends Dependency> getDependencies() {
		return singletonList(content);
	}
}
