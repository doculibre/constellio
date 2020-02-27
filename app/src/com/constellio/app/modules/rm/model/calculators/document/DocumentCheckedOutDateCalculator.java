package com.constellio.app.modules.rm.model.calculators.document;

import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.schemas.MetadataValueType;
import org.joda.time.LocalDateTime;

import java.util.Arrays;
import java.util.List;

public class DocumentCheckedOutDateCalculator implements MetadataValueCalculator<LocalDateTime> {
	LocalDependency<Content> content = LocalDependency.toAContent(Document.CONTENT);

	@Override
	public LocalDateTime calculate(CalculatorParameters parameters) {
		Content documentContent = parameters.get(content);
		return documentContent != null ? documentContent.getCheckoutDateTime() : null;
	}

	@Override
	public LocalDateTime getDefaultValue() {
		return null;
	}

	@Override
	public MetadataValueType getReturnType() {
		return MetadataValueType.DATE_TIME;
	}

	@Override
	public boolean isMultiValue() {
		return false;
	}

	@Override
	public List<? extends Dependency> getDependencies() {
		return Arrays.asList(content);
	}
}
