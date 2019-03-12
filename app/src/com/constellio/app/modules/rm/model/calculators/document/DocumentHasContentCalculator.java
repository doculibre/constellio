package com.constellio.app.modules.rm.model.calculators.document;

import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.model.entities.calculators.AbstractMetadataValueCalculator;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;

import java.util.List;

import static java.util.Arrays.asList;

public class DocumentHasContentCalculator extends AbstractMetadataValueCalculator<Boolean> {

	private LocalDependency<Content> contentParam = LocalDependency.toAContent(Document.CONTENT);
	private LocalDependency<Boolean> logicallyDeletedParam =
			LocalDependency.toABoolean(Schemas.LOGICALLY_DELETED_STATUS.getLocalCode());

	@Override
	public Boolean calculate(CalculatorParameters parameters) {
		Content content = parameters.get(contentParam);
		Boolean logicallyDeleted = parameters.get(logicallyDeletedParam);
		return content != null && !Boolean.TRUE.equals(logicallyDeleted);
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
		return asList(contentParam, logicallyDeletedParam);
	}

}
