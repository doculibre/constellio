package com.constellio.app.modules.rm.model.calculators;

import com.constellio.model.entities.calculators.AbstractMetadataValueCalculator;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.wrappers.UserDocument;
import com.constellio.model.entities.schemas.MetadataValueType;

import java.util.List;

import static java.util.Collections.singletonList;

public class UserDocumentContentSizeCalculator extends AbstractMetadataValueCalculator<Long> {

	private LocalDependency<Content> contentParam = LocalDependency.toAContent(UserDocument.CONTENT);

	@Override
	public Long calculate(CalculatorParameters parameters) {
		Content content = parameters.get(contentParam);
		if (content == null) {
			return 0L;
		}
		return content.getCurrentVersion().getLength();
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
		return singletonList(contentParam);
	}

}
