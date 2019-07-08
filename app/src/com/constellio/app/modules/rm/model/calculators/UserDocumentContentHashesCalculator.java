package com.constellio.app.modules.rm.model.calculators;

import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.ContentVersion;
import com.constellio.model.entities.records.wrappers.UserDocument;
import com.constellio.model.entities.schemas.MetadataValueType;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

public class UserDocumentContentHashesCalculator implements MetadataValueCalculator<List<String>> {

	LocalDependency<Content> contentParam = LocalDependency.toAContent(UserDocument.CONTENT)
			.whichIsRequired();

	@Override
	public List<String> calculate(CalculatorParameters parameters) {

		Content content = parameters.get(contentParam);
		List<String> contentHashes = null;
		if (content != null) {
			contentHashes = new ArrayList<>();
			for (ContentVersion version : content.getVersions()) {
				contentHashes.add(version.getHash());
			}
		}
		return contentHashes;
	}

	@Override
	public List<String> getDefaultValue() {
		return null;
	}

	@Override
	public MetadataValueType getReturnType() {
		return MetadataValueType.STRING;
	}

	@Override
	public boolean isMultiValue() {
		return true;
	}

	@Override
	public List<? extends Dependency> getDependencies() {
		return asList(contentParam);
	}

}
